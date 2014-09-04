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

exports.getArgs = function(argv) {
    var ret = {};
    var posArgs = [];
    for (var i = 2, arg; arg = argv[i] || i < argv.length; ++i) {
        if (/^--/.exec(arg)) {
          // Support for string optional arguments
            var val = /^(.*?)="(.*)"$/.exec(arg);
            if (val) {
                ret[val[1]] = val[2];
            } else {
                ret[arg] = true;
            }
        } else {
            posArgs.push(arg);
        }
    }
    ret._ = posArgs;
    return ret;
};
