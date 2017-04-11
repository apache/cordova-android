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

var Q = require('q');
var fs = require('fs');
var path = require('path');
var shell = require('shelljs');
var events = require('cordova-common').events;
var CordovaError = require('cordova-common').CordovaError;

function GenericBuilder (projectDir) {
    this.root = projectDir || path.resolve(__dirname, '../../..');
    this.binDirs = {
        studio: path.join(this.root, 'app', 'build', 'outputs', 'apk'),
        gradle: path.join(this.root, 'build', 'outputs', 'apk')
    };
}

function hasCustomRules(projectRoot) {
    return fs.existsSync(path.join(projectRoot, 'custom_rules.xml'));
}

GenericBuilder.prototype.prepEnv = function() {
    return Q();
};

GenericBuilder.prototype.build = function() {
    events.emit('log', 'Skipping build...');
    return Q(null);
};

GenericBuilder.prototype.clean = function() {
    return Q();
};

GenericBuilder.prototype.findOutputApks = function(build_type, arch) {
    var self = this;
    return Object.keys(this.binDirs)
    .reduce(function (result, builderName) {
        console.log('builderName:'+ builderName);
        var binDir = self.binDirs[builderName];
        return result.concat(findOutputApksHelper(binDir, build_type, builderName === 'ant' ? null : arch));
    }, [])
    .sort(apkSorter);
};

module.exports = GenericBuilder;

function apkSorter(fileA, fileB) {
    // De-prioritize unsigned builds
    var unsignedRE = /-unsigned/;
    if (unsignedRE.exec(fileA)) {
        return 1;
    } else if (unsignedRE.exec(fileB)) {
        return -1;
    }

    var timeDiff = fs.statSync(fileA).mtime - fs.statSync(fileB).mtime;
    return timeDiff === 0 ? fileA.length - fileB.length : timeDiff;
}

function findOutputApksHelper(dir, build_type, arch) {
    var shellSilent = shell.config.silent;
    shell.config.silent = true;

    var ret = shell.ls(path.join(dir, '*.apk'))
    .filter(function(candidate) {
        var apkName = path.basename(candidate);
        // Need to choose between release and debug .apk.
        if (build_type === 'debug') {
            return /-debug/.exec(apkName) && !/-unaligned|-unsigned/.exec(apkName);
        }
        if (build_type === 'release') {
            return /-release/.exec(apkName) && !/-unaligned/.exec(apkName);
        }
        return true;
    })
    .sort(apkSorter);

    shellSilent = shellSilent;

    if (ret.length === 0) {
        return ret;
    }
    // Assume arch-specific build if newest apk has -x86 or -arm.
    var archSpecific = !!/-x86|-arm/.exec(path.basename(ret[0]));
    // And show only arch-specific ones (or non-arch-specific)
    ret = ret.filter(function(p) {
        /*jshint -W018 */
        return !!/-x86|-arm/.exec(path.basename(p)) == archSpecific;
        /*jshint +W018 */
    });

    if (archSpecific && ret.length > 1 && arch) {
        ret = ret.filter(function(p) {
            return path.basename(p).indexOf('-' + arch) != -1;
        });
    }

    return ret;
}
