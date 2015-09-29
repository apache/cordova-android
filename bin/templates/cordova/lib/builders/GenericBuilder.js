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

function GenericBuilder (eventEmitter) {
    this.events = eventEmitter || new (require('events').EventEmitter)();
}

GenericBuilder.prototype.prepEnv = function() {
    return Q();
};

GenericBuilder.prototype.build = function() {
    this.events.emit('log', 'Skipping build...');
    return Q(null);
};

GenericBuilder.prototype.clean = function() {
    return Q();
};

GenericBuilder.prototype.findOutputApks = function(build_type, arch) {
    var AntBuilder = require('./AntBuilder');
    var GradleBuilder = require('./GradleBuilder');

    return GenericBuilder.sortFilesByDate((new AntBuilder()).findOutputApks(build_type, arch)
        .concat((new GradleBuilder()).findOutputApks(build_type, arch)));
};

module.exports = GenericBuilder;

GenericBuilder.sortFilesByDate = function(files) {
    return files.map(function(p) {
        return { p: p, t: fs.statSync(p).mtime };
    }).sort(function(a, b) {
        var timeDiff = b.t - a.t;
        return timeDiff === 0 ? a.p.length - b.p.length : timeDiff;
    }).map(function(p) { return p.p; });
};

GenericBuilder.findApks = function (directory) {
    var ret = [];
    if (fs.existsSync(directory)) {
        fs.readdirSync(directory).forEach(function(p) {
            if (path.extname(p) == '.apk') {
                ret.push(path.join(directory, p));
            }
        });
    }
    return ret;
};

GenericBuilder.findOutputApksHelper = function(dir, build_type, arch) {
    var ret = GenericBuilder.findApks(dir).filter(function(candidate) {
        var apkName = path.basename(candidate);
        // Need to choose between release and debug .apk.
        if (build_type === 'debug') {
            return /-debug/.exec(apkName) && !/-unaligned|-unsigned/.exec(apkName);
        }
        if (build_type === 'release') {
            return /-release/.exec(apkName) && !/-unaligned/.exec(apkName);
        }
        return true;
    });
    ret = GenericBuilder.sortFilesByDate(ret);
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
    if (archSpecific && ret.length > 1) {
        ret = ret.filter(function(p) {
            return path.basename(p).indexOf('-' + arch) != -1;
        });
    }

    return ret;
};
