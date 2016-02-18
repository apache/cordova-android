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

var retry      = require('./retry');
var build      = require('./build');
var path = require('path');
var Adb = require('./Adb');
var AndroidManifest = require('./AndroidManifest');
var events = require('cordova-common').events;
var spawn = require('cordova-common').superspawn.spawn;
var CordovaError = require('cordova-common').CordovaError;

var Q             = require('q');
var os            = require('os');
var child_process = require('child_process');

// constants
var ONE_SECOND              = 1000; // in milliseconds
var ONE_MINUTE              = 60 * ONE_SECOND; // in milliseconds
var INSTALL_COMMAND_TIMEOUT = 5 * ONE_MINUTE; // in milliseconds
var NUM_INSTALL_RETRIES     = 3;
var CHECK_BOOTED_INTERVAL   = 3 * ONE_SECOND; // in milliseconds
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
    return spawn('android', ['list', 'avds'])
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
    return this.list_images()
    .then(function(images) {
        // Just return undefined if there is no images
        if (images.length === 0) return;

        var closest = 9999;
        var best = images[0];
        // Loading check_reqs at run-time to avoid test-time vs run-time directory structure difference issue
        var project_target = require('./check_reqs').get_target().replace('android-', '');
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
    return Adb.devices({emulators: true});
};

// Returns a promise.
module.exports.list_targets = function() {
    return spawn('android', ['list', 'targets'], {cwd: os.tmpdir()})
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
 * If no boot timeout is given or the value is negative it will wait forever for
 * the emulator to boot
 *
 * Returns a promise.
 */
module.exports.start = function(emulator_ID, boot_timeout) {
    var self = this;

    return Q().then(function() {
        if (emulator_ID) return Q(emulator_ID);

        return self.best_image()
        .then(function(best) {
            if (best && best.name) {
                events.emit('warn', 'No emulator specified, defaulting to ' + best.name);
                return best.name;
            }

            // Loading check_reqs at run-time to avoid test-time vs run-time directory structure difference issue
            var androidCmd = require('./check_reqs').getAbsoluteAndroidCmd();
            return Q.reject(new CordovaError('No emulator images (avds) found.\n' +
                '1. Download desired System Image by running: ' + androidCmd + ' sdk\n' +
                '2. Create an AVD by running: ' + androidCmd + ' avd\n' +
                'HINT: For a faster emulator, use an Intel System Image and install the HAXM device driver\n'));
        });
    }).then(function(emulatorId) {
        var uuid = 'cordova_emulator_' + new Date().getTime();
        var uuidProp = 'emu.uuid=' + uuid;
        var args = ['-avd', emulatorId, '-prop', uuidProp];
        // Don't wait for it to finish, since the emulator will probably keep running for a long time.
        child_process
            .spawn('emulator', args, { stdio: 'inherit', detached: true })
            .unref();

        // wait for emulator to start
        events.emit('log', 'Waiting for emulator...');
        return self.wait_for_emulator(uuid);
    }).then(function(emulatorId) {
        if (!emulatorId)
            return Q.reject(new CordovaError('Failed to start emulator'));

        //wait for emulator to boot up
        process.stdout.write('Booting up emulator (this may take a while)...');
        return self.wait_for_boot(emulatorId, boot_timeout)
        .then(function(success) {
            if (success) {
                events.emit('log','BOOT COMPLETE');
                //unlock screen
                return Adb.shell(emulatorId, 'input keyevent 82')
                .then(function() {
                    //return the new emulator id for the started emulators
                    return emulatorId;
                });
            } else {
                // We timed out waiting for the boot to happen
                return null;
            }
        });
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
            promises.push(
                Adb.shell(emulator, 'getprop emu.uuid')
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
 * Waits for the core android process of the emulator to start. Returns a
 * promise that resolves to a boolean indicating success. Not specifying a
 * time_remaining or passing a negative value will cause it to wait forever
 */
module.exports.wait_for_boot = function(emulator_id, time_remaining) {
    var self = this;
    return Adb.shell(emulator_id, 'ps')
    .then(function(output) {
        if (output.match(/android\.process\.acore/)) {
            return true;
        } else if (time_remaining === 0) {
            return false;
        } else {
            process.stdout.write('.');

            // Check at regular intervals
            return Q.delay(time_remaining < CHECK_BOOTED_INTERVAL ? time_remaining : CHECK_BOOTED_INTERVAL).then(function() {
                var updated_time = time_remaining >= 0 ? Math.max(time_remaining - CHECK_BOOTED_INTERVAL, 0) : time_remaining;
                return self.wait_for_boot(emulator_id, updated_time);
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
        return spawn('android', ['create', 'avd', '--name', name, '--target', target])
        .then(null, function(error) {
            console.error('ERROR : Failed to create emulator image : ');
            console.error(' Do you have the latest android targets including ' + target + '?');
            console.error(error);
        });
    } else {
        console.log('WARNING : Project target not found, creating avd with a different target but the project may fail to install.');
        return spawn('android', ['create', 'avd', '--name', name, '--target', this.list_targets()[0]])
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
    var manifest = new AndroidManifest(path.join(__dirname, '../../AndroidManifest.xml'));
    var pkgName = manifest.getPackageId();

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
        // This promise is always resolved, even if 'adb uninstall' fails to uninstall app
        // or the app doesn't installed at all, so no error catching needed.
        return Q.when()
        .then(function() {

            var apk_path = build.findBestApkForArchitecture(buildResults, target.arch);
            var execOptions = {
                cwd: os.tmpdir(),
                timeout:    INSTALL_COMMAND_TIMEOUT, // in milliseconds
                killSignal: EXEC_KILL_SIGNAL
            };

            events.emit('log', 'Using apk: ' + apk_path);
            events.emit('verbose', 'Installing app on emulator...');

            // A special function to call adb install in specific environment w/ specific options.
            // Introduced as a part of fix for http://issues.apache.org/jira/browse/CB-9119
            // to workaround sporadic emulator hangs
            function adbInstallWithOptions(target, apk, opts) {
                events.emit('verbose', 'Installing apk ' + apk + ' on ' + target + '...');

                var command = 'adb -s ' + target + ' install -r "' + apk + '"';
                return Q.promise(function (resolve, reject) {
                    child_process.exec(command, opts, function(err, stdout, stderr) {
                        if (err) reject(new CordovaError('Error executing "' + command + '": ' + stderr));
                        // adb does not return an error code even if installation fails. Instead it puts a specific
                        // message to stdout, so we have to use RegExp matching to detect installation failure.
                        else if (/Failure/.test(stdout)) reject(new CordovaError('Failed to install apk to emulator: ' + stdout));
                        else resolve(stdout);
                    });
                });
            }

            function installPromise () {
                return adbInstallWithOptions(target.target, apk_path, execOptions)
                .catch(function (error) {
                    // CB-9557 CB-10157 only uninstall and reinstall app if the one that
                    // is already installed on device was signed w/different certificate
                    if (!/INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES/.test(error.toString()))
                        throw error;

                    events.emit('warn', 'Uninstalling app from device and reinstalling it again because the ' +
                        'installed app already signed with different key');

                    // This promise is always resolved, even if 'adb uninstall' fails to uninstall app
                    // or the app doesn't installed at all, so no error catching needed.
                    return Adb.uninstall(target.target, pkgName)
                    .then(function() {
                        return adbInstallWithOptions(target.target, apk_path, execOptions);
                    });
                });
            }

            return retry.retryPromise(NUM_INSTALL_RETRIES, installPromise)
            .then(function (output) {
                events.emit('log', 'INSTALL SUCCESS');
            });
        });
    // unlock screen
    }).then(function () {

        events.emit('verbose', 'Unlocking screen...');
        return Adb.shell(target.target, 'input keyevent 82');
    }).then(function () {
        Adb.start(target.target, pkgName + '/.' + manifest.getActivity().getName());
    // report success or failure
    }).then(function (output) {
        events.emit('log', 'LAUNCH SUCCESS');
    });
};
