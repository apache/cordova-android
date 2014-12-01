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
    os  = require('os'),
    Q     = require('q'),
    child_process = require('child_process'),
    ROOT  = path.join(__dirname, '..', '..');

/*
 * Starts running logcat in the shell.
 * Returns a promise.
 */
module.exports.run = function() {
    var cmd = 'adb logcat | grep -v nativeGetEnabledTags';
    var d = Q.defer();
    var adb = child_process.spawn('adb', ['logcat'], {cwd: os.tmpdir()});

    adb.stdout.on('data', function(data) {
        var lines = data ? data.toString().split('\n') : [];
        var out = lines.filter(function(x) { return x.indexOf('nativeGetEnabledTags') < 0; });
        console.log(out.join('\n'));
    });

    adb.stderr.on('data', console.error);
    adb.on('close', function(code) {
        if (code > 0) {
            d.reject('Failed to run logcat command.');
        } else d.resolve();
    });

    return d.promise;
}

module.exports.help = function() {
    console.log('Usage: ' + path.relative(process.cwd(), path.join(ROOT, 'cordova', 'log')));
    console.log('Gives the logcat output on the command line.');
    process.exit(0);
}
