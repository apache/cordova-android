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
const path = require('node:path');
const rewire = require('rewire');

describe('android_sdk', () => {
    let android_sdk;
    let execaSpy;

    beforeEach(() => {
        android_sdk = rewire('../../lib/android_sdk');
        execaSpy = jasmine.createSpy('execa');
        android_sdk.__set__('execa', execaSpy);
    });

    describe('sort_by_largest_numerical_suffix', () => {
        it('should return the newest version first', () => {
            const ids = ['android-P', 'android-24', 'android-19', 'android-27', 'android-23'];
            const sortedIds = ['android-27', 'android-24', 'android-23', 'android-19', 'android-P'];
            expect(ids.sort(android_sdk.__get__('sort_by_largest_numerical_suffix'))).toEqual(sortedIds);
        });

        describe('should return release version over preview versions', () => {
            it('Test #001', () => {
                const ids = ['android-27', 'android-P'];
                expect(android_sdk.__get__('sort_by_largest_numerical_suffix')(ids[0], ids[1])).toBe(-1);
            });

            it('Test #002', () => {
                const ids = ['android-P', 'android-27'];
                expect(android_sdk.__get__('sort_by_largest_numerical_suffix')(ids[0], ids[1])).toBe(1);
            });
        });
    });

    describe('print_newest_available_sdk_target', () => {
        it('should log the newest version', () => {
            const sortedIds = ['android-27', 'android-24', 'android-23', 'android-19'];

            spyOn(android_sdk, 'list_targets').and.returnValue(Promise.resolve(sortedIds));
            spyOn(sortedIds, 'sort');
            spyOn(console, 'log');

            return android_sdk.print_newest_available_sdk_target().then(() => {
                expect(sortedIds.sort).toHaveBeenCalledWith(android_sdk.__get__('sort_by_largest_numerical_suffix'));
                expect(console.log).toHaveBeenCalledWith(sortedIds[0]);
            });
        });
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
