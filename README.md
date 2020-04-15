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

# Cordova Android

[![NPM](https://nodei.co/npm/cordova-android.png)](https://nodei.co/npm/cordova-android/)

[![Node CI](https://github.com/apache/cordova-android/workflows/Node%20CI/badge.svg?branch=master)](https://github.com/apache/cordova-android/actions?query=branch%3Amaster)
[![codecov.io](https://codecov.io/github/apache/cordova-android/coverage.svg?branch=master)](https://codecov.io/github/apache/cordova-android?branch=master)

Cordova Android is an Android application library that allows for Cordova-based projects to be built for the Android Platform. Cordova based applications are, at the core, applications written with web technology: HTML, CSS and JavaScript.

[Apache Cordova](https://cordova.apache.org) is a project of The Apache Software Foundation (ASF).

## Requires

- Java JDK 1.8
- Android SDK [http://developer.android.com](https://developer.android.com/)

## Cordova Android Developer Tools

We recommend using the [Cordova command-line tool](https://www.npmjs.com/package/cordova) to create projects and be able to easily install plugins.

However, the following scripts can be used instead:

    ./bin/create [path package activity] ... creates the ./example app or a cordova android project
    ./bin/check_reqs ....................... checks that your environment is set up for cordova-android development
    ./bin/update [path] .................... updates an existing cordova-android project to the version of the framework

These commands live in a generated Cordova Android project. Any interactions with the emulator require you to have an AVD defined.

    ./cordova/clean ........................ cleans the project
    ./cordova/build ........................ calls `clean` then compiles the project
    ./cordova/log   ........................ streams device or emulator logs to STDOUT
    ./cordova/run   ........................ calls `build` then deploys to a connected Android device. If no Android device is detected, will launch an emulator and deploy to it.
    ./cordova/version ...................... returns the cordova-android version of the current project

## Using Android Studio

1. Create a project
2. Import it via "Non-Android Studio Project"

## Running the Native Tests

The `test/` directory in this project contains an Android test project that can be used to run different kinds of native tests. Check out the [README contained therein](test/README.md) for more details!
