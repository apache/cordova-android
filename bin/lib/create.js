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

var path = require('path');
var fs = require('fs-extra');
var utils = require('../templates/cordova/lib/utils');
var constants = require('../../framework/defaults.json');
var ROOT = path.join(__dirname, '..', '..');
const TemplateFile = require('../templates/cordova/lib/TemplateFile');

var CordovaError = require('cordova-common').CordovaError;
var AndroidManifest = require('../templates/cordova/lib/AndroidManifest');

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
exports.writeNameForAndroidStudio = writeNameForAndroidStudio;

function getFrameworkDir (projectPath, shared) {
    return shared ? path.join(ROOT, 'framework') : path.join(projectPath, 'CordovaLib');
}

function copyJsAndLibrary (projectPath, shared, projectName, targetAPI) {
    var nestedCordovaLibPath = getFrameworkDir(projectPath, false);
    var srcCordovaJsPath = path.join(ROOT, 'bin', 'templates', 'project', 'assets', 'www', 'cordova.js');
    var app_path = path.join(projectPath, 'app', 'src', 'main');
    const platform_www = path.join(projectPath, 'platform_www');

    fs.copySync(srcCordovaJsPath, path.join(app_path, 'assets', 'www', 'cordova.js'));

    // Copy the cordova.js file to platforms/<platform>/platform_www/
    // The www dir is nuked on each prepare so we keep cordova.js in platform_www
    fs.ensureDirSync(platform_www);
    fs.copySync(srcCordovaJsPath, path.join(platform_www, 'cordova.js'));

    // Copy cordova-js-src directory into platform_www directory.
    // We need these files to build cordova.js if using browserify method.
    fs.copySync(path.join(ROOT, 'cordova-js-src'), path.join(platform_www, 'cordova-js-src'));

    if (shared) {
        var relativeFrameworkPath = path.relative(projectPath, getFrameworkDir(projectPath, true));
        fs.symlinkSync(relativeFrameworkPath, nestedCordovaLibPath, 'dir');
    } else {
        fs.ensureDirSync(nestedCordovaLibPath);
        fs.copySync(path.join(ROOT, 'framework', 'AndroidManifest.xml'), path.join(nestedCordovaLibPath, 'AndroidManifest.xml'));
        TemplateFile.render(path.join(ROOT, 'framework', 'project.properties'), path.join(nestedCordovaLibPath, 'project.properties'), {
            DEFAULT_SDK_VERSION: targetAPI || constants.DEFAULT_SDK_VERSION,
            DEFAULTS_FILE_PATH: './defaults.json'
        });
        TemplateFile.render(path.join(ROOT, 'framework', 'build.gradle'), path.join(nestedCordovaLibPath, 'build.gradle'), {
            DEFAULT_MIN_SDK_VERSION: constants.DEFAULT_MIN_SDK_VERSION
        });
        TemplateFile.render(path.join(ROOT, 'framework', 'cordova.gradle'), path.join(nestedCordovaLibPath, 'cordova.gradle'), {
            DEFAULT_BUILD_TOOLS_VERSION: constants.DEFAULT_BUILD_TOOLS_VERSION
        });
        fs.copySync(path.join(ROOT, 'framework', 'repositories.gradle'), path.join(nestedCordovaLibPath, 'repositories.gradle'));
        fs.copySync(path.join(ROOT, 'framework', 'src'), path.join(nestedCordovaLibPath, 'src'));
        fs.copySync(path.join(ROOT, 'framework', 'defaults.json'), path.join(projectPath, 'defaults.json'));
    }
}

function extractSubProjectPaths (data) {
    var ret = {};
    var r = /^\s*android\.library\.reference\.\d+=(.*)(?:\s|$)/mg;
    var m;
    while ((m = r.exec(data))) {
        ret[m[1]] = 1;
    }
    return Object.keys(ret);
}

function writeProjectProperties (projectPath, target_api) {
    var dstPath = path.join(projectPath, 'project.properties');
    var templatePath = path.join(ROOT, 'bin', 'templates', 'project', 'project.properties');
    var srcPath = fs.existsSync(dstPath) ? dstPath : templatePath;

    var data = fs.readFileSync(srcPath, 'utf8');
    data = data.replace(/^target=.*/m, `target=android-${target_api}`);
    var subProjects = extractSubProjectPaths(data);
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
    for (var i = 0; i < subProjects.length; ++i) {
        data += 'android.library.reference.' + (i + 1) + '=' + subProjects[i] + '\n';
    }
    fs.writeFileSync(dstPath, data);
}

// This makes no sense, what if you're building with a different build system?
function prepBuildFiles (projectPath) {
    var buildModule = require('../templates/cordova/lib/builders/builders');
    buildModule.getBuilder(projectPath).prepBuildFiles();
}

function copyBuildRules (projectPath, isLegacy) {
    var srcDir = path.join(ROOT, 'bin', 'templates', 'project');

    if (isLegacy) {
        // The project's build.gradle is identical to the earlier build.gradle, so it should still work
        fs.copySync(path.join(srcDir, 'legacy', 'build.gradle'), path.join(projectPath, 'legacy', 'build.gradle'));
        fs.copySync(path.join(srcDir, 'wrapper.gradle'), path.join(projectPath, 'wrapper.gradle'));
    } else {
        TemplateFile.render(path.join(srcDir, 'build.gradle'), path.join(projectPath, 'build.gradle'), {
            DEFAULT_BUILD_TOOLS_VERSION: constants.DEFAULT_BUILD_TOOLS_VERSION,
            DEFAULT_MIN_SDK_VERSION: constants.DEFAULT_MIN_SDK_VERSION,
            DEFAULT_SDK_VERSION: constants.DEFAULT_SDK_VERSION
        });
        TemplateFile.render(path.join(srcDir, 'app', 'build.gradle'), path.join(projectPath, 'app', 'build.gradle'), {
            DEFAULT_GRADLE_VERSION: constants.DEFAULT_GRADLE_VERSION
        });
        fs.copySync(path.join(srcDir, 'app', 'repositories.gradle'), path.join(projectPath, 'app', 'repositories.gradle'));
        fs.copySync(path.join(srcDir, 'repositories.gradle'), path.join(projectPath, 'repositories.gradle'));
        fs.copySync(path.join(srcDir, 'wrapper.gradle'), path.join(projectPath, 'wrapper.gradle'));
    }
}

function copyScripts (projectPath) {
    var bin = path.join(ROOT, 'bin');
    var srcScriptsDir = path.join(bin, 'templates', 'cordova');
    var destScriptsDir = path.join(projectPath, 'cordova');
    // Delete old scripts directory if this is an update.
    fs.removeSync(destScriptsDir);
    // Copy in the new ones.
    fs.copySync(srcScriptsDir, destScriptsDir);

    const nodeModulesDir = path.join(ROOT, 'node_modules');
    if (fs.existsSync(nodeModulesDir)) fs.copySync(nodeModulesDir, path.join(destScriptsDir, 'node_modules'));

    fs.copySync(path.join(bin, 'check_reqs'), path.join(destScriptsDir, 'check_reqs'));
    fs.copySync(path.join(bin, 'check_reqs.bat'), path.join(destScriptsDir, 'check_reqs.bat'));
    fs.copySync(path.join(bin, 'android_sdk_version'), path.join(destScriptsDir, 'android_sdk_version'));
    fs.copySync(path.join(bin, 'android_sdk_version.bat'), path.join(destScriptsDir, 'android_sdk_version.bat'));

    var check_reqs = path.join(destScriptsDir, 'check_reqs');
    var android_sdk_version = path.join(destScriptsDir, 'android_sdk_version');

    // TODO: the two files being edited on-the-fly here are shared between
    // platform and project-level commands. the below is updating the
    // `require` path for the two libraries. if there's a better way to share
    // modules across both the repo and generated projects, we should make sure
    // to remove/update this.
    const templatesCordovaRegex = /templates\/cordova\//;
    utils.replaceFileContents(android_sdk_version, templatesCordovaRegex, '');
    utils.replaceFileContents(check_reqs, templatesCordovaRegex, '');
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
    var msg = 'Error validating package name. ';

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
    var msg = 'Error validating project name. ';
    // Make sure there's something there
    if (project_name === '') {
        return Promise.reject(new CordovaError(msg + 'Project name cannot be empty'));
    }

    return Promise.resolve();
}

/**
 * Write the name of the app in "platforms/android/.idea/.name" so that Android Studio can show that name in the
 * project listing. This is helpful to quickly look in the Android Studio listing if there are so many projects in
 * Android Studio.
 *
 * https://github.com/apache/cordova-android/issues/1172
 */
function writeNameForAndroidStudio (project_path, project_name) {
    const ideaPath = path.join(project_path, '.idea');
    fs.ensureDirSync(ideaPath);
    fs.writeFileSync(path.join(ideaPath, '.name'), project_name);
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
    project_path = path.relative(process.cwd(), (project_path || 'CordovaExample'));
    // Check if project already exists
    if (fs.existsSync(project_path)) {
        return Promise.reject(new CordovaError('Project already exists! Delete and recreate'));
    }

    var package_name = config.android_packageName() || config.packageName() || 'my.cordova.project';
    var project_name = config.name()
        ? config.name().replace(/[^\w.]/g, '_') : 'CordovaExample';

    var safe_activity_name = config.android_activityName() || options.activityName || 'MainActivity';
    let target_api = parseInt(config.getPreference('android-targetSdkVersion', 'android'), 10);
    if (isNaN(target_api)) {
        target_api = constants.DEFAULT_SDK_VERSION;
    }

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
            events.emit('log', '\tAndroid target: ' + target_api);

            events.emit('verbose', 'Copying android template project to ' + project_path);

            var project_template_dir = options.customTemplate || path.join(ROOT, 'bin', 'templates', 'project');
            var app_path = path.join(project_path, 'app', 'src', 'main');

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
            var java_path = path.join(app_path, 'java');
            var assets_path = path.join(app_path, 'assets');
            var resource_path = path.join(app_path, 'res');
            fs.ensureDirSync(java_path);
            fs.ensureDirSync(assets_path);
            fs.ensureDirSync(resource_path);

            // interpolate the activity name and package
            var packagePath = package_name.replace(/\./g, path.sep);
            var activity_dir = path.join(java_path, packagePath);
            var activity_path = path.join(activity_dir, safe_activity_name + '.java');

            fs.ensureDirSync(activity_dir);
            fs.copySync(path.join(project_template_dir, 'Activity.java'), activity_path);
            utils.replaceFileContents(activity_path, /__ACTIVITY__/, safe_activity_name);
            utils.replaceFileContents(path.join(app_path, 'res', 'values', 'strings.xml'), /__NAME__/, project_name);
            utils.replaceFileContents(activity_path, /__ID__/, package_name);

            var manifest = new AndroidManifest(path.join(project_template_dir, 'AndroidManifest.xml'));
            manifest.setPackageId(package_name)
                .getActivity().setName(safe_activity_name);

            var manifest_path = path.join(app_path, 'AndroidManifest.xml');
            manifest.write(manifest_path);

            exports.copyScripts(project_path);
            exports.copyBuildRules(project_path);

            // Link it to local android install.
            exports.writeProjectProperties(project_path, target_api);
            exports.prepBuildFiles(project_path);
            exports.writeNameForAndroidStudio(project_path, project_name);
            events.emit('log', generateDoneMessage('create', options.link));
        }).then(() => project_path);
};

function generateDoneMessage (type, link) {
    var pkg = require('../../package');
    var msg = 'Android project ' + (type === 'update' ? 'updated ' : 'created ') + 'with ' + pkg.name + '@' + pkg.version;
    if (link) {
        msg += ' and has a linked CordovaLib';
    }
    return msg;
}

// Returns a promise.
exports.update = function (projectPath, options, events) {
    var errorString =
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
