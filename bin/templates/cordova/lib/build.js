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

var shell   = require('shelljs'),
    exec    = require('./exec'),
    Q       = require('q'),
    clean   = require('./clean'),
    path    = require('path'),
    fs      = require('fs'),
    ROOT    = path.join(__dirname, '..', '..');

/*
 * Builds the project with ant.
 * Returns a promise.
 */
module.exports.run = function(build_type) {
    //default build type
    build_type = typeof build_type !== 'undefined' ? build_type : "--debug";
    var cmd;
    switch(build_type) {
        case '--debug' :
            cmd = 'ant debug -f "' + path.join(ROOT, 'build.xml') + '"';
            break;
        case '--release' :
            cmd = 'ant release -f "' + path.join(ROOT, 'build.xml') + '"';
            break;
        case '--nobuild' :
            console.log('Skipping build...');
            return Q();
        default :
            return Q.reject('Build option \'' + build_type + '\' not recognized.');
    }
    if(cmd) {
        return clean.run() // TODO: Can we stop cleaning every time and let ant build incrementally?
        .then(function() {
            return exec(cmd);
        });
    }
    return Q();
}

/*
 * Gets the path to the apk file, if not such file exists then
 * the script will error out. (should we error or just return undefined?)
 */
module.exports.get_apk = function() {
    if(fs.existsSync(path.join(ROOT, 'bin'))) {
        var bin_files = fs.readdirSync(path.join(ROOT, 'bin'));
        for (file in bin_files) {
            if(path.extname(bin_files[file]) == '.apk') {
                return path.join(ROOT, 'bin', bin_files[file]);
            }
        }
        console.error('ERROR : No .apk found in \'bin\' folder');
        process.exit(2);
    } else {
        console.error('ERROR : unable to find project bin folder, could not locate .apk');
        process.exit(2);
    }
}

module.exports.help = function() {
    console.log('Usage: ' + path.relative(process.cwd(), path.join(ROOT, 'corodva', 'build')) + ' [build_type]');
    console.log('Build Types : ');
    console.log('    \'--debug\': Default build, will build project in using ant debug');
    console.log('    \'--release\': will build project using ant release');
    console.log('    \'--nobuild\': will skip build process (can be used with run command)');
    process.exit(0);
}
