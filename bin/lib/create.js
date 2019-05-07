#!/usr/bin/env node

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

var shell = require('shelljs');
var Q = require('q');
var path = require('path');
var fs = require('fs');
var check_reqs = require('./../templates/cordova/lib/check_reqs');
var ROOT = path.join(__dirname, '..', '..');

var CordovaError = require('cordova-common').CordovaError;
var AndroidManifest = require('../templates/cordova/lib/AndroidManifest');

// Export all helper functions, and make sure internally within this module, we
// reference these methods via the `exports` object - this helps with testing
// (since we can then mock and control behaviour of all of these functions)
exports.validatePackageName = validatePackageName;
exports.validateProjectName = validateProjectName;
exports.setShellFatal = setShellFatal;
exports.copyJsAndLibrary = copyJsAndLibrary;
exports.copyScripts = copyScripts;
exports.copyBuildRules = copyBuildRules;
exports.writeProjectProperties = writeProjectProperties;
exports.prepBuildFiles = prepBuildFiles;

function setShellFatal (value, func) {
    var oldVal = shell.config.fatal;
    shell.config.fatal = value;
    func();
    shell.config.fatal = oldVal;
}

function getFrameworkDir (projectPath, shared) {
    return shared ? path.join(ROOT, 'framework') : path.join(projectPath, 'CordovaLib');
}

function copyJsAndLibrary (projectPath, shared, projectName, isLegacy) {
    var nestedCordovaLibPath = getFrameworkDir(projectPath, false);
    var srcCordovaJsPath = path.join(ROOT, 'bin', 'templates', 'project', 'assets', 'www', 'cordova.js');
    var app_path = path.join(projectPath, 'app', 'src', 'main');

    if (isLegacy) {
        app_path = projectPath;
    }

    shell.cp('-f', srcCordovaJsPath, path.join(app_path, 'assets', 'www', 'cordova.js'));

    // Copy the cordova.js file to platforms/<platform>/platform_www/
    // The www dir is nuked on each prepare so we keep cordova.js in platform_www
    shell.mkdir('-p', path.join(projectPath, 'platform_www'));
    shell.cp('-f', srcCordovaJsPath, path.join(projectPath, 'platform_www'));

    // Copy cordova-js-src directory into platform_www directory.
    // We need these files to build cordova.js if using browserify method.
    shell.cp('-rf', path.join(ROOT, 'cordova-js-src'), path.join(projectPath, 'platform_www'));

    // Don't fail if there are no old jars.
    exports.setShellFatal(false, function () {
        shell.ls(path.join(app_path, 'libs', 'cordova-*.jar')).forEach(function (oldJar) {
            console.log('Deleting ' + oldJar);
            shell.rm('-f', oldJar);
        });
        var wasSymlink = true;
        try {
            // Delete the symlink if it was one.
            fs.unlinkSync(nestedCordovaLibPath);
        } catch (e) {
            wasSymlink = false;
        }
        // Delete old library project if it existed.
        if (shared) {
            shell.rm('-rf', nestedCordovaLibPath);
        } else if (!wasSymlink) {
            // Delete only the src, since Eclipse / Android Studio can't handle their project files being deleted.
            shell.rm('-rf', path.join(nestedCordovaLibPath, 'src'));
        }
    });
    if (shared) {
        var relativeFrameworkPath = path.relative(projectPath, getFrameworkDir(projectPath, true));
        fs.symlinkSync(relativeFrameworkPath, nestedCordovaLibPath, 'dir');
    } else {
        shell.mkdir('-p', nestedCordovaLibPath);
        shell.cp('-f', path.join(ROOT, 'framework', 'AndroidManifest.xml'), nestedCordovaLibPath);
        shell.cp('-f', path.join(ROOT, 'framework', 'project.properties'), nestedCordovaLibPath);
        shell.cp('-f', path.join(ROOT, 'framework', 'build.gradle'), nestedCordovaLibPath);
        shell.cp('-f', path.join(ROOT, 'framework', 'cordova.gradle'), nestedCordovaLibPath);
        shell.cp('-r', path.join(ROOT, 'framework', 'src'), nestedCordovaLibPath);
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
    data = data.replace(/^target=.*/m, 'target=' + target_api);
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
        shell.cp('-f', path.join(srcDir, 'legacy', 'build.gradle'), projectPath);
        shell.cp('-f', path.join(srcDir, 'wrapper.gradle'), projectPath);
    } else {
        shell.cp('-f', path.join(srcDir, 'build.gradle'), projectPath);
        shell.cp('-f', path.join(srcDir, 'app', 'build.gradle'), path.join(projectPath, 'app'));
        shell.cp('-f', path.join(srcDir, 'wrapper.gradle'), projectPath);
    }
}

function copyScripts (projectPath) {
    var bin = path.join(ROOT, 'bin');
    var srcScriptsDir = path.join(bin, 'templates', 'cordova');
    var destScriptsDir = path.join(projectPath, 'cordova');
    // Delete old scripts directory if this is an update.
    shell.rm('-rf', destScriptsDir);
    // Copy in the new ones.
    shell.cp('-r', srcScriptsDir, projectPath);

    let nodeModulesDir = path.join(ROOT, 'node_modules');
    if (fs.existsSync(nodeModulesDir)) shell.cp('-r', nodeModulesDir, destScriptsDir);

    shell.cp(path.join(bin, 'check_reqs*'), destScriptsDir);
    shell.cp(path.join(bin, 'android_sdk_version*'), destScriptsDir);
    var check_reqs = path.join(destScriptsDir, 'check_reqs');
    var android_sdk_version = path.join(destScriptsDir, 'android_sdk_version');
    // TODO: the two files being edited on-the-fly here are shared between
    // platform and project-level commands. the below `sed` is updating the
    // `require` path for the two libraries. if there's a better way to share
    // modules across both the repo and generated projects, we should make sure
    // to remove/update this.
    shell.sed('-i', /templates\/cordova\//, '', android_sdk_version);
    shell.sed('-i', /templates\/cordova\//, '', check_reqs);
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
        return Q.reject(new CordovaError(msg + 'Must look like: `com.company.Name`. Currently is: `' + package_name + '`'));
    }

    // Class is a reserved word
    if (/\b[Cc]lass\b/.test(package_name)) {
        return Q.reject(new CordovaError(msg + '"class" is a reserved word'));
    }

    return Q.resolve();
}

/**
 * Test whether a project name is acceptable for use as an android class.
 * Returns a promise, fulfilled if the project name is acceptable; rejected
 * otherwise.
 */
function validateProjectName (project_name) {
    var msg = 'Error validating project name. ';
    // Make sure there's something there
    if (project_name === '') {
        return Q.reject(new CordovaError(msg + 'Project name cannot be empty'));
    }

    // Enforce stupid name error
    if (project_name === 'CordovaActivity') {
        return Q.reject(new CordovaError(msg + 'Project name cannot be CordovaActivity'));
    }

    // Classes in Java don't begin with numbers
    if (/^[0-9]/.test(project_name)) {
        return Q.reject(new CordovaError(msg + 'Project name must not begin with a number'));
    }

    return Q.resolve();
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
        return Q.reject(new CordovaError('Project already exists! Delete and recreate'));
    }

    var package_name = config.android_packageName() || config.packageName() || 'my.cordova.project';
    var project_name = config.name() ?
        config.name().replace(/[^\w.]/g, '_') : 'CordovaExample';

    var safe_activity_name = config.android_activityName() || options.activityName || 'MainActivity';
    var target_api = check_reqs.get_target();

    // Make the package conform to Java package types
    return exports.validatePackageName(package_name)
        .then(function () {
            exports.validateProjectName(project_name);
        }).then(function () {
        // Log the given values for the project
            events.emit('log', 'Creating Cordova project for the Android platform:');
            events.emit('log', '\tPath: ' + project_path);
            events.emit('log', '\tPackage: ' + package_name);
            events.emit('log', '\tName: ' + project_name);
            events.emit('log', '\tActivity: ' + safe_activity_name);
            events.emit('log', '\tAndroid target: ' + target_api);

            events.emit('verbose', 'Copying android template project to ' + project_path);

            exports.setShellFatal(true, function () {
                var project_template_dir = options.customTemplate || path.join(ROOT, 'bin', 'templates', 'project');
                var app_path = path.join(project_path, 'app', 'src', 'main');

                // copy project template
                shell.mkdir('-p', app_path);
                shell.cp('-r', path.join(project_template_dir, 'assets'), app_path);
                shell.cp('-r', path.join(project_template_dir, 'res'), app_path);
                shell.cp(path.join(project_template_dir, 'gitignore'), path.join(project_path, '.gitignore'));

                // Manually create directories that would be empty within the template (since git doesn't track directories).
                shell.mkdir(path.join(app_path, 'libs'));

                // copy cordova.js, cordova.jar
                exports.copyJsAndLibrary(project_path, options.link, safe_activity_name);

                // Set up ther Android Studio paths
                var java_path = path.join(app_path, 'java');
                var assets_path = path.join(app_path, 'assets');
                var resource_path = path.join(app_path, 'res');
                shell.mkdir('-p', java_path);
                shell.mkdir('-p', assets_path);
                shell.mkdir('-p', resource_path);

                // interpolate the activity name and package
                var packagePath = package_name.replace(/\./g, path.sep);
                var activity_dir = path.join(java_path, packagePath);
                var activity_path = path.join(activity_dir, safe_activity_name + '.java');

                shell.mkdir('-p', activity_dir);
                shell.cp('-f', path.join(project_template_dir, 'Activity.java'), activity_path);
                shell.sed('-i', /__ACTIVITY__/, safe_activity_name, activity_path);
                shell.sed('-i', /__NAME__/, project_name, path.join(app_path, 'res', 'values', 'strings.xml'));
                shell.sed('-i', /__ID__/, package_name, activity_path);

                var manifest = new AndroidManifest(path.join(project_template_dir, 'AndroidManifest.xml'));
                manifest.setPackageId(package_name)
                    .getActivity().setName(safe_activity_name);

                var manifest_path = path.join(app_path, 'AndroidManifest.xml');
                manifest.write(manifest_path);

                exports.copyScripts(project_path);
                exports.copyBuildRules(project_path);
            });
            // Link it to local android install.
            exports.writeProjectProperties(project_path, target_api);
            exports.prepBuildFiles(project_path);
            events.emit('log', generateDoneMessage('create', options.link));
        }).thenResolve(project_path);
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

    return Q.reject(errorString);
};
