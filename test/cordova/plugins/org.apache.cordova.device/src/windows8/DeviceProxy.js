/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/


var cordova = require('cordova');
var utils = require('cordova/utils');

module.exports = {

    getDeviceInfo:function(win,fail,args) {

        // deviceId aka uuid, stored in Windows.Storage.ApplicationData.current.localSettings.values.deviceId
        var deviceId;

        var localSettings = Windows.Storage.ApplicationData.current.localSettings;

        if (localSettings.values.deviceId) {
            deviceId = localSettings.values.deviceId;
        }
        else {
            deviceId = localSettings.values.deviceId = utils.createUUID();
        }

        setTimeout(function () {
            win({ platform: "windows8", version: "8", uuid: deviceId, cordova: '0.0.0', model: window.clientInformation.platform });
        }, 0);
    }

};

require("cordova/windows8/commandProxy").add("Device", module.exports);

