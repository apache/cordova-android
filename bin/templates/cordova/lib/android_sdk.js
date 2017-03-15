

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

var Q = require('q'),
    superspawn = require('cordova-common').superspawn;

var suffix_number_regex = /(\d+)$/;
// Used for sorting Android targets, example strings to sort:
//   android-19
//   android-L
//   Google Inc.:Google APIs:20
//   Google Inc.:Glass Development Kit Preview:20
// The idea is to sort based on largest "suffix" number - meaning the bigger
// the number at the end, the more recent the target, the closer to the
// start of the array.
function sort_by_largest_numerical_suffix(a, b) {
    var suffix_a = a.match(suffix_number_regex);
    var suffix_b = b.match(suffix_number_regex);
    if (suffix_a && suffix_b) {
        // If the two targets being compared have suffixes, return less than
        // zero, or greater than zero, based on which suffix is larger.
        return (parseInt(suffix_a[1]) > parseInt(suffix_b[1]) ? -1 : 1);
    } else {
        // If no suffix numbers were detected, leave the order as-is between
        // elements a and b.
        return 0;
    }
}

module.exports.print_newest_available_sdk_target = function() {
    return module.exports.list_targets()
    .then(function(targets) {
        targets.sort(sort_by_largest_numerical_suffix);
        console.log(targets[0]);
    });
};

module.exports.version_string_to_api_level = {
    '4.0': 14,
    '4.0.3': 15,
    '4.1': 16,
    '4.2': 17,
    '4.3': 18,
    '4.4': 19,
    '4.4W': 20,
    '5.0': 21,
    '5.1': 22,
    '6.0': 23,
    '7.0': 24,
    '7.1.1': 25
};

module.exports.list_targets_with_android = function() {
    return superspawn.spawn('android', ['list', 'targets'])
    .then(function(stdout) {
        var target_out = stdout.split('\n');
        var targets = [];
        for (var i = target_out.length - 1; i >= 0; i--) {
            if(target_out[i].match(/id:/)) {
                targets.push(target_out[i].match(/"(.+)"/)[1]);
            }
        }
        return targets;
    });
};

module.exports.list_targets_with_sdkmanager = function() {
    return superspawn.spawn('sdkmanager', ['--list'])
    .then(function(stdout) {
        var parsing_installed_packages = false;
        var lines = stdout.split('\n');
        var targets = [];
        for (var i = 0, l = lines.length; i < l; i++) {
            var line = lines[i];
            if (line.match(/Installed packages/)) {
                parsing_installed_packages = true;
            } else if (line.match(/Available Packages/) || line.match(/Available Updates/)) {
                // we are done working through installed packages, exit
                break;
            }
            if (parsing_installed_packages) {
                // Match stock android platform
                if (line.match(/platforms;android-\d+/)) {
                    targets.push(line.match(/(android-\d+)/)[1]);
                }
                // Match Google APIs
                if (line.match(/addon-google_apis-google-\d+/)) {
                    var description = lines[i + 1];
                    // munge description to match output from old android sdk tooling
                    var api_level = description.match(/Android (\d+)/); //[1];
                    if (api_level) {
                        targets.push('Google Inc.:Google APIs:' + api_level[1]);
                    }
                }
                // TODO: match anything else?
            }
        }
        return targets;
    });
};

module.exports.list_targets = function() {
    return module.exports.list_targets_with_android()
    .catch(function(err) {
        // there's a chance `android` no longer works.
        // lets see if `sdkmanager` is available and we can figure it out
        var avail_regex = /"?android"? command is no longer available/;
        if (err.code && ((err.stdout && err.stdout.match(avail_regex)) || (err.stderr && err.stderr.match(avail_regex)))) {
            return module.exports.list_targets_with_sdkmanager();
        } else throw err;
    }).then(function(targets) {
        if (targets.length === 0) {
            return Q.reject(new Error('No android targets (SDKs) installed!'));
        }
        return targets;
    });
};
