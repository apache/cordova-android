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

var path  = require('path'),
    build = require('./build'),
    emulator = require('./emulator'),
    device   = require('./device');

/*
 * Runs the application on a device if availible.
 * If not device is found, it will use a started emulator.
 * If no started emulators are found it will attempt to start an avd.
 * If no avds are found it will error out.
 */
 module.exports.run = function(args) {
    var build_type;
    var install_target;

    for (var i=2; i<args.length; i++) {
        if (args[i] == '--debug') {
            build_type = '--debug';
        } else if (args[i] == '--release') {
            build_type = '--release';
        } else if (args[i] == '--nobuild') {
            build_type = '--nobuild';
        } else if (args[i] == '--device') {
            install_target = '--device';
        } else if (args[i] == '--emulator') {
            install_target = '--emulator';
        } else if (args[i].substring(0, 9) == '--target=') {
            install_target = args[i].substring(9, args[i].length);
        } else {
            console.error('ERROR : Run option \'' + args[i] + '\' not recognized.');
            process.exit(2);
        }
    }
    build.run(build_type);
    if (install_target == '--device') {
        device.install();
    } else if (install_target == '--emulator') {
        if (emulator.list_started() == 0) {
            emulator.start();
        }
        emulator.install();
    } else if (install_target) {
        var devices = device.list();
        var started_emulators = emulator.list_started();
        var avds = emulator.list_images();
        if (devices.indexOf(install_target) > -1) {
            device.install(install_target);
        } else if (started_emulators.indexOf(install_target) > -1) {
            emulator.install(install_target);
        } else {
            // if target emulator isn't started, then start it.
            var emulator_ID;
            for(avd in avds) {
                if(avds[avd].name == install_target) {
                    emulator_ID = emulator.start(install_target);
                    emulator.install(emulator_ID);
                    break;
                }
            }
            if(!emulator_ID) {
                console.error('ERROR : Target \'' + install_target + '\' not found, unalbe to run project');
                process.exit(2);
            }
        }
    } else {
        // no target given, deploy to device if availible, otherwise use the emulator.
        var device_list = device.list();
        if (device_list.length > 0) {
            console.log('WARNING : No target specified, deploying to device \'' + device_list[0] + '\'.');
            device.install(device_list[0])
        } else {
            var emulator_list = emulator.list_started();
            if (emulator_list.length > 0) {
                console.log('WARNING : No target specified, deploying to emulator \'' + emulator_list[0] + '\'.');
                emulator.install(emulator_list[0]);
            } else {
                console.log('WARNING : No started emulators found, starting an emulator.');
                var best_avd = emulator.best_image();
                if(best_avd) {
                    var emulator_ID = emulator.start(best_avd.name);
                    console.log('WARNING : No target specified, deploying to emulator \'' + emulator_ID + '\'.');
                    emulator.install(emulator_ID);
                } else {
                    emulator.start();
                }
            }
        }
    }
}

module.exports.help = function() {
    console.log('Usage: ' + path.relative(process.cwd(), args[0]) + ' [options]');
    console.log('Build options :');
    console.log('    --debug : Builds project in debug mode');
    console.log('    --release : Builds project in release mode');
    console.log('    --nobuild : Runs the currently built project without recompiling');
    console.log('Deploy options :');
    console.log('    --device : Will deploy the built project to a device');
    console.log('    --emulator : Will deploy the built project to an emulator if one exists');
    console.log('    --target=<target_id> : Installs to the target with the specified id.');
    process.exit(0);
}