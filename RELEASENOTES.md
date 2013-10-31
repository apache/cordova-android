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
## Release Notes for Cordova (Android) ##

### 3.1.0 (Sept 2013) ###

55 commits from 9 authors. Highlights include:

* [CB-4817] Remove unused assets in project template.
* Fail fast in create script if package name is not com.foo.bar.
* [CB-4782] Convert ApplicationInfo.java -> appinfo.js
* [CB-4766] Deprecated JSONUtils.java (moved into plugins)
* [CB-4765] Deprecated ExifHelper.java (moved into plugins)
* [CB-4764] Deprecated DirectoryManager.java (moved into plugins)
* [CB-4763] Deprecated FileHelper.java (moved into plugins), Move getMimeType() into CordovaResourceApi.
* [CB-4725] Add CordovaWebView.CORDOVA_VERSION constant
* Incrementing version check for Android 4.3 API Level 18
* [CB-3542] rewrote cli tooling scripts in node
* Allow CordovaChromeClient subclasses access to CordovaInterface and CordovaWebView members
* Refactor CordovaActivity.init so that subclasses can easily override factory methods for webview objects
* [CB-4652] Allow default project template to be overridden on create
* Tweak the online bridge to not send excess online events.
* [CB-4495] Modify start-emulator script to exit immediately on a fatal emulator error.
* Log WebView IOExceptions only when they are not 404s
* Use a higher threshold for slow exec() warnings when debugger is attached.
* Fix data URI decoding in CordovaResourceApi
* [CB-3819] Made it easier to set SplashScreen delay.
* [CB-4013] Fixed loadUrlTimeoutValue preference.
* Upgrading project to Android 4.3
* [CB-4198] bin/create script should be better at handling non-word characters in activity name. Patched windows script as well.
* [CB-4198] bin/create should handle spaces in activity better.
* [CB-4096] Implemented new unified whitelist for android
* [CB-3384] Fix thread assertion when plugins remap URIs

