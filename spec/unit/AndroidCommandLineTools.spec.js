/**
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

const fs = require('node:fs');
const path = require('node:path');
const AndroidCommandLineTools = require('../../lib/env/AndroidCommandLineTools');

describe('AndroidCommandLineTools', () => {
    beforeAll(() => {
        // For the purposes of these test, we will assume ANDROID_HOME is proper.
        spyOn(fs, 'existsSync').and.returnValue(true);
    });

    describe('getAvailableVersions', () => {
        describe('should return a list of command line versions', () => {
            it('in descending order', () => {
                spyOn(fs, 'readdirSync').and.returnValue([
                    '10.0',
                    '15.0',
                    '13'
                ]);

                expect(AndroidCommandLineTools.getAvailableVersions()).toEqual([
                    '15.0',
                    '13',
                    '10.0'
                ]);
            });

            it('stable releases appear before prereleases', () => {
                spyOn(fs, 'readdirSync').and.returnValue([
                    '15.0-rc01',
                    '15.0-alpha01',
                    '15.0'
                ]);

                expect(AndroidCommandLineTools.getAvailableVersions()).toEqual([
                    '15.0',
                    '15.0-rc01',
                    '15.0-alpha01'
                ]);
            });

            it('"latest" should take all precedence', () => {
                spyOn(fs, 'readdirSync').and.returnValue([
                    '15.0-rc01',
                    '15.0-alpha01',
                    '15.0',
                    'latest'
                ]);

                expect(AndroidCommandLineTools.getAvailableVersions()).toEqual([
                    'latest',
                    '15.0',
                    '15.0-rc01',
                    '15.0-alpha01'
                ]);
            });

            it('invalid versions are ignored', () => {
                spyOn(fs, 'readdirSync').and.returnValue([
                    '15.0-rc01',
                    'xyz',
                    '15.0'
                ]);

                expect(AndroidCommandLineTools.getAvailableVersions()).toEqual([
                    '15.0',
                    '15.0-rc01'
                ]);
            });
        });
    });

    describe('getBinPath', () => {
        beforeEach(() => {
            spyOn(AndroidCommandLineTools, '__getAndroidHome').and.returnValue('/Android/Sdk');
        });

        it('should return the bin path of the latest version', () => {
            spyOn(AndroidCommandLineTools, 'getAvailableVersions').and.returnValue([
                '19.0',
                '18.0'
            ]);

            expect(AndroidCommandLineTools.getBinPath()).toBe(path.resolve('/Android/Sdk/cmdline-tools/19.0/bin'));
        });
    });
});
