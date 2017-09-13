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

var emu = require('../../bin/templates/cordova/lib/emulator');
var superspawn = require('cordova-common').superspawn;
var Q = require('q');
var fs = require('fs');
var path = require('path');
var shelljs = require('shelljs');

describe('emulator', function () {
    describe('list_images_using_avdmanager', function () {
        it('should properly parse details of SDK Tools 25.3.1 `avdmanager` output', function (done) {
            var deferred = Q.defer();
            spyOn(superspawn, 'spawn').and.returnValue(deferred.promise);
            deferred.resolve(fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.3-avdmanager_list_avd.txt'), 'utf-8'));
            return emu.list_images_using_avdmanager().then(function (list) {
                expect(list).toBeDefined();
                expect(list[0].name).toEqual('nexus5-5.1');
                expect(list[0].target).toEqual('Android 5.1 (API level 22)');
                expect(list[1].device).toEqual('pixel (Google)');
                expect(list[2].abi).toEqual('default/x86_64');
            }).fail(function (err) {
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
    });
    describe('list_images_using_android', function () {
        it('should invoke `android` with the `list avd` command and _not_ the `list avds` command, as the plural form is not supported in some Android SDK Tools versions', function () {
            var deferred = Q.defer();
            spyOn(superspawn, 'spawn').and.returnValue(deferred.promise);
            emu.list_images_using_android();
            expect(superspawn.spawn).toHaveBeenCalledWith('android', ['list', 'avd']);
        });
        it('should properly parse details of SDK Tools pre-25.3.1 `android list avd` output', function (done) {
            var deferred = Q.defer();
            spyOn(superspawn, 'spawn').and.returnValue(deferred.promise);
            deferred.resolve(fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.2-android_list_avd.txt'), 'utf-8'));
            return emu.list_images_using_android().then(function (list) {
                expect(list).toBeDefined();
                expect(list[0].name).toEqual('QWR');
                expect(list[0].device).toEqual('Nexus 5 (Google)');
                expect(list[0].path).toEqual('/Users/shazron/.android/avd/QWR.avd');
                expect(list[0].target).toEqual('Android 7.1.1 (API level 25)');
                expect(list[0].abi).toEqual('google_apis/x86_64');
                expect(list[0].skin).toEqual('1080x1920');
            }).fail(function (err) {
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
    });
    describe('list_images', function () {
        beforeEach(function () {
            spyOn(fs, 'realpathSync').and.callFake(function (cmd) {
                return cmd;
            });
        });
        it('should try to parse AVD information using `avdmanager` first', function (done) {
            spyOn(shelljs, 'which').and.callFake(function (cmd) {
                if (cmd === 'avdmanager') {
                    return true;
                } else {
                    return false;
                }
            });
            var deferred = Q.defer();
            var avdmanager_spy = spyOn(emu, 'list_images_using_avdmanager').and.returnValue(deferred.promise);
            deferred.resolve([]);
            emu.list_images().then(function () {
                expect(avdmanager_spy).toHaveBeenCalled();
            }).fail(function (err) {
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
        it('should delegate to `android` if `avdmanager` cant be found and `android` can', function (done) {
            spyOn(shelljs, 'which').and.callFake(function (cmd) {
                if (cmd === 'avdmanager') {
                    return false;
                } else {
                    return true;
                }
            });
            var deferred = Q.defer();
            var android_spy = spyOn(emu, 'list_images_using_android').and.returnValue(deferred.promise);
            deferred.resolve([]);
            emu.list_images().then(function () {
                expect(android_spy).toHaveBeenCalled();
            }).fail(function (err) {
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
        it('should correct api level information and fill in the blanks about api level if exists', function (done) {
            spyOn(shelljs, 'which').and.callFake(function (cmd) {
                if (cmd === 'avdmanager') {
                    return true;
                } else {
                    return false;
                }
            });
            var deferred = Q.defer();
            spyOn(emu, 'list_images_using_avdmanager').and.returnValue(deferred.promise);
            deferred.resolve([
                {
                    name: 'Pixel_7.0',
                    device: 'pixel (Google)',
                    path: '/Users/maj/.android/avd/Pixel_7.0.avd',
                    abi: 'google_apis/x86_64',
                    target: 'Android 7.0 (API level 24)'
                }, {
                    name: 'Pixel_8.0',
                    device: 'pixel (Google)',
                    path: '/Users/maj/.android/avd/Pixel_8.0.avd',
                    abi: 'google_apis/x86',
                    target: 'Android API 26'
                }
            ]);
            emu.list_images().then(function (avds) {
                expect(avds[1].target).toContain('Android 8');
                expect(avds[1].target).toContain('API level 26');
            }).fail(function (err) {
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
        it('should throw an error if neither `avdmanager` nor `android` are able to be found', function (done) {
            spyOn(shelljs, 'which').and.returnValue(false);
            return emu.list_images().catch(function (err) {
                expect(err).toBeDefined();
                expect(err.message).toContain('Could not find either `android` or `avdmanager`');
                done();
            });
        });
    });
});
