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
    fs    = require('fs'),
    path  = require('path'),
    ROOT = path.join(__dirname, '..', '..');

/*
 * Displays the version, gotten from cordova.js
 */
module.exports.run = function() {
    var cordovajs_path = path.join(ROOT, 'assets', 'www', 'cordova.js');
    if (fs.existsSync(cordovajs_path)) {
        var version_line = shell.grep(/^.*CORDOVA_JS_BUILD_LABEL.*$/, cordovajs_path);
        var version = version_line.match(/(\d+)\.(\d+)\.(\d+)(rc\d)?/)[0];
        if (version) {
            console.log(version);
            return version;
        } else {
            console.error("ERROR : Unable to find version in cordova.js");
            process.exit(2);
        }
    } else {
        console.error("ERROR : Could not find cordova.js");
        console.error('Expected Location : ' + cordovajs_path);
        process.exit(2);
    }
}

module.exports.help = function() {
    console.log('Usage: ' + path.relative(process.cwd(), path.join(ROOT, 'corodva', 'version')));
    console.log('Returns the version of Cordova.');
    process.exit(0);
}