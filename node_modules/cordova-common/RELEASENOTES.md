<!--
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
-->
# Cordova-common Release Notes

### 1.1.0 (Feb 16, 2016)
* CB-10482 Remove references to windows8 from cordova-lib/cli
* CB-10430 Adds forwardEvents method to easily connect two EventEmitters
* CB-10176 Adds CordovaLogger class, based on logger module from cordova-cli
* CB-10052 Expose child process' io streams via promise progress notification
* CB-10497 Prefer .bat over .cmd on windows platform
* CB-9984 Bumps plist version and fixes failing cordova-common test

### 1.0.0 (Oct 29, 2015)

* CB-9890 Documents cordova-common
* CB-9598 Correct cordova-lib -> cordova-common in README
* Pick ConfigParser changes from apache@0c3614e
* CB-9743 Removes system frameworks handling from ConfigChanges
* CB-9598 Cleans out code which has been moved to `cordova-common`
* Pick ConfigParser changes from apache@ddb027b
* Picking CordovaError changes from apache@a3b1fca
* CB-9598 Adds tests and fixtures based on existing cordova-lib ones
* CB-9598 Initial implementation for cordova-common

