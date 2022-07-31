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

const rewire = require('rewire');
const android_sdk = require('../../lib/android_sdk');
const fs = require('fs-extra');
const path = require('path');
const events = require('cordova-common').events;
const which = require('which');

const {
    SDK_VERSION: DEFAULT_TARGET_API
} = require('../../lib/gradle-config-defaults');

describe('check_reqs', function () {
    let check_reqs;
    beforeEach(() => {
        check_reqs = rewire('../../lib/check_reqs');
    });

    let original_env;
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
        it('should return the version', async () => {
            check_reqs.__set__({
                java: { getVersion: async () => ({ version: '1.8.0' }) }
            });

            await expectAsync(check_reqs.check_java()).toBeResolvedTo({ version: '1.8.0' });
        });
    });

    describe('check_android', function () {
        describe('find and set ANDROID_HOME when neither ANDROID_HOME nor ANDROID_SDK_ROOT is set', function () {
            beforeEach(function () {
                delete process.env.ANDROID_HOME;
                delete process.env.ANDROID_SDK_ROOT;
            });
            describe('even if no Android binaries are on the PATH', function () {
                beforeEach(function () {
                    spyOn(which, 'sync').and.returnValue(null);
                    spyOn(fs, 'existsSync').and.returnValue(true);
                });
                it('it should set ANDROID_HOME on Windows', () => {
                    spyOn(check_reqs, 'isWindows').and.returnValue(true);
                    process.env.LOCALAPPDATA = 'windows-local-app-data';
                    process.env.ProgramFiles = 'windows-program-files';
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toContain('windows-local-app-data');
                    });
                });
                it('it should set ANDROID_HOME on Darwin', () => {
                    spyOn(check_reqs, 'isWindows').and.returnValue(false);
                    spyOn(check_reqs, 'isDarwin').and.returnValue(true);
                    process.env.HOME = 'home is where the heart is';
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toContain('home is where the heart is');
                    });
                });
            });
            describe('if some Android tooling exists on the PATH', function () {
                beforeEach(function () {
                    spyOn(fs, 'realpathSync').and.callFake(function (path) {
                        return path;
                    });
                });
                it('should set ANDROID_HOME based on `adb` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(which, 'sync').and.callFake(function (cmd) {
                        if (cmd === 'adb') {
                            return path.normalize('/android/sdk/platform-tools/adb');
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toEqual(path.normalize('/android/sdk'));
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
                it('should set ANDROID_HOME based on `avdmanager` command if command exists in a SDK-like directory structure', () => {
                    spyOn(fs, 'existsSync').and.returnValue(true);
                    spyOn(which, 'sync').and.callFake(function (cmd) {
                        if (cmd === 'avdmanager') {
                            return path.normalize('/android/sdk/tools/bin/avdmanager');
                        } else {
                            return null;
                        }
                    });
                    return check_reqs.check_android().then(function () {
                        expect(process.env.ANDROID_HOME).toEqual(path.normalize('/android/sdk'));
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

        describe('ANDROID_HOME environment variable detection', () => {
            beforeEach(() => {
                delete process.env.ANDROID_HOME;
                delete process.env.ANDROID_SDK_ROOT;
                check_reqs.__set__('forgivingWhichSync', jasmine.createSpy().and.returnValue(''));
            });

            const expectedAndroidSdkPath = path.sep + 'android' + path.sep + 'sdk';
            const expectedAndroidRootSdkPath = path.sep + 'android' + path.sep + 'sdk' + path.sep + 'root';

            it('should error if neither ANDROID_HOME nor ANDROID_SDK_ROOT is defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                return check_reqs.check_android().catch((error) => {
                    expect(error.toString()).toContain('Failed to find \'ANDROID_HOME\' environment variable.');
                });
            });

            it('should use ANDROID_HOME if defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                process.env.ANDROID_HOME = path.normalize('/android/sdk');
                return check_reqs.check_android().then(() => {
                    expect(process.env.ANDROID_HOME).toContain(expectedAndroidSdkPath);
                });
            });

            it('should use ANDROID_SDK_ROOT if defined and ANDROID_HOME is not defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                process.env.ANDROID_SDK_ROOT = path.normalize('/android/sdk/root');
                return check_reqs.check_android().then(() => {
                    expect(process.env.ANDROID_SDK_ROOT).toContain(expectedAndroidRootSdkPath);
                });
            });

            it('should use ANDROID_HOME if defined and ANDROID_SDK_ROOT is defined', () => {
                spyOn(fs, 'existsSync').and.returnValue(true);
                process.env.ANDROID_HOME = path.normalize('/android/sdk');
                process.env.ANDROID_SDK_ROOT = path.normalize('/android/sdk/root');
                return check_reqs.check_android().then(() => {
                    expect(process.env.ANDROID_HOME).toContain(expectedAndroidSdkPath);
                });
            });

            it('should throw if ANDROID_HOME points to an invalid path', () => {
                process.env.ANDROID_HOME = path.normalize('/android/sdk');
                return check_reqs.check_android().catch((error) => {
                    expect(error.toString()).toContain('\'ANDROID_HOME\' environment variable is set to non-existent path:');
                });
            });
        });

        describe('set PATH for various Android binaries if not available', function () {
            beforeEach(function () {
                spyOn(which, 'sync').and.returnValue(null);
                process.env.ANDROID_HOME = 'let the children play';
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
                delete process.env.ANDROID_HOME;
                delete process.env.ANDROID_SDK_ROOT;
                spyOn(check_reqs, 'get_gradle_wrapper').and.callFake(() => {
                    return path.normalize((process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT) + '/bin/gradle');
                });
            });

            it('with ANDROID_HOME / without ANDROID_SDK_ROOT', async () => {
                process.env.ANDROID_HOME = path.normalize('/android/sdk/home');
                await expectAsync(check_reqs.check_gradle()).toBeResolvedTo(path.normalize('/android/sdk/home/bin/gradle'));
            });

            it('without ANDROID_HOME / with ANDROID_SDK_ROOT', async () => {
                process.env.ANDROID_SDK_ROOT = path.normalize('/android/sdk/root');
                await expectAsync(check_reqs.check_gradle()).toBeResolvedTo(path.normalize('/android/sdk/root/bin/gradle'));
            });

            it('with ANDROID_HOME / with ANDROID_SDK_ROOT', async () => {
                process.env.ANDROID_HOME = path.normalize('/android/sdk/home');
                process.env.ANDROID_SDK_ROOT = path.normalize('/android/sdk/root');
                await expectAsync(check_reqs.check_gradle()).toBeResolvedTo(path.normalize('/android/sdk/home/bin/gradle'));
            });

            it('without ANDROID_HOME / without ANDROID_SDK_ROOT', () => {
                return check_reqs.check_gradle().catch((error) => {
                    expect(error.toString()).toContain('Could not find gradle wrapper within Android SDK. Could not find Android SDK directory.');
                });
            });
        });

        it('should error if sdk is installed but no gradle found', () => {
            process.env.ANDROID_HOME = path.normalize('/android/sdk');
            spyOn(check_reqs, 'get_gradle_wrapper').and.callFake(() => {
                return '';
            });

            return check_reqs.check_gradle().catch((error) => {
                expect(error.toString()).toContain('Could not find an installed version of Gradle');
            });
        });
    });

    describe('get_target', function () {
        const projectRoot = 'fakeProjectRoot';
        let ConfigParser;
        let getPreferenceSpy;
        beforeEach(function () {
            getPreferenceSpy = jasmine.createSpy();
            ConfigParser = jasmine.createSpy().and.returnValue({
                getPreference: getPreferenceSpy
            });
            check_reqs.__set__('ConfigParser', ConfigParser);
        });

        it('should retrieve DEFAULT_TARGET_API', function () {
            const target = check_reqs.get_target(projectRoot);
            expect(target).toBeDefined();
            expect(target).toContain('android-' + DEFAULT_TARGET_API);
        });

        it('should override target from config.xml preference', () => {
            spyOn(fs, 'existsSync').and.returnValue(true);
            getPreferenceSpy.and.returnValue(String(DEFAULT_TARGET_API + 1));

            const target = check_reqs.get_target(projectRoot);

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + (DEFAULT_TARGET_API + 1));
        });

        it('should fallback to default target if config.xml has invalid preference', () => {
            spyOn(fs, 'existsSync').and.returnValue(true);
            getPreferenceSpy.and.returnValue('android-99');

            const target = check_reqs.get_target(projectRoot);

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + DEFAULT_TARGET_API);
        });

        it('should warn if target sdk preference is lower than the minimum required target SDK', () => {
            spyOn(fs, 'existsSync').and.returnValue(true);

            spyOn(events, 'emit');

            getPreferenceSpy.and.returnValue(String(DEFAULT_TARGET_API - 1));

            const target = check_reqs.get_target(projectRoot);

            expect(getPreferenceSpy).toHaveBeenCalledWith('android-targetSdkVersion', 'android');
            expect(target).toBe('android-' + DEFAULT_TARGET_API);
            expect(events.emit).toHaveBeenCalledWith('warn', 'android-targetSdkVersion should be greater than or equal to ' + DEFAULT_TARGET_API + '.');
        });
    });

    describe('check_android_target', function () {
        it('should should return full list of supported targets if there is a match to ideal api level', () => {
            const fake_targets = ['you are my fire', 'my one desire'];
            spyOn(android_sdk, 'list_targets').and.resolveTo(fake_targets);
            spyOn(check_reqs, 'get_target').and.returnValue('you are my fire');
            return check_reqs.check_android_target().then(function (targets) {
                expect(targets).toBeDefined();
                expect(targets).toEqual(fake_targets);
            });
        });
        it('should error out if there is no match between ideal api level and installed targets', () => {
            const fake_targets = ['you are my fire', 'my one desire'];
            spyOn(android_sdk, 'list_targets').and.resolveTo(fake_targets);
            spyOn(check_reqs, 'get_target').and.returnValue('and i knowwwwwwwwwwww');
            return check_reqs.check_android_target().then(() => {
                fail('Expected promise to be rejected');
            }, err => {
                expect(err).toEqual(jasmine.any(Error));
                expect(err.message).toContain('Please install the Android SDK Platform');
            });
        });
    });
});
