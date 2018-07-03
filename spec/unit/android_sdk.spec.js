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

const superspawn = require('cordova-common').superspawn;
const fs = require('fs');
const path = require('path');
const rewire = require('rewire');

describe('android_sdk', () => {
    let android_sdk;

    beforeEach(() => {
        android_sdk = rewire('../../bin/templates/cordova/lib/android_sdk');
    });

    describe('sort_by_largest_numerical_suffix', () => {
        it('should return the newest version first', () => {
            const ids = ['android-24', 'android-19', 'android-27', 'android-23'];
            const sortedIds = ['android-27', 'android-24', 'android-23', 'android-19'];
            expect(ids.sort(android_sdk.__get__('sort_by_largest_numerical_suffix'))).toEqual(sortedIds);
        });

        it('should return 0 (no sort) if one of the versions has no number', () => {
            const ids = ['android-27', 'android-P'];
            expect(android_sdk.__get__('sort_by_largest_numerical_suffix')(ids[0], ids[1])).toBe(0);
        });
    });

    describe('print_newest_available_sdk_target', () => {
        it('should log the newest version', () => {
            const sortedIds = ['android-27', 'android-24', 'android-23', 'android-19'];
            const logSpy = jasmine.createSpy('log');

            spyOn(android_sdk, 'list_targets').and.returnValue(Promise.resolve(sortedIds));
            spyOn(sortedIds, 'sort');

            android_sdk.__set__({ console: { log: logSpy } });

            return android_sdk.print_newest_available_sdk_target().then(() => {
                expect(sortedIds.sort).toHaveBeenCalledWith(android_sdk.__get__('sort_by_largest_numerical_suffix'));
                expect(logSpy).toHaveBeenCalledWith(sortedIds[0]);
            });
        });
    });

    describe('list_targets_with_android', () => {
        it('should invoke `android` with the `list target` command and _not_ the `list targets` command, as the plural form is not supported in some Android SDK Tools versions', () => {
            spyOn(superspawn, 'spawn').and.returnValue(new Promise(() => {}, () => {}));
            android_sdk.list_targets_with_android();
            expect(superspawn.spawn).toHaveBeenCalledWith('android', ['list', 'target']);
        });

        it('should parse and return results from `android list targets` command', () => {
            const testTargets = fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.2-android_list_targets.txt'), 'utf-8');
            spyOn(superspawn, 'spawn').and.returnValue(Promise.resolve(testTargets));

            return android_sdk.list_targets_with_android().then(list => {
                [ 'Google Inc.:Google APIs:23',
                    'Google Inc.:Google APIs:22',
                    'Google Inc.:Google APIs:21',
                    'android-25',
                    'android-24',
                    'android-N',
                    'android-23',
                    'android-MNC',
                    'android-22',
                    'android-21',
                    'android-20' ].forEach((target) => expect(list).toContain(target));
            });
        });
    });

    describe('list_targets_with_avdmanager', () => {
        it('should parse and return results from `avdmanager list target` command', () => {
            const testTargets = fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.3-avdmanager_list_target.txt'), 'utf-8');
            spyOn(superspawn, 'spawn').and.returnValue(Promise.resolve(testTargets));

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

        it('should parse Android SDK installed target information with `android` command if list_targets_with_avdmanager fails with ENOENT', () => {
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(Promise.reject({ code: 'ENOENT' }));
            const avdmanager_spy = spyOn(android_sdk, 'list_targets_with_android').and.returnValue(Promise.resolve(['target1']));

            return android_sdk.list_targets().then(targets => {
                expect(avdmanager_spy).toHaveBeenCalled();
                expect(targets[0]).toEqual('target1');
            });
        });

        it('should parse Android SDK installed target information with `android` command if list_targets_with_avdmanager fails with not-recognized error (Windows)', () => {
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(Promise.reject({
                code: 1,
                stderr: "'avdmanager' is not recognized as an internal or external commmand,\r\noperable program or batch file.\r\n"
            }));

            const avdmanager_spy = spyOn(android_sdk, 'list_targets_with_android').and.returnValue(Promise.resolve(['target1']));
            return android_sdk.list_targets().then(targets => {
                expect(avdmanager_spy).toHaveBeenCalled();
                expect(targets[0]).toEqual('target1');
            });
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
