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

### 6.2.1 (Apr 02, 2017)
* [CB-12621](https://issues.apache.org/jira/browse/CB-12621) reverted elementtree dep to 0.1.6

### 6.2.0 (Mar 28, 2017)
* [CB-12614](https://issues.apache.org/jira/browse/CB-12614) Adding headers to tests
* [CB-8978](https://issues.apache.org/jira/browse/CB-8978) Prepare copy `resource-files` from `config.xml`
* [CB-12605](https://issues.apache.org/jira/browse/CB-12605) Fix a requirements check failure on **Windows**
* [CB-12595](https://issues.apache.org/jira/browse/CB-12595) This should find an **Android Studio** installation and use the sweet gradle center found inside
* [CB-12546](https://issues.apache.org/jira/browse/CB-12546) leverage `avdmanager` if `android` warns it is no longer useful, which happens in **Android SDK Tools 25.3.1**. Explicitly set the `CWD` of the spawned emulator process to workaround a recent google android sdk bug. Rename `android_sdk_version.js` to `android_sdk.js`, to better reflect its contents. Have `create.js` copy over the `android_sdk_version` batch file.
* [CB-12524](https://issues.apache.org/jira/browse/CB-12524) Fix for missing gradle template error. This now fetches the template from inside of the **Android Studio** directory, and falls back to a locally installed Gradle instance
* [CB-12465](https://issues.apache.org/jira/browse/CB-12465) Writing new JUnit Test Instrumentation to replace tests and retire problmatic tests

### 6.1.2 (Jan 26, 2017)
* **Security** Change to `https` by default
* [CB-12018](https://issues.apache.org/jira/browse/CB-12018): updated tests to work with jasmine (promise matcher tests commented out for now)
* created directories and corresponding images for `xxhdpi` and `xxxhdpi`, both drawables and `mipmaps`

### 6.1.1 (Jan 03, 2017)
* [CB-12159](https://issues.apache.org/jira/browse/CB-12159) **Android** Keystore password prompt won't show up
* [CB-12169](https://issues.apache.org/jira/browse/CB-12169) Check for build directory before running a clean
* Fixed `AndroidStudio` tests to actually run, removed `app/src/main/assets/` as a requirement and added `app/src/main/res` instead, added placeholder for `build/` folder, Removed dupe `gitignore`

### 6.1.0 (Nov 02, 2016)
* [CB-12108](https://issues.apache.org/jira/browse/CB-12108) Updating gradle files to work with the latest version of Android Studio
* [CB-12102](https://issues.apache.org/jira/browse/CB-12102) Bump travis to build to API 25
* Bumping up the version
* [CB-12101](https://issues.apache.org/jira/browse/CB-12101) Fix so that CLI builds don't conflict with Android Studio builds
* [CB-12077](https://issues.apache.org/jira/browse/CB-12077) Fix paths for Android icons/splashscreens
* added framework/build to .ratignore
* Fix for broken testUrl test
* Last minute change of test targets
* Update JS snapshot to version 6.1.0-dev (via coho)
* Set VERSION to 6.1.0-dev (via coho)

### 6.0.0 (Oct 20, 2016)

This release adds significant functionality, and also introduces a number
of breaking changes.  Some of the changes to the code base will be of
particular interest to third party webview plugin developers.

#### Major Changes ####
* Primary bridge is the EVAL_BRIDGE, which tells the WebView to execute JS directly.  This is more stable than the ONLINE_EVENT bridge
* Full Support for Android Nougat (API 24)
* Ice Cream Sandwich Support has been deprecated.  Minimum Supported Android Version is Jellybean (API 16/ Android 4.1)
* Plugin Installation now CLEANS the build directory, this speeds up gradle build times and allows for CLI develoment to be more predictable

Changes For Third-Party WebView Developers:
* executeJavascript method added and is an abstract method that must be implemented
* the EVAL_BRIDGE must be added to the WebView


#### Curated Changes from the Git Commit Logs ####
* Updating the gradle build for test to use the latest
* [CB-11083](https://issues.apache.org/jira/browse/CB-11083) Fixing syncronous file check and future-proofing the JS for Travis
* [CB-11083](https://issues.apache.org/jira/browse/CB-11083) Reading files to check for CordovaLib dependency, if so, we exclude CordovaLib to be safe
* [CB-11083](https://issues.apache.org/jira/browse/CB-11083) Plugin build script for dependencies without a gradle file
* [CB-11083](https://issues.apache.org/jira/browse/CB-11083) The GradleBuidler can tell the difference between a Cordova Plugin Framework and a regular framework based on the name
* [CB-11083](https://issues.apache.org/jira/browse/CB-11083) Fix to deal with custom frameworks with their own Gradle configuration
* [CB-12003](https://issues.apache.org/jira/browse/CB-12003) updated node_modules
* [CB-11771](https://issues.apache.org/jira/browse/CB-11771) Deep symlink directories to target project instead of linking the directory itself
* [CB-11880](https://issues.apache.org/jira/browse/CB-11880) android: Fail-safe for cordova.exec()
* [CB-11999](https://issues.apache.org/jira/browse/CB-11999) add message, catch exception if require fails
* fix issue with app_name containing apostrophes
* [CB-8722](https://issues.apache.org/jira/browse/CB-8722) - Move icons from drawable to mipmap
* [CB-11964](https://issues.apache.org/jira/browse/CB-11964) Call clean after plugin install and mock it in tests
* Did a try/catch to deal with the unit tests vs actual project environment, code duplication is needed because of builderEnv
* [CB-11964](https://issues.apache.org/jira/browse/CB-11964) Do a clean when installing a plugin to et around the bug
* [CB-11921](https://issues.apache.org/jira/browse/CB-11921) - Add github pull request template
* [CB-11935](https://issues.apache.org/jira/browse/CB-11935) Does a best-effort attempt to pause any processing that can be paused safely, such as animations and geolocation.
* [CB-11640](https://issues.apache.org/jira/browse/CB-11640) Fixing check_reqs.js so it actually works
* [CB-11640](https://issues.apache.org/jira/browse/CB-11640) Changing requirements check to ask for Java 8
* [CB-11869](https://issues.apache.org/jira/browse/CB-11869) Fix cordova-js android exec tests
* [CB-11907](https://issues.apache.org/jira/browse/CB-11907) Bumping Gradle to work with Android Studio 2.2 and the Android Gradle Plugin
* Enable background start of Cordova Android apps
* fixing jshint issues
* replace Integer.parseInt with BigInteger so that you can use longer Android version codes
* [CB-11828](https://issues.apache.org/jira/browse/CB-11828) Adding dirty userAgent checking to see if we're running Jellybean or not for bridge modes
* [CB-11828](https://issues.apache.org/jira/browse/CB-11828) Switching default bridge back to ONLINE_BRIDGE
* Add gradle build flag to enable dex in process for large projects
* added ability for cordova activity to be viewed in a real full screen regardless of android version (as was the case in previous cordova versions)
* Updating travis
* Adding Static Method to CoreAndroid Plugin so we can get the BuildConfig data from other plugins
* Bump Target and Min API levels
* Make evaluateJavaScript brige default
* Creating an evaluateJavascript branch
* [CB-11727](https://issues.apache.org/jira/browse/CB-11727) - travis ci setup is still using 0.10.32 node
* [CB-11726](https://issues.apache.org/jira/browse/CB-11726) - Update appveyor node versions to 4 and 6, so they will always use the latest versions
* Close invalid PRs
* [CB-11683](https://issues.apache.org/jira/browse/CB-11683) Fixed linking to directories during plugin installation.
* fixed [CB-11078](https://issues.apache.org/jira/browse/CB-11078) Empty string for BackgroundColor preference crashes application This closes #316
* Update JS snapshot to version 5.3.0-dev (via coho)
* Set VERSION to 5.3.0-dev (via coho)
* [CB-11626](https://issues.apache.org/jira/browse/CB-11626) Updated RELEASENOTES and Version for release 5.2.2
* updated cordoova-common to 1.4.0
* This closes #195
* Updaing the gradle for the tests to the latest
* [CB-11550](https://issues.apache.org/jira/browse/CB-11550) Updated RELEASENOTES for release 5.2.1
* [CB-9489](https://issues.apache.org/jira/browse/CB-9489) Fixed "endless waiting for emulator" issue
* Update JS snapshot to version 5.3.0-dev (via coho)
* Set VERSION to 5.3.0-dev (via coho)
* [CB-11444](https://issues.apache.org/jira/browse/CB-11444) Updated RELEASENOTES and Version for release 5.2.0
* [CB-11481](https://issues.apache.org/jira/browse/CB-11481) android-library is deprecated use com.android.library instead

### 5.2.2 (Jul 26, 2016)
* [CB-11615](https://issues.apache.org/jira/browse/CB-11615) updated `cordoova-common` to `1.4.0`

### 5.2.1 (Jul 11, 2016)
* [CB-9489](https://issues.apache.org/jira/browse/CB-9489) Fixed "endless waiting for emulator" issue
* [CB-11481](https://issues.apache.org/jira/browse/CB-11481) android-library is deprecated use com.android.library instead

### 5.2.0 (Jun 29, 2016)
* [CB-11383](https://issues.apache.org/jira/browse/CB-11383) Update to gradle for using `jcenter` and correct Application plugin
* [CB-11365](https://issues.apache.org/jira/browse/CB-11365) fixed plugin rm issue with emit being `undefined`
* [CB-11117](https://issues.apache.org/jira/browse/CB-11117) Use `FileUpdater` to optimize prepare for **android** platform
* [CB-10096](https://issues.apache.org/jira/browse/CB-10096) Upgrade test project to `Gradle Plugin 2.1.0`
* [CB-11292](https://issues.apache.org/jira/browse/CB-11292) fix broken `MessageChannel` after plugins are recreated
* [CB-11259](https://issues.apache.org/jira/browse/CB-11259) Improving build output
* [CB-10096](https://issues.apache.org/jira/browse/CB-10096) Upgrading to `Gradle Plugin 2.1.0`
* [CB-11198](https://issues.apache.org/jira/browse/CB-11198) Skip **android** target sdk check. This closes #303.
* [CB-11138](https://issues.apache.org/jira/browse/CB-11138) Reuse `PluginManager` from `common` to add/rm plugins
* [CB-11133](https://issues.apache.org/jira/browse/CB-11133) Handle **android** emulator start failure
* [CB-11132](https://issues.apache.org/jira/browse/CB-11132) Fix Error: Cannot read property `match` of undefined in `cordova-android` `emulator.js`
* Add simple log for package name being deployed
* [CB-11015](https://issues.apache.org/jira/browse/CB-11015) Error adding plugin with gradle extras
* [CB-11095](https://issues.apache.org/jira/browse/CB-11095) Fix plugin add/removal when running on `Node v.010`
* [CB-11022](https://issues.apache.org/jira/browse/CB-11022) Duplicate www files to both destinations on plugin operations
* [CB-10964](https://issues.apache.org/jira/browse/CB-10964) Handle `build.json` file starting with a BOM.
* [CB-10963](https://issues.apache.org/jira/browse/CB-10963) Handle overlapping permission requests from plugins
* [CB-8582](https://issues.apache.org/jira/browse/CB-8582) Obscure `INSTALL_FAILED_VERSION_DOWNGRADE` error when installing app
* [CB-10862](https://issues.apache.org/jira/browse/CB-10862) Cannot set `minsdkversion`
* [CB-10896](https://issues.apache.org/jira/browse/CB-10896) We never enabled cookies on the `WebView` proper
* [CB-10837](https://issues.apache.org/jira/browse/CB-10837) Support platform-specific orientation on **Android**
* [CB-10600](https://issues.apache.org/jira/browse/CB-10600) `cordova run android --release` does not use signed and zip-aligned version of `APK`
* [CB-9710](https://issues.apache.org/jira/browse/CB-9710) Fixing issues parsing `android avd list` output for certain AVDs which resulted in them not being included in the selection process even if they are the best match.
* [CB-10888](https://issues.apache.org/jira/browse/CB-10888) Enable coverage reports collection via codecov
* [CB-10846](https://issues.apache.org/jira/browse/CB-10846) Add Travis and AppVeyor badges to readme
* [CB-10846](https://issues.apache.org/jira/browse/CB-10846) Add AppVeyor configuration
* [CB-10749](https://issues.apache.org/jira/browse/CB-10749) Use `cordova-common.CordovaLogger` in `cordova-android`
* [CB-10673](https://issues.apache.org/jira/browse/CB-10673) fixed conflicting plugin install issue with overlapped `<source-file>` tag. Add `--force` flag.
* [CB-8976](https://issues.apache.org/jira/browse/CB-8976) Removing the auto-version for non-Crosswalk applications
* [CB-10768](https://issues.apache.org/jira/browse/CB-10768) Use `cordova-common.superspawn` in `GradleBuilder`
* [CB-10729](https://issues.apache.org/jira/browse/CB-10729) Move plugin handlers tests for into platform's repo
* [CB-10669](https://issues.apache.org/jira/browse/CB-10669) `cordova run --list` cannot find `adb`
* [CB-10660](https://issues.apache.org/jira/browse/CB-10660) fixed the exception when removing a non-existing directory.

### 5.1.1 (Feb 24, 2016)
* updated `cordova-common` dependnecy to `1.1.0`
* [CB-10628](https://issues.apache.org/jira/browse/CB-10628) Fix `emulate android --target`
* [CB-10618](https://issues.apache.org/jira/browse/CB-10618) Handle gradle frameworks on plugin installation/uninstallation
* [CB-10510](https://issues.apache.org/jira/browse/CB-10510) Add an optional timeout to `emu` start script
* [CB-10498](https://issues.apache.org/jira/browse/CB-10498) Resume event should be sticky if it has a plugin result
* fix `HtmlNotFoundTest` so that it passes when file not found is handled correctly
* [CB-10472](https://issues.apache.org/jira/browse/CB-10472) `NullPointerException`: `org.apache.cordova.PluginManager.onSaveInstanceState` check if `pluginManager` is `null` before using it
* [CB-10138](https://issues.apache.org/jira/browse/CB-10138) Adds missing plugin metadata to `plugin_list` module.
* [CB-10443](https://issues.apache.org/jira/browse/CB-10443) Pass original options instead of remaining
* [CB-10443](https://issues.apache.org/jira/browse/CB-10443) Fix `this.root` null reference
* [CB-10421](https://issues.apache.org/jira/browse/CB-10421) Fixes exception when calling run script with `--help` option
* updated `.gitignore`
* [CB-10406](https://issues.apache.org/jira/browse/CB-10406) Fixes an exception, thrown when building using Ant.
* [CB-10157](https://issues.apache.org/jira/browse/CB-10157) Uninstall app from device/emulator only when signed apk is already installed

### 5.1.0 (Jan 19, 2016)
* [CB-10386](https://issues.apache.org/jira/browse/CB-10386) Add `android.useDeprecatedNdk=true` to support `NDK` in `gradle`
* [CB-8864](https://issues.apache.org/jira/browse/CB-8864) Fixing this to mitigate [CB-8685](https://issues.apache.org/jira/browse/CB-8685) and [CB-10104](https://issues.apache.org/jira/browse/CB-10104)
* [CB-10105](https://issues.apache.org/jira/browse/CB-10105) Spot fix for tilde errors on paths.
* Update theme to `Theme.DeviceDefault.NoActionBar`
* [CB-10014](https://issues.apache.org/jira/browse/CB-10014) Set gradle `applicationId` to `package name`.
* [CB-9949](https://issues.apache.org/jira/browse/CB-9949) Fixing menu button event not fired in **Android**
* [CB-9479](https://issues.apache.org/jira/browse/CB-9479) Fixing the conditionals again, we should
* [CB-8917](https://issues.apache.org/jira/browse/CB-8917) New Plugin API for passing results on resume after Activity destruction
* [CB-9971](https://issues.apache.org/jira/browse/CB-9971) Suppress `gradlew _JAVA_OPTIONS` output during build
* [CB-9836](https://issues.apache.org/jira/browse/CB-9836) Add `.gitattributes` to prevent `CRLF` line endings in repos
* added node_modules back into `.gitignore`

### 5.0.0 (Nov 01, 2015)
* Update CordovaWebViewEngine.java
* [CB-9909](https://issues.apache.org/jira/browse/CB-9909) Shouldn't escape spaces in paths on Windows.
* [CB-9870](https://issues.apache.org/jira/browse/CB-9870) updated hello world template
* [CB-9880](https://issues.apache.org/jira/browse/CB-9880) Fixes platform update failure when upgrading from android@<4.1.0
* [CB-9844](https://issues.apache.org/jira/browse/CB-9844) Remove old .java after renaming activity
* [CB-9800](https://issues.apache.org/jira/browse/CB-9800) Fixing contribute link.
* [CB-9782](https://issues.apache.org/jira/browse/CB-9782) Check in `cordova-common` dependency
* Adds licence header to Adb to pass rat audit
* [CB-9835](https://issues.apache.org/jira/browse/CB-9835) Downgrade `properties-parser` to prevent failures in Node < 4.x
* [CB-9782](https://issues.apache.org/jira/browse/CB-9782) Implements PlatformApi contract for Android platform.
* [CB-9826](https://issues.apache.org/jira/browse/CB-9826) Fixed `test-build` script on windows.
* Refactor of the Cordova Plugin/Permissions API
* Manually updating version to 5.0.0-dev for engine tags
* Bump up to API level 23
* Commiting code to handle permissions, and the special case of the Geolocation Plugin
* [CB-9608](https://issues.apache.org/jira/browse/CB-9608) cordova-android no longer builds on Node 0.10 or below
* [CB-9080](https://issues.apache.org/jira/browse/CB-9080) Cordova CLI run for Android versions 4.1.1 and lower throws error
* [CB-9557](https://issues.apache.org/jira/browse/CB-9557) Fixes apk install failure when switching from debug to release build
* [CB-9496](https://issues.apache.org/jira/browse/CB-9496) removed permissions added for crosswalk
* [CB-9402](https://issues.apache.org/jira/browse/CB-9402) Allow to set gradle distubutionUrl via env variable CORDOVA_ANDROID_GRADLE_DISTRIBUTION_URL
* [CB-9428](https://issues.apache.org/jira/browse/CB-9428) update script now bumps up minSdkVersion to 14 if it is less than that.
* [CB-9430](https://issues.apache.org/jira/browse/CB-9430) Fixes check_reqs failure when javac returns an extra line
* [CB-9172](https://issues.apache.org/jira/browse/CB-9172) Improved emulator deploy stability. This closes #188.
* [CB-9404](https://issues.apache.org/jira/browse/CB-9404) Fixed an exception when path contained -debug or -release
* [CB-8320](https://issues.apache.org/jira/browse/CB-8320) Setting up gradle so we can use CordovaLib as a standard Android Library
* [CB-9185](https://issues.apache.org/jira/browse/CB-9185) Fixed an issue when unsigned apks couldn't be found.
* [CB-9397](https://issues.apache.org/jira/browse/CB-9397) Fixes minor issues with `cordova requirements android`
* [CB-9389](https://issues.apache.org/jira/browse/CB-9389) Fixes build/check_reqs hang

### Release 4.1.1 (Aug 2015) ###

* [CB-9428](https://issues.apache.org/jira/browse/CB-9428) update script now bumps up minSdkVersion to 14 if it is less than that
* [CB-9430](https://issues.apache.org/jira/browse/CB-9430) Fixes check_reqs failure when javac returns an extra line

### Release 4.1.0 (Jul 2015) ###
* [CB-9392](https://issues.apache.org/jira/browse/CB-9392) Fixed printing flavored versions. This closes #184.
* [CB-9382](https://issues.apache.org/jira/browse/CB-9382) [Android] Fix KeepRunning setting when Plugin activity is showed. This closes #200
* [CB-9391](https://issues.apache.org/jira/browse/CB-9391) Fixes cdvBuildMultipleApks option casting
* [CB-9343](https://issues.apache.org/jira/browse/CB-9343) Split the Content-Type to obtain a clean mimetype
* [CB-9255](https://issues.apache.org/jira/browse/CB-9255) Make getUriType case insensitive.
* [CB-9149](https://issues.apache.org/jira/browse/CB-9149) Fixes JSHint issue introduced by 899daa9
* [CB-9372](https://issues.apache.org/jira/browse/CB-9372) Remove unused files: 'main.js' & 'master.css'. This closes #198
* [CB-9149](https://issues.apache.org/jira/browse/CB-9149) Make gradle alias subprojects in order to handle libs that depend on libs. This closes #182
* Update min SDK version to 14
* Update licenses. This closes #190
* [CB-9185](https://issues.apache.org/jira/browse/CB-9185) Fix signed release build exception. This closes #193.
* [CB-9286](https://issues.apache.org/jira/browse/CB-9286) Fixes build failure when ANDROID_HOME is not set.
* [CB-9284](https://issues.apache.org/jira/browse/CB-9284) Fix for handling absolute path for keystore in build.json
* [CB-9260](https://issues.apache.org/jira/browse/CB-9260) Install Android-22 on Travis-CI
* Adding .ratignore file.
* [CB-9119](https://issues.apache.org/jira/browse/CB-9119) Adding lib/retry.js for retrying promise-returning functions. Retrying 'adb install' in emulator.js because it sometimes hangs.
* [CB-9115](https://issues.apache.org/jira/browse/CB-9115) android: Grant Lollipop permission req
* Remove extra console message
* [CB-8898](https://issues.apache.org/jira/browse/CB-8898) Report expected gradle location properly
* [CB-8898](https://issues.apache.org/jira/browse/CB-8898) Fixes gradle check failure due to missing quotes
* [CB-9080](https://issues.apache.org/jira/browse/CB-9080) -d option is not supported on Android 4.1.1 and lower, removing
* [CB-8954](https://issues.apache.org/jira/browse/CB-8954) Adds `requirements` command support to check_reqs module
* Update JS snapshot to version 4.1.0-dev (via coho)
* [CB-8417](https://issues.apache.org/jira/browse/CB-8417) updated platform specific files from cordova.js repo
* Adding tests to confirm that preferences aren't changed by Intents
* Forgot to remove the method that copied over the intent data
* Getting around to removing this old Intent code
* Update JS snapshot to version 4.1.0-dev (via coho)
* Fix CordovaPluginTest on KitKat (start-up events seem to change)
* [CB-3360](https://issues.apache.org/jira/browse/CB-3360) Allow setting a custom User-Agent (close #162)
* [CB-8902](https://issues.apache.org/jira/browse/CB-8902) Use immersive mode when available when going fullscreen (close #175)
* Make BridgeMode methods public (they were always supposed to be)
* Simplify: EncodingUtils.getBytes(str) -> str.getBytes()
* Don't show warning when gradlew file is read-only
* Don't show warning when prepEnv copies gradlew and it's read-only
* Make gradle wrapper prepEnv code work even when android-sdk is read-only
* [CB-8897](https://issues.apache.org/jira/browse/CB-8897) Delete drawable/icon.png since it duplicates drawable-mdpi/icon.png
* Updating the template to target mininumSdkTarget=14
* [CB-8894](https://issues.apache.org/jira/browse/CB-8894) Updating the template to target mininumSdkTarget=14
* [CB-8891](https://issues.apache.org/jira/browse/CB-8891) Add a note about when the gradle helpers were added
* [CB-8891](https://issues.apache.org/jira/browse/CB-8891) Add a gradle helper for retrieving config.xml preference values
* [CB-8884](https://issues.apache.org/jira/browse/CB-8884) Delete Eclipse tweaks from create script
* [CB-8834](https://issues.apache.org/jira/browse/CB-8834) Don't fail to install on VERSION_DOWNGRADE
* Automated tools fail, and you have to remember all four places where this is set.
* Update the package.json
* [CB-9042](https://issues.apache.org/jira/browse/CB-9042) coho failed to update version, so here we are
* CB9042 - Updating Release Notes
* Adding tests to confirm that preferences aren't changed by Intents
* updating existing test code
* Forgot to remove the method that copied over the intent data
* Getting around to removing this old Intent code
* [CB-8834](https://issues.apache.org/jira/browse/CB-8834) Don't fail to install on VERSION_DOWNGRADE

### Release 4.0.2 (May 2015) ###

* Removed Intent Functionality from Preferences - Preferences can no longer be set by intents

### Release 4.0.1 (April 2015) ###

* Bug fixed where platform failed to install on a version downgrade

### Release 4.0.0 (March 2015) ###

This release adds significant functionality, and also introduces a number
of breaking changes.  Some of the changes to the code base will be of
particular interest to plugin developers.

#### Major Changes ####
* Support for pluggable WebViews
  * The system WebView can be replaced in your app, via a plugin
  * Core WebView functionality is encapsulated, with extension points exposed
    via interfaces
* Support for Crosswalk to bring the modern Chromium WebView to older devices
  * Uses the pluggable WebView framework
  * You will need to add the new [cordova-crosswalk-engine](https://github.com/MobileChromeApps/cordova-crosswalk-engine) plugin
* Splash screen functionality is now provided via plugin
  * You will need to add the new [cordova-plugin-splashscreen](https://github.com/apache/cordova-plugin-splashscreen) plugin to continue using a splash screen
* Whitelist functionality is now provided via plugin (CB-7747)
  * The whitelist has been enhanced to be more secure and configurable
  * Setting of Content-Security-Policy is now supported by the framework (see details in plugin readme)
  * You will need to add the new [cordova-plugin-whitelist](https://github.com/apache/cordova-plugin-whitelist) plugin
  * Legacy whitelist behaviour is still available via plugin (although not recommended).

Changes For Plugin Developers:

* Develop in Android Studio
  * Android Studio is now fully supported, and recommended over Eclipse
* Build using Gradle
  * All builds [use Gradle by default](Android%20Shell%20Tool%20Guide_building_with_gradle), instead of Ant
  * Plugins can add their own gradle build steps!
  * Plugins can depend on Maven libraries using `<framework>` tags
* New APIs: `onStart`, `onStop`, `onConfigurationChanged`
* `"onScrollChanged"` message removed. Use `view.getViewTreeObserver().addOnScrollChangedListener(...)` instead
* [CB-8702](https://issues.apache.org/jira/browse/CB-8702) New API for plugins to override `shouldInterceptRequest` with a stream

#### Other Changes ####
* [CB-8378](https://issues.apache.org/jira/browse/CB-8378) Removed `hidekeyboard` and `showkeyboard` events (apps should use a plugin instead)
* [CB-8735](https://issues.apache.org/jira/browse/CB-8735) `bin/create` regex relaxed / better support for numbers
* [CB-8699](https://issues.apache.org/jira/browse/CB-8699) Fix CordovaResourceApi `copyResource` creating zero-length files when src=uncompressed asset
* [CB-8693](https://issues.apache.org/jira/browse/CB-8693) CordovaLib should not contain icons / splashscreens
* [CB-8592](https://issues.apache.org/jira/browse/CB-8592) Fix NPE if lifecycle events reach CordovaWebView before `init()` has been called
* [CB-8588](https://issues.apache.org/jira/browse/CB-8588) Add CATEGORY_BROWSABLE to intents from showWebPage openExternal=true
* [CB-8587](https://issues.apache.org/jira/browse/CB-8587) Don't allow WebView navigations within showWebPage that are not whitelisted
* [CB-7827](https://issues.apache.org/jira/browse/CB-7827) Add `--activity-name` for `bin/create`
* [CB-8548](https://issues.apache.org/jira/browse/CB-8548) Use debug-signing.properties and release-signing.properties when they exist
* [CB-8545](https://issues.apache.org/jira/browse/CB-8545) Don't add a layout as a parent of the WebView
* [CB-7159](https://issues.apache.org/jira/browse/CB-7159) BackgroundColor not used when `<html style="opacity:0">`, nor during screen rotation
* [CB-6630](https://issues.apache.org/jira/browse/CB-6630) Removed OkHttp from core library. It's now available as a plugin: [cordova-plugin-okhttp](https://www.npmjs.com/package/cordova-plugin-okhttp)

### Release 3.7.1 (January 2015) ###
* [CB-8411](https://issues.apache.org/jira/browse/CB-8411) Initialize plugins only after `createViews()` is called (regression in 3.7.0)

### Release 3.7.0 (January 2015) ###

* [CB-8328](https://issues.apache.org/jira/browse/CB-8328) Allow plugins to handle certificate challenges (close #150)
* [CB-8201](https://issues.apache.org/jira/browse/CB-8201) Add support for auth dialogs into Cordova Android
* [CB-8017](https://issues.apache.org/jira/browse/CB-8017) Add support for `<input type=file>` for Lollipop
* [CB-8143](https://issues.apache.org/jira/browse/CB-8143) Loads of gradle improvements (try it with cordova/build --gradle)
* [CB-8329](https://issues.apache.org/jira/browse/CB-8329) Cancel outstanding ActivityResult requests when a new startActivityForResult occurs
* [CB-8026](https://issues.apache.org/jira/browse/CB-8026) Bumping up Android Version and setting it up to allow third-party cookies.  This might change later.
* [CB-8210](https://issues.apache.org/jira/browse/CB-8210) Use PluginResult for various events from native so that content-security-policy <meta> can be used
* [CB-8168](https://issues.apache.org/jira/browse/CB-8168) Add support for `cordova/run --list` (closes #139)
* [CB-8176](https://issues.apache.org/jira/browse/CB-8176) Vastly better auto-detection of SDK & JDK locations
* [CB-8079](https://issues.apache.org/jira/browse/CB-8079) Use activity class package name, but fallback to application package name when looking for splash screen drawable
* [CB-8147](https://issues.apache.org/jira/browse/CB-8147) Have corodva/build warn about unrecognized flags rather than fail
* [CB-7881](https://issues.apache.org/jira/browse/CB-7881) Android tooling shouldn't lock application directory
* [CB-8112](https://issues.apache.org/jira/browse/CB-8112) Turn off mediaPlaybackRequiresUserGesture
* [CB-6153](https://issues.apache.org/jira/browse/CB-6153) Add a preference for controlling hardware button audio stream (DefaultVolumeStream)
* [CB-8031](https://issues.apache.org/jira/browse/CB-8031) Fix race condition that shows as ConcurrentModificationException
* [CB-7974](https://issues.apache.org/jira/browse/CB-7974) Cancel timeout timer if view is destroyed
* [CB-7940](https://issues.apache.org/jira/browse/CB-7940) Disable exec bridge if bridgeSecret is wrong
* [CB-7758](https://issues.apache.org/jira/browse/CB-7758) Allow content-url-hosted pages to access the bridge
* [CB-6511](https://issues.apache.org/jira/browse/CB-6511) Fixes build for android when app name contains unicode characters.
* [CB-7707](https://issues.apache.org/jira/browse/CB-7707) Added multipart PluginResult
* [CB-6837](https://issues.apache.org/jira/browse/CB-6837) Fix leaked window when hitting back button while alert being rendered
* [CB-7674](https://issues.apache.org/jira/browse/CB-7674) Move preference activation back into onCreate()
* [CB-7499](https://issues.apache.org/jira/browse/CB-7499) Support RTL text direction
* [CB-7330](https://issues.apache.org/jira/browse/CB-7330) Don't run check_reqs for bin/create.

### 3.6.4 (Sept 30, 2014) ###

* Set VERSION to 3.6.4 (via coho)
* Update JS snapshot to version 3.6.4 (via coho)
* [CB-7634](https://issues.apache.org/jira/browse/CB-7634) Detect JAVA_HOME properly on Ubuntu
* [CB-7579](https://issues.apache.org/jira/browse/CB-7579) Fix run script's ability to use non-arch-specific APKs
* [CB-6511](https://issues.apache.org/jira/browse/CB-6511) Fixes build for android when app name contains unicode characters.
* [CB-7463](https://issues.apache.org/jira/browse/CB-7463) Adding licences.  I don't know what the gradle syntax is for comments, that still needs to be done.
* [CB-7463](https://issues.apache.org/jira/browse/CB-7463) Looked at the Apache BigTop git, gradle uses C-style comments
* [CB-7460](https://issues.apache.org/jira/browse/CB-7460) Fixing bug with KitKat where the background colour would override the CSS colours on the application

### 3.6.0 (Sept 2014) ###

* Set VERSION to 3.6.0 (via coho)
* [CB-7410](https://issues.apache.org/jira/browse/CB-7410) fix the menu test
* [CB-7410](https://issues.apache.org/jira/browse/CB-7410) Fix the errorUrl test
* [CB-7410](https://issues.apache.org/jira/browse/CB-7410) Fix Basic Authentication test
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Allow build and run scripts to select APK by architecture
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Add environment variable 'BUILD_MULTIPLE_APKS' for splitting APKs based on architecture
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Ensure that JAR files in libs directory are included
* [CB-7267](https://issues.apache.org/jira/browse/CB-7267) update RELEASENOTES for 3.5.1
* [CB-7410](https://issues.apache.org/jira/browse/CB-7410) clarify the title
* [CB-7385](https://issues.apache.org/jira/browse/CB-7385) update cordova.js for testing prior to branch/tag
* [CB-7410](https://issues.apache.org/jira/browse/CB-7410) add whitelist entries to get iframe/GoogleMaps working
* [CB-7291](https://issues.apache.org/jira/browse/CB-7291) propogate change in method signature to the native tests
* [CB-7291](https://issues.apache.org/jira/browse/CB-7291) Restrict meaning of "\*" in internal whitelist to just http and https
* [CB-7291](https://issues.apache.org/jira/browse/CB-7291) Only add file, content and data URLs to internal whitelist
* [CB-7291](https://issues.apache.org/jira/browse/CB-7291) Add defaults to external whitelist
* [CB-7291](https://issues.apache.org/jira/browse/CB-7291) Add external-launch-whitelist and use it for filtering intent launches
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Read project.properties to configure gradle libraries
* [CB-7325](https://issues.apache.org/jira/browse/CB-7325) Fix error message in android_sdk_version.js when missing SDK on windows
* [CB-7335](https://issues.apache.org/jira/browse/CB-7335) Add a .gitignore to android project template
* [CB-7330](https://issues.apache.org/jira/browse/CB-7330) Fix dangling function call in last commit (broke gradle builds)
* [CB-7330](https://issues.apache.org/jira/browse/CB-7330) Don't run "android update" during creation
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Add gradle support clean command (plus some code cleanup)
* [CB-7044](https://issues.apache.org/jira/browse/CB-7044) Fix typo in prev commit causing check_reqs to always fail.
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Copy gradle wrapper in build instead of create
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Add .gradle template files for "update" as well as "create"
* [CB-7044](https://issues.apache.org/jira/browse/CB-7044) Add JAVA_HOME when not set. Be stricter about ANDROID_HOME
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Speed up gradle building (incremental builds go from 10s -> 1.5s for me)
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) android: Copy Gradle wrapper from Android SDK rather than bundling a JAR
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Add which to checked-in node_modules
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Add option to build and install with gradle
* [CB-3445](https://issues.apache.org/jira/browse/CB-3445) Add an initial set of Gradle build scripts
* [CB-7321](https://issues.apache.org/jira/browse/CB-7321) Don't require ant for create script
* CB-7044, [CB-7299](https://issues.apache.org/jira/browse/CB-7299) Fix up PATH problems when possible.
* Change in test's AndroidManifest.xml needed for the test to run properly. Forgot the manifest.
* Change in test's AndroidManifest.xml needed for the test to run properly
* Adding tests related to 3.5.1
* [CB-7261](https://issues.apache.org/jira/browse/CB-7261) Fix setNativeToJsBridgeMode sometimes crashing when switching to ONLINE_EVENT
* [CB-7265](https://issues.apache.org/jira/browse/CB-7265) Fix crash when navigating to custom protocol (introduced in 3.5.1)
* Filter out non-launchable intents
* Handle unsupported protocol errors in webview better
* [CB-7238](https://issues.apache.org/jira/browse/CB-7238) I should have collapsed this, but Config.init() must go before the creation of CordovaWebView
* [CB-7238](https://issues.apache.org/jira/browse/CB-7238) Minor band-aid to get tests running again, this has to go away before 3.6.0 is released, since this is an API change.
* Extend whitelist to handle URLs without // chars
* [CB-7172](https://issues.apache.org/jira/browse/CB-7172) Force window to have focus after resume
* [CB-7159](https://issues.apache.org/jira/browse/CB-7159) Set background color of webView as well as its parent
* [CB-7018](https://issues.apache.org/jira/browse/CB-7018) Fix setButtonPlumbedToJs never un-listening
* Undeprecate some just-deprecated symbols in PluginManager.
* @Deprecate methods of PluginManager that were never meant to be public
* Move plugin instantiation and instance storing logic PluginEntry->PluginManager
* Fix broken unit test due to missing Config.init() call
* Update to check for Google Glass APIs
* Fix for `android` not being in PATH check on Windows
* Displaying error when regex does not match.
* Fix broken compile due to previous commit :(
* Tweak CordovaPlugin.initialize method to be less deprecated.
* Un-deprecate CordovaActivity.init() - it's needed to tweak prefs in onCreate
* Tweak log messages in CordovaBridge with bridgeSecret is wrong
* Backport CordovaBridge from 4.0.x -> master
* Update unit tests to not use most deprecated things (e.g. DroidGap)
* Add non-String overloades for CordovaPreferences.set()
* Make CordovaWebview resilient to init() not being called (for backwards-compatibility)
* Add node_module licenses to LICENSE
* Update cordova.js snapshot to work with bridge changes
* Provide CordovaPlugin with CordovaPreferences. Add new Plugin.initialize()
* Convert usages of Config.\* to use the non-static versions
* Change getProperty -> prefs.get\* within CordovaActivity
* Make CordovaUriHelper class package-private
* Fix PluginManager.setPluginEntries not removing old entries
* Move registration of App plugin from config.xml -> code
* Make setWebViewClient an override instead of an overload. Delete Location-change JS->Native bridge mode (missed some of it).
* [CB-4404](https://issues.apache.org/jira/browse/CB-4404) Revert setting android:windowSoftInputMode to "adjustPan"
* Refactor: Use ConfigXmlParser in activity. Adds CordovaWebView.init()
* Deprecate some convenience methods on CordovaActivity
* Fix CordovaPreferences not correctly parsing hex values (valueOf->decode)
* Refactor: Move url-filter information into PluginEntry.
* Don't re-parse config.xml in onResume.
* Move handling of Fullscreen preference to CordovaActivity
* Delete dead code from CordovaActivity
* Update .classpath to make Eclipse happy (just re-orders one line)
* Delete "CB-3064: The errorUrl is..." Log message left over from debugging presumably
* Refactor Config into ConfigXmlParser, CordovaPreferences
* Delete Location-change JS->Native bridge mode
* [CB-5988](https://issues.apache.org/jira/browse/CB-5988) Allow exec() only from file: or start-up URL's domain
* [CB-6761](https://issues.apache.org/jira/browse/CB-6761) Fix native->JS bridge ceasing to fire when page changes and online is set to false and the JS loads quickly
* Update the errorurl to no longer use intents
* This breaks running the JUnit tests, we'll bring it back soon
* Refactoring the URI handling on Cordova, removing dead code
* [CB-7018](https://issues.apache.org/jira/browse/CB-7018) Clean up and deprecation of some button-related functions
* [CB-7017](https://issues.apache.org/jira/browse/CB-7017) Fix onload=true being set on all subsequent plugins
* [CB-5971](https://issues.apache.org/jira/browse/CB-5971) Fix package / project validation
* [CB-5971](https://issues.apache.org/jira/browse/CB-5971) Add unit tests to cordova-android
* [CB-5971](https://issues.apache.org/jira/browse/CB-5971) Factor out package/project name validation logic
* Delete explicit activity.finish() in back button handling. No change in behaviour.
* [CB-5971](https://issues.apache.org/jira/browse/CB-5971) This would have been a good first bug, too bad
* [CB-4404](https://issues.apache.org/jira/browse/CB-4404) Changing where android:windowSoftInputMode is in the manifest so it works
* Add documentation referencing other implementation.
* [CB-6851](https://issues.apache.org/jira/browse/CB-6851) Deprecate WebView.sendJavascript()
* [CB-6876](https://issues.apache.org/jira/browse/CB-6876) Show the correct executable name
* [CB-6876](https://issues.apache.org/jira/browse/CB-6876) Fix the "print usage"
* Trivial spelling fix in comments when reading CordovaResourceApi
* [CB-6818](https://issues.apache.org/jira/browse/CB-6818) I want to remove this code, because Square didn't do their headers properly
* [CB-6860](https://issues.apache.org/jira/browse/CB-6860) Add activity_name and launcher_name to AndroidManifest.xml & strings.xml
* Add a comment to custom_rules.xml saying why we move AndroidManifest.xml
* Remove +x from README.md
* [CB-6784](https://issues.apache.org/jira/browse/CB-6784) Add missing licenses
* [CB-6784](https://issues.apache.org/jira/browse/CB-6784) Add license to CONTRIBUTING.md
* Revert "defaults.xml: Add AndroidLaunchMode preference"
* updated RELEASENOTES
* [CB-6315](https://issues.apache.org/jira/browse/CB-6315) Wrapping this so it runs on the UI thread
* [CB-6723](https://issues.apache.org/jira/browse/CB-6723) Update package name for Robotium
* [CB-6707](https://issues.apache.org/jira/browse/CB-6707) Update minSdkVersion to 10 consistently
* [CB-5652](https://issues.apache.org/jira/browse/CB-5652) make visible cordova version
* Update JS snapshot to version 3.6.0-dev (via coho)
* Update JS snapshot to version 3.6.0-dev (via coho)
* Set VERSION to 3.6.0-dev (via coho)

### 3.5.1 (August 2014) ###

This was a security update to address CVE-2014-3500, CVE-2014-3501,
and CVE-2014-3502. For more information, see
http://cordova.apache.org/announcements/2014/08/04/android-351.html

* Filter out non-launchable intents
* Handle unsupported protocol errors in webview better
* Update the errorurl to no longer use intents
* Refactoring the URI handling on Cordova, removing dead code

### 3.5.0 (May 2014) ###

* OkHttp has broken headers. Updating for ASF compliance.
* Revert accidentally removed lines from NOTICE
* [CB-6552](https://issues.apache.org/jira/browse/CB-6552) added top level package.json
* [CB-6491](https://issues.apache.org/jira/browse/CB-6491) add CONTRIBUTING.md
* [CB-6543](https://issues.apache.org/jira/browse/CB-6543) Fix cordova/run failure when no custom_rules.xml available
* defaults.xml: Add AndroidLaunchMode preference
* Add JavaDoc for CordovaResourceApi
* [CB-6388](https://issues.apache.org/jira/browse/CB-6388) Handle binary data correctly in LOAD_URL bridge
* Fix [CB-6048](https://issues.apache.org/jira/browse/CB-6048) Set launchMode=singleTop so tapping app icon does not always restart app
* Remove incorrect usage of AlertDialog.Builder.create
* Catch uncaught exceptions in from plugins and turn them into error responses.
* Add NOTICE file
* [CB-6047](https://issues.apache.org/jira/browse/CB-6047) Fix online sometimes getting in a bad state on page transitions.
* Add another convenience overload for CordovaResourceApi.copyResource
* Update framework's .classpath to what Eclipse wants it to be.
* README.md: `android update` to `android-19`.
* Fix NPE when POLLING bridge mode is used.
* Updating NOTICE to include Square for OkHttp
* [CB-5398](https://issues.apache.org/jira/browse/CB-5398) Apply KitKat content URI fix to all content URIs
* [CB-5398](https://issues.apache.org/jira/browse/CB-5398) Work-around for KitKat content: URLs not rendering in <img> tags
* [CB-5908](https://issues.apache.org/jira/browse/CB-5908) add splascreen images to template
* [CB-5395](https://issues.apache.org/jira/browse/CB-5395) Make scheme and host (but not path) case-insensitive in whitelist
* Ignore multiple onPageFinished() callbacks & onReceivedError due to stopLoading()
* Removing addJavascriptInterface support from all Android versions lower than 4.2 due to security vu
* [CB-4984](https://issues.apache.org/jira/browse/CB-4984) Don't create on CordovaActivity name
* [CB-5917](https://issues.apache.org/jira/browse/CB-5917) Add a loadUrlIntoView overload that doesn't recreate plugins.
* Use thread pool for load timeout.
* [CB-5715](https://issues.apache.org/jira/browse/CB-5715) For CLI, hide assets/www and res/xml/config.xml by default
* [CB-5793](https://issues.apache.org/jira/browse/CB-5793) ant builds: Rename AndroidManifest during -post-build to avoid Eclipse detecting ant-build/
* [CB-5889](https://issues.apache.org/jira/browse/CB-5889) Make update script find project name instead of using "null" for CordovaLib
* [CB-5889](https://issues.apache.org/jira/browse/CB-5889) Add a message in the update script about needing to import CordovaLib when using an IDE.

### 3.4.0 (Feb 2014) ###

43 commits from 10 authors. Highlights include:

* Removing addJavascriptInterface support from all Android versions lower than 4.2 due to security vulnerability
* [CB-5917](https://issues.apache.org/jira/browse/CB-5917) Add a loadUrlIntoView overload that doesn't recreate plugins.
* [CB-5889](https://issues.apache.org/jira/browse/CB-5889) Make update script find project name instead of using "null" for CordovaLib
* [CB-5889](https://issues.apache.org/jira/browse/CB-5889) Add a message in the update script about needing to import CordovaLib when using an IDE.
* [CB-5793](https://issues.apache.org/jira/browse/CB-5793) Don't clean before build and change output directory to ant-build to avoid conflicts with Eclipse.
* [CB-5803](https://issues.apache.org/jira/browse/CB-5803) Fix cordova/emulate on windows.
* [CB-5801](https://issues.apache.org/jira/browse/CB-5801) exec->spawn in build to make sure compile errors are shown.
* [CB-5799](https://issues.apache.org/jira/browse/CB-5799) Update version of OkHTTP to 1.3
* [CB-4910](https://issues.apache.org/jira/browse/CB-4910) Update CLI project template to point to config.xml at the root now that it's not in www/ by default.
* [CB-5504](https://issues.apache.org/jira/browse/CB-5504) Adding onDestroy to app plugin to deregister telephonyReceiver
* [CB-5715](https://issues.apache.org/jira/browse/CB-5715) Add Eclipse .project file to create template. For CLI projects, it adds refs for root www/ & config.xml and hides platform versions
* [CB-5447](https://issues.apache.org/jira/browse/CB-5447) Removed android:debuggable=“true” from project template.
* [CB-5714](https://issues.apache.org/jira/browse/CB-5714) Fix of android build when too big output stops build with error due to buffer overflow.
* [CB-5592](https://issues.apache.org/jira/browse/CB-5592) Set MIME type for openExternal when scheme is file:

### 3.3.0 (Dec 2013) ###

41 commits from 11 authors. Highlights include:

* [CB-5481](https://issues.apache.org/jira/browse/CB-5481) Fix for Cordova trying to get config.xml from the wrong namespace
* [CB-5487](https://issues.apache.org/jira/browse/CB-5487) Enable Remote Debugging when your Android app is debuggable.
* [CB-5445](https://issues.apache.org/jira/browse/CB-5445) Adding onScrollChanged and the ScrollEvent object
* [CB-5422](https://issues.apache.org/jira/browse/CB-5422) Don't require JAVA_HOME to be defined
* [CB-5490](https://issues.apache.org/jira/browse/CB-5490) Add javadoc target to ant script
* [CB-5471](https://issues.apache.org/jira/browse/CB-5471) Deprecated DroidGap class
* [CB-5255](https://issues.apache.org/jira/browse/CB-5255) Prefer Google API targets over android-## targets when building.
* [CB-5232](https://issues.apache.org/jira/browse/CB-5232) Change create script to use Cordova as a Library Project instead of a .jar
* [CB-5302](https://issues.apache.org/jira/browse/CB-5302) Massive movement to get tests working again
* [CB-4996](https://issues.apache.org/jira/browse/CB-4996) Fix paths with spaces while launching on emulator and device
* [CB-5209](https://issues.apache.org/jira/browse/CB-5209) Cannot build Android app if project path contains spaces


### 3.2.0 (Nov 2013) ###

27 commits from 7 authors. Highlights include:

* [CB-5193](https://issues.apache.org/jira/browse/CB-5193) Fix Android WebSQL sometime throwing SECURITY_ERR.
* [CB-5191](https://issues.apache.org/jira/browse/CB-5191) Deprecate <url-filter>
* Updating shelljs to 0.2.6. Copy now preserves mode bits.
* [CB-4872](https://issues.apache.org/jira/browse/CB-4872) Added android version scripts (android_sdk_version, etc)
* [CB-5117](https://issues.apache.org/jira/browse/CB-5117) Output confirmation message if check_reqs passes.
* [CB-5080](https://issues.apache.org/jira/browse/CB-5080) Find resources in a way that works with aapt's --rename-manifest-package
* [CB-4527](https://issues.apache.org/jira/browse/CB-4527) Don't delete .bat files even when on non-windows platform
* [CB-4892](https://issues.apache.org/jira/browse/CB-4892) Fix create script only escaping the first space instead of all spaces.

### 3.1.0 (Sept 2013) ###

55 commits from 9 authors. Highlights include:

* [CB-4817](https://issues.apache.org/jira/browse/CB-4817) Remove unused assets in project template.
* Fail fast in create script if package name is not com.foo.bar.
* [CB-4782](https://issues.apache.org/jira/browse/CB-4782) Convert ApplicationInfo.java -> appinfo.js
* [CB-4766](https://issues.apache.org/jira/browse/CB-4766) Deprecated JSONUtils.java (moved into plugins)
* [CB-4765](https://issues.apache.org/jira/browse/CB-4765) Deprecated ExifHelper.java (moved into plugins)
* [CB-4764](https://issues.apache.org/jira/browse/CB-4764) Deprecated DirectoryManager.java (moved into plugins)
* [CB-4763](https://issues.apache.org/jira/browse/CB-4763) Deprecated FileHelper.java (moved into plugins), Move getMimeType() into CordovaResourceApi.
* [CB-4725](https://issues.apache.org/jira/browse/CB-4725) Add CordovaWebView.CORDOVA_VERSION constant
* Incrementing version check for Android 4.3 API Level 18
* [CB-3542](https://issues.apache.org/jira/browse/CB-3542) rewrote cli tooling scripts in node
* Allow CordovaChromeClient subclasses access to CordovaInterface and CordovaWebView members
* Refactor CordovaActivity.init so that subclasses can easily override factory methods for webview objects
* [CB-4652](https://issues.apache.org/jira/browse/CB-4652) Allow default project template to be overridden on create
* Tweak the online bridge to not send excess online events.
* [CB-4495](https://issues.apache.org/jira/browse/CB-4495) Modify start-emulator script to exit immediately on a fatal emulator error.
* Log WebView IOExceptions only when they are not 404s
* Use a higher threshold for slow exec() warnings when debugger is attached.
* Fix data URI decoding in CordovaResourceApi
* [CB-3819](https://issues.apache.org/jira/browse/CB-3819) Made it easier to set SplashScreen delay.
* [CB-4013](https://issues.apache.org/jira/browse/CB-4013) Fixed loadUrlTimeoutValue preference.
* Upgrading project to Android 4.3
* [CB-4198](https://issues.apache.org/jira/browse/CB-4198) bin/create script should be better at handling non-word characters in activity name. Patched windows script as well.
* [CB-4198](https://issues.apache.org/jira/browse/CB-4198) bin/create should handle spaces in activity better.
* [CB-4096](https://issues.apache.org/jira/browse/CB-4096) Implemented new unified whitelist for android
* [CB-3384](https://issues.apache.org/jira/browse/CB-3384) Fix thread assertion when plugins remap URIs

