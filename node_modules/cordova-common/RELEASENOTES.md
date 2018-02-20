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

### 2.2.1 (Dec 14, 2017)
* [CB-13674](https://issues.apache.org/jira/browse/CB-13674): updated dependencies

### 2.2.0 (Nov 22, 2017)
* [CB-13471](https://issues.apache.org/jira/browse/CB-13471) File Provider fix belongs in cordova-common 
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Spot fix for upcoming `cordova-android@7` changes. https://github.com/apache/cordova-android/pull/389

### 2.1.1 (Oct 04, 2017)
* [CB-13145](https://issues.apache.org/jira/browse/CB-13145) added `getFrameworks` to unit tests
* [CB-13145](https://issues.apache.org/jira/browse/CB-13145) added variable replacing to framework tag

### 2.1.0 (August 30, 2017)
* [CB-13145](https://issues.apache.org/jira/browse/CB-13145) added variable replacing to `framework` tag
* [CB-13211](https://issues.apache.org/jira/browse/CB-13211) Add `allows-arbitrary-loads-for-media` attribute parsing for `getAccesses`
* [CB-11968](https://issues.apache.org/jira/browse/CB-11968) Added support for `<config-file>` in `config.xml`
* [CB-12895](https://issues.apache.org/jira/browse/CB-12895) set up `eslint` and removed `jshint`
* [CB-12785](https://issues.apache.org/jira/browse/CB-12785) added `.gitignore`, `travis`, and `appveyor` support
* [CB-12250](https://issues.apache.org/jira/browse/CB-12250) & [CB-12409](https://issues.apache.org/jira/browse/CB-12409) *iOS*: Fix bug with escaping properties from `plist` file
* [CB-12762](https://issues.apache.org/jira/browse/CB-12762) updated `common`, `fetch`, and `serve` `pkgJson` to point `pkgJson` repo items to github mirrors
* [CB-12766](https://issues.apache.org/jira/browse/CB-12766) Consistently write `JSON` with 2 spaces indentation

### 2.0.3 (May 02, 2017)
* [CB-8978](https://issues.apache.org/jira/browse/CB-8978) Add option to get `resource-file` from `root`
* [CB-11908](https://issues.apache.org/jira/browse/CB-11908) Add tests for `edit-config` in `config.xml`
* [CB-12665](https://issues.apache.org/jira/browse/CB-12665) removed `enginestrict` since it is deprecated

### 2.0.2 (Apr 14, 2017)
* [CB-11233](https://issues.apache.org/jira/browse/CB-11233) - Support installing frameworks into 'Embedded Binaries' section of the Xcode project
* [CB-10438](https://issues.apache.org/jira/browse/CB-10438) - Install correct dependency version. Removed shell.remove, added pkg.json to dependency tests 1-3, and updated install.js (.replace) to fix tests in uninstall.spec.js and update to workw with jasmine 2.0
* [CB-11120](https://issues.apache.org/jira/browse/CB-11120) - Allow short/display name in config.xml
* [CB-11346](https://issues.apache.org/jira/browse/CB-11346) - Remove known platforms check
* [CB-11977](https://issues.apache.org/jira/browse/CB-11977) - updated engines and enginescript for common, fetch, and serve

### 2.0.1 (Mar 09, 2017)
* [CB-12557](https://issues.apache.org/jira/browse/CB-12557) add both stdout and stderr properties to the error object passed to superspawn reject handler.

### 2.0.0 (Jan 17, 2017)
* [CB-8978](https://issues.apache.org/jira/browse/CB-8978) Add `resource-file` parsing to `config.xml`
* [CB-12018](https://issues.apache.org/jira/browse/CB-12018): updated `jshint` and updated tests to work with `jasmine@2` instead of `jasmine-node`
* [CB-12163](https://issues.apache.org/jira/browse/CB-12163) Add reference attrib to `resource-file` for **Windows**
* Move windows-specific logic to `cordova-windows`
* [CB-12189](https://issues.apache.org/jira/browse/CB-12189) Add implementation attribute to framework

### 1.5.1 (Oct 12, 2016)
* [CB-12002](https://issues.apache.org/jira/browse/CB-12002) Add `getAllowIntents()` to `ConfigParser`
* [CB-11998](https://issues.apache.org/jira/browse/CB-11998) `cordova platform add` error with `cordova-common@1.5.0`

### 1.5.0 (Oct 06, 2016)
* [CB-11776](https://issues.apache.org/jira/browse/CB-11776) Add test case for different `edit-config` targets
* [CB-11908](https://issues.apache.org/jira/browse/CB-11908) Add `edit-config` to `config.xml`
* [CB-11936](https://issues.apache.org/jira/browse/CB-11936) Support four new **App Transport Security (ATS)** keys
* update `config.xml` location if it is a **Android Studio** project.
* use `array` methods and `object.keys` for iterating. avoiding `for-in` loops
* [CB-11517](https://issues.apache.org/jira/browse/CB-11517) Allow `.folder` matches
* [CB-11776](https://issues.apache.org/jira/browse/CB-11776) check `edit-config` target exists

### 1.4.1 (Aug 09, 2016)
* Add general purpose `ConfigParser.getAttribute` API
* [CB-11653](https://issues.apache.org/jira/browse/CB-11653) moved `findProjectRoot` from `cordova-lib` to `cordova-common`
* [CB-11636](https://issues.apache.org/jira/browse/CB-11636) Handle attributes with quotes correctly
* [CB-11645](https://issues.apache.org/jira/browse/CB-11645) added check to see if `getEditConfig` exists before trying to use it
* [CB-9825](https://issues.apache.org/jira/browse/CB-9825) framework tag spec parsing

### 1.4.0 (Jul 12, 2016)
* [CB-11023](https://issues.apache.org/jira/browse/CB-11023) Add edit-config functionality

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

