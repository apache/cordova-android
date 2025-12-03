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

[Apache Cordova](https://cordova.apache.org/) is a project of [The Apache Software Foundation (ASF)](https://apache.org/).

## Requirements

* Java Development Kit (JDK) 17
* [Android SDK](https://developer.android.com/)
* [Node.js](https://nodejs.org)

## Create a Cordova project

Follow the instructions in the [**Create your first Cordova app**](https://cordova.apache.org/docs/en/latest/guide/cli/index.html) section of [Apache Cordova Docs](https://cordova.apache.org/docs/en/latest/)

To use a **shared framework**, for example in development, link the appropriate cordova-android platform folder path:

```bash
cordova platform add --link /path/to/cordova-android
```

## Updating a Cordova project

When you install a new version of the [`Cordova CLI`](https://www.npmjs.com/package/cordova) that pins a new version of the [`Cordova-Android`](https://www.npmjs.com/package/cordova-android) platform, you can follow these simple upgrade steps within your project:

```bash
cordova platform rm android
cordova platform add android
```

## Debugging in Android Studio

Import project in Android Studio through _File > Open_ and targeting `/path/to/your-cdv-project/platforms/android/`.

## How to Test Repo Development

```bash
npm install
npm test
```

## Install Nightly Build

```bash
cordova platform add android@nightly
```

### Notes

Nightly builds are **not recommended for production apps**. They are intended for testing purposes. This allows users either to check if recent changes in the main branch have fixed existing issues or to identify new bugs before an official release.

Nightly builds are generated daily and may be **unstable**.

See [Apache Cordova - Nightly Builds](https://cordova.apache.org/contribute/nightly_builds.html) for more details.

## Further reading

* [Apache Cordova](https://cordova.apache.org/)
