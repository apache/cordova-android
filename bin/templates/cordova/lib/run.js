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
var emulator = require('./emulator');
var device = require('./device');
var PackageType = require('./PackageType');
const { CordovaError, events } = require('cordova-common');

function getInstallTarget (runOptions) {
    var install_target;
    if (runOptions.target) {
        install_target = runOptions.target;
    } else if (runOptions.device) {
        install_target = '--device';
    } else if (runOptions.emulator) {
        install_target = '--emulator';
    }

    return install_target;
}

async function isEmulatorName (name) {
    const emus = await emulator.list_images();
    return emus.some(avd => avd.name === name);
}

function formatResolvedTarget ({ target, isEmulator }) {
    return `${isEmulator ? 'emulator' : 'device'} ${target}`;
}

async function resolveInstallTarget (install_target) {
    events.emit('verbose', `Trying to resolve install target "${install_target}"`);

    let type;
    if (/^--(device|emulator)$/.test(install_target)) {
        type = RegExp.$1;
        install_target = undefined;
    }

    if (type !== 'emulator') {
        events.emit('verbose', 'Trying to resolve target to a connected device');
        try {
            return await device.resolveTarget(install_target);
        } catch (error) {
            if (!error.message.match(
                /Unable to find target|no devices found/
            )) throw error;
        }
    }

    if (type !== 'device') {
        events.emit('verbose', 'Trying to resolve target to a started emulator');
        try {
            return await emulator.resolveTarget(install_target);
        } catch (error) {
            if (!error.message.match(
                /Unable to find target|No running Android emulators/
            )) throw error;
        }

        // Last chance: try to start an emulator with ID install_target
        // if install_target is undefined, pick best match regarding target API
        if (!install_target || await isEmulatorName(install_target)) {
            const emulatorId = await emulator.start(install_target);
            return emulator.resolveTarget(emulatorId);
        }
    }

    throw new CordovaError(`Target '${install_target}' not found, unable to run project`);
}

/**
 * Runs the application on a device if available. If no device is found, it will
 *   use a started emulator. If no started emulators are found it will attempt
 *   to start an avd. If no avds are found it will error out.
 *
 * @param   {Object}  runOptions  various run/build options. See Api.js build/run
 *   methods for reference.
 *
 * @return  {Promise}
 */
module.exports.run = function (runOptions) {
    runOptions = runOptions || {};

    var self = this;
    var install_target = getInstallTarget(runOptions);

    return resolveInstallTarget(install_target).then(function (resolvedTarget) {
        events.emit('log', `Deploying to ${formatResolvedTarget(resolvedTarget)}`);

        return new Promise((resolve) => {
            const buildOptions = require('./build').parseBuildOptions(runOptions, null, self.root);

            // Android app bundles cannot be deployed directly to the device
            if (buildOptions.packageType === PackageType.BUNDLE) {
                const packageTypeErrorMessage = 'Package type "bundle" is not supported during cordova run.';
                events.emit('error', packageTypeErrorMessage);
                throw packageTypeErrorMessage;
            }

            resolve(self._builder.fetchBuildResults(buildOptions.buildType, buildOptions.arch));
        }).then(function (buildResults) {
            if (resolvedTarget && resolvedTarget.isEmulator) {
                return emulator.wait_for_boot(resolvedTarget.target).then(function () {
                    return emulator.install(resolvedTarget, buildResults);
                });
            }

            return device.install(resolvedTarget, buildResults);
        });
    });
};

module.exports.help = function () {
    console.log('Usage: ' + path.relative(process.cwd(), process.argv[1]) + ' [options]');
    console.log('Build options :');
    console.log('    --debug : Builds project in debug mode');
    console.log('    --release : Builds project in release mode');
    console.log('    --nobuild : Runs the currently built project without recompiling');
    console.log('Deploy options :');
    console.log('    --device : Will deploy the built project to a device');
    console.log('    --emulator : Will deploy the built project to an emulator if one exists');
    console.log('    --target=<target_id> : Installs to the target with the specified id.');
    process.exit(0);
};
