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
# Android Native Tests

These tests are designed to verify Android native features and other Android specific features.
They currently are in disrepair, and don't pass / work on KitKat+ :(.

## Initial Setup

### Setting env vars

Run:

    ../bin/check_reqs

Use the output to set your `ANDROID_HOME` and `JAVA_HOME` environment variables.

### Adding `gradlew`

Copy it from a freshly created project:

    ../bin/create foo
    (cd foo && cordova/build --gradle; cp -r gradlew gradle ..)
    rm -r foo

### Robotium

Robotium has to be installed for the onScrollChanged tests to work correctly.  It can be
found at https://code.google.com/p/robotium/ and the jar should be put in the
'androidTests/libs' directory'.

## Running

To run manual tests:

    ./gradlew installDebug

To run unit tests:

    ./gradlew connectedAndroidTest

## Android Studio

1. Use "Non-Android Studio Project" to import the `test` directory.
2. Right click on the `junit` package in the left-side nav
3. Select "Debug"`->`_The one with the Android icon_
