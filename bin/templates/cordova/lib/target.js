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

const path = require('path');
const Adb = require('./Adb');
const build = require('./build');
const AndroidManifest = require('./AndroidManifest');
const { retryPromise } = require('./retry');
const { events } = require('cordova-common');

const INSTALL_COMMAND_TIMEOUT = 5 * 60 * 1000;
const NUM_INSTALL_RETRIES = 3;
const EXEC_KILL_SIGNAL = 'SIGKILL';

exports.install = async function ({ target, arch, isEmulator }, buildResults) {
    const apk_path = build.findBestApkForArchitecture(buildResults, arch);
    const manifest = new AndroidManifest(path.join(__dirname, '../../app/src/main/AndroidManifest.xml'));
    const pkgName = manifest.getPackageId();
    const launchName = pkgName + '/.' + manifest.getActivity().getName();

    events.emit('log', 'Using apk: ' + apk_path);
    events.emit('log', 'Package name: ' + pkgName);
    events.emit('verbose', `Installing app on target ${target}`);

    async function doInstall (execOptions = {}) {
        try {
            await Adb.install(target, apk_path, { replace: true, execOptions });
        } catch (error) {
            // CB-9557 CB-10157 only uninstall and reinstall app if the one that
            // is already installed on device was signed w/different certificate
            if (!/INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES/.test(error.toString())) throw error;

            events.emit('warn', 'Uninstalling app from device and reinstalling it again because the ' +
                'installed app already signed with different key');

            // This promise is always resolved, even if 'adb uninstall' fails to uninstall app
            // or the app doesn't installed at all, so no error catching needed.
            await Adb.uninstall(target, pkgName);
            await Adb.install(target, apk_path, { replace: true });
        }
    }

    if (isEmulator) {
        // Work around sporadic emulator hangs: http://issues.apache.org/jira/browse/CB-9119
        await retryPromise(NUM_INSTALL_RETRIES, () => doInstall({
            timeout: INSTALL_COMMAND_TIMEOUT,
            killSignal: EXEC_KILL_SIGNAL
        }));
    } else {
        await doInstall();
    }
    events.emit('log', 'INSTALL SUCCESS');

    events.emit('verbose', 'Unlocking screen...');
    await Adb.shell(target, 'input keyevent 82');

    await Adb.start(target, launchName);
    events.emit('log', 'LAUNCH SUCCESS');
};
