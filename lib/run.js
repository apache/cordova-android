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

const emulator = require('./emulator');
const target = require('./target');
const build = require('./build');
const PackageType = require('./PackageType');
const AndroidManifest = require('./AndroidManifest');
const { CordovaError, events } = require('cordova-common');
const CordovaGradleConfigParserFactory = require('./config/CordovaGradleConfigParserFactory');

/**
 * Builds a target spec from a runOptions object
 *
 * @param {{target?: string, device?: boolean, emulator?: boolean}} runOptions
 * @return {target.TargetSpec}
 */
function buildTargetSpec (runOptions) {
    const spec = {};
    if (runOptions.target) {
        spec.id = runOptions.target;
    } else if (runOptions.device) {
        spec.type = 'device';
    } else if (runOptions.emulator) {
        spec.type = 'emulator';
    }
    return spec;
}

function formatResolvedTarget ({ id, type }) {
    return `${type} ${id}`;
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
module.exports.run = async function (runOptions = {}) {
    const { packageType, buildType } = build.parseBuildOptions(runOptions, null, this.root);

    // Android app bundles cannot be deployed directly to the device
    if (packageType === PackageType.BUNDLE) {
        throw new CordovaError('Package type "bundle" is not supported during cordova run.');
    }

    const buildResults = this._builder.fetchBuildResults(buildType);
    if (buildResults.apkPaths.length === 0) {
        throw new CordovaError('Could not find any APKs to deploy');
    }

    const targetSpec = buildTargetSpec(runOptions);
    const resolvedTarget = await target.resolve(targetSpec, buildResults);
    events.emit('log', `Deploying to ${formatResolvedTarget(resolvedTarget)}`);

    if (resolvedTarget.type === 'emulator') {
        await emulator.wait_for_boot(resolvedTarget.id);
    }

    const manifest = new AndroidManifest(this.locations.manifest);
    const cordovaGradleConfigParser = CordovaGradleConfigParserFactory.create(this.locations.root);

    return target.install(resolvedTarget, { manifest, buildResults, cordovaGradleConfigParser });
};

module.exports.listDevices = async function () {
    events.emit('log', `\nAvailable ${this.platform} devices:`);

    const { list } = require('./target');

    await list().then(targets => {
        const deviceIds = targets
            .filter(({ type }) => type === 'device')
            .map(({ id }) => id);

        console.log(deviceIds.join('\n'));
    }, function (err) {
        console.error('ERROR: ' + err);
        process.exit(2);
    });
};

module.exports.listEmulators = async function () {
    events.emit('log', `\nAvailable ${this.platform} virtual devices:`);
    const emulators = require('./emulator');

    await emulators.list_images().then(function (emulator_list) {
        emulator_list && emulator_list.forEach(function (emu) {
            console.log(emu.name);
        });
    }, function (err) {
        console.error('ERROR: ' + err);
        process.exit(2);
    });
};

module.exports.runListDevices = async function (options = {}) {
    const { options: cliArgs = {} } = options;

    if (cliArgs?.device) {
        await module.exports.listDevices.call(this);
    } else if (cliArgs?.emulator) {
        await module.exports.listEmulators.call(this);
    } else {
        await module.exports.listDevices.call(this);
        await module.exports.listEmulators.call(this);
    }

    return true;
};
