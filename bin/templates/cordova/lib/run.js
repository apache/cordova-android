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
    device   = require('./device'),
    Q = require('q');

/*
 * Runs the application on a device if available.
 * If not device is found, it will use a started emulator.
 * If no started emulators are found it will attempt to start an avd.
 * If no avds are found it will error out.
 * Returns a promise.
 */
 module.exports.run = function(args) {
    var buildFlags = [];
    var install_target;

    for (var i=2; i<args.length; i++) {
        if (args[i] == '--debug') {
            buildFlags.push('--debug');
        } else if (args[i] == '--release') {
            buildFlags.push('--release');
        } else if (args[i] == '--nobuild') {
            buildFlags.push('--nobuild');
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

    return build.run(buildFlags).then(function(buildResults) {
        if (install_target == '--device') {
            return device.install(null, buildResults);
        } else if (install_target == '--emulator') {
            return emulator.list_started().then(function(started) {
                var p = started && started.length > 0 ? Q() : emulator.start();
                return p.then(function() { return emulator.install(null, buildResults); });
            });
        } else if (install_target) {
            var devices, started_emulators, avds;
            return device.list()
            .then(function(res) {
                devices = res;
                return emulator.list_started();
            }).then(function(res) {
                started_emulators = res;
                return emulator.list_images();
            }).then(function(res) {
                avds = res;
                if (devices.indexOf(install_target) > -1) {
                    return device.install(install_target, buildResults);
                } else if (started_emulators.indexOf(install_target) > -1) {
                    return emulator.install(install_target, buildResults);
                } else {
                    // if target emulator isn't started, then start it.
                    var emulator_ID;
                    for(avd in avds) {
                        if(avds[avd].name == install_target) {
                            return emulator.start(install_target)
                            .then(function() { emulator.install(emulator_ID, buildResults); });
                        }
                    }
                    return Q.reject('Target \'' + install_target + '\' not found, unable to run project');
                }
            });
        } else {
            // no target given, deploy to device if available, otherwise use the emulator.
            return device.list()
            .then(function(device_list) {
                if (device_list.length > 0) {
                    console.log('WARNING : No target specified, deploying to device \'' + device_list[0] + '\'.');
                    return device.install(device_list[0], buildResults);
                } else {
                    return emulator.list_started()
                    .then(function(emulator_list) {
                        if (emulator_list.length > 0) {
                            console.log('WARNING : No target specified, deploying to emulator \'' + emulator_list[0] + '\'.');
                            return emulator.install(emulator_list[0], buildResults);
                        } else {
                            console.log('WARNING : No started emulators found, starting an emulator.');
                            return emulator.best_image()
                            .then(function(best_avd) {
                                if(best_avd) {
                                    return emulator.start(best_avd.name)
                                    .then(function(emulator_ID) {
                                        console.log('WARNING : No target specified, deploying to emulator \'' + emulator_ID + '\'.');
                                        return emulator.install(emulator_ID, buildResults);
                                    });
                                } else {
                                    return emulator.start();
                                }
                            });
                        }
                    });
                }
            });
        }
    });
}

module.exports.help = function(args) {
    console.log('Usage: ' + path.relative(process.cwd(), args[1]) + ' [options]');
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
