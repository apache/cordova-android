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

### 3.5.0 (May 2014) ###

* OkHttp has broken headers. Updating for ASF compliance.
* Revert accidentally removed lines from NOTICE
* CB-6552: added top level package.json
* CB-6491 add CONTRIBUTING.md
* CB-6543 Fix cordova/run failure when no custom_rules.xml available
* defaults.xml: Add AndroidLaunchMode preference
* Add JavaDoc for CordovaResourceApi
* CB-6388: Handle binary data correctly in LOAD_URL bridge
* Fix CB-6048: Set launchMode=singleTop so tapping app icon does not always restart app
* Remove incorrect usage of AlertDialog.Builder.create
* Catch uncaught exceptions in from plugins and turn them into error responses.
* Add NOTICE file
* CB-6047 Fix online sometimes getting in a bad state on page transitions.
* Add another convenience overload for CordovaResourceApi.copyResource
* Update framework's .classpath to what Eclipse wants it to be.
* README.md: `android update` to `android-19`.
* Fix NPE when POLLING bridge mode is used.
* Updating NOTICE to include Square for OkHttp
* CB-5398 Apply KitKat content URI fix to all content URIs
* CB-5398 Work-around for KitKat content: URLs not rendering in <img> tags
* CB-5908: add splascreen images to template
* CB-5395: Make scheme and host (but not path) case-insensitive in whitelist
* Ignore multiple onPageFinished() callbacks & onReceivedError due to stopLoading()
* Removing addJavascriptInterface support from all Android versions lower than 4.2 due to security vu
* CB-4984 Don't create on CordovaActivity name
* CB-5917 Add a loadUrlIntoView overload that doesn't recreate plugins.
* Use thread pool for load timeout.
* CB-5715 For CLI, hide assets/www and res/xml/config.xml by default
* CB-5793 ant builds: Rename AndroidManifest during -post-build to avoid Eclipse detecting ant-build/
* CB-5889 Make update script find project name instead of using "null" for CordovaLib
* CB-5889 Add a message in the update script about needing to import CordovaLib when using an IDE.

### 3.4.0 (Feb 2014) ###

43 commits from 10 authors. Highlights include:

* Removing addJavascriptInterface support from all Android versions lower than 4.2 due to security vulnerability
* CB-5917 Add a loadUrlIntoView overload that doesn't recreate plugins.
* CB-5889 Make update script find project name instead of using "null" for CordovaLib
* CB-5889 Add a message in the update script about needing to import CordovaLib when using an IDE.
* CB-5793 Don't clean before build and change output directory to ant-build to avoid conflicts with Eclipse.
* CB-5803 Fix cordova/emulate on windows.
* CB-5801 exec->spawn in build to make sure compile errors are shown.
* CB-5799 Update version of OkHTTP to 1.3
* CB-4910 Update CLI project template to point to config.xml at the root now that it's not in www/ by default.
* CB-5504 Adding onDestroy to app plugin to deregister telephonyReceiver
* CB-5715 Add Eclipse .project file to create template. For CLI projects, it adds refs for root www/ & config.xml and hides platform versions
* CB-5447 Removed android:debuggable=“true” from project template.
* CB-5714 Fix of android build when too big output stops build with error due to buffer overflow.
* CB-5592 Set MIME type for openExternal when scheme is file:

### 3.3.0 (Dec 2013) ###

41 commits from 11 authors. Highlights include:

* CB-5481 Fix for Cordova trying to get config.xml from the wrong namespace
* CB-5487 Enable Remote Debugging when your Android app is debuggable.
* CB-5445 Adding onScrollChanged and the ScrollEvent object
* CB-5422 Don't require JAVA_HOME to be defined
* CB-5490 Add javadoc target to ant script
* CB-5471 Deprecated DroidGap class
* CB-5255 Prefer Google API targets over android-## targets when building.
* CB-5232 Change create script to use Cordova as a Library Project instead of a .jar
* CB-5302 Massive movement to get tests working again
* CB-4996 Fix paths with spaces while launching on emulator and device
* CB-5209 Cannot build Android app if project path contains spaces


### 3.2.0 (Nov 2013) ###

27 commits from 7 authors. Highlights include:

* CB-5193 Fix Android WebSQL sometime throwing SECURITY_ERR.
* CB-5191 Deprecate <url-filter>
* Updating shelljs to 0.2.6. Copy now preserves mode bits.
* CB-4872 Added android version scripts (android_sdk_version, etc)
* CB-5117 Output confirmation message if check_reqs passes.
* CB-5080 Find resources in a way that works with aapt's --rename-manifest-package
* CB-4527 Don't delete .bat files even when on non-windows platform
* CB-4892 Fix create script only escaping the first space instead of all spaces.

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

