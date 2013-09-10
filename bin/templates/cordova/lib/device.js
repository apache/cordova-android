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
    build = require('./build'),
    appinfo = require('./appinfo'),
    exec  = require('child_process').exec,
    ROOT = path.join(__dirname, '..', '..');

/**
 * Returns a list of the device ID's found
 */
module.exports.list = function() {
    var cmd = 'adb devices';
    var result = shell.exec(cmd, {silent:true, async:false});
    if (result.code > 0) {
        console.error('Failed to execute android command \'' + cmd + '\'.');
        process.exit(2);
    } else {
        var response = result.output.split('\n');
        var device_list = [];
        for (var i = 1; i < response.length; i++) {
            if (response[i].match(/\w+\tdevice/) && !response[i].match(/emulator/)) {
                device_list.push(response[i].replace(/\tdevice/, '').replace('\r', ''));
            }
        }
        return device_list;
    }
}

/*
 * Installs a previously built application on the device
 * and launches it.
 */
module.exports.install = function(target) {
    var device_list = this.list();
    if (device_list.length > 0) {
        // default device
        target = typeof target !== 'undefined' ? target : device_list[0];
        if (device_list.indexOf(target) > -1) {
            var apk_path = build.get_apk();
            var launchName = appinfo.getActivityName();
            console.log('Installing app on device...');
            cmd = 'adb -s ' + target + ' install -r ' + apk_path;
            var install = shell.exec(cmd, {silent:false, async:false});
            if (install.error || install.output.match(/Failure/)) {
                console.error('ERROR : Failed to install apk to device : ');
                console.error(install.output);
                process.exit(2);
            }

            //unlock screen
            cmd = 'adb -s ' + target + ' shell input keyevent 82';
            shell.exec(cmd, {silent:true, async:false});

            // launch the application
            console.log('Launching application...');
            cmd = 'adb -s ' + target + ' shell am start -W -a android.intent.action.MAIN -n ' + launchName;
            var launch = shell.exec(cmd, {silent:true, async:false});
            if(launch.code > 0) {
                console.error('ERROR : Failed to launch application on emulator : ' + launch.error);
                console.error(launch.output);
                process.exit(2);
            } else {
                console.log('LANCH SUCCESS');
            }
        } else {
            console.error('ERROR : Unable to find target \'' + target + '\'.');
            console.error('Failed to deploy to device.');
            process.exit(2);
        }
    } else {
        console.error('ERROR : Failed to deploy to device, no devices found.');
        process.exit(2);
    }
}
