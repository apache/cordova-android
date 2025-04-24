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
## Release Notes for Cordova (Android)

### 14.0.1 (Apr 24, 2025)

**Fixes:**

* [GH-1795](https://github.com/apache/cordova-android/pull/1795) fix: configure gradle `java.home`
* [GH-1793](https://github.com/apache/cordova-android/pull/1793) fix(windows): get gradle path with `which` command

### 14.0.0 (Mar 23, 2025)

**Breaking Changes:**

* [GH-1788](https://github.com/apache/cordova-android/pull/1788) dep!: bump npm packages
  * nyc@17.1.0
  * which@5.0.0
  * semver@7.7.1
  * jasmine@5.6.0
  * android-versions@2.1.0
  * cordova-common@5.0.1
  * fast-glob@3.3.3
  * nopt@8.1.0
* [GH-1789](https://github.com/apache/cordova-android/pull/1789) feat!: bump node engine requirement `>=20.5.0`
* [GH-1784](https://github.com/apache/cordova-android/pull/1784) feat!: bump java default targets to 11
* [GH-1771](https://github.com/apache/cordova-android/pull/1771) feat!: deprecate CordovaPlugin's method initialize
* [GH-1767](https://github.com/apache/cordova-android/pull/1767) feat!: use kotlin-stdlib instead of kotlin-stdlib-jdk*
* [GH-1763](https://github.com/apache/cordova-android/pull/1763) feat!: SDK 35 Support

**Features:**

* [GH-1785](https://github.com/apache/cordova-android/pull/1785) feat: bump gradle to 8.13
* [GH-1779](https://github.com/apache/cordova-android/pull/1779) feat: add `AndroidEdgeToEdge` preference & theme flag
* [GH-1778](https://github.com/apache/cordova-android/pull/1778) feat: Account for Node security patch
* [GH-1768](https://github.com/apache/cordova-android/pull/1768) feat: `androidx.core:core-splashscreen@1.0.1`
* [GH-1766](https://github.com/apache/cordova-android/pull/1766) feat: `com.google.gms:google-services@4.4.2`
* [GH-1765](https://github.com/apache/cordova-android/pull/1765) feat: `androidx.webkit:webkit@1.12.1`
* [GH-1764](https://github.com/apache/cordova-android/pull/1764) feat: `androidx.appcompat:appcompat@1.7.0`

**Fixes:**

* [GH-1790](https://github.com/apache/cordova-android/pull/1790) fix: replace fs-extra.ensureFileSync with fs.writeFileSync
* [GH-1781](https://github.com/apache/cordova-android/pull/1781) fix: copy gradle wrapper from tools to platform root dir
* [GH-1770](https://github.com/apache/cordova-android/pull/1770) fix: creation of cdv-gradle-config.json w/ --link flag
* [GH-1739](https://github.com/apache/cordova-android/pull/1739) fix(docs): Incorrect JDK requirement stated in README
* [GH-1718](https://github.com/apache/cordova-android/pull/1718) fix: app restart when BT keyboard is connected in some devices

**Chores & Refactoring:**

* [GH-1786](https://github.com/apache/cordova-android/pull/1786) chore: add AndroidX build test to gitignore
* [GH-1774](https://github.com/apache/cordova-android/pull/1774) style: update & resolve doc block warnings
* [GH-1772](https://github.com/apache/cordova-android/pull/1772) refactor: replace fs-extra with node:fs
* [GH-1769](https://github.com/apache/cordova-android/pull/1769) refactor: prefix node:*
* [GH-1748](https://github.com/apache/cordova-android/pull/1748) chore(deps): bump cross-spawn from 7.0.3 to 7.0.6
* [GH-1750](https://github.com/apache/cordova-android/pull/1750) chore(ci): Fix dependabot PR failures
* [GH-1736](https://github.com/apache/cordova-android/pull/1736) chore(deps): bump micromatch from 4.0.5 to 4.0.8
* [GH-1716](https://github.com/apache/cordova-android/pull/1716) chore(deps): bump braces from 3.0.2 to 3.0.3

### 13.0.0 (May 15, 2024)

**Breaking Changes:**

* [GH-1678](https://github.com/apache/cordova-android/pull/1678) feat!: API 34 Support
* [GH-1543](https://github.com/apache/cordova-android/pull/1543) feat!: bump `kotlin@1.9.24` & drop `kotlin-android-extensions` when kotlin `>=1.8.0`

**Features:**

* [GH-1700](https://github.com/apache/cordova-android/pull/1700) feat(splash): Support `SplashScreenBackgroundColor` preference
* [GH-1609](https://github.com/apache/cordova-android/pull/1609) feat: add camera intent with file input capture
* [GH-1696](https://github.com/apache/cordova-android/pull/1696) feat: Add `ResolveServiceWorkerRequests` preference

**Chores, Dependencies & CI:**

* [GH-1677](https://github.com/apache/cordova-android/pull/1677) chore(deps-dev): bump `@babel/traverse` from `7.22.10` to `7.23.2`
* [GH-1713](https://github.com/apache/cordova-android/pull/1713) dep: bump npm dependencies 20240515
  * `semver@7.6.2`
  * `rewire@7.0.0`
  * `nopt@7.2.1`
  * `jasmine@5.1.0`
  * `fs-extra@11.2.0`
  * `fast-glob@3.3.2`
  * `dedent@1.5.3`
  * `@cordova/eslint-config@5.1.0`
  * `which@4.0.0`
  * `properties-parser@0.6.0`
  * `android-versions@2.0.0`
* [GH-1711](https://github.com/apache/cordova-android/pull/1711) ci: Set up CodeQL analysis w/ fixes
* [GH-1687](https://github.com/apache/cordova-android/pull/1687) ci(release-audit): add license header and dependency checker
* [GH-1703](https://github.com/apache/cordova-android/pull/1703) ci: update `codecov@v4` w/ token

### 12.0.1 (Aug 23, 2023)

* [GH-1632](https://github.com/apache/cordova-android/pull/1632) fix(android): `monochrome` checks
* [GH-1649](https://github.com/apache/cordova-android/pull/1649) chore: rebuild `package-lock` w/ lint corrections

### 12.0.0 (May 20, 2023)

**Breaking:**

* [GH-1605](https://github.com/apache/cordova-android/pull/1605) fix!: Make `CoreAndroid` plugin instantiate on load
* [GH-1539](https://github.com/apache/cordova-android/pull/1539) feat!: bump Gradle 7.6 & AGP 7.4.2
* [GH-1571](https://github.com/apache/cordova-android/pull/1571) feat!: bump min SDK to 24
* [GH-1538](https://github.com/apache/cordova-android/pull/1538) feat!: bump target sdk & build tools for SDK 33 support
* [GH-1540](https://github.com/apache/cordova-android/pull/1540) feat!: bump node engine requirement `>=16.13.0`
* [GH-1597](https://github.com/apache/cordova-android/pull/1597) deprecate: `CoreAndroid.getBuildConfigValue`
* [GH-1541](https://github.com/apache/cordova-android/pull/1541) dep(npm)!: bump acceptable modules w/ rebuilt `package-lock`
* [GH-1566](https://github.com/apache/cordova-android/pull/1566) dep(npm)!: bump `cordova-common@5.0.0`

**Features:**

* [GH-1602](https://github.com/apache/cordova-android/pull/1602) feat: add `listTarget` api
* [GH-1574](https://github.com/apache/cordova-android/pull/1574) feat: add plugin hooks for `WebViewClient.onRenderProcessGone`
* [GH-1594](https://github.com/apache/cordova-android/pull/1594) feat: bump default `kotlin` to version 1.7.21
* [GH-1550](https://github.com/apache/cordova-android/pull/1550) feat: add `monochrome` app icon support
* [GH-1589](https://github.com/apache/cordova-android/pull/1589) feat: `InspectableWebview` preference
* [GH-1568](https://github.com/apache/cordova-android/pull/1568) feat: bump `androidx.appcompat.appcompat` 1.6.1
* [GH-1567](https://github.com/apache/cordova-android/pull/1567) feat: bump `androidx.webkit.webkit` 1.6.0
* [GH-1545](https://github.com/apache/cordova-android/pull/1545) feat: bump `androidx.webkit.webkit` 1.5.0
* [GH-1547](https://github.com/apache/cordova-android/pull/1547) feat: bump `com.google.gms.google-services` 4.3.15
* [GH-1546](https://github.com/apache/cordova-android/pull/1546) feat: bump `androidx.core.core-splashscreen` 1.0.0
* [GH-1544](https://github.com/apache/cordova-android/pull/1544) feat: bump `androidx.appcompat.appcompat` 1.5.1

**Fixes:**

* [GH-1606](https://github.com/apache/cordova-android/pull/1606) fix: Gradle Args parsing
* [GH-1575](https://github.com/apache/cordova-android/pull/1575) fix(`BuildHelper`): get package name from `ApplicationInfo`
* [GH-1595](https://github.com/apache/cordova-android/pull/1595) fix(test): Native test namespace refactor
* [GH-1471](https://github.com/apache/cordova-android/pull/1471) fix: `ANDROID_HOME` is the new default, to check first and give advice
* [GH-1573](https://github.com/apache/cordova-android/pull/1573) fix(GH-1432): Default `content` `src` when content tag is missing
* [GH-1506](https://github.com/apache/cordova-android/pull/1506) fix: only do fadeout animation if `FadeSplashScreen` is true
* [GH-1505](https://github.com/apache/cordova-android/pull/1505) fix: correctly flag API dependency on `AppCompat` for Maven
* [GH-1487](https://github.com/apache/cordova-android/pull/1487) fix: Add **Android** prefix to `WindowSplashScreenBrandingImage`
* [GH-1489](https://github.com/apache/cordova-android/pull/1489) fix: import type definitions from obsolete `cordova-plugin-splashscreen`

**Chores, Refactor,  Dependencies & CI:**

* [GH-1493](https://github.com/apache/cordova-android/pull/1493) chore: add `lint:fix` script for fixing lint errors
* [GH-1491](https://github.com/apache/cordova-android/pull/1491) chore: Use gradle 7.4.2 distribution url
* [GH-1588](https://github.com/apache/cordova-android/pull/1588) refactor: Removed obsolete version code checks
* [GH-1492](https://github.com/apache/cordova-android/pull/1492) refactor: replace deprecated `Handler` constructor
* [GH-1587](https://github.com/apache/cordova-android/pull/1587) dep: bump npm dependencies
  * `fs-extra@11.1.1`
  * `nopt@7.1.0`
  * `@cordova/eslint-config@5.0.0`
  * `jasmine@4.6.0`
* [GH-1607](https://github.com/apache/cordova-android/pull/1607) ci: Added NodeJS 20.x to the workflow matrix
* [GH-1542](https://github.com/apache/cordova-android/pull/1542) ci(workflow): update `codecov/codecov-action@v3`
* [GH-1532](https://github.com/apache/cordova-android/pull/1532) ci: update `codecov/codecov-action` reporting format

### 11.0.0 (Jul 04, 2022)

**Breaking:**

* [GH-1441](https://github.com/apache/cordova-android/pull/1441) feat!: **Android** 12 splash screen
* [GH-1427](https://github.com/apache/cordova-android/pull/1427) feat!: API 32 support
* [GH-1410](https://github.com/apache/cordova-android/pull/1410) feat!: API 31 support
* [GH-1444](https://github.com/apache/cordova-android/pull/1444) fix!: set & use `ANDROID_HOME` as default
* [GH-1411](https://github.com/apache/cordova-android/pull/1411) chore!: Drop Node 12 support

**Features:**

* [GH-1448](https://github.com/apache/cordova-android/pull/1448) feat: Update `androidx.appcompat` version
* [GH-1446](https://github.com/apache/cordova-android/pull/1446) feat: Update gradle plugin version
* [GH-1447](https://github.com/apache/cordova-android/pull/1447) feat: Update google services pluging
* [GH-1431](https://github.com/apache/cordova-android/pull/1431) feat: support custom `compileSdk` setting
* [GH-1311](https://github.com/apache/cordova-android/pull/1311) feat: added support for BoM imports

**Fixes:**

* [GH-1455](https://github.com/apache/cordova-android/pull/1455) fix(`prepare`): `destFile` path separator
* [GH-1453](https://github.com/apache/cordova-android/pull/1453) fix: support installing platfrom from local git checkout
* [GH-1449](https://github.com/apache/cordova-android/pull/1449) fix: accept file cookies only if `AndroidInsecureFileModeEnabled`
* [GH-1443](https://github.com/apache/cordova-android/pull/1443) fix: force `hostname` to lowercase
* [GH-1434](https://github.com/apache/cordova-android/pull/1434) fix: restore `checkReqs` in `prepare.js`
* [GH-1154](https://github.com/apache/cordova-android/pull/1154) fix: move `MainActivity.java` to folder that tracks the app package name (widget id)

**Chores, Dependencies & CI:**

* [GH-1451](https://github.com/apache/cordova-android/pull/1451) chore: display warning on deprecated `<splash>` tag usage
* [GH-1430](https://github.com/apache/cordova-android/pull/1430) chore: remove unneeded deprecated annotation
* [GH-1421](https://github.com/apache/cordova-android/pull/1421) chore(npm): bump `@cordova/eslint-config@^4.0.0`
* [GH-1420](https://github.com/apache/cordova-android/pull/1420) chore(npm): bump dependencies
* [GH-1452](https://github.com/apache/cordova-android/pull/1452) dep: bump `jasmine@4.2.1` w/ `package-lock` rebuild
* [GH-1439](https://github.com/apache/cordova-android/pull/1439) ci: update github action workflow
* [GH-1424](https://github.com/apache/cordova-android/pull/1424) ci: Added Node 18 to test matrix

### 10.1.2 (Apr 11, 2022)

**Fixes:**

* [GH-1372](https://github.com/apache/cordova-android/pull/1372) fix(`AndroidManifest`): explicitly define the `activity` attribute `android:exported`
* [GH-1406](https://github.com/apache/cordova-android/pull/1406) fix: detect `JAVA_HOME` with Java 11
* [GH-1401](https://github.com/apache/cordova-android/pull/1401) fix(GH-1391): Reword minimum build tools version to make it more clear what is actually required.
* [GH-1384](https://github.com/apache/cordova-android/pull/1384) fix: escape `strings.xml` app name

**Chores:**

* [GH-1413](https://github.com/apache/cordova-android/pull/1413) chore: update `package-lock` to satisfy `npm audit`
* [GH-1348](https://github.com/apache/cordova-android/pull/1348) chore: `npmrc`

### 10.1.1 (Sep 13, 2021)

**Fixes:**

* [GH-1349](https://github.com/apache/cordova-android/pull/1349) fix(`PluginManager`): `AllowNavigation` default policy to handle scheme & hostname
* [GH-1342](https://github.com/apache/cordova-android/pull/1342) fix(`AllowListPlugin`): Safely handle default allow navigation policy in allow request
* [GH-1332](https://github.com/apache/cordova-android/pull/1332) fix(`PluginManager`): `AllowBridgeAccess` default policy to handle scheme & hostname

### 10.1.0 (Aug 13, 2021)

**Features:**

* [GH-1213](https://github.com/apache/cordova-android/pull/1213) feat: unify `create` default values & stop project name transform
* [GH-1306](https://github.com/apache/cordova-android/pull/1306) feat: bump `ANDROIDX_APP_COMPAT@1.3.1`
* [GH-1303](https://github.com/apache/cordova-android/pull/1303) feat: bump `Google Services Gradle Plugin@4.3.8`
* [GH-1302](https://github.com/apache/cordova-android/pull/1302) feat: bump `kotlin@1.5.21`
* [GH-1298](https://github.com/apache/cordova-android/pull/1298) feat: support `http` w/ `content` `src` fix

**Fixes:**

* [GH-1214](https://github.com/apache/cordova-android/pull/1214) fix: display project name in Android Studio
* [GH-1300](https://github.com/apache/cordova-android/pull/1300) fix: fall back to project root `repositories.gradle`

**Docs:**

* [GH-1308](https://github.com/apache/cordova-android/pull/1308) doc: update `README` about development & testing

### 10.0.1 (Jul 27, 2021)

**Fixes:**

* [GH-1295](https://github.com/apache/cordova-android/pull/1295) fix: `maven-publish` setup
* [GH-1293](https://github.com/apache/cordova-android/pull/1293) fix: `gradle` build tools config
* [GH-1294](https://github.com/apache/cordova-android/pull/1294) fix: automatic latest build tools finding
* [GH-1287](https://github.com/apache/cordova-android/pull/1287) fix: Google Services Gradle Plugin version check failure

**Chores:**

* [GH-1291](https://github.com/apache/cordova-android/pull/1291) chore: add missing release notes
* [GH-1286](https://github.com/apache/cordova-android/pull/1286) chore: update `README` requirements

### 10.0.0 (Jul 17, 2021)

**Breaking:**

* [GH-1052](https://github.com/apache/cordova-android/pull/1052) feat!: only support `AndroidX`
* [GH-1137](https://github.com/apache/cordova-android/pull/1137) feat!: implement `WebViewAssetLoader`
* [GH-1268](https://github.com/apache/cordova-android/pull/1268) feat!: release build defaults to `aab` package type
* [GH-1182](https://github.com/apache/cordova-android/pull/1182) feat!: bump `target sdk@30` w/ `build-tool@30.0.3`
* [GH-1257](https://github.com/apache/cordova-android/pull/1257) feat!: upgrade `gradle@7.1.1`
* [GH-1197](https://github.com/apache/cordova-android/pull/1197) feat!: upgrade `gradle@6.8.3`
* [GH-1256](https://github.com/apache/cordova-android/pull/1256) feat!: upgrade `kotlin@1.5.20`
* [GH-1204](https://github.com/apache/cordova-android/pull/1204) feat!: upgrade `kotlin@1.4.32`
* [GH-1200](https://github.com/apache/cordova-android/pull/1200) feat!: upgrade `kotlin@1.4.31`
* [GH-1255](https://github.com/apache/cordova-android/pull/1255) feat!: upgrade `android-gradle-plugin@4.2.2`
* [GH-1232](https://github.com/apache/cordova-android/pull/1232) feat!: upgrade `android-gradle-plugin@4.2.1`
* [GH-1198](https://github.com/apache/cordova-android/pull/1198) feat!: upgrade `android-gradle-plugin@4.1.3`
* [GH-1199](https://github.com/apache/cordova-android/pull/1199) feat!: upgrade `Google Services Gradle Plugin@4.3.5`
* [GH-1262](https://github.com/apache/cordova-android/pull/1262) feat!: bump `appcompat@1.3.0`
* [GH-1258](https://github.com/apache/cordova-android/pull/1258) feat!: bump `android.webkit@1.4.0`
* [GH-1252](https://github.com/apache/cordova-android/pull/1252) feat!: drop abandoned `com.github.dcendents:android-maven-gradle-plugin`
* [GH-1212](https://github.com/apache/cordova-android/pull/1212) feat!: unify & fix gradle library/tooling overrides
* [GH-1138](https://github.com/apache/cordova-android/pull/1138) feat(allow-list)!: integrate and refactor core plugin
* [GH-1201](https://github.com/apache/cordova-android/pull/1201) feat!: upgrade jfrog `gradle-bintray-plugin@1.8.5`
* [GH-1279](https://github.com/apache/cordova-android/pull/1279) chore!: bump all dependencies
* [GH-1278](https://github.com/apache/cordova-android/pull/1278) chore!: drop `node` 10 support
* [GH-1205](https://github.com/apache/cordova-android/pull/1205) chore! (`npm`): update all dependencies
* [GH-1274](https://github.com/apache/cordova-android/pull/1274) cleanup!: remove deprecated settings & add todo comments
* [GH-1048](https://github.com/apache/cordova-android/pull/1048) cleanup!: remove `keystore` password prompt
* [GH-1251](https://github.com/apache/cordova-android/pull/1251) cleanup!: drop `jcenter` & update dependencies
* [GH-1269](https://github.com/apache/cordova-android/pull/1269) refactor!: do not copy JS lib to platform project
* [GH-1270](https://github.com/apache/cordova-android/pull/1270) refactor(Api)!: use version from `package.json`
* [GH-1266](https://github.com/apache/cordova-android/pull/1266) refactor(run)!: `run` method
* [GH-1083](https://github.com/apache/cordova-android/pull/1083) refactor!: drop support for `android` SDK tool
* [GH-1100](https://github.com/apache/cordova-android/pull/1100) refactor!: remove most platform binaries

**Features:**

* [GH-1241](https://github.com/apache/cordova-android/pull/1241) feat: remove `java` 1.8 version check
* [GH-1254](https://github.com/apache/cordova-android/pull/1254) feat: support `webkit` version override
* [GH-1229](https://github.com/apache/cordova-android/pull/1229) feat: `CORDOVA_JAVA_HOME` env variable
* [GH-1222](https://github.com/apache/cordova-android/pull/1222) feat: add backwards compatibility mode for `WebViewAssetLoader`
* [GH-1166](https://github.com/apache/cordova-android/pull/1166) feat: overload `PluginEntry` constructor to set onload property
* [GH-1208](https://github.com/apache/cordova-android/pull/1208) feat: allow `appcompat` version to be configurable
* [GH-1047](https://github.com/apache/cordova-android/pull/1047) feat: Deprecated `onRequestPermissionResult` in favour for `onRequestPermissionsResult` for consistency

**Fixes:**

* [GH-1283](https://github.com/apache/cordova-android/pull/1283) fix: add missing apache-license header to `getASPath.bat`
* [GH-1275](https://github.com/apache/cordova-android/pull/1275) fix: add `WebViewAssetloader` to default allow list
* [GH-1216](https://github.com/apache/cordova-android/pull/1216) fix: request focus after custom view hided
* [GH-1264](https://github.com/apache/cordova-android/pull/1264) fix: missing `super.onRequestPermissionsResult` error (`MissingSuperCall`)
* [GH-563](https://github.com/apache/cordova-android/pull/563) fix(build): support tilde expansion on Windows
* [GH-1220](https://github.com/apache/cordova-android/pull/1220) fix(`requirements` check): use regex to get java version from javac output
* [GH-1227](https://github.com/apache/cordova-android/pull/1227) fix(prepare): delete splash screens if none are used
* [GH-1228](https://github.com/apache/cordova-android/pull/1228) fix: java checks
* [GH-1276](https://github.com/apache/cordova-android/pull/1276) fix: remove forced default `gradle.daemon` setting

**Refactors:**

* [GH-1265](https://github.com/apache/cordova-android/pull/1265) refactor: do not infer project root from script location
* [GH-1267](https://github.com/apache/cordova-android/pull/1267) refactor: use target SDK of built APK to determine best emulator
* [GH-1253](https://github.com/apache/cordova-android/pull/1253) refactor: `gradle` cleanup
* [GH-1260](https://github.com/apache/cordova-android/pull/1260) refactor(`check_reqs`): drop `originalError` param from `check_android_target`
* [GH-1246](https://github.com/apache/cordova-android/pull/1246) refactor(`env/java`): improve tests and implementation

**Chores & Cleanup:**

* [GH-1273](https://github.com/apache/cordova-android/pull/1273) chore: remove old `VERSION` file
* [GH-1272](https://github.com/apache/cordova-android/pull/1272) cleanup: delete old ANT & Eclipse files
* [GH-1141](https://github.com/apache/cordova-android/pull/1141) cleanup: remove app cache settings

**CI, Build & Testing:**

* [GH-1218](https://github.com/apache/cordova-android/pull/1218) ci: Add `Node16` to CI matrix
* [GH-1271](https://github.com/apache/cordova-android/pull/1271) build: build `cordova.js` during npm prepare
* [GH-1207](https://github.com/apache/cordova-android/pull/1207) test(`AndroidManifest`): update theme to `Theme.AppCompat.NoActionBar`
* [GH-1263](https://github.com/apache/cordova-android/pull/1263) test(`check_reqs`): do not hardcode `DEFAULT_TARGET_API`
* [GH-1259](https://github.com/apache/cordova-android/pull/1259) test(`prepare`): factor out common vars

### 9.1.0 (Apr 09, 2021)

**Features:**

* [GH-1104](https://github.com/apache/cordova-android/pull/1104) feat: support `gzip` encoding requests & use `GZIPInputStream`
* [GH-1167](https://github.com/apache/cordova-android/pull/1167) feat: handle `intent://` scheme links with `browser_fallback_url` param
* [GH-1179](https://github.com/apache/cordova-android/pull/1179) feat: add `repositories` support
* [GH-1173](https://github.com/apache/cordova-android/pull/1173) feat(android-studio): display app name as project name
* [GH-1113](https://github.com/apache/cordova-android/pull/1113) feat: `webp` support for splashscreen
* [GH-1125](https://github.com/apache/cordova-android/pull/1125) feat(Adb): list `devices` _and_ `emulators` in one go

**Fixes:**

* [GH-1186](https://github.com/apache/cordova-android/pull/1186) fix: copy `repositories.gradle` to project on create
* [GH-1184](https://github.com/apache/cordova-android/pull/1184) fix: unit-test failure
* [GH-733](https://github.com/apache/cordova-android/pull/733) fix(splashscreen): nav & title bar showing in fullscreen mode
* [GH-1157](https://github.com/apache/cordova-android/pull/1157) fix: restore key event handlers when DOM element is fullscreen
* [GH-1073](https://github.com/apache/cordova-android/pull/1073) fix(android): Avoid Crash Report: ConcurrentModificationException
* [GH-1148](https://github.com/apache/cordova-android/pull/1148) fix: add not null checks to prevent running on destroyed activity
* [GH-1091](https://github.com/apache/cordova-android/pull/1091) fix: concurrent modification exception (#924)
* [GH-1153](https://github.com/apache/cordova-android/pull/1153) fix: optional arch parameter
* [GH-1136](https://github.com/apache/cordova-android/pull/1136) fix(prepare): `mapImageResources` always returning `[]`
* [GH-1111](https://github.com/apache/cordova-android/pull/1111) fix(android): allow file access for existing behavior
* [GH-1045](https://github.com/apache/cordova-android/pull/1045) fix: Reflect minimum required NodeJS
* [GH-1084](https://github.com/apache/cordova-android/pull/1084) fix(prepare): fix pattern used to collect image resources
* [GH-1014](https://github.com/apache/cordova-android/pull/1014) fix(`pluginHandlers`): properly check if path is inside another
* [GH-1018](https://github.com/apache/cordova-android/pull/1018) fix: gradle ignore properties
* [GH-1185](https://github.com/apache/cordova-android/pull/1185) fix(regression): Cannot read version of undefined caused by Java refactor
* [GH-1117](https://github.com/apache/cordova-android/pull/1117) fix: allow changing min sdk version

**Refactors:**

* [GH-1101](https://github.com/apache/cordova-android/pull/1101) refactor: unify target resolution for devices & emulators
* [GH-1130](https://github.com/apache/cordova-android/pull/1130) refactor: java checks
* [GH-1099](https://github.com/apache/cordova-android/pull/1099) refactor(`ProjectBuilder`): clean up output file collection code
* [GH-1123](https://github.com/apache/cordova-android/pull/1123) refactor: unify installation on devices & emulators
* [GH-1102](https://github.com/apache/cordova-android/pull/1102) refactor(`check_reqs`): cleanup default Java location detection on **Windows**
* [GH-1103](https://github.com/apache/cordova-android/pull/1103) refactor: do not kill adb on UNIX-like systems
* [GH-1086](https://github.com/apache/cordova-android/pull/1086) refactor(retry): simplify retryPromise using modern JS
* [GH-1085](https://github.com/apache/cordova-android/pull/1085) refactor(utils): reduce number of utils
* [GH-1046](https://github.com/apache/cordova-android/pull/1046) refactor: Stop suppressing un-needed TruelyRandom lints
* [GH-1016](https://github.com/apache/cordova-android/pull/1016) refactor: save `ProjectBuilder` instance in Api instance
* [GH-1108](https://github.com/apache/cordova-android/pull/1108) refactor: remove copied Adb.install from `emulator.install`

**Chores:**

* [GH-1196](https://github.com/apache/cordova-android/pull/1196) chore: add missing header license
* chore(asf): Update GitHub repo metadata
* [GH-1183](https://github.com/apache/cordova-android/pull/1183) chore: rebuilt package-lock
* [GH-1015](https://github.com/apache/cordova-android/pull/1015) chore: remove unnecessary stuff
* [GH-1081](https://github.com/apache/cordova-android/pull/1081) chore(pkg): remove deprecated `no-op` field `"engineStrict"`
* [GH-1019](https://github.com/apache/cordova-android/pull/1019) chore: remove unused `emulator.create_image` and its dependencies

**Tests & CI:**

* [GH-1017](https://github.com/apache/cordova-android/pull/1017) test(java): fix, improve and move clean script
* [GH-1012](https://github.com/apache/cordova-android/pull/1012) test: fix missing stack traces in jasmine output
* [GH-1013](https://github.com/apache/cordova-android/pull/1013) test(`pluginHandlers/common`): better setup & teardown
* [GH-1094](https://github.com/apache/cordova-android/pull/1094) test: fix unit test failures for certain random orders
* [GH-1094](https://github.com/apache/cordova-android/pull/1094) test: ensure single top-level describe block in test file
* [GH-1129](https://github.com/apache/cordova-android/pull/1129) test(java): remove duplicate code in `BackButtonMultipageTest`
* [GH-975](https://github.com/apache/cordova-android/pull/975) ci: Added Node 14.x

### 9.0.0 (Jun 23, 2020)

* [GH-1005](https://github.com/apache/cordova-android/pull/1005) chore: set AndroidX off by default
* [GH-971](https://github.com/apache/cordova-android/pull/971) fix: Accept multiple mime types on file input
* [GH-1001](https://github.com/apache/cordova-android/pull/1001) fix: support both adaptive and standard icons at the same time
* [GH-985](https://github.com/apache/cordova-android/pull/985) fix: Plugin install fails when preview sdk is installed
* [GH-994](https://github.com/apache/cordova-android/pull/994) chore: cleanup yaml files
* [GH-999](https://github.com/apache/cordova-android/pull/999) chore: remove trailing spaces from Java sources
* [GH-992](https://github.com/apache/cordova-android/pull/992) chore: update some dependencies
* [GH-998](https://github.com/apache/cordova-android/pull/998) chore: remove trailing spaces from framework build files
* [GH-997](https://github.com/apache/cordova-android/pull/997) chore: remove trailing spaces from project template
* [GH-996](https://github.com/apache/cordova-android/pull/996) chore: remove trailing spaces from bat files
* [GH-995](https://github.com/apache/cordova-android/pull/995) remove trailing spaces from markdown files
* [GH-993](https://github.com/apache/cordova-android/pull/993) chore: update `devDependencies`
* [GH-987](https://github.com/apache/cordova-android/pull/987) breaking: reduce combined response cutoff to 16 MB
* [GH-988](https://github.com/apache/cordova-android/pull/988) major: Gradle 6.5 & **Android** Gradle plugin 4.0.0 updates
* [GH-990](https://github.com/apache/cordova-android/pull/990) chore: remove trailing spaces from `app/build.gradle`
* [GH-989](https://github.com/apache/cordova-android/pull/989) breaking: remove `legacy/build.gradle` from template
* [GH-978](https://github.com/apache/cordova-android/pull/978) fix: `wait_for_boot` waiting forever
* [GH-965](https://github.com/apache/cordova-android/pull/965) fix: Increased `detectArchitecture()` timeout
* [GH-962](https://github.com/apache/cordova-android/pull/962) breaking: Bump **Android** gradle plugin to 3.6.0
* [GH-948](https://github.com/apache/cordova-android/pull/948) feature: JVM Args flag
* [GH-951](https://github.com/apache/cordova-android/pull/951) fix: `ANDROID_SDK_ROOT` variable
* [GH-959](https://github.com/apache/cordova-android/pull/959) test: synced AndroidX gradle versions to the same version as the **Android** test
* [GH-960](https://github.com/apache/cordova-android/pull/960) feat: `com.android.tools.build:gradle:3.5.3`
* [GH-956](https://github.com/apache/cordova-android/pull/956) chore(npm): add `package-lock.json`
* [GH-958](https://github.com/apache/cordova-android/pull/958) chore(npm): add ignore list
* [GH-957](https://github.com/apache/cordova-android/pull/957) chore: various cleanup
* [GH-955](https://github.com/apache/cordova-android/pull/955) chore(eslint): bump package & apply eslint fix
* [GH-954](https://github.com/apache/cordova-android/pull/954) breaking(npm): bump packages
* [GH-953](https://github.com/apache/cordova-android/pull/953) chore(npm): use short notation in `package.json`
* [GH-823](https://github.com/apache/cordova-android/pull/823) fix: prevent exit fullscreen mode from closing application
* [GH-950](https://github.com/apache/cordova-android/pull/950) fix: removed redundent logcat print
* [GH-915](https://github.com/apache/cordova-android/pull/915) breaking: bump minSdkVersion to 22 and drop pre-Lollipop specific code
* [GH-941](https://github.com/apache/cordova-android/pull/941) fix: GH-873 App bundle builds to obey command-line arguments
* [GH-940](https://github.com/apache/cordova-android/pull/940) ci: drop travis & move codecov to gh-actions
* [GH-929](https://github.com/apache/cordova-android/pull/929) chore: updated `README` to reflect what **Android** requires more accurately, which is Java 8, not anything less, not anything greater. Java 1.8.x is required.
* [GH-937](https://github.com/apache/cordova-android/pull/937) fix: GH-935 replaced `compare-func` with native sort method
* [GH-939](https://github.com/apache/cordova-android/pull/939) fix: test failure with shebang interpreter in `rewired` files
* [GH-911](https://github.com/apache/cordova-android/pull/911) refactor: use es6 class
* [GH-910](https://github.com/apache/cordova-android/pull/910) refactor (eslint): use `cordova-eslint`
* [GH-909](https://github.com/apache/cordova-android/pull/909) chore: remove appveyor residual
* [GH-895](https://github.com/apache/cordova-android/pull/895) feat: add github actions
* [GH-842](https://github.com/apache/cordova-android/pull/842) refactor: remove `shelljs` dependency
* [GH-896](https://github.com/apache/cordova-android/pull/896) feat: add Kotlin support
* [GH-901](https://github.com/apache/cordova-android/pull/901) feat: add AndroidX support
* [GH-849](https://github.com/apache/cordova-android/pull/849) fix: cordova requirements consider the `android-targetSdkVersion`
* [GH-904](https://github.com/apache/cordova-android/pull/904) fix (adb): shell to return expected stdout
* [GH-792](https://github.com/apache/cordova-android/pull/792) feat: upgrade `gradle` to 6.1 & gradle build tools to 3.5.3
* [GH-902](https://github.com/apache/cordova-android/pull/902) chore: remove `.project` file & add `.settings` to `gitignore`
* [GH-900](https://github.com/apache/cordova-android/pull/900) refactor: simplify `doFindLatestInstalledBuildTools`
* [GH-751](https://github.com/apache/cordova-android/pull/751) feat: use Java package name for loading `BuildConfig`
* [GH-898](https://github.com/apache/cordova-android/pull/898) chore: rename gradle plugin google services `preference` options
* [GH-893](https://github.com/apache/cordova-android/pull/893) feat: add Google Services support
* [GH-709](https://github.com/apache/cordova-android/pull/709) feat: add `version-compare` library to compare `build-tools` versions properly.
* [GH-831](https://github.com/apache/cordova-android/pull/831) chore: ignore auto-generated eclipse buildship files
* [GH-848](https://github.com/apache/cordova-android/pull/848) breaking: increased default target sdk to 29
* [GH-859](https://github.com/apache/cordova-android/pull/859) breaking: removed unnecessary project name restriction
* [GH-833](https://github.com/apache/cordova-android/pull/833) chore: drop `q` module
* [GH-862](https://github.com/apache/cordova-android/pull/862) chore: replace `superspawn` & `child_process` with `execa`
* [GH-860](https://github.com/apache/cordova-android/pull/860) feat: don't filter gradle's stderr anymore
* [GH-832](https://github.com/apache/cordova-android/pull/832) chore: drop node 6 and 8 support
* [GH-890](https://github.com/apache/cordova-android/pull/890) chore: bump version to 9.0.0-dev
* [GH-697](https://github.com/apache/cordova-android/pull/697) chore: optimization code
* [GH-863](https://github.com/apache/cordova-android/pull/863) chore: removed comment that serves no purpose
* [GH-861](https://github.com/apache/cordova-android/pull/861) chore: update `jasmine` to 3.5.0
* [GH-858](https://github.com/apache/cordova-android/pull/858) chore: modernize our one E2E test
* [GH-854](https://github.com/apache/cordova-android/pull/854) chore: ensure to lint as many files as possible

### 8.1.0 (Sep 11, 2019)

* [GH-827](https://github.com/apache/cordova-android/pull/827) chore: bump dependencies for release 8.1.0
* [GH-651](https://github.com/apache/cordova-android/pull/651) feat: added multiple selection for filepicker
* [GH-672](https://github.com/apache/cordova-android/pull/672) chore: compress files in /res with tinypng.com
* [GH-815](https://github.com/apache/cordova-android/pull/815) fix: `clean` command
* [GH-750](https://github.com/apache/cordova-android/pull/750) Don't request focus explicitly if not needed
* [GH-800](https://github.com/apache/cordova-android/pull/800) [GH-799](https://github.com/apache/cordova-android/pull/799) (android) Stop webview from restarting when activity resizes
* [GH-764](https://github.com/apache/cordova-android/pull/764) feat: Build app bundles (.aab files)
* [GH-788](https://github.com/apache/cordova-android/pull/788) Simplify `apkSorter` using `compare-func` package
* [GH-787](https://github.com/apache/cordova-android/pull/787) Simplify and fix promise handling in specs
* [GH-784](https://github.com/apache/cordova-android/pull/784) Properly handle promise in create script
* [GH-783](https://github.com/apache/cordova-android/pull/783) Do not clobber process properties with test mocks
* [GH-782](https://github.com/apache/cordova-android/pull/782) Do not clobber `console.log` to spy on it
* [GH-724](https://github.com/apache/cordova-android/pull/724) Add Node.js 12 to CI Services
* [GH-777](https://github.com/apache/cordova-android/pull/777) ci(travis): set `dist: trusty` in `.travis.yml`
* [GH-779](https://github.com/apache/cordova-android/pull/779) Consistent order from `ProjectBuilder.apkSorter`
* [GH-778](https://github.com/apache/cordova-android/pull/778) test: use verbose spec reporter
* [GH-774](https://github.com/apache/cordova-android/pull/774) `rewire` workaround for NodeJS 12
* [GH-772](https://github.com/apache/cordova-android/pull/772) `nyc@14` update in devDependencies
* [GH-765](https://github.com/apache/cordova-android/pull/765) ci(travis): Fix **Android** SDK
* [GH-713](https://github.com/apache/cordova-android/pull/713) Do not explicitly require modules from project directory
* [GH-676](https://github.com/apache/cordova-android/pull/676) Added allprojects repositories for Framework Release Builds
* [GH-699](https://github.com/apache/cordova-android/pull/699) Improve Gradle Build Arguments
* [GH-710](https://github.com/apache/cordova-android/pull/710) Fix deprecation warning in `SystemCookieManager`
* [GH-691](https://github.com/apache/cordova-android/pull/691) [GH-690](https://github.com/apache/cordova-android/pull/690): Run `prepare` with the correct `ConfigParser`
* [GH-673](https://github.com/apache/cordova-android/pull/673) Updated `Android_HOME` Test to Follow [GH-656](https://github.com/apache/cordova-android/pull/656) Change

### 8.0.0 (Feb 13, 2019)
* [GH-669](https://github.com/apache/cordova-android/pull/669) Added Missing License Headers
* [GH-655](https://github.com/apache/cordova-android/pull/655) Use custom Gradle properties to read minSdkVersion value from `config.xml`
* [GH-656](https://github.com/apache/cordova-android/pull/656) Quick fix to support **Android**_SDK_ROOT
* [GH-632](https://github.com/apache/cordova-android/pull/632) Ignore more Gradle build artifacts in **Android** project
* [GH-642](https://github.com/apache/cordova-android/pull/642) **Android** tools 3.3 & **Gradle** 4.10.3 update
* [GH-654](https://github.com/apache/cordova-android/pull/654) Quick updates to top-level `project.properties`
* [GH-635](https://github.com/apache/cordova-android/pull/635) Ignore **Android** Studio `.idea` files in project
* [GH-624](https://github.com/apache/cordova-android/pull/624) Add missing log to Java version check
* [GH-630](https://github.com/apache/cordova-android/pull/630) Update `emulator.js` to fix issue [GH-608](https://github.com/apache/cordova-android/pull/608)
* [GH-626](https://github.com/apache/cordova-android/pull/626) Added `package-lock.json` to `.gitignore`
* [GH-620](https://github.com/apache/cordova-android/pull/620) Fix requirements error messages for JDK 8
* [GH-619](https://github.com/apache/cordova-android/pull/619) javac error message fixes in requirements check
* [GH-612](https://github.com/apache/cordova-android/pull/612) Android Platform Release Preparation (Cordova 9)
* [GH-607](https://github.com/apache/cordova-android/pull/607) Copy `node_modules` if the directory exists
* [GH-582](https://github.com/apache/cordova-android/pull/582) Improve Test `README`
* [GH-589](https://github.com/apache/cordova-android/pull/589) Rewrite install dir resolution for legacy plugins
* [GH-572](https://github.com/apache/cordova-android/pull/572) Resolve issue with plugin `target-dir="app*"` subdirs
* [GH-567](https://github.com/apache/cordova-android/pull/567) Output current package name if package name can't be validated
* [GH-507](https://github.com/apache/cordova-android/pull/507) Gradle Updates
* [GH-559](https://github.com/apache/cordova-android/pull/559) Eslint ignore version file
* [GH-550](https://github.com/apache/cordova-android/pull/550) Fix for old plugins with non-Java sources
* [GH-558](https://github.com/apache/cordova-android/pull/558) Update `cordova.js` from `cordova-js@4.2.3`
* [GH-553](https://github.com/apache/cordova-android/pull/553) Check for `build-extras.gradle` in the app-parent directory
* [GH-551](https://github.com/apache/cordova-android/pull/551) Add missing cast for `cdvMinSdkVersion`
* [GH-539](https://github.com/apache/cordova-android/pull/539) Fix destination path fallback
* [GH-544](https://github.com/apache/cordova-android/pull/544) Remove obsolete check for JellyBean
* [GH-465](https://github.com/apache/cordova-android/pull/465) Removes Gradle property in-line command arguments for `gradle.properties`
* [GH-523](https://github.com/apache/cordova-android/pull/523) Always put the Google repo above jcenter
* [GH-486](https://github.com/apache/cordova-android/pull/486) Change deprecated "compile" to "implementation"
* [GH-495](https://github.com/apache/cordova-android/pull/495) Incorrect default sdk version issue fix
* [GH-493](https://github.com/apache/cordova-android/pull/493) Remove bundled dependencies
* [GH-490](https://github.com/apache/cordova-android/pull/490) Fixes build & run related bugs from builder refactor
* [GH-464](https://github.com/apache/cordova-android/pull/464) Unit tests for **Android**_sdk and **Android**Project
* [GH-448](https://github.com/apache/cordova-android/pull/448) [CB-13685](https://issues.apache.org/jira/browse/CB-13685) Adaptive Icon Support
* [GH-487](https://github.com/apache/cordova-android/pull/487) Do not attempt an activity intent AND a url load into the webview, return from the internal webview load.
* [GH-461](https://github.com/apache/cordova-android/pull/461) Remove old builders code
* [GH-463](https://github.com/apache/cordova-android/pull/463) Emulator: Add unit tests and remove Q
* [GH-462](https://github.com/apache/cordova-android/pull/462) Device: Add unit tests and remove Q
* [GH-457](https://github.com/apache/cordova-android/pull/457) Emulator: handle "device still connecting" error
* [GH-445](https://github.com/apache/cordova-android/pull/445) Run and retryPromise improvements and tests
* [GH-453](https://github.com/apache/cordova-android/pull/453) Lint JS files w/out extension too
* [GH-452](https://github.com/apache/cordova-android/pull/452) Emit log event instead of logging directly
* [GH-449](https://github.com/apache/cordova-android/pull/449) Increase old plugin compatibility
* [GH-442](https://github.com/apache/cordova-android/pull/442) Fixes and cleanup for Java tests and CI
* [GH-446](https://github.com/apache/cordova-android/pull/446) [CB-14101](https://issues.apache.org/jira/browse/CB-14101) Fix Java version check for Java >= 9
* [CB-14127](https://issues.apache.org/jira/browse/CB-14127) Move google maven repo ahead of jcenter
* [CB-14038](https://issues.apache.org/jira/browse/CB-14038) Fix false positive detecting project type
* [CB-14008](https://issues.apache.org/jira/browse/CB-14008) Updating Gradle Libraries to work with **Android** Studio 3.1.0
* [CB-13975](https://issues.apache.org/jira/browse/CB-13975) Fix to fire pause event when cdvStartInBackground=true
* [CB-13830](https://issues.apache.org/jira/browse/CB-13830) Add handlers for plugins that use non-Java source files, such as Camera
* [CB-13923](https://issues.apache.org/jira/browse/CB-13923) Fix -1 length for compressed files

### 7.1.4 (Nov 22, 2018)

* Update android-versions to `1.4.0`, with added support for Android Pie ([#573](https://github.com/apache/cordova-android/pull/573))
* Output current package name if package name can't be validated ([#567](https://github.com/apache/cordova-android/pull/567))
* Resolve issue with plugin `target-dir="*app*"` subdirs ([#572](https://github.com/apache/cordova-android/pull/572))

### 7.1.3 (Nov 19, 2018)

* [GH-495](https://github.com/apache/cordova-android/pull/495) Incorrect default sdk version issue fix
* [GH-496](https://github.com/apache/cordova-android/pull/496) update comments in `build.gradle`
* [GH-539](https://github.com/apache/cordova-android/pull/539) Fix dest overwrite, in case of of plugin `source-file` element with `target-dir` that does not need remapping
* [GH-540](https://github.com/apache/cordova-android/issues/540) support plugin `source-file` element with any app `target-dir` value
* [GH-547](https://github.com/apache/cordova-android/issues/547) Compatibility of old plugins with non-Java `source-file` entries (individual files)
* [GH-551](https://github.com/apache/cordova-android/pull/551) add missing cast for cdvMinSdkVersion to `build.gradle`
* [GH-552](https://github.com/apache/cordova-android/issues/552) check for `build-extras.gradle` in the parent app directory

### 7.1.2 (Nov 08, 2018)
* [CB-14127](https://issues.apache.org/jira/browse/CB-14127): Always put the Google repo above jcenter
* [CB-14165](https://issues.apache.org/jira/browse/CB-14165): Emulator: handle "device still connecting" error (#457)
* [CB-14125](https://issues.apache.org/jira/browse/CB-14125): Increase old plugin compatibility
* [CB-13830](https://issues.apache.org/jira/browse/CB-13830): Add handlers for plugins that use non-Java source files, such as Camera
* [CB-14038](https://issues.apache.org/jira/browse/CB-14038): fix false positive detecting project type

### 7.1.1 (Jul 11, 2018)
* Fix unsafe property access in run.js (#445)
* Emit log event instead of logging directly (#452)
* [CB-14101](https://issues.apache.org/jira/browse/CB-14101) Fix Java version check for Java >= 9 (#446)
* [CB-14127](https://issues.apache.org/jira/browse/CB-14127) (android) Move google maven repo ahead of jcenter
* [CB-13923](https://issues.apache.org/jira/browse/CB-13923) (android) fix -1 length for compressed files
* [CB-14145](https://issues.apache.org/jira/browse/CB-14145) use cordova-common@2.2.5 and update other dependencies to resolve `npm audit` warnings
* [CB-9366](https://issues.apache.org/jira/browse/CB-9366) log error.stack in cordova.js

### 7.1.0 (Feb 20, 2018)
* [CB-13879](https://issues.apache.org/jira/browse/CB-13879) updated gradle tools dependency to 3.0.1 for project template
* [CB-13831](https://issues.apache.org/jira/browse/CB-13831) Update `android-versions` to 1.3.0 to support SDK 27.
* [CB-13800](https://issues.apache.org/jira/browse/CB-13800) Drop pre-KitKat specific code
* [CB-13724](https://issues.apache.org/jira/browse/CB-13724) Updated the **Android** Tooling required for the latest version on both the test project, and the template
* [CB-13724](https://issues.apache.org/jira/browse/CB-13724) Bump Target SDK to API 27
* [CB-13646](https://issues.apache.org/jira/browse/CB-13646) Using the deprecated `NDK` by default breaks the build.  Crosswalk users need to specify the Gradle parameters to keep it working.
* [CB-12218](https://issues.apache.org/jira/browse/CB-12218) Fix consistency of null result message
* [CB-13571](https://issues.apache.org/jira/browse/CB-13571) Prevent crash with unrecognized **Android** version
* [CB-13721](https://issues.apache.org/jira/browse/CB-13721) Fix build apps that use `cdvHelpers.getConfigPreference`
* [CB-13621](https://issues.apache.org/jira/browse/CB-13621) Wrote similar warning to [CB-12948](https://issues.apache.org/jira/browse/CB-12948) on **iOS**. We no longer support `cordova update` command.

### 7.0.0 (Nov 30, 2017)
* [CB-13612](https://issues.apache.org/jira/browse/CB-13612) Fix the remapper so that XML files copy over and the Camera works again.
* [CB-13741](https://issues.apache.org/jira/browse/CB-13741) Bump `package.json` so we can install plugins
* [CB-13610](https://issues.apache.org/jira/browse/CB-13610) Compress the default app assets
* [CB-12835](https://issues.apache.org/jira/browse/CB-12835) add a Context getter in CordovaInterface
* [CB-8976](https://issues.apache.org/jira/browse/CB-8976) Added the `cdvVersionCodeForceAbiDigit` flag to the template build.gradle that appends 0 to the versionCode when `cdvBuildMultipleApks` is not set
* [CB-12291](https://issues.apache.org/jira/browse/CB-12291) (android) Add x86_64, arm64 and armeabi architecture flavors
* [CB-13602](https://issues.apache.org/jira/browse/CB-13602) We were setting the path wrong, this is hacky but it works
* [CB-13601](https://issues.apache.org/jira/browse/CB-13601) Fixing the standalone run scripts to make sure this works without using the CLI
* [CB-13580](https://issues.apache.org/jira/browse/CB-13580) fix build for multiple apks (different product flavors)
* [CB-13558](https://issues.apache.org/jira/browse/CB-13558) Upgrading the gradle so we can upload the AAR
* [CB-13297](https://issues.apache.org/jira/browse/CB-13297) This just works once you bump the project structure.  Java 1.8 compatibility baked-in
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) **Android** Studio 3 work, things have changed with how the platform is built
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Found bug where the gradle subproject changes weren't actually getting written to the correct gradle file
* [CB-13470](https://issues.apache.org/jira/browse/CB-13470) Fix Clean so that it cleans the **Android** Studio structure
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Adding specs for resource files inside an **Android** Studio Project
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Added remapping for drawables
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Found bug in Api.js where xml/strings.xml is used instead of values/strings.xml
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Setup Api.js to support multiple builders based on project structure
* [CB-11244](https://issues.apache.org/jira/browse/CB-11244) Changing directory creation, will most likely hide this behind a flag for the next release of `cordova-android`, and then make it default in the next major pending feedback
* Adding the Studio Builder to build a project based on **Android** Studio, and deleting Ant, since Google does not support Ant Builds anymore. Sorry guys!

### 6.4.0 (Nov 06, 2017)
* [CB-13289](https://issues.apache.org/jira/browse/CB-13289) Fixing build problems with Studio Three, but keeping **Windows** Gradle fix for now, will be deprecated
* [CB-13289](https://issues.apache.org/jira/browse/CB-13289) Fix test to work with new Google **Android** Gradle DSL
* :CB-13501 : update appveyor node versions to support node 8
* [CB-13499](https://issues.apache.org/jira/browse/CB-13499) Remove duplicate "setting" in error strings
* Include missing values for task.name when 'cdvBuildMultipleApks' option is true, 'task.name' can have 'validateSigningArmv7Release' or 'validateSigningX86Release' values too.
* [CB-13406](https://issues.apache.org/jira/browse/CB-13406) Fixed AVD API level comparison when choosing sub-par API level match. Added tests for the best_image method.
* [CB-13404](https://issues.apache.org/jira/browse/CB-13404) add **Android**-versions to bundledDependencies. Ignore best emulator selection when parsed AVD information does not include API level in the target
* [CB-12895](https://issues.apache.org/jira/browse/CB-12895) : eslint ignoring cordova.js
* [CB-12895](https://issues.apache.org/jira/browse/CB-12895) Temporarily disabling eslint since cordova-js does not have eslint yet.

### 6.3.0 (Sep 25, 2017)
* [CB-6936](https://issues.apache.org/jira/browse/CB-6936) fix crash when calling methods on a destroyed webview
* [CB-12981](https://issues.apache.org/jira/browse/CB-12981) handle SDK 26.0.2 slightly different AVD list output for **Android** 8+ AVDs. Would cause "cannot read property replace of undefined" errors when trying to deploy an **Android** 8 emulator.
* Updated maven repo to include most recent lib versions
* [CB-13177](https://issues.apache.org/jira/browse/CB-13177) Updating to API Level 26
* Revert [CB-12015](https://issues.apache.org/jira/browse/CB-12015) initial-scale values less than 1.0 are ignored on **Android**
* [CB-12730](https://issues.apache.org/jira/browse/CB-12730) The Cordova Compatibility Plugin is now integrated into cordova-android
* [CB-12453](https://issues.apache.org/jira/browse/CB-12453) Remove unnecessary double quotes from .bat files which are the causes of crash if project path contains spaces
* [CB-13031](https://issues.apache.org/jira/browse/CB-13031) Fix bug with case-sensitivity of **Android**-packageName
* [CB-10916](https://issues.apache.org/jira/browse/CB-10916) Support display name for **Android**
* [CB-12423](https://issues.apache.org/jira/browse/CB-12423) make explicit JDK 1.8 or greater is needed in the `README`, we require 1.8 for compilation, but do not have 1.8 Java features yet
* [CB-13006](https://issues.apache.org/jira/browse/CB-13006) removed create and update end-to-end tests, and instead added more unit test coverage. tweaked code coverage invocation so that we get coverage details on the create.js module. slight changes to the create.js module so that it is slightly easier to test.
* [CB-12950](https://issues.apache.org/jira/browse/CB-12950) lots of tweaks for end-to-end test runs, especially on CI: - rename npm tasks to reflect what they do (npm run unit-tests, npm run e2e-tests). main `npm test` runs linter, unit tests and e2e tests now. - locked jasmine down to ~2.6.0. - consolidate gitignores. - updated travis to run `npm test`. add **Android** sdk installation to appveyor ci run.align **Android** dpendencies across travis and appveyor. have appveyor install gradle. force gradle to version 3.4.1 in appveyor, as that seems to be the only version choco has. explicitly invoke sdkmanager to move license accepting process along.
* [CB-12605](https://issues.apache.org/jira/browse/CB-12605) In **Windows** get **Android** studio path from the registry
* [CB-12762](https://issues.apache.org/jira/browse/CB-12762) : pointed `package.json` repo items to github mirrors instead of apache repos site
* [CB-12617](https://issues.apache.org/jira/browse/CB-12617) : removed node0.x support for platforms and added engineStrict

### 6.2.3 (May 2, 2017)
* [CB-12640](https://issues.apache.org/jira/browse/CB-12640) better handling of unrecognized Android SDK commands on **Windows**.
* [CB-12640](https://issues.apache.org/jira/browse/CB-12640) flipped avd parsing logic so that it always tries to use avdmanager to retrieve avds first, then falls back to android command if avdmanager cannot be found (and errors with ENOENT). updated tests - and added explicit tests to ensure to shell out to singular forms of sub-commands when executing `android`
* [CB-12640](https://issues.apache.org/jira/browse/CB-12640) support for android sdk tools 26.0.1.

### 6.2.2 (Apr 24, 2017)
* [CB-12697](https://issues.apache.org/jira/browse/CB-12697) Updated checked-in `node_modules`

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

