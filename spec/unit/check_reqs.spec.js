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
var android_sdk = require('../../bin/templates/cordova/lib/android_sdk');
var fs = require('fs-extra');
var path = require('path');
var events = require('cordova-common').events;
var which = require('which');
const { CordovaError } = require('cordova-common');

// This should match /bin/templates/project/build.gradle
const DEFAULT_TARGET_API = 29;

describe('check_reqs', function () {
    let check_reqs;
    beforeEach(() => {
        check_reqs = rewire('../../bin/templates/cordova/lib/check_reqs');
    });

    var original_env;
    beforeAll(function () {
        original_env = Object.assign({}, process.env);
    });
    afterEach(function () {
        // process.env has some special behavior, so we do not
        // replace it but only restore its original properties
        Object.keys(process.env).forEach(k => {
            delete process.env[k];
        });
        Object.assign(process.env, original_env);
    });

    describe('check_java', () => {
        it('detects if unexpected JDK version is installed', async () => {
            check_reqs.__set__({
                EXPECTED_JAVA_VERSION: '9999.9999.9999',
                java: { getVersion: async () => ({ version: '1.8.0' }) }
            });

            await expectAsync(check_reqs.check_java()).toBeRejectedWithError(CordovaError, /Requirements check failed for JDK 9999.9999.9999! Detected version: 1.8.0/);
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
                    spyOn(which, 'sync').and.returnValue(null);
                    spyOn(fs, 'existsSync').and.returnValue(true);
                });
                it('it should set ANDROID_SDK_ROOT on Windows', () => {
                    spyOn(check_reqs, 'isWindows').and.returnValue(true);
                    process.env.LOCALAPPDATA = 'windows-local-app-data';
                    process.env.ProgramFiles = 'windows-program-files';
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_SDK_ROOT).toContain('windows-local-app-data');
                    });
                });
                it('it should set ANDROID_SDK_ROOT on Darwin', () => {
                    spyOn(check_reqs, 'isWindows').and.returnValue(false);
                    spyOn(check_reqs, 'isDarwin').and.returnValue(true);
                    process.env.HOME = 'home is where the heart is';
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_SDK_ROOT).toContain('home is where the heart is');
                    });
                });
            });
            describe('if some Android tooling exists on the PATH', function () {
                beforeEach(function () {
                    spyOn(fs, 'realpathSync').and.callFake(function (path) {
                        return path;
                    });
                });
                it('should set ANDROID_SDK_ROOT based on `android` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(which, 'sync').and.callFake(function (cmd) {
                        if (cmd === 'android') {
                            return '/android/sdk/tools/android';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_SDK_ROOT).toEqual('/android/sdk');
                    });
                });
                it('should error out if `android` command exists in a non-SDK-like directory structure', () => {
                    spyOn(which, 'sync').and.callFake(function (cmd) {
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
                it('should set ANDROID_SDK_ROOT based on `adb` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(which, 'sync').and.callFake(function (cmd) {
                        if (cmd === 'adb') {
                            return '/android/sdk/platform-tools/adb';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_SDK_ROOT).toEqual('/android/sdk');
                    });
                });
                it('should error out if `adb` command exists in a non-SDK-like directory structure', () => {
                    spyOn(which, 'sync').and.callFake(function (cmd) {
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
                it('should set ANDROID_SDK_ROOT based on `avdmanager` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(which, 'sync').and.callFake(function (cmd) {
                        if (cmd === 'avdmanager') {
                            return '/android/sdk/tools/bin/avdmanager';
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_SDK_ROOT).toEqual('/android/sdk');
                    });
                });
                it('should error out if `avdmanager` command exists in a non-SDK-like directory structure', () => {
                    spyOn(which, 'sync').and.callFake(function (cmd) {
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

        describe('ANDROID_SDK_ROOT environment variable detection', () => {
            beforeEach(() => {
                delete process.env.ANDROID_SDK_ROOT;
                delete process.env.ANDROID_HOME;
                check_reqs.__set__('forgivingWhichSync', jasmine.createSpy().and.returnValue(''));
            });

            const expectedAndroidSdkPath = path.sep + 'android' + path.sep + 'sdk';
            const expectedAndroidRootSdkPath = path.sep + 'android' + path.sep + 'sdk' + path.sep + 'root';

            it('should error if neither ANDROID_SDK_ROOT or ANDROID_HOME is defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                return check_reqs.check_android().catch((error) => {
                    expect(error.toString()).toContain('Failed to find \'ANDROID_SDK_ROOT\' environment variable.');
                });
            });

            it('should use ANDROID_SDK_ROOT if defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                process.env.ANDROID_SDK_ROOT = '/android/sdk';
                return check_reqs.check_android().then(() => {
                    expect(process.env.ANDROID_SDK_ROOT).toContain(expectedAndroidSdkPath);
                });
            });

            it('should use ANDROID_HOME if defined and ANDROID_SDK_ROOT is not defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                process.env.ANDROID_HOME = '/android/sdk';
                return check_reqs.check_android().then(() => {
                    expect(process.env.ANDROID_SDK_ROOT).toContain(expectedAndroidSdkPath);
                });
            });

            it('should use ANDROID_SDK_ROOT if defined and ANDROID_HOME is defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                process.env.ANDROID_SDK_ROOT = '/android/sdk/root';
                process.env.ANDROID_HOME = '/android/sdk';
                return check_reqs.check_android().then(() => {
                    expect(process.env.ANDROID_SDK_ROOT).toContain(expectedAndroidRootSdkPath);
                });
            });

            it('should throw if ANDROID_SDK_ROOT points to an invalid path', () => {
                process.env.ANDROID_SDK_ROOT = '/android/sdk';
                return check_reqs.check_android().catch((error) => {
                    expect(error.toString()).toContain('\'ANDROID_SDK_ROOT\' environment variable is set to non-existent path:');
                });
            });
        });

        describe('set PATH for various Android binaries if not available', function () {
            beforeEach(function () {
                spyOn(which, 'sync').and.returnValue(null);
                process.env.ANDROID_SDK_ROOT = 'let the children play';
                spyOn(fs, 'existsSync').and.returnValue(true);
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

    describe('check_gradle', () => {
        describe('environment variable checks', () => {
            beforeEach(() => {
                delete process.env.ANDROID_SDK_ROOT;
                delete process.env.ANDROID_HOME;
                spyOn(check_reqs, 'get_gradle_wrapper').and.callFake(() => {
                    return (process.env.ANDROID_SDK_ROOT || process.env.ANDROID_HOME) + '/bin/gradle';
                });
            });

            it('with ANDROID_SDK_ROOT / without ANDROID_HOME', async () => {
                process.env.ANDROID_SDK_ROOT = '/android/sdk/root';
                await expectAsync(check_reqs.check_gradle()).toBeResolvedTo('/android/sdk/root/bin/gradle');
            });

            it('with ANDROID_SDK_ROOT / with ANDROID_HOME', async () => {
                process.env.ANDROID_SDK_ROOT = '/android/sdk/root';
                process.env.ANDROID_HOME = '/android/sdk/home';
                await expectAsync(check_reqs.check_gradle()).toBeResolvedTo('/android/sdk/root/bin/gradle');
            });

            it('without ANDROID_SDK_ROOT / with ANDROID_HOME', async () => {
                process.env.ANDROID_HOME = '/android/sdk/home';
                await expectAsync(check_reqs.check_gradle()).toBeResolvedTo('/android/sdk/home/bin/gradle');
            });

            it('without ANDROID_SDK_ROOT / without ANDROID_HOME', () => {
                return check_reqs.check_gradle().catch((error) => {
                    expect(error.toString()).toContain('Could not find gradle wrapper within Android SDK. Could not find Android SDK directory.');
                });
            });
        });

        it('should error if sdk is installed but no gradle found', () => {
            process.env.ANDROID_SDK_ROOT = '/android/sdk';
            spyOn(check_reqs, 'get_gradle_wrapper').and.callFake(() => {
                return '';
            });

            return check_reqs.check_gradle().catch((error) => {
                expect(error.toString()).toContain('Could not find an installed version of Gradle');
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

            spyOn(events, 'emit');

            getPreferenceSpy.and.returnValue(DEFAULT_TARGET_API - 1);

            var target = check_reqs.get_target();

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + DEFAULT_TARGET_API);
            expect(events.emit).toHaveBeenCalledWith('warn', 'android-targetSdkVersion should be greater than or equal to ' + DEFAULT_TARGET_API + '.');
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
