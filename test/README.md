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
# Android Native Tests #

These tests are designed to verify Android native features and other Android specific features.

## Initial Setup ##

There really isn't any manual setup to do. The ant script takes care of that.
You don't even need to compile cordova-x.y.z.jar or copy it, because 
project.properties has a library reference to ../framework.  However, Robotium
has to be installed for the onScrollChanged tests to work correctly.  It can be
found at https://code.google.com/p/robotium/ and the jar should be put in the
'libs' directory'.

To run manually from command line:

0. Build by entering `ant debug install`
0. Run tests by clicking on "CordovaNativeTests" app icon on the device

To run from Eclipse:

0. Import Android project into Eclipse
0. Ensure Project properties "Java Build Path" includes the lib/cordova-x.y.z.jar
0. Create run configuration if not already created
0. Run As -> Android JUnit Test

## Automatic Runs ##

Once you have installed the test, you can launch and run the tests
automatically with the below command:

    adb shell am instrument -w org.apache.cordova.test/android.test.InstrumentationTestRunner
