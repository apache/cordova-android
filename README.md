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

# Cordova Android

[![npm - Latest](https://img.shields.io/npm/v/cordova-android/latest?label=Latest%20Release%20(npm))](https://npmjs.com/package/cordova-android)

[![GitHub](https://img.shields.io/github/package-json/v/apache/cordova-android?label=Development%20(Git))](https://github.com/apache/cordova-android)
[![GitHub - Node Workflow](https://github.com/apache/cordova-android/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/apache/cordova-android/actions/workflows/ci.yml?query=branch%3Amaster)
[![GitHub - Release Audit Workflow](https://github.com/apache/cordova-android/actions/workflows/release-audit.yml/badge.svg?branch=master)](https://github.com/apache/cordova-android/actions/workflows/release-audit.yml?query=branch%3Amaster)
[![Code Coverage](https://codecov.io/github/apache/cordova-android/coverage.svg?branch=master)](https://codecov.io/github/apache/cordova-android?branch=master)

Cordova Android is an Android application library that allows for Cordova-based projects to be built for the Android Platform. Cordova based applications are, at the core, applications written with web technology: HTML, CSS and JavaScript.

[Apache Cordova](https://cordova.apache.org/) is a project of [The Apache Software Foundation (ASF)](https://apache.org/).

## Requirements

* Java Development Kit (JDK)
* [Android SDK](https://developer.android.com/)
* [Gradle](https://gradle.org/)
* [Node.js](https://nodejs.org)

> [!TIP]
> Refer to the official Apache Cordova documentation for details on the requirements and prerequisites for building Cordova-Android applications. These resources outline the necessary tools, supported versions, and platform requirements:
>
> * [System Requirements](https://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html#system-requirements)
> * [Android API Level Support](https://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html#android-api-level-support)

## Create a Cordova project

Follow the instructions in the [**Create your first Cordova app**](https://cordova.apache.org/docs/en/latest/guide/cli/index.html) section of [Apache Cordova Docs](https://cordova.apache.org/docs/en/latest/)

## Updating a Cordova project

When you install a new version of the [`Cordova CLI`](https://www.npmjs.com/package/cordova) that pins a new version of the [`Cordova-Android`](https://www.npmjs.com/package/cordova-android) platform, you can follow these simple upgrade steps within your project:

```bash
cordova platform rm android
cordova platform add android
```

## Debugging in Android Studio

Import project in Android Studio through _File > Open_ and targeting `/path/to/your-cdv-project/platforms/android/`.

## Further reading

* [Apache Cordova](https://cordova.apache.org/)
