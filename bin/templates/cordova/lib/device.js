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

var build = require('./build');
var Adb = require('./Adb');
var CordovaError = require('cordova-common').CordovaError;

/**
 * Returns a promise for the list of the device ID's found
 */
module.exports.list = async () => {
    return (await Adb.devices())
        .filter(id => !id.startsWith('emulator-'));
};

module.exports.resolveTarget = function (target) {
    return this.list().then(function (device_list) {
        if (!device_list || !device_list.length) {
            return Promise.reject(new CordovaError('Failed to deploy to device, no devices found.'));
        }
        // default device
        target = target || device_list[0];

        if (device_list.indexOf(target) < 0) {
            return Promise.reject(new CordovaError('ERROR: Unable to find target \'' + target + '\'.'));
        }

        return build.detectArchitecture(target).then(function (arch) {
            return { target: target, arch: arch, isEmulator: false };
        });
    });
};
