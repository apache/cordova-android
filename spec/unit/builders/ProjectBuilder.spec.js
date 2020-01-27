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

const fs = require('fs-extra');
const path = require('path');
const rewire = require('rewire');

const CordovaError = require('cordova-common').CordovaError;

describe('ProjectBuilder', () => {
    const rootDir = '/root';

    let builder;
    let ProjectBuilder;
    let execaSpy;

    beforeEach(() => {
        execaSpy = jasmine.createSpy('execa').and.returnValue(new Promise(() => {}));
        ProjectBuilder = rewire('../../../bin/templates/cordova/lib/builders/ProjectBuilder');
        ProjectBuilder.__set__('execa', execaSpy);

        builder = new ProjectBuilder(rootDir);
    });

    describe('constructor', () => {
        it('should set the project directory to the passed argument', () => {
            expect(builder.root).toBe(rootDir);
        });

        it('should set the project directory to three folders up', () => {
            ProjectBuilder.__set__('__dirname', 'projecttest/platforms/android/app');
            builder = new ProjectBuilder();
            expect(builder.root).toMatch(/projecttest$/);
        });
    });

    describe('getArgs', () => {
        beforeEach(() => {
            spyOn(fs, 'existsSync').and.returnValue(true);
        });

        it('should set release argument', () => {
            const args = builder.getArgs('release', {});
            expect(args[0]).toBe('cdvBuildRelease');
        });

        it('should set debug argument', () => {
            const args = builder.getArgs('debug', {});
            expect(args[0]).toBe('cdvBuildDebug');
        });

        it('should set apk release', () => {
            const args = builder.getArgs('release', {
                packageType: 'apk'
            });
            expect(args[0]).withContext(args).toBe('cdvBuildRelease');
        });

        it('should set apk debug', () => {
            const args = builder.getArgs('debug', {
                packageType: 'apk'
            });
            expect(args[0]).withContext(args).toBe('cdvBuildDebug');
        });

        it('should set bundle release', () => {
            const args = builder.getArgs('release', {
                packageType: 'bundle'
            });
            expect(args[0]).withContext(args).toBe(':app:bundleRelease');
        });

        it('should set bundle debug', () => {
            const args = builder.getArgs('debug', {
                packageType: 'bundle'
            });
            expect(args[0]).withContext(args).toBe(':app:bundleDebug');
        });

        it('should add architecture if it is passed', () => {
            const arch = 'unittest';
            const args = builder.getArgs('debug', { arch });

            expect(args).toContain(`-PcdvBuildArch=${arch}`);
        });

        it('should clean apk', () => {
            const args = builder.getArgs('clean', {
                packageType: 'apk'
            });
            expect(args[0]).toBe('clean');
        });

        it('should clean bundle', () => {
            const args = builder.getArgs('clean', {
                packageType: 'bundle'
            });
            expect(args[0]).toBe('clean');
        });
    });

    describe('runGradleWrapper', () => {
        it('should run the provided gradle command if a gradle wrapper does not already exist', () => {
            spyOn(fs, 'existsSync').and.returnValue(false);
            builder.runGradleWrapper('/my/sweet/gradle');
            expect(execaSpy).toHaveBeenCalledWith('/my/sweet/gradle', jasmine.any(Array), jasmine.any(Object));
        });

        it('should do nothing if a gradle wrapper exists in the project directory', () => {
            spyOn(fs, 'existsSync').and.returnValue(true);
            builder.runGradleWrapper('/my/sweet/gradle');
            expect(execaSpy).not.toHaveBeenCalledWith('/my/sweet/gradle', jasmine.any(Array), jasmine.any(Object));
        });
    });

    describe('extractRealProjectNameFromManifest', () => {
        it('should get the project name from the Android Manifest', () => {
            const projectName = 'unittestproject';
            const projectId = `io.cordova.${projectName}`;
            const manifest = `<?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${projectId}"></manifest>`;

            spyOn(fs, 'readFileSync').and.returnValue(manifest);

            expect(builder.extractRealProjectNameFromManifest()).toBe(projectName);
        });

        it('should throw an error if there is no package in the Android manifest', () => {
            const manifest = `<?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"></manifest>`;

            spyOn(fs, 'readFileSync').and.returnValue(manifest);

            expect(() => builder.extractRealProjectNameFromManifest()).toThrow(jasmine.any(CordovaError));
        });
    });

    describe('build', () => {
        beforeEach(() => {
            spyOn(builder, 'getArgs');
        });

        it('should set build type to debug', () => {
            const opts = { buildType: 'debug' };
            builder.build(opts);
            expect(builder.getArgs).toHaveBeenCalledWith('debug', opts);
        });

        it('should set build type to release', () => {
            const opts = { buildType: 'release' };
            builder.build(opts);
            expect(builder.getArgs).toHaveBeenCalledWith('release', opts);
        });

        it('should default build type to release', () => {
            const opts = {};
            builder.build(opts);
            expect(builder.getArgs).toHaveBeenCalledWith('release', opts);
        });

        it('should spawn gradle with correct args', () => {
            const testArgs = ['test', 'args', '-c'];
            builder.getArgs.and.returnValue(testArgs);

            builder.build({});

            expect(execaSpy).toHaveBeenCalledWith(path.join(rootDir, 'gradlew'), testArgs, jasmine.anything());
        });

        it('should reject if the spawn fails', () => {
            const errorMessage = 'Test error';
            execaSpy.and.rejectWith(new Error(errorMessage));

            return builder.build({}).then(
                () => fail('Unexpectedly resolved'),
                error => {
                    expect(error.message).toBe(errorMessage);
                }
            );
        });

        it('should check the Android target if failed to find target', () => {
            const checkReqsSpy = jasmine.createSpyObj('check_reqs', ['check_android_target']);
            const testError = 'failed to find target with hash string';

            ProjectBuilder.__set__('check_reqs', checkReqsSpy);
            checkReqsSpy.check_android_target.and.resolveTo();
            execaSpy.and.rejectWith(testError);

            return builder.build({}).then(
                () => fail('Unexpectedly resolved'),
                error => {
                    expect(checkReqsSpy.check_android_target).toHaveBeenCalledWith(testError);
                    expect(error).toBe(testError);
                }
            );
        });
    });

    describe('clean', () => {
        beforeEach(() => {
            const marker = ProjectBuilder.__get__('MARKER');
            spyOn(fs, 'readFileSync').and.returnValue(`Some Header Here: ${marker}`);
            spyOn(fs, 'removeSync');
            spyOn(builder, 'getArgs');
            execaSpy.and.returnValue(Promise.resolve());
        });

        it('should get arguments for cleaning', () => {
            const opts = {};

            return builder.clean(opts).then(() => {
                expect(builder.getArgs).toHaveBeenCalledWith('clean', opts);
            });
        });

        it('should spawn gradle', () => {
            const opts = {};
            const gradleArgs = ['test', 'args', '-f'];
            builder.getArgs.and.returnValue(gradleArgs);

            return builder.clean(opts).then(() => {
                expect(execaSpy).toHaveBeenCalledWith(path.join(rootDir, 'gradlew'), gradleArgs, jasmine.anything());
            });
        });

        it('should remove "out" folder', () => {
            return builder.clean({}).then(() => {
                expect(fs.removeSync).toHaveBeenCalledWith(path.join(rootDir, 'out'));
            });
        });

        it('should remove signing files if they are autogenerated', () => {
            const debugSigningFile = path.join(rootDir, 'debug-signing.properties');
            const releaseSigningFile = path.join(rootDir, 'release-signing.properties');

            spyOn(fs, 'existsSync').and.returnValue(true);

            return builder.clean({}).then(() => {
                expect(fs.removeSync).toHaveBeenCalledWith(debugSigningFile);
                expect(fs.removeSync).toHaveBeenCalledWith(releaseSigningFile);
            });
        });

        it('should not remove signing files if they are not autogenerated', () => {
            const debugSigningFile = path.join(rootDir, 'debug-signing.properties');
            const releaseSigningFile = path.join(rootDir, 'release-signing.properties');

            spyOn(fs, 'existsSync').and.returnValue(false);

            return builder.clean({}).then(() => {
                expect(fs.removeSync).not.toHaveBeenCalledWith(debugSigningFile);
                expect(fs.removeSync).not.toHaveBeenCalledWith(releaseSigningFile);
            });
        });
    });

    describe('fileSorter', () => {
        it('should sort APKs from most recent to oldest, deprioritising unsigned arch-specific builds', () => {
            const APKs = {
                'app-debug.apk': new Date('2018-04-20'),
                'app-release.apk': new Date('2018-05-20'),
                'app-debug-x86.apk': new Date('2018-06-01'),
                'app-release-x86.apk': new Date('2018-06-20'),
                'app-debug-arm.apk': new Date('2018-05-24'),
                'app-release-arm.apk': new Date('2018-06-24'),
                'app-release-unsigned.apk': new Date('2018-06-28')
            };

            const expectedResult = ['app-release.apk', 'app-debug.apk', 'app-release-unsigned.apk',
                'app-release-arm.apk', 'app-release-x86.apk', 'app-debug-x86.apk', 'app-debug-arm.apk'];

            spyOn(fs, 'statSync').and.callFake(filename => {
                return { mtime: APKs[filename] };
            });

            const apkArray = Object.keys(APKs);
            const sortedApks = apkArray.sort(ProjectBuilder.__get__('fileSorter'));

            expect(sortedApks).toEqual(expectedResult);
        });
    });
});
