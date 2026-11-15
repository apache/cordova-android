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
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
-->

# Cordova Android Test Project

The project in this directory is an Android Test project that enables those
interested in further developing `cordova-android` to validate their changes.

## Requirements

The requirements in the [top-level README](../README.md) still apply. In
addition, ensure you have installed Gradle, and that it is (at the time of this
writing) at least version 3.3 or newer.

## Getting Started

You can run this test project from both the command line as well as from
Android Studio:

### Command Line

From the repository root, prepare the Gradle wrapper and generated build inputs,
and run the Java unit tests:

    npm run java-unit-tests

Then build the application and the complete instrumentation test APK:

    cd test/androidx
    tools/gradlew :app:assembleDebug :app:assembleDebugAndroidTest

With an Android device or emulator connected, run the instrumentation tests:

    tools/gradlew connectedDebugAndroidTest

To make sure all tests are run, add the `--rerun-tasks` parameter.

### Android Studio

Import this `test/androidx/` directory into Android Studio, and hit the Play button.

## Continuous integration

The separate [Android Instrumentation workflow](../../.github/workflows/android-instrumentation.yml)
runs the complete instrumentation suite on x86_64 emulators for API 24
(the minimum supported API, AOSP image with WebView 52) and API 36 (the current target
API, AOSP image). Update
this matrix when the supported API range changes. The system images supply
the WebView. The API 24 job requires the active AOSP provider to be WebView
52.x and fails before tests if the image supplies another version. The API 36
job uses its bundled provider without a version restriction. The version check
and both jobs' diagnostics record the selected provider, not merely installed APKs.
The API 24 job bypasses the emulator action's animation helper, which previously
hung on this image, and sets animation scales with bounded adb commands instead.

The initial bridge-startup race investigated in [#2036](https://github.com/apache/cordova-android/pull/2036)
was reproduced with WebView 52–59 on Android 7.1.1, but was not observed with
WebView 60 or newer in the tested versions. The controlled API 25 ARM64 sweep
recorded 59 failures in 600 starts with 59.0.3071.92 and none in 1,300 starts
with 60.0.3112.78. The previous API 24 Google APIs image used WebView 69.0.3497.100 and
therefore did not cover this supported minimum. The API 24 AOSP job now explicitly
targets WebView 52; the API 36 reference used WebView 133.0.6943.137. This describes
the measured regression range, not a minimum supported WebView policy.

It runs on pushes and pull requests, and can also be started manually through
GitHub Actions. It is independent of Node CI, so instrumentation jobs can be
rerun without rerunning the Node.js matrix. Matrix jobs do not cancel each
other when one fails, and test failures are not retried or ignored.

The workflow reuses `npm run java-unit-tests` to prepare the wrapper and
generated inputs, builds both APKs, then runs `:app:connectedDebugAndroidTest`.
A 10-minute test-command timeout leaves time to collect diagnostics within
the 30-minute job limit. Emulator boot has a separate 10-minute timeout.

Each API job uploads HTML/XML test results, the Gradle test log, logcat,
device properties, and WebView information for 14 days, including on test
failure. Device diagnostics are collected before the emulator action shuts
down the emulator. If setup or emulator boot fails, device diagnostics and
test reports may not be available; inspect the workflow log in that case.
