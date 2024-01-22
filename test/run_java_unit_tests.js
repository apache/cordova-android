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

const path = require('path');
const execa = require('execa');
const fs = require('fs-extra');
const ProjectBuilder = require('../lib/builders/ProjectBuilder');

class AndroidTestRunner {
    constructor (testTitle, projectDir) {
        this.testTitle = testTitle;
        this.projectDir = projectDir;
        this.gradleWrapper = path.join(this.projectDir, 'gradlew');
    }

    _gradlew (...args) {
        return execa(
            this.gradleWrapper,
            args,
            {
                stdio: 'inherit',
                cwd: this.projectDir
            }
        );
    }

    _getGradleVersion () {
        const config = JSON.parse(
            fs.readFileSync(path.resolve(this.projectDir, '../../framework/cdv-gradle-config-defaults.json'), {
                encoding: 'utf-8'
            })
        );

        return config.GRADLE_VERSION;
    }

    _createProjectBuilder () {
        return new ProjectBuilder(this.projectDir).installGradleWrapper(this._getGradleVersion());
    }

    run () {
        return Promise.resolve()
            .then(_ => console.log(`[${this.testTitle}] Preparing Gradle wrapper for Java unit tests.`))
            .then(_ => {
                // TODO we should probably not only copy these files, but instead create a new project from scratch
                fs.copyFileSync(path.resolve(this.projectDir, '../../framework/cdv-gradle-config-defaults.json'), path.resolve(this.projectDir, 'cdv-gradle-config.json'));
                fs.copyFileSync(
                    path.join(__dirname, '../templates/project/assets/www/cordova.js'),
                    path.join(this.projectDir, 'app/src/main/assets/www/cordova.js')
                );
            })
            .then(_ => this._createProjectBuilder())
            .then(_ => this._gradlew('--version'))
            .then(_ => console.log(`[${this.testTitle}] Gradle wrapper is ready. Running tests now.`))
            .then(_ => this._gradlew('test'))
            .then(_ => console.log(`[${this.testTitle}] Java unit tests completed successfully`));
    }
}

Promise.resolve()
    .then(_ => console.log('Starting to run all android platform tests'))

    // AndroidX Test
    .then(_ => new AndroidTestRunner('AndroidX Project', path.resolve(__dirname, 'androidx')))
    .then(test => test.run())

    .then(_ => console.log('Finished running all android platform tests'));

process.on('unhandledRejection', err => {
    // If err has a stderr property, we have seen the message already
    if (!('stderr' in err)) console.error(err.message);
    console.error('JAVA UNIT TESTS FAILED!');
    process.exitCode = err.code || 1;
});
