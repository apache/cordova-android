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

module.exports.run = function(project_path, package_name, project_name, project_template_dir) {

    var VERSION = fs.readFileSync(path.join(ROOT, 'VERSION'), 'utf-8');

    // Set default values for path, package and name
    project_path = typeof project_path !== 'undefined' ? project_path : "CordovaExample";
    package_name = typeof package_name !== 'undefined' ? package_name : 'my.cordova.project';
    project_name = typeof project_name !== 'undefined' ? project_name : 'CordovaExample';
    project_template_dir = typeof project_template_dir !== 'undefined' ? 
                           project_template_dir : 
                           path.join(ROOT, 'bin', 'templates', 'project');

    var safe_activity_name = project_name.replace(/\W/, '');
    var package_as_path = package_name.replace(/\./g, path.sep);
    var activity_dir    = path.join(project_path, 'src', package_as_path);
    var activity_path   = path.join(activity_dir, safe_activity_name + '.java');
    var target_api      = check_reqs.get_target();
    var strings_path    = path.join(project_path, 'res', 'values', 'strings.xml');
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
    console.log('Creating Cordova project for the Android platform :');
    console.log('\tPath : ' + path.relative(process.cwd(), project_path));
    console.log('\tPackage : ' + package_name);
    console.log('\tName : ' + project_name);
    console.log('\tAndroid target : ' + target_api);

    // build from source. distro should have these files
    if(!fs.existsSync(path.join(ROOT, 'framework', 'cordova-' + VERSION + '.jar')) && fs.existsSync(path.join(ROOT, 'framework'))) {
        console.log('Building jar and js files...');
        // update the cordova-android framework for the desired target
        exec('android update project --target ' + target_api + ' --path ' + path.join(ROOT, 'framework'));

        // compile cordova.js and cordova.jar
        var cwd = process.cwd();
        process.chdir(path.join(ROOT, 'framework'));
        exec('ant jar');
        process.chdir(cwd);
    }

    // create new android project
    var create_cmd = 'android create project --target "'+target_api+'" --path "'+path.relative(process.cwd(), project_path)+'" --package "'+package_name+'" --activity "'+safe_activity_name+'"';
    exec(create_cmd);

    console.log('Copying template files...');

    // copy project template
    shell.cp('-r', path.join(project_template_dir, 'assets'), project_path);
    shell.cp('-r', path.join(project_template_dir, 'res'), project_path);

    // copy cordova.js, cordova.jar and res/xml
    if(fs.existsSync(path.join(ROOT, 'framework'))) {
        shell.cp('-r', path.join(ROOT, 'framework', 'res', 'xml'), path.join(project_path, 'res'));
        shell.cp(path.join(ROOT, 'framework', 'assets', 'www', 'cordova.js'), path.join(project_path, 'assets', 'www', 'cordova.js'));
        shell.cp(path.join(ROOT, 'framework', 'cordova-' + VERSION + '.jar'), path.join(project_path, 'libs', 'cordova-' + VERSION + '.jar'));
    } else {
        shell.cp('-r', path.join(ROOT, 'xml'), path.join(project_path, 'res'));
        shell.cp(path.join(ROOT, 'cordova.js'), path.join(project_path, 'assets', 'www', 'cordova.js'));
        shell.cp(path.join(ROOT, 'cordova-' + VERSION + '.jar'), path.join(project_path, 'libs', 'cordova-' + VERSION + '.jar'));
    }

    // interpolate the activity name and package
    shell.mkdir('-p', activity_dir);
    shell.cp('-f', path.join(project_template_dir, 'Activity.java'), activity_path);
    replaceInFile(activity_path, /__ACTIVITY__/, safe_activity_name);
    replaceInFile(activity_path, /__ID__/, package_name);

    // interpolate the app name into strings.xml
    replaceInFile(strings_path, />Cordova</, '>' + project_name + '<');

    shell.cp('-f', path.join(project_template_dir, 'AndroidManifest.xml'), manifest_path);
    replaceInFile(manifest_path, /__ACTIVITY__/, safe_activity_name);
    replaceInFile(manifest_path, /__PACKAGE__/, package_name);
    replaceInFile(manifest_path, /__APILEVEL__/, target_api.split('-')[1]);

    var cordova_path = path.join(ROOT, 'bin', 'templates', 'cordova');
    // creating cordova folder and copying run/build/log/launch/check_reqs scripts
    var lib_path = path.join(cordova_path, 'lib');
    shell.mkdir(path.join(project_path, 'cordova'));
    shell.mkdir(path.join(project_path, 'cordova', 'lib'));

    shell.cp(path.join(cordova_path, 'build'), path.join(project_path, 'cordova', 'build'));
    shell.chmod(755, path.join(project_path, 'cordova', 'build'));
    shell.cp(path.join(cordova_path, 'clean'), path.join(project_path, 'cordova', 'clean'));
    shell.chmod(755, path.join(project_path, 'cordova', 'clean'));
    shell.cp(path.join(cordova_path, 'log'), path.join(project_path, 'cordova', 'log'));
    shell.chmod(755, path.join(project_path, 'cordova', 'log'));
    shell.cp(path.join(cordova_path, 'run'), path.join(project_path, 'cordova', 'run'));
    shell.chmod(755, path.join(project_path, 'cordova', 'run'));
    shell.cp(path.join(cordova_path, 'version'), path.join(project_path, 'cordova', 'version'));
    shell.chmod(755, path.join(project_path, 'cordova', 'version'));
    shell.cp(path.join(ROOT, 'bin', 'check_reqs'), path.join(project_path, 'cordova', 'check_reqs'));
    shell.chmod(755, path.join(project_path, 'cordova', 'check_reqs'));

    shell.cp(path.join(lib_path, 'appinfo.js'), path.join(project_path, 'cordova', 'lib', 'appinfo.js'));
    shell.cp(path.join(lib_path, 'build.js'), path.join(project_path, 'cordova', 'lib', 'build.js'));
    shell.cp(path.join(ROOT, 'bin', 'lib', 'check_reqs.js'), path.join(project_path, 'cordova', 'lib', 'check_reqs.js'));
    shell.cp(path.join(lib_path, 'clean.js'), path.join(project_path, 'cordova', 'lib', 'clean.js'));
    shell.cp(path.join(lib_path, 'device.js'), path.join(project_path, 'cordova', 'lib', 'device.js'));
    shell.cp(path.join(lib_path, 'emulator.js'), path.join(project_path, 'cordova', 'lib', 'emulator.js'));
    shell.cp(path.join(lib_path, 'log.js'), path.join(project_path, 'cordova', 'lib', 'log.js'));
    shell.cp(path.join(lib_path, 'run.js'), path.join(project_path, 'cordova', 'lib', 'run.js'));
    shell.cp(path.join(lib_path, 'install-device'), path.join(project_path, 'cordova', 'lib', 'install-device'));
    shell.chmod(755, path.join(project_path, 'cordova', 'lib', 'install-device'));
    shell.cp(path.join(lib_path, 'install-emulator'), path.join(project_path, 'cordova', 'lib', 'install-emulator'));
    shell.chmod(755, path.join(project_path, 'cordova', 'lib', 'install-emulator'));
    shell.cp(path.join(lib_path, 'list-devices'), path.join(project_path, 'cordova', 'lib', 'list-devices'));
    shell.chmod(755, path.join(project_path, 'cordova', 'lib', 'list-devices'));
    shell.cp(path.join(lib_path, 'list-emulator-images'), path.join(project_path, 'cordova', 'lib', 'list-emulator-images'));
    shell.chmod(755, path.join(project_path, 'cordova', 'lib', 'list-emulator-images'));
    shell.cp(path.join(lib_path, 'list-started-emulators'), path.join(project_path, 'cordova', 'lib', 'list-started-emulators'));
    shell.chmod(755, path.join(project_path, 'cordova', 'lib', 'list-started-emulators'));
    shell.cp(path.join(lib_path, 'start-emulator'), path.join(project_path, 'cordova', 'lib', 'start-emulator'));
    shell.chmod(755, path.join(project_path, 'cordova', 'lib', 'start-emulator'));

    // if on windows, copy .bat scripts
    // TODO : make these not nessesary, they clutter the scripting folder.
    if(process.platform == 'win32' || process.platform == 'win64') {
        shell.cp(path.join(cordova_path, 'build.bat'), path.join(project_path, 'cordova', 'build.bat'));
        shell.cp(path.join(cordova_path, 'clean.bat'), path.join(project_path, 'cordova', 'clean.bat'));
        shell.cp(path.join(cordova_path, 'log.bat'), path.join(project_path, 'cordova', 'log.bat'));
        shell.cp(path.join(cordova_path, 'run.bat'), path.join(project_path, 'cordova', 'run.bat'));
        shell.cp(path.join(cordova_path, 'version.bat'), path.join(project_path, 'cordova', 'version.bat'));
        shell.cp(path.join(ROOT, 'bin', 'check_reqs.bat'), path.join(project_path, 'cordova', 'check_reqs.bat'));

        // lib scripts
        shell.cp(path.join(lib_path, 'install-device.bat'), path.join(project_path, 'cordova', 'lib', 'install-device.bat'));
        shell.cp(path.join(lib_path, 'install-emulator.bat'), path.join(project_path, 'cordova', 'lib', 'install-emulator.bat'));
        shell.cp(path.join(lib_path, 'list-devices.bat'), path.join(project_path, 'cordova', 'lib', 'list-devices.bat'));
        shell.cp(path.join(lib_path, 'list-emulator-images.bat'), path.join(project_path, 'cordova', 'lib', 'list-emulator-images.bat'));
        shell.cp(path.join(lib_path, 'list-started-emulators.bat'), path.join(project_path, 'cordova', 'lib', 'list-started-emulators.bat'));
        shell.cp(path.join(lib_path, 'start-emulator.bat'), path.join(project_path, 'cordova', 'lib', 'start-emulator.bat'));
    }

    // copy node related files
    shell.cp(path.join(ROOT, 'bin', 'package.json'), path.join(project_path, 'cordova', 'package.json'));
    shell.cp('-r', path.join(ROOT, 'bin', 'node_modules'), path.join(project_path, 'cordova'));

    /*
     * HELPER FUNCTIONS
     */

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

    function replaceInFile(filename, regex, replacement) {
        write(filename, read(filename).replace(regex, replacement));
    }

    function read(filename) {
        if(fs.existsSync(filename)) {
            if(fs.lstatSync(filename).isFile()) {
                return fs.readFileSync(filename, 'utf-8');
            } else {
                console.error('Uanble to read directory : ' + filename);
                process.exit(1);
            }
        } else {
            console.error('Uanble to read file, not found : ' + filename);
            process.exit(1);
        }
    }

    function write(filename, content) {
        fs.writeFileSync(filename, content, 'utf-8');
    }
}

/**
 * Usage information.
 **/

module.exports.help = function() {
    console.log('Usage: ' + path.relative(process.cwd(), path.join(ROOT, 'bin', 'create')) + ' <path_to_new_project> <package_name> <project_name>');
    console.log('Make sure the Android SDK tools folder is in your PATH!');
    console.log('    <path_to_new_project>: Path to your new Cordova Android project');
    console.log('    <package_name>: Package name, following reverse-domain style convention');
    console.log('    <project_name>: Project name');
    process.exit(0);
}



