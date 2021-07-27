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

    _createProjectBuilder () {
        return new ProjectBuilder(this.projectDir).runGradleWrapper('gradle');
    }

    _log (...args) {
        console.log.apply(console, [`[${this.testTitle}]`, ...args])
    }

    run () {
        return Promise.resolve()
            .then(_ => this._log('Staging Project Files'))
            .then(_ => {
                // TODO we should probably not only copy these files, but instead create a new project from scratch
                fs.copyFileSync(path.resolve(this.projectDir, '../../framework/cdv-gradle-config-defaults.json'), path.resolve(this.projectDir, 'cdv-gradle-config.json'));
                fs.copyFileSync(
                    path.join(__dirname, '../templates/project/assets/www/cordova.js'),
                    path.join(this.projectDir, 'app/src/main/assets/www/cordova.js')
                );
            })

            .then(_ => this._log('Creating Gradle Wrapper'))
            .then(_ => this._createProjectBuilder())

            .then(_ => this._log('Getting Gradle Wrapper Version Info'))
            .then(_ => this._gradlew('--version'))

            .then(_ => this._log('Running Java Unit Tests'))
            .then(_ => this._gradlew('test'))
            .then(_ => this._log('Finished Java Unit Test'))

            .then(_ => this._log('Running Java Instrumentation Tests'))
            .then(_ => this._gradlew('connectedAndroidTest'))
            .then(_ => this._log('Finished Java Instrumentation Tests'))
    }
}

Promise.resolve()
    .then(_ => console.log('Starting Android Platform Java Tests'))

    // AndroidX Test
    .then(_ => new AndroidTestRunner('AndroidX Project', path.resolve(__dirname, 'androidx')))
    .then(test => test.run())

    .then(_ => console.log('Finished Running Android Platform Java Tests'));

process.on('unhandledRejection', err => {
    // If err has a stderr property, we have seen the message already
    if (!('stderr' in err)) console.error(err.message);
    console.error('JAVA TESTS FAILED!');
    process.exitCode = err.code || 1;
});
