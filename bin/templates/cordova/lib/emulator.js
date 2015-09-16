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

/* jshint sub:true */

var exec       = require('./exec');
var appinfo    = require('./appinfo');
var retry      = require('./retry');
var build      = require('./build');
var check_reqs = require('./check_reqs');

var Q             = require('q');
var os            = require('os');
var child_process = require('child_process');

// constants
var ONE_SECOND              = 1000; // in milliseconds
var ONE_MINUTE              = 60 * ONE_SECOND; // in milliseconds
var INSTALL_COMMAND_TIMEOUT = 5 * ONE_MINUTE; // in milliseconds
var NUM_INSTALL_RETRIES     = 3;
var EXEC_KILL_SIGNAL        = 'SIGKILL';

/**
 * Returns a Promise for a list of emulator images in the form of objects
 * {
       name   : <emulator_name>,
       path   : <path_to_emulator_image>,
       target : <api_target>,
       abi    : <cpu>,
       skin   : <skin>
   }
 */
module.exports.list_images = function() {
    return exec('android list avds')
    .then(function(output) {
        var response = output.split('\n');
        var emulator_list = [];
        for (var i = 1; i < response.length; i++) {
            // To return more detailed information use img_obj
            var img_obj = {};
            if (response[i].match(/Name:\s/)) {
                img_obj['name'] = response[i].split('Name: ')[1].replace('\r', '');
                if (response[i + 1].match(/Path:\s/)) {
                    i++;
                    img_obj['path'] = response[i].split('Path: ')[1].replace('\r', '');
                }
                if (response[i + 1].match(/\(API\slevel\s/)) {
                    i++;
                    img_obj['target'] = response[i].replace('\r', '');
                }
                if (response[i + 1].match(/ABI:\s/)) {
                    i++;
                    img_obj['abi'] = response[i].split('ABI: ')[1].replace('\r', '');
                }
                if (response[i + 1].match(/Skin:\s/)) {
                    i++;
                    img_obj['skin'] = response[i].split('Skin: ')[1].replace('\r', '');
                }

                emulator_list.push(img_obj);
            }
            /* To just return a list of names use this
            if (response[i].match(/Name:\s/)) {
                emulator_list.push(response[i].split('Name: ')[1].replace('\r', '');
            }*/

        }
        return emulator_list;
    });
};

/**
 * Will return the closest avd to the projects target
 * or undefined if no avds exist.
 * Returns a promise.
 */
module.exports.best_image = function() {
    var project_target = check_reqs.get_target().replace('android-', '');
    return this.list_images()
    .then(function(images) {
        var closest = 9999;
        var best = images[0];
        for (var i in images) {
            var target = images[i].target;
            if(target) {
                var num = target.split('(API level ')[1].replace(')', '');
                if (num == project_target) {
                    return images[i];
                } else if (project_target - num < closest && project_target > num) {
                    closest = project_target - num;
                    best = images[i];
                }
            }
        }
        return best;
    });
};

// Returns a promise.
module.exports.list_started = function() {
    return exec('adb devices', os.tmpdir())
    .then(function(output) {
        var response = output.split('\n');
        var started_emulator_list = [];
        for (var i = 1; i < response.length; i++) {
            if (response[i].match(/device/) && response[i].match(/emulator/)) {
                started_emulator_list.push(response[i].replace(/\tdevice/, '').replace('\r', ''));
            }
        }
        return started_emulator_list;
    });
};

// Returns a promise.
module.exports.list_targets = function() {
    return exec('android list targets', os.tmpdir())
    .then(function(output) {
        var target_out = output.split('\n');
        var targets = [];
        for (var i = target_out.length; i >= 0; i--) {
            if(target_out[i].match(/id:/)) {
                targets.push(targets[i].split(' ')[1]);
            }
        }
        return targets;
    });
};

/*
 * Starts an emulator with the given ID,
 * and returns the started ID of that emulator.
 * If no ID is given it will use the first image available,
 * if no image is available it will error out (maybe create one?).
 *
 * Returns a promise.
 */
module.exports.start = function(emulator_ID) {
    var self = this;
    var now = new Date();
    var uuid = 'cordova_emulator_' + now.getTime();
    var emulator_id;

    return Q().then(function(list) {
        if (!emulator_ID) {
            return self.list_images()
            .then(function(emulator_list) {
                if (emulator_list.length > 0) {
                    return self.best_image()
                    .then(function(best) {
                        emulator_ID = best.name;
                        console.log('WARNING : no emulator specified, defaulting to ' + emulator_ID);
                        return emulator_ID;
                    });
                } else {
                    var androidCmd = check_reqs.getAbsoluteAndroidCmd();
                    return Q.reject('ERROR : No emulator images (avds) found.\n' +
                        '1. Download desired System Image by running: ' + androidCmd + ' sdk\n' +
                        '2. Create an AVD by running: ' + androidCmd + ' avd\n' +
                        'HINT: For a faster emulator, use an Intel System Image and install the HAXM device driver\n');
                }
            });
        } else {
            return Q(emulator_ID);
        }
    }).then(function() {
        var cmd = 'emulator';
        var uuidProp = 'emu.uuid=' + uuid;
        var args = ['-avd', emulator_ID, '-prop', uuidProp];
        var proc = child_process.spawn(cmd, args, { stdio: 'inherit', detached: true });
        proc.unref(); // Don't wait for it to finish, since the emulator will probably keep running for a long time.
    }).then(function() {
        // wait for emulator to start
        console.log('Waiting for emulator...');
        return self.wait_for_emulator(uuid);
    }).then(function(emId) {
        emulator_id = emId;
        if (!emulator_id) return Q.reject('ERROR :  Failed to start emulator');

        //wait for emulator to boot up
        process.stdout.write('Booting up emulator (this may take a while)...');
        return self.wait_for_boot(emulator_id);
    }).then(function() {
        console.log('BOOT COMPLETE');

        //unlock screen
        return exec('adb -s ' + emulator_id + ' shell input keyevent 82', os.tmpdir());
    }).then(function() {
        //return the new emulator id for the started emulators
        return emulator_id;
    });
};

/*
 * Waits for an emulator with given uuid to apear on the started-emulator list.
 * Returns a promise with this emulator's ID.
 */
module.exports.wait_for_emulator = function(uuid) {
    var self = this;
    return self.list_started()
    .then(function(new_started) {
        var emulator_id = null;
        var promises = [];

        new_started.forEach(function (emulator) {
            promises.push(exec('adb -s ' + emulator + ' shell getprop emu.uuid', os.tmpdir())
                .then(function (output) {
                    if (output.indexOf(uuid) >= 0) {
                        emulator_id = emulator;
                    }
                })
            );
        });

        return Q.all(promises).then(function () {
            return emulator_id || self.wait_for_emulator(uuid);
        });
     });
};

/*
 * Waits for the core android process of the emulator to start
 */
module.exports.wait_for_boot = function(emulator_id) {
    var self = this;
    return exec('adb -s ' + emulator_id + ' shell ps', os.tmpdir())
    .then(function(output) {
        if (output.match(/android\.process\.acore/)) {
            return;
        } else {
            process.stdout.write('.');
            return Q.delay(3000).then(function() {
                return self.wait_for_boot(emulator_id);
            });
        }
    });
};

/*
 * Create avd
 * TODO : Enter the stdin input required to complete the creation of an avd.
 * Returns a promise.
 */
module.exports.create_image = function(name, target) {
    console.log('Creating avd named ' + name);
    if (target) {
        return exec('android create avd --name ' + name + ' --target ' + target)
        .then(null, function(error) {
            console.error('ERROR : Failed to create emulator image : ');
            console.error(' Do you have the latest android targets including ' + target + '?');
            console.error(error);
        });
    } else {
        console.log('WARNING : Project target not found, creating avd with a different target but the project may fail to install.');
        return exec('android create avd --name ' + name + ' --target ' + this.list_targets()[0])
        .then(function() {
            // TODO: This seems like another error case, even though it always happens.
            console.error('ERROR : Unable to create an avd emulator, no targets found.');
            console.error('Please insure you have targets available by running the "android" command');
            return Q.reject();
        }, function(error) {
            console.error('ERROR : Failed to create emulator image : ');
            console.error(error);
        });
    }
};

module.exports.resolveTarget = function(target) {
    return this.list_started()
    .then(function(emulator_list) {
        if (emulator_list.length < 1) {
            return Q.reject('No started emulators found, please start an emultor before deploying your project.');
        }

        // default emulator
        target = target || emulator_list[0];
        if (emulator_list.indexOf(target) < 0) {
            return Q.reject('Unable to find target \'' + target + '\'. Failed to deploy to emulator.');
        }

        return build.detectArchitecture(target)
        .then(function(arch) {
            return {target:target, arch:arch, isEmulator:true};
        });
    });
};

/*
 * Installs a previously built application on the emulator and launches it.
 * If no target is specified, then it picks one.
 * If no started emulators are found, error out.
 * Returns a promise.
 */
module.exports.install = function(givenTarget, buildResults) {

    var target;

    // resolve the target emulator
    return Q().then(function () {
        if (givenTarget && typeof givenTarget == 'object') {
            return givenTarget;
        } else {
            return module.exports.resolveTarget(givenTarget);
        }

    // set the resolved target
    }).then(function (resolvedTarget) {
        target = resolvedTarget;

    // install the app
    }).then(function () {
        var pkgName = appinfo.getPackageName();
        console.log('Uninstalling ' + pkgName + ' from emulator...');
        // This promise is always resolved, even if 'adb uninstall' fails to uninstall app
        // or the app doesn't installed at all, so no error catching needed.
        return exec('adb -s ' + target.target + ' uninstall ' + pkgName, os.tmpdir())
        .then(function() {

            var apk_path = build.findBestApkForArchitecture(buildResults, target.arch);
            var execOptions = {
                timeout:    INSTALL_COMMAND_TIMEOUT, // in milliseconds
                killSignal: EXEC_KILL_SIGNAL
            };

            console.log('Installing app on emulator...');
            console.log('Using apk: ' + apk_path);

            var retriedInstall = retry.retryPromise(
                NUM_INSTALL_RETRIES,
                exec, 'adb -s ' + target.target + ' install -r -d "' + apk_path + '"', os.tmpdir(), execOptions
            );

            return retriedInstall.then(function (output) {
                if (output.match(/Failure/)) {
                    return Q.reject('Failed to install apk to emulator: ' + output);
                } else {
                    console.log('INSTALL SUCCESS');
                }
            }, function (err) {
                return Q.reject('Failed to install apk to emulator: ' + err);
            });
        });
    // unlock screen
    }).then(function () {

        console.log('Unlocking screen...');
        return exec('adb -s ' + target.target + ' shell input keyevent 82', os.tmpdir());

    // launch the application
    }).then(function () {

        console.log('Launching application...');
        var launchName = appinfo.getActivityName();
        var cmd = 'adb -s ' + target.target + ' shell am start -W -a android.intent.action.MAIN -n ' + launchName;
        return exec(cmd, os.tmpdir());

    // report success or failure
    }).then(function (output) {
        console.log('LAUNCH SUCCESS');
    }, function (err) {
        return Q.reject('Failed to launch app on emulator: ' + err);
    });
};
