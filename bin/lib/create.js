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
var shell = require('shelljs'),
    path  = require('path'),
    fs    = require('fs'),
    check_reqs = require('./check_reqs'),
    ROOT    = path.join(__dirname, '..', '..');

function exec(command) {
    var result;
    try {
        result = shell.exec(command, {silent:false, async:false});
    } catch(e) {
        console.error('Command error on execuation : ' + command);
        console.error(e);
        process.exit(2);
    }
    if(result && result.code > 0) {
        console.error('Command failed to execute : ' + command);
        console.error(result.output);
        process.exit(2);
    } else {
        return result;
    }
}

function setShellFatal(value, func) {
    var oldVal = shell.config.fatal;
    shell.config.fatal = value;
    func();
    shell.config.fatal = oldVal;
}

function ensureJarIsBuilt(version, target_api) {
    var isDevVersion = /-dev$/.test(version);
    if (isDevVersion || !fs.existsSync(path.join(ROOT, 'framework', 'cordova-' + version + '.jar')) && fs.existsSync(path.join(ROOT, 'framework'))) {
        var valid_target = check_reqs.get_target();
        console.log('Building cordova-' + version + '.jar');
        // update the cordova-android framework for the desired target
        exec('android --silent update lib-project --target "' + target_api + '" --path "' + path.join(ROOT, 'framework') + '"');
        // compile cordova.js and cordova.jar
        var cwd = process.cwd();
        process.chdir(path.join(ROOT, 'framework'));
        exec('ant jar');
        process.chdir(cwd);
    }
}

function copyJsAndJar(projectPath, version) {
    shell.cp('-f', path.join(ROOT, 'framework', 'assets', 'www', 'cordova.js'), path.join(projectPath, 'assets', 'www', 'cordova.js'));
    // Don't fail if there are no old jars.
    setShellFatal(false, function() {
        shell.ls(path.join(projectPath, 'libs', 'cordova-*.jar')).forEach(function(oldJar) {
            shell.rm('-f', path.join(projectPath, 'libs', oldJar));
        });
    });
    shell.cp('-f', path.join(ROOT, 'framework', 'cordova-' + version + '.jar'), path.join(projectPath, 'libs', 'cordova-' + version + '.jar'));
}

function copyScripts(projectPath) {
    var srcScriptsDir = path.join(ROOT, 'bin', 'templates', 'cordova');
    var destScriptsDir = path.join(projectPath, 'cordova');
    // Delete old scripts directory if this is an update.
    shell.rm('-rf', destScriptsDir);
    // Copy in the new ones.
    shell.cp('-r', srcScriptsDir, projectPath);
    shell.cp('-r', path.join(ROOT, 'bin', 'node_modules'), destScriptsDir);
    shell.cp(path.join(ROOT, 'bin', 'check_reqs'), path.join(destScriptsDir, 'check_reqs'));
    shell.cp(path.join(ROOT, 'bin', 'lib', 'check_reqs.js'), path.join(projectPath, 'cordova', 'lib', 'check_reqs.js'));

    if (!/^win/.test(process.platform)) {
        // Ensure they are all executable and delete .bat files.
        shell.find(destScriptsDir).forEach(function(p) {
            if (/\.bat$/.test(p)) {
                shell.rm(p);
            } else {
                shell.chmod(755, p);
            }
        });
    }
}

/**
 * $ create [options]
 *
 * Creates an android application with the given options.
 *
 * Options:
 *
 *   - `project_path` 	{String} Path to the new Cordova android project.
 *   - `package_name`{String} Package name, following reverse-domain style convention.
 *   - `project_name` 	{String} Project name.
 *   - 'project_template_dir' {String} Path to project template (override).
 */

exports.createProject = function(project_path, package_name, project_name, project_template_dir) {
    var VERSION = fs.readFileSync(path.join(ROOT, 'VERSION'), 'utf-8').trim();

    // Set default values for path, package and name
    project_path = typeof project_path !== 'undefined' ? project_path : "CordovaExample";
    project_path = path.relative(process.cwd(), project_path);
    package_name = typeof package_name !== 'undefined' ? package_name : 'my.cordova.project';
    project_name = typeof project_name !== 'undefined' ? project_name : 'CordovaExample';
    project_template_dir = typeof project_template_dir !== 'undefined' ? 
                           project_template_dir : 
                           path.join(ROOT, 'bin', 'templates', 'project');

    var safe_activity_name = project_name.replace(/\W/g, '');
    var package_as_path = package_name.replace(/\./g, path.sep);
    var activity_dir    = path.join(project_path, 'src', package_as_path);
    var activity_path   = path.join(activity_dir, safe_activity_name + '.java');
    var target_api      = check_reqs.get_target();
    var manifest_path   = path.join(project_path, 'AndroidManifest.xml');

    // Check if project already exists
    if(fs.existsSync(project_path)) {
        console.error('Project already exists! Delete and recreate');
        process.exit(2);
    }

    if (!/[a-zA-Z0-9_]+\.[a-zA-Z0-9_](.[a-zA-Z0-9_])*/.test(package_name)) {
        console.error('Package name must look like: com.company.Name');
        process.exit(2);
    }

    // Check that requirements are met and proper targets are installed
    if(!check_reqs.run()) {
        process.exit(2);
    }

    // Log the given values for the project
    console.log('Creating Cordova project for the Android platform:');
    console.log('\tPath: ' + project_path);
    console.log('\tPackage: ' + package_name);
    console.log('\tName: ' + project_name);
    console.log('\tAndroid target: ' + target_api);

    // build from source. distro should have these files
    ensureJarIsBuilt(VERSION, target_api);

    console.log('Copying template files...');

    setShellFatal(true, function() {
        // copy project template
        shell.cp('-r', path.join(project_template_dir, 'assets'), project_path);
        shell.cp('-r', path.join(project_template_dir, 'res'), project_path);
        // Manually create directories that would be empty within the template (since git doesn't track directories).
        shell.mkdir(path.join(project_path, 'libs'));

        // copy cordova.js, cordova.jar and res/xml
        shell.cp('-r', path.join(ROOT, 'framework', 'res', 'xml'), path.join(project_path, 'res'));
        copyJsAndJar(project_path, VERSION);

        // interpolate the activity name and package
        shell.mkdir('-p', activity_dir);
        shell.cp('-f', path.join(project_template_dir, 'Activity.java'), activity_path);
        shell.sed('-i', /__ACTIVITY__/, safe_activity_name, activity_path);
        shell.sed('-i', /__NAME__/, project_name, path.join(project_path, 'res', 'values', 'strings.xml'));
        shell.sed('-i', /__ID__/, package_name, activity_path);

        shell.cp('-f', path.join(project_template_dir, 'AndroidManifest.xml'), manifest_path);
        shell.sed('-i', /__ACTIVITY__/, safe_activity_name, manifest_path);
        shell.sed('-i', /__PACKAGE__/, package_name, manifest_path);
        shell.sed('-i', /__APILEVEL__/, target_api.split('-')[1], manifest_path);
        copyScripts(project_path);
    });
    // Link it to local android install.
    console.log('Running "android update project"');
    exec('android --silent update project --target "'+target_api+'" --path "'+ project_path+'"');
    console.log('Project successfully created.');
}

exports.updateProject = function(projectPath) {
    // Check that requirements are met and proper targets are installed
    if (!check_reqs.run()) {
        process.exit(2);
    }
    var version = fs.readFileSync(path.join(ROOT, 'VERSION'), 'utf-8').trim();
    var target_api = check_reqs.get_target();
    ensureJarIsBuilt(version, target_api);
    copyJsAndJar(projectPath, version);
    copyScripts(projectPath);
    console.log('Android project is now at version ' + version);
};

