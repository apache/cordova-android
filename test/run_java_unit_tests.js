#!/usr/bin/env node

/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

var Q = require('q');
var path = require('path');
var superspawn = require('cordova-common').superspawn;

// First we make sure the gradlew helper file is built and ready.
var GradleBuilder = require('../bin/templates/cordova/lib/builders/GradleBuilder');
var builder = new GradleBuilder(__dirname);
var needs_gradlew_built = builder.runGradleWrapper('gradle', 'build.gradle');

if (!needs_gradlew_built) {
    // Due to interface of gradle builder, if the gradlew file already exists, `runGradleWrapper` returns undefined.
    // In this case, we will fill the gap and create a resolved promise here now, this way the next bit of code
    // will jump straight to running the tests
    // TODO: maybe this should be done in GradleBuilder `runGradleWrapper` method instead?
    needs_gradlew_built = Q.fcall(function () { return true; });
}

needs_gradlew_built.then(function () {
    return superspawn.spawn(path.join(__dirname, 'gradlew'), ['test'], {stdio: 'inherit'});
}, function (err) {
    console.error('There was an error building the gradlew file:', err);
}).then(function () {
    console.log('Tests completed successfully.');
}).fail(function (err) {
    console.error('Tests failed!', err);
});
