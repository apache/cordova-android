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

### 3.3.0 (Dec 2013) ###

44 commits by 11 authors, Changes include:

* Set VERSION to 3.3.0 (via coho)
* Update JS snapshot to version 3.3.0 (via coho)
* CB-5481 Fix for Cordova trying to get config.xml from the wrong namespace
* Forgot Apache Headers on MessageTest
* Set VERSION to 3.3.0-rc1 (via coho)
* Update JS snapshot to version 3.3.0-rc1 (via coho)
* prevent ClassNotFound exception for emtpy class name
* CB-5487: Remote Debugging is on when your Android app is debuggable.
* Updating the README
* Making the object less chatty
* Updating tests to KitKat, and making the tests more thread-safe
* Incrementing API target
* CB-5445: Adding onScrollChanged and the ScrollEvent object. (Forgot to add the WebView)
* CB-5445: Adding onScrollChanged and the ScrollEvent object
* Updated CordovaWebView to experiment with onScrollChanged messages
* Moving the console.log out of run() method
* CB-5422: Don't require JAVA_HOME to be defined
* Thanks for Benn Mapes for making this process easy.  Updating the Android API level.
* CB-5490: add javadoc target to ant script
* CB-5471: add deprecation javadoc/annotation
* Add javadoc comments to source classes
* CB-5255: Checking in the Google Check, TODO: Add Amazon FireOS check
* CB-5232 Change create script to use Cordova as a library.
* Remove Application settings from framework/AndroidManifest.xml
* CB-5346: remove dependency on device plugin
* CB-5346: delete a stale file that isn't needed
* Updating instructions to indicate that the device plugin is required to be installed via plugman
* Fixing plugins configuration, Device shouldn't be checked in here
* Removing device plugin
* Removing the plugins directory after the plugins were incorrectly removed
* This should use plugman to install plugins.  Adding path depenencies for plugins is wrong, and shouldn't be done
* CB-5349: fixed regression in update script
* CB-5346 Fix and cleanup broken Android unit test
* CB-5307 Remove references to Callback and Incubator
* CB-5302: Massive movement to get tests working again
* Set VERSION to 3.3.0-dev (via coho)
* CB-5301 add missing license headers
* [CB-4996] Fix paths with spaces while launching on emulator and device
* CB-5284 Fixing the version from coho
* Fixing the VERSION file, it got auto-incremented by coho by accident
* Update JS snapshot to version 2.10.0-dev (via coho)
* Set VERSION to 2.10.0-dev (via coho)
* CB-5209 Win: Cannot build Android app if project path contains spaces
* CB-5209: Dirty, Dirty Fix for Building.  This works, but needs to be prettier.

### 3.2.0 (Nov 2013) ####

27 commits by 7 authors, Changes include:

 Set VERSION to 3.2.0 (via coho)
 * Update JS snapshot to version 3.2.0 (via coho)
 * CB-5301 add missing license headers
 * CB-5349: fixed regression in update script
 * Set VERSION to 3.2.0-rc1 (via coho)
 * Update JS snapshot to version 3.2.0-rc1 (via coho)
 * CB-5193 Fix Android WebSQL sometime throwing SECURITY_ERR.
 * CB-5191 Deprecate <url-filter>
 * Updating shelljs to 0.2.6. Copy now preserves mode bits.
 * CB-4872 - moved version script to promise model
 * CB-4872 - make sure to copy over version scripts to project
 * [CB-4872] - added android version scripts
 * CB-5117: Output confirmation message if check_reqs passes.
 * Refactoring Android project-level and platform scripts to use Q.js
 * Updating to latest shelljs, old version doesn't preserve +x bits
 * Remove cordova.xml fallback from Config.java (it was removed from PluginManager for 3.0)
 * CB-5080 Find resources in a way that works with aapt's --rename-manifest-package
 * Update JS snapshot to version 3.2.0-dev (via coho)
 * Remove a couple incorrect lines from RELEASENOTES.md
 * CB-4961: shell.js returns the full path on ls, rebuilding the full path isn't really needed
 * Updating README.md to have latest Android SDK
 * CB-4527: This was an easy fix, since the script deletes batch files
 * [CB-4892] Fix create script only escaping the first space instead of all spaces.
 * Fix update script to clobber cordova.js file (missing -f)
 * Add missing copyright header for Whitelist.java.
 * [CB-4832] Add 3.1.0 RELEASENOTES.md
 * Update JS snapshot to version 3.2.0-dev (via coho)
 * Set VERSION to 3.2.0-dev (via coho)

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
* Incremeting version check for Android 4.3 API Level 18
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

