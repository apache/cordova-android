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

var rewire = require('rewire');
var check_reqs = rewire('../../bin/templates/cordova/lib/check_reqs');
var android_sdk = require('../../bin/templates/cordova/lib/android_sdk');
var shelljs = require('shelljs');
var fs = require('fs');
var path = require('path');
var events = require('cordova-common').events;

// This should match /bin/templates/project/build.gradle
const DEFAULT_TARGET_API = 28;

describe('check_reqs', function () {
    var original_env;
    beforeAll(function () {
        original_env = Object.create(process.env);
    });
    afterEach(function () {
        Object.keys(original_env).forEach(function (k) {
            process.env[k] = original_env[k];
        });
    });
    describe('check_android', function () {
        describe('find and set ANDROID_HOME when ANDROID_HOME and ANDROID_SDK_ROOT is not set', function () {
            beforeEach(function () {
                delete process.env.ANDROID_HOME;
                delete process.env.ANDROID_SDK_ROOT;
            });
            describe('even if no Android binaries are on the PATH', function () {
                beforeEach(function () {
                    spyOn(shelljs, 'which').and.returnValue(null);
                    spyOn(fs, 'existsSync').and.returnValue(true);
                });
                it('it should set ANDROID_HOME on Windows', () => {
                    spyOn(check_reqs, 'isWindows').and.returnValue(true);
                    process.env.LOCALAPPDATA = 'windows-local-app-data';
                    process.env.ProgramFiles = 'windows-program-files';
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toContain('windows-local-app-data');
                    }).finally(function () {
                        delete process.env.LOCALAPPDATA;
                        delete process.env.ProgramFiles;
                    });
                });
                it('it should set ANDROID_HOME on Darwin', () => {
                    spyOn(check_reqs, 'isWindows').and.returnValue(false);
                    spyOn(check_reqs, 'isDarwin').and.returnValue(true);
                    process.env.HOME = 'home is where the heart is';
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toContain('home is where the heart is');
                    }).finally(function () {
                        delete process.env.HOME;
                    });
                });
            });
            describe('if some Android tooling exists on the PATH', function () {
                beforeEach(function () {
                    spyOn(fs, 'realpathSync').and.callFake(function (path) {
                        return path;
                    });
                });
                it('should set ANDROID_HOME based on `android` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(shelljs, 'which').and.callFake(function (cmd) {
                        if (cmd === 'android') {
                            return '/android/sdk/tools/android';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toEqual('/android/sdk');
                    });
                });
                it('should error out if `android` command exists in a non-SDK-like directory structure', () => {
                    spyOn(shelljs, 'which').and.callFake(function (cmd) {
                        if (cmd === 'android') {
                            return '/just/some/random/path/android';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(() => {
                        fail('Expected promise to be rejected');
                    }, err => {
                        expect(err).toEqual(jasmine.any(Error));
                        expect(err.message).toContain('update your PATH to include valid path');
                    });
                });
                it('should set ANDROID_HOME based on `adb` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(shelljs, 'which').and.callFake(function (cmd) {
                        if (cmd === 'adb') {
                            return '/android/sdk/platform-tools/adb';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toEqual('/android/sdk');
                    });
                });
                it('should error out if `adb` command exists in a non-SDK-like directory structure', () => {
                    spyOn(shelljs, 'which').and.callFake(function (cmd) {
                        if (cmd === 'adb') {
                            return '/just/some/random/path/adb';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(() => {
                        fail('Expected promise to be rejected');
                    }, err => {
                        expect(err).toEqual(jasmine.any(Error));
                        expect(err.message).toContain('update your PATH to include valid path');
                    });
                });
                it('should set ANDROID_HOME based on `avdmanager` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(shelljs, 'which').and.callFake(function (cmd) {
                        if (cmd === 'avdmanager') {
                            return '/android/sdk/tools/bin/avdmanager';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toEqual('/android/sdk');
                    });
                });
                it('should error out if `avdmanager` command exists in a non-SDK-like directory structure', () => {
                    spyOn(shelljs, 'which').and.callFake(function (cmd) {
                        if (cmd === 'avdmanager') {
                            return '/just/some/random/path/avdmanager';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(() => {
                        fail('Expected promise to be rejected');
                    }, err => {
                        expect(err).toEqual(jasmine.any(Error));
                        expect(err.message).toContain('update your PATH to include valid path');
                    });
                });
            });
        });
        describe('set PATH for various Android binaries if not available', function () {
            beforeEach(function () {
                spyOn(shelljs, 'which').and.returnValue(null);
                process.env.ANDROID_HOME = 'let the children play';
                spyOn(fs, 'existsSync').and.returnValue(true);
            });
            afterEach(function () {
                delete process.env.ANDROID_HOME;
            });
            it('should add tools/bin,tools,platform-tools to PATH if `avdmanager`,`android`,`adb` is not found', () => {
                return check_reqs.check_android().then(function () {
                    expect(process.env.PATH).toContain('let the children play' + path.sep + 'tools');
                    expect(process.env.PATH).toContain('let the children play' + path.sep + 'platform-tools');
                    expect(process.env.PATH).toContain('let the children play' + path.sep + 'tools' + path.sep + 'bin');
                });
            });
        });
    });

    describe('get_target', function () {
        var ConfigParser;
        var getPreferenceSpy;
        beforeEach(function () {
            getPreferenceSpy = jasmine.createSpy();
            ConfigParser = jasmine.createSpy().and.returnValue({
                getPreference: getPreferenceSpy
            });
            check_reqs.__set__('ConfigParser', ConfigParser);
        });

        it('should retrieve target from framework project.properties file', function () {
            var target = check_reqs.get_target();
            expect(target).toBeDefined();
            expect(target).toContain('android-' + DEFAULT_TARGET_API);
        });

        it('should throw error if target cannot be found', function () {
            spyOn(fs, 'existsSync').and.returnValue(false);
            expect(function () {
                check_reqs.get_target();
            }).toThrow();
        });

        it('should override target from config.xml preference', () => {
            var realExistsSync = fs.existsSync;
            spyOn(fs, 'existsSync').and.callFake(function (path) {
                if (path.indexOf('config.xml') > -1) {
                    return true;
                } else {
                    return realExistsSync.call(fs, path);
                }
            });

            getPreferenceSpy.and.returnValue(DEFAULT_TARGET_API + 1);

            var target = check_reqs.get_target();

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + (DEFAULT_TARGET_API + 1));
        });

        it('should fallback to default target if config.xml has invalid preference', () => {
            var realExistsSync = fs.existsSync;
            spyOn(fs, 'existsSync').and.callFake(function (path) {
                if (path.indexOf('config.xml') > -1) {
                    return true;
                } else {
                    return realExistsSync.call(fs, path);
                }
            });

            getPreferenceSpy.and.returnValue(NaN);

            var target = check_reqs.get_target();

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + DEFAULT_TARGET_API);
        });

        it('should warn if target sdk preference is lower than the minimum required target SDK', () => {
            var realExistsSync = fs.existsSync;
            spyOn(fs, 'existsSync').and.callFake(function (path) {
                if (path.indexOf('config.xml') > -1) {
                    return true;
                } else {
                    return realExistsSync.call(fs, path);
                }
            });

            getPreferenceSpy.and.returnValue(DEFAULT_TARGET_API - 1);

            var target = check_reqs.get_target();

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + DEFAULT_TARGET_API);
            expect(events.emit).toHaveBeenCalledWith('warn', 'android-targetSdkVersion must be greater than or equal to ' + DEFAULT_TARGET_API + '.');
        });
    });

    describe('check_android_target', function () {
        it('should should return full list of supported targets if there is a match to ideal api level', () => {
            var fake_targets = ['you are my fire', 'my one desire'];
            spyOn(android_sdk, 'list_targets').and.resolveTo(fake_targets);
            spyOn(check_reqs, 'get_target').and.returnValue('you are my fire');
            return check_reqs.check_android_target().then(function (targets) {
                expect(targets).toBeDefined();
                expect(targets).toEqual(fake_targets);
            });
        });
        it('should error out if there is no match between ideal api level and installed targets', () => {
            var fake_targets = ['you are my fire', 'my one desire'];
            spyOn(android_sdk, 'list_targets').and.resolveTo(fake_targets);
            spyOn(check_reqs, 'get_target').and.returnValue('and i knowwwwwwwwwwww');
            return check_reqs.check_android_target().then(() => {
                fail('Expected promise to be rejected');
            }, err => {
                expect(err).toEqual(jasmine.any(Error));
                expect(err.message).toContain('Please install Android target');
            });
        });
    });
});
