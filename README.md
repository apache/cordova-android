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
Cordova Android
===

Cordova Android is an Android application library that allows for Cordova-based
projects to be built for the Android Platform. Cordova based applications are,
at the core, applications written with web technology: HTML, CSS and JavaScript.

[Apache Cordova](http://cordova.io) is a project of The Apache Software Foundation (ASF).


Requires
---

- Java JDK 1.5 or greater
- Apache Ant 1.8.0 or greater
- Android SDK [http://developer.android.com](http://developer.android.com)


Cordova Android Developer Tools
---

The Cordova developer tooling is split between general tooling and project level tooling.

General Commands

    ./bin/create [path package activity] ... creates the ./example app or a cordova android project
    ./bin/check_reqs ....................... checks that your environment is set up for cordova-android development
    ./bin/update [path] .................... updates an existing cordova-android project to the version of the framework

Project Commands

These commands live in a generated Cordova Android project. Any interactions with the emulator require you to have an AVD defined.

    ./cordova/clean ........................ cleans the project
    ./cordova/build ........................ calls `clean` then compiles the project
    ./cordova/log   ........................ streams device or emulator logs to STDOUT
    ./cordova/run   ........................ calls `build` then deploys to a connected Android device. If no Android device is detected, will launch an emulator and deploy to it.
    ./cordova/version ...................... returns the cordova-android version of the current project

Importing a Cordova Android Project into Eclipse
----

1. File > New > Project...
2. Android > Android Project
3. Create project from existing source (point to the generated app found in tmp/android)
4. Right click on libs/cordova.jar and add to build path
5. Right click on the project root: Run as > Run Configurations
6. Click on the Target tab and select Manual (this way you can choose the emulator or device to build to)

Building without the Tooling
---
Note: The Developer Tools handle this.  This is only to be done if the tooling fails, or if
you are developing directly against the framework.


To create your `cordova.jar` file, run in the framework directory:

    android update project -p . -t android-19
    ant jar


Running Tests
----
Please see details under test/README.md.

Further Reading
----

- [http://developer.android.com](http://developer.android.com)
- [http://cordova.apache.org/](http://cordova.apache.org)
- [http://wiki.apache.org/cordova/](http://wiki.apache.org/cordova/)
