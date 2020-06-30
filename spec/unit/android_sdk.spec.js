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

const fs = require('fs');
const path = require('path');
const rewire = require('rewire');

describe('android_sdk', () => {
    let android_sdk;
    let execaSpy;

    beforeEach(() => {
        android_sdk = rewire('../../bin/templates/cordova/lib/android_sdk');
        execaSpy = jasmine.createSpy('execa');
        android_sdk.__set__('execa', execaSpy);
    });

    describe('list_targets_with_avdmanager', () => {
        it('should parse and return results from `avdmanager list target` command', () => {
            const testTargets = fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.3-avdmanager_list_target.txt'), 'utf-8');
            execaSpy.and.returnValue(Promise.resolve({ stdout: testTargets }));

            return android_sdk.list_targets_with_avdmanager().then(list => {
                expect(list).toContain('android-25');
            });
        });
    });

    describe('list_targets', () => {
        it('should parse Android SDK installed target information with `avdmanager` command first', () => {
            const avdmanager_spy = spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(new Promise(() => {}, () => {}));
            android_sdk.list_targets();
            expect(avdmanager_spy).toHaveBeenCalled();
        });

        it('should throw an error if `avdmanager` command fails with an unknown error', () => {
            const errorMsg = 'Some random error';
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(Promise.reject(errorMsg));

            return android_sdk.list_targets().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toBe(errorMsg);
                }
            );
        });

        it('should throw an error if no Android targets were found.', () => {
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(Promise.resolve([]));

            return android_sdk.list_targets().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('No android targets (SDKs) installed!');
                }
            );
        });
    });
});
