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

### 1.4.1 (Aug 09, 2016)
* Add general purpose `ConfigParser.getAttribute` API
* [CB-11653](https://issues.apache.org/jira/browse/CB-11653) moved `findProjectRoot` from `cordova-lib` to `cordova-common`
* [CB-11636](https://issues.apache.org/jira/browse/CB-11636) Handle attributes with quotes correctly
* [CB-11645](https://issues.apache.org/jira/browse/CB-11645) added check to see if `getEditConfig` exists before trying to use it
* [CB-9825](https://issues.apache.org/jira/browse/CB-9825) framework tag spec parsing

### 1.3.0 (May 12, 2016)
* [CB-11259](https://issues.apache.org/jira/browse/CB-11259): Improving prepare and build logging
* [CB-11194](https://issues.apache.org/jira/browse/CB-11194) Improve cordova load time
* [CB-1117](https://issues.apache.org/jira/browse/CB-1117) Add `FileUpdater` module to `cordova-common`.
* [CB-11131](https://issues.apache.org/jira/browse/CB-11131) Fix `TypeError: message.toUpperCase` is not a function in `CordovaLogger`

### 1.2.0 (Apr 18, 2016)
* [CB-11022](https://issues.apache.org/jira/browse/CB-11022) Save modulesMetadata to both www and platform_www when necessary
* [CB-10833](https://issues.apache.org/jira/browse/CB-10833) Deduplicate common logic for plugin installation/uninstallation
* [CB-10822](https://issues.apache.org/jira/browse/CB-10822) Manage plugins/modules metadata using PlatformJson
* [CB-10940](https://issues.apache.org/jira/browse/CB-10940) Can't add Android platform from path
* [CB-10965](https://issues.apache.org/jira/browse/CB-10965) xml helper allows multiple instances to be merge in config.xml

### 1.1.1 (Mar 18, 2016)
* [CB-10694](https://issues.apache.org/jira/browse/CB-10694) Update test to reflect merging of [CB-9264](https://issues.apache.org/jira/browse/CB-9264) fix
* [CB-10694](https://issues.apache.org/jira/browse/CB-10694) Platform-specific configuration preferences don't override global settings
* [CB-9264](https://issues.apache.org/jira/browse/CB-9264) Duplicate entries in `config.xml`
* [CB-10791](https://issues.apache.org/jira/browse/CB-10791) Add `adjustLoggerLevel` to `cordova-common.CordovaLogger`
* [CB-10662](https://issues.apache.org/jira/browse/CB-10662) Add tests for `ConfigParser.getStaticResources`
* [CB-10622](https://issues.apache.org/jira/browse/CB-10622) fix target attribute being ignored for images in `config.xml`.
* [CB-10583](https://issues.apache.org/jira/browse/CB-10583) Protect plugin preferences from adding extra Array properties.

### 1.1.0 (Feb 16, 2016)
* [CB-10482](https://issues.apache.org/jira/browse/CB-10482) Remove references to windows8 from cordova-lib/cli
* [CB-10430](https://issues.apache.org/jira/browse/CB-10430) Adds forwardEvents method to easily connect two EventEmitters
* [CB-10176](https://issues.apache.org/jira/browse/CB-10176) Adds CordovaLogger class, based on logger module from cordova-cli
* [CB-10052](https://issues.apache.org/jira/browse/CB-10052) Expose child process' io streams via promise progress notification
* [CB-10497](https://issues.apache.org/jira/browse/CB-10497) Prefer .bat over .cmd on windows platform
* [CB-9984](https://issues.apache.org/jira/browse/CB-9984) Bumps plist version and fixes failing cordova-common test

### 1.0.0 (Oct 29, 2015)

* [CB-9890](https://issues.apache.org/jira/browse/CB-9890) Documents cordova-common
* [CB-9598](https://issues.apache.org/jira/browse/CB-9598) Correct cordova-lib -> cordova-common in README
* Pick ConfigParser changes from apache@0c3614e
* [CB-9743](https://issues.apache.org/jira/browse/CB-9743) Removes system frameworks handling from ConfigChanges
* [CB-9598](https://issues.apache.org/jira/browse/CB-9598) Cleans out code which has been moved to `cordova-common`
* Pick ConfigParser changes from apache@ddb027b
* Picking CordovaError changes from apache@a3b1fca
* [CB-9598](https://issues.apache.org/jira/browse/CB-9598) Adds tests and fixtures based on existing cordova-lib ones
* [CB-9598](https://issues.apache.org/jira/browse/CB-9598) Initial implementation for cordova-common

