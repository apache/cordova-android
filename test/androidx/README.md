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
