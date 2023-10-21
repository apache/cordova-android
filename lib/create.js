/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

const path = require('path');
const fs = require('fs-extra');
const utils = require('./utils');
const check_reqs = require('./check_reqs');
const ROOT = path.join(__dirname, '..');
const { createEditor } = require('properties-parser');
const CordovaGradleConfigParserFactory = require('./config/CordovaGradleConfigParserFactory');

const CordovaError = require('cordova-common').CordovaError;
const AndroidManifest = require('./AndroidManifest');

// Export all helper functions, and make sure internally within this module, we
// reference these methods via the `exports` object - this helps with testing
// (since we can then mock and control behaviour of all of these functions)
exports.validatePackageName = validatePackageName;
exports.validateProjectName = validateProjectName;
exports.copyJsAndLibrary = copyJsAndLibrary;
exports.copyScripts = copyScripts;
exports.copyBuildRules = copyBuildRules;
exports.writeProjectProperties = writeProjectProperties;
exports.prepBuildFiles = prepBuildFiles;

function getFrameworkDir (projectPath, shared) {
    return shared ? path.join(ROOT, 'framework') : path.join(projectPath, 'CordovaLib');
}

function copyJsAndLibrary (projectPath, shared, projectName, targetAPI) {
    const nestedCordovaLibPath = getFrameworkDir(projectPath, false);
    const srcCordovaJsPath = path.join(ROOT, 'templates', 'project', 'assets', 'www', 'cordova.js');
    const app_path = path.join(projectPath, 'app', 'src', 'main');
    const platform_www = path.join(projectPath, 'platform_www');

    fs.copySync(srcCordovaJsPath, path.join(app_path, 'assets', 'www', 'cordova.js'));

    // Copy the cordova.js file to platforms/<platform>/platform_www/
    // The www dir is nuked on each prepare so we keep cordova.js in platform_www
    fs.ensureDirSync(platform_www);
    fs.copySync(srcCordovaJsPath, path.join(platform_www, 'cordova.js'));

    if (shared) {
        const relativeFrameworkPath = path.relative(projectPath, getFrameworkDir(projectPath, true));
        fs.symlinkSync(relativeFrameworkPath, nestedCordovaLibPath, 'dir');
    } else {
        fs.ensureDirSync(nestedCordovaLibPath);
        fs.copySync(path.join(ROOT, 'framework', 'AndroidManifest.xml'), path.join(nestedCordovaLibPath, 'AndroidManifest.xml'));
        const propertiesEditor = createEditor(path.join(ROOT, 'framework', 'project.properties'));
        propertiesEditor.set('target', targetAPI);
        propertiesEditor.save(path.join(nestedCordovaLibPath, 'project.properties'));
        fs.copySync(path.join(ROOT, 'framework', 'build.gradle'), path.join(nestedCordovaLibPath, 'build.gradle'));
        fs.copySync(path.join(ROOT, 'framework', 'cordova.gradle'), path.join(nestedCordovaLibPath, 'cordova.gradle'));
        fs.copySync(path.join(ROOT, 'framework', 'repositories.gradle'), path.join(nestedCordovaLibPath, 'repositories.gradle'));
        fs.copySync(path.join(ROOT, 'framework', 'src'), path.join(nestedCordovaLibPath, 'src'));
        fs.copySync(path.join(ROOT, 'framework', 'cdv-gradle-config-defaults.json'), path.join(projectPath, 'cdv-gradle-config.json'));
    }
}

function extractSubProjectPaths (data) {
    const ret = {};
    const r = /^\s*android\.library\.reference\.\d+=(.*)(?:\s|$)/mg;
    let m;
    while ((m = r.exec(data))) {
        ret[m[1]] = 1;
    }
    return Object.keys(ret);
}

function writeProjectProperties (projectPath, target_api) {
    const dstPath = path.join(projectPath, 'project.properties');
    const templatePath = path.join(ROOT, 'templates', 'project', 'project.properties');
    const srcPath = fs.existsSync(dstPath) ? dstPath : templatePath;

    let data = fs.readFileSync(srcPath, 'utf8');
    data = data.replace(/^target=.*/m, 'target=' + target_api);
    let subProjects = extractSubProjectPaths(data);
    subProjects = subProjects.filter(function (p) {
        return !(/^CordovaLib$/m.exec(p) ||
                 /[\\/]cordova-android[\\/]framework$/m.exec(p) ||
                 /^(\.\.[\\/])+framework$/m.exec(p));
    });
    subProjects.unshift('CordovaLib');
    data = data.replace(/^\s*android\.library\.reference\.\d+=.*\n/mg, '');
    if (!/\n$/.exec(data)) {
        data += '\n';
    }
    for (let i = 0; i < subProjects.length; ++i) {
        data += 'android.library.reference.' + (i + 1) + '=' + subProjects[i] + '\n';
    }
    fs.writeFileSync(dstPath, data);
}

// This makes no sense, what if you're building with a different build system?
function prepBuildFiles (projectPath) {
    const buildModule = require('./builders/builders');
    buildModule.getBuilder(projectPath).prepBuildFiles();
}

function copyBuildRules (projectPath) {
    const srcDir = path.join(ROOT, 'templates', 'project');

    fs.copySync(path.join(srcDir, 'build.gradle'), path.join(projectPath, 'build.gradle'));
    fs.copySync(path.join(srcDir, 'app', 'build.gradle'), path.join(projectPath, 'app', 'build.gradle'));
    fs.copySync(path.join(srcDir, 'app', 'repositories.gradle'), path.join(projectPath, 'app', 'repositories.gradle'));
    fs.copySync(path.join(srcDir, 'repositories.gradle'), path.join(projectPath, 'repositories.gradle'));
}

function copyScripts (projectPath) {
    const srcScriptsDir = path.join(ROOT, 'templates', 'cordova');
    const destScriptsDir = path.join(projectPath, 'cordova');
    // Delete old scripts directory if this is an update.
    fs.removeSync(destScriptsDir);
    // Copy in the new ones.
    fs.copySync(srcScriptsDir, destScriptsDir);
}

/**
 * Test whether a package name is acceptable for use as an android project.
 * Returns a promise, fulfilled if the package name is acceptable; rejected
 * otherwise.
 */
function validatePackageName (package_name) {
    // Make the package conform to Java package types
    // http://developer.android.com/guide/topics/manifest/manifest-element.html#package
    // Enforce underscore limitation
    const msg = 'Error validating package name. ';

    if (!/^[a-zA-Z][a-zA-Z0-9_]+(\.[a-zA-Z][a-zA-Z0-9_]*)+$/.test(package_name)) {
        return Promise.reject(new CordovaError(msg + 'Must look like: `com.company.Name`. Currently is: `' + package_name + '`'));
    }

    // Class is a reserved word
    if (/\b[Cc]lass\b/.test(package_name)) {
        return Promise.reject(new CordovaError(msg + '"class" is a reserved word'));
    }

    return Promise.resolve();
}

/**
 * Test whether given string is acceptable for use as a project name
 * Returns a promise, fulfilled if the project name is acceptable; rejected
 * otherwise.
 */
function validateProjectName (project_name) {
    const msg = 'Error validating project name. ';
    // Make sure there's something there
    if (project_name === '') {
        return Promise.reject(new CordovaError(msg + 'Project name cannot be empty'));
    }

    return Promise.resolve();
}

/**
 * Creates an android application with the given options.
 *
 * @param   {String}  project_path  Path to the new Cordova android project.
 * @param   {ConfigParser}  config  Instance of ConfigParser to retrieve basic
 *   project properties.
 * @param   {Object}  [options={}]  Various options
 * @param   {String}  [options.activityName='MainActivity']  Name for the
 *   activity
 * @param   {Boolean}  [options.link=false]  Specifies whether javascript files
 *   and CordovaLib framework will be symlinked to created application.
 * @param   {String}  [options.customTemplate]  Path to project template
 *   (override)
 * @param   {EventEmitter}  [events]  An EventEmitter instance for logging
 *   events
 *
 * @return  {Promise<String>}  Directory where application has been created
 */
exports.create = function (project_path, config, options, events) {
    options = options || {};

    // Set default values for path, package and name
    project_path = path.relative(process.cwd(), project_path);
    // Check if project already exists
    if (fs.existsSync(project_path)) {
        return Promise.reject(new CordovaError('Project already exists! Delete and recreate'));
    }

    const package_name = config.android_packageName() || config.packageName() || 'io.cordova.helloCordova';
    const project_name = config.name() || 'Hello Cordova';

    const safe_activity_name = config.android_activityName() || options.activityName || 'MainActivity';
    const target_api = check_reqs.get_target(project_path);
    const compile_api = check_reqs.get_compile(project_path);

    // Make the package conform to Java package types
    return exports.validatePackageName(package_name)
        .then(function () {
            return exports.validateProjectName(project_name);
        }).then(function () {
            // Log the given values for the project
            events.emit('log', 'Creating Cordova project for the Android platform:');
            events.emit('log', '\tPath: ' + project_path);
            events.emit('log', '\tPackage: ' + package_name);
            events.emit('log', '\tName: ' + project_name);
            events.emit('log', '\tActivity: ' + safe_activity_name);
            events.emit('log', '\tAndroid Target SDK: ' + target_api);
            events.emit('log', '\tAndroid Compile SDK: ' + compile_api);

            events.emit('verbose', 'Copying android template project to ' + project_path);

            const project_template_dir = options.customTemplate || path.join(ROOT, 'templates', 'project');
            const app_path = path.join(project_path, 'app', 'src', 'main');

            // copy project template
            fs.ensureDirSync(app_path);
            fs.copySync(path.join(project_template_dir, 'assets'), path.join(app_path, 'assets'));
            fs.copySync(path.join(project_template_dir, 'res'), path.join(app_path, 'res'));
            fs.copySync(path.join(project_template_dir, 'gitignore'), path.join(project_path, '.gitignore'));

            // Manually create directories that would be empty within the template (since git doesn't track directories).
            fs.ensureDirSync(path.join(app_path, 'libs'));

            // copy cordova.js, cordova.jar
            exports.copyJsAndLibrary(project_path, options.link, safe_activity_name, target_api);

            // Set up ther Android Studio paths
            const java_path = path.join(app_path, 'java');
            const assets_path = path.join(app_path, 'assets');
            const resource_path = path.join(app_path, 'res');
            fs.ensureDirSync(java_path);
            fs.ensureDirSync(assets_path);
            fs.ensureDirSync(resource_path);

            // store package name in cdv-gradle-config
            const cdvGradleConfig = CordovaGradleConfigParserFactory.create(project_path);
            cdvGradleConfig.setPackageName(package_name)
                .write();

            // interpolate the activity name and package
            const packagePath = package_name.replace(/\./g, path.sep);
            const activity_dir = path.join(java_path, packagePath);
            const activity_path = path.join(activity_dir, safe_activity_name + '.java');

            fs.ensureDirSync(activity_dir);
            fs.copySync(path.join(project_template_dir, 'Activity.java'), activity_path);
            utils.replaceFileContents(activity_path, /__ACTIVITY__/, safe_activity_name);
            utils.replaceFileContents(path.join(app_path, 'res', 'values', 'strings.xml'), /__NAME__/, utils.escape(project_name));
            utils.replaceFileContents(activity_path, /__ID__/, package_name);

            const manifest = new AndroidManifest(path.join(project_template_dir, 'AndroidManifest.xml'));
            manifest.getActivity().setName(safe_activity_name);

            const manifest_path = path.join(app_path, 'AndroidManifest.xml');
            manifest.write(manifest_path);

            exports.copyScripts(project_path);
            exports.copyBuildRules(project_path);

            // Link it to local android install.
            exports.writeProjectProperties(project_path, target_api);
            exports.prepBuildFiles(project_path);
            events.emit('log', generateDoneMessage('create', options.link));
        }).then(() => project_path);
};

function generateDoneMessage (type, link) {
    const pkg = require('../package');
    let msg = 'Android project ' + (type === 'update' ? 'updated ' : 'created ') + 'with ' + pkg.name + '@' + pkg.version;
    if (link) {
        msg += ' and has a linked CordovaLib';
    }
    return msg;
}

// Returns a promise.
exports.update = function (projectPath, options, events) {
    const errorString =
        'An in-place platform update is not supported. \n' +
        'The `platforms` folder is always treated as a build artifact in the CLI workflow.\n' +
        'To update your platform, you have to remove, then add your android platform again.\n' +
        'Make sure you save your plugins beforehand using `cordova plugin save`, and save \n' + 'a copy of the platform first if you had manual changes in it.\n' +
        '\tcordova plugin save\n' +
        '\tcordova platform rm android\n' +
        '\tcordova platform add android\n'
        ;

    return Promise.reject(errorString);
};
