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

var android_sdk = require('../../bin/templates/cordova/lib/android_sdk');
var superspawn = require('cordova-common').superspawn;
var fs = require('fs');
var path = require('path');
var Q = require('q');

describe('android_sdk', function () {
    describe('list_targets_with_android', function () {
        it('should invoke `android` with the `list target` command and _not_ the `list targets` command, as the plural form is not supported in some Android SDK Tools versions', function () {
            var deferred = Q.defer();
            spyOn(superspawn, 'spawn').and.returnValue(deferred.promise);
            android_sdk.list_targets_with_android();
            expect(superspawn.spawn).toHaveBeenCalledWith('android', ['list', 'target']);
        });
        it('should parse and return results from `android list targets` command', function (done) {
            var deferred = Q.defer();
            spyOn(superspawn, 'spawn').and.returnValue(deferred.promise);
            deferred.resolve(fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.2-android_list_targets.txt'), 'utf-8'));
            return android_sdk.list_targets_with_android().then(function (list) {
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
                    'android-20' ].forEach(function (target) { expect(list).toContain(target); });
            }).fail(function (err) {
                console.log(err);
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
    });
    describe('list_targets_with_avdmanager', function () {
        it('should parse and return results from `avdmanager list target` command', function (done) {
            var deferred = Q.defer();
            spyOn(superspawn, 'spawn').and.returnValue(deferred.promise);
            deferred.resolve(fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.3-avdmanager_list_target.txt'), 'utf-8'));
            return android_sdk.list_targets_with_avdmanager().then(function (list) {
                expect(list).toContain('android-25');
            }).fail(function (err) {
                console.log(err);
                expect(err).toBeUndefined();
            }).fin(function () {
                done();
            });
        });
    });
    describe('list_targets', function () {
        it('should parse Android SDK installed target information with `avdmanager` command first', function () {
            var deferred = Q.defer();
            var avdmanager_spy = spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(deferred.promise);
            android_sdk.list_targets();
            expect(avdmanager_spy).toHaveBeenCalled();
        });
        it('should parse Android SDK installed target information with `android` command if list_targets_with_avdmanager fails with ENOENT', function (done) {
            var deferred = Q.defer();
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(deferred.promise);
            deferred.reject({
                code: 'ENOENT'
            });
            var twoferred = Q.defer();
            twoferred.resolve(['target1']);
            var avdmanager_spy = spyOn(android_sdk, 'list_targets_with_android').and.returnValue(twoferred.promise);
            return android_sdk.list_targets().then(function (targets) {
                expect(avdmanager_spy).toHaveBeenCalled();
                expect(targets[0]).toEqual('target1');
                done();
            });
        });
        it('should parse Android SDK installed target information with `android` command if list_targets_with_avdmanager fails with not-recognized error (Windows)', function (done) {
            var deferred = Q.defer();
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(deferred.promise);
            deferred.reject({
                code: 1,
                stderr: "'avdmanager' is not recognized as an internal or external commmand,\r\noperable program or batch file.\r\n"
            });
            var twoferred = Q.defer();
            twoferred.resolve(['target1']);
            var avdmanager_spy = spyOn(android_sdk, 'list_targets_with_android').and.returnValue(twoferred.promise);
            return android_sdk.list_targets().then(function (targets) {
                expect(avdmanager_spy).toHaveBeenCalled();
                expect(targets[0]).toEqual('target1');
                done();
            });
        });
        it('should throw an error if no Android targets were found.', function (done) {
            var deferred = Q.defer();
            spyOn(android_sdk, 'list_targets_with_avdmanager').and.returnValue(deferred.promise);
            deferred.resolve([]);
            return android_sdk.list_targets().then(function (targets) {
                done.fail();
            }).catch(function (err) {
                expect(err).toBeDefined();
                expect(err.message).toContain('No android targets (SDKs) installed!');
                done();
            });
        });
    });
});
