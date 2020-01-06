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
var execa = require('execa');
var ProjectBuilder = require('../bin/templates/cordova/lib/builders/ProjectBuilder');

Q.resolve()
    .then(_ => console.log('Preparing Gradle wrapper for Java unit tests.'))
    .then(_ => new ProjectBuilder(__dirname).runGradleWrapper('gradle'))
    .then(_ => gradlew('--version'))

    .then(_ => console.log('Gradle wrapper is ready. Running tests now.'))
    .then(_ => gradlew('test'))
    .then(_ => console.log('Java unit tests completed successfully.'));

process.on('unhandledRejection', err => {
    // If err has a stderr property, we have seen the message already
    if (!('stderr' in err)) console.error(err.message);
    console.error('JAVA UNIT TESTS FAILED!');
    process.exitCode = err.code || 1;
});

function gradlew () {
    const wrapperPath = path.join(__dirname, 'gradlew');
    return execa(wrapperPath, Array.from(arguments), {
        stdio: 'inherit',
        cwd: __dirname
    });
}
