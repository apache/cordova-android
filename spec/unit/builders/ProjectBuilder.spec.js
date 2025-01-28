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

const fs = require('node:fs');
const path = require('node:path');
const rewire = require('rewire');
const { isWindows } = require('../../../lib/utils');

describe('ProjectBuilder', () => {
    const rootDir = '/root';

    let builder;
    let ProjectBuilder;
    let execaSpy;

    beforeEach(() => {
        execaSpy = jasmine.createSpy('execa').and.returnValue(new Promise(() => {}));
        ProjectBuilder = rewire('../../../lib/builders/ProjectBuilder');
        ProjectBuilder.__set__('execa', execaSpy);

        builder = new ProjectBuilder(rootDir);
    });

    describe('constructor', () => {
        it('should set the project directory to the passed argument', () => {
            expect(builder.root).toBe(rootDir);
        });

        it('should throw if project directory is missing', () => {
            expect(() => new ProjectBuilder()).toThrowError(TypeError);
        });
    });

    describe('getArgs', () => {
        beforeEach(() => {
            spyOn(fs, 'existsSync').and.returnValue(true);
        });

        it('should set release argument', () => {
            const args = builder.getArgs('release', {});
            expect(args[args.length - 1]).toBe('cdvBuildRelease');
        });

        it('should set debug argument', () => {
            const args = builder.getArgs('debug', {});
            expect(args[args.length - 1]).toBe('cdvBuildDebug');
        });

        it('should set apk release', () => {
            const args = builder.getArgs('release', {
                packageType: 'apk'
            });
            expect(args[args.length - 1]).withContext(args).toBe('cdvBuildRelease');
        });

        it('should set apk debug', () => {
            const args = builder.getArgs('debug', {
                packageType: 'apk'
            });
            expect(args[args.length - 1]).withContext(args).toBe('cdvBuildDebug');
        });

        it('should set bundle release', () => {
            const args = builder.getArgs('release', {
                packageType: 'bundle'
            });
            expect(args[args.length - 1]).withContext(args).toBe(':app:bundleRelease');
        });

        it('should set bundle debug', () => {
            const args = builder.getArgs('debug', {
                packageType: 'bundle'
            });
            expect(args[args.length - 1]).withContext(args).toBe(':app:bundleDebug');
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
            expect(args[args.length - 1]).toBe('clean');
        });

        it('should clean bundle', () => {
            const args = builder.getArgs('clean', {
                packageType: 'bundle'
            });
            expect(args[args.length - 1]).toBe('clean');
        });

        describe('should accept extra arguments', () => {
            it('apk', () => {
                const args = builder.getArgs('debug', {
                    extraArgs: ['-PcdvVersionCode=12344']
                });
                expect(args).toContain('-PcdvVersionCode=12344');
            });

            it('bundle', () => {
                const args = builder.getArgs('debug', {
                    packageType: 'bundle',
                    extraArgs: ['-PcdvVersionCode=12344']
                });
                expect(args).toContain('-PcdvVersionCode=12344');
            });
        });
    });

    describe('installGradleWrapper', () => {
        beforeEach(() => {
            execaSpy.and.resolveTo();
        });

        it('should run gradle wrapper 8.7', async () => {
            await builder.installGradleWrapper('8.7');
            expect(execaSpy).toHaveBeenCalledWith('gradle', ['-p', path.normalize('/root/tools'), 'wrapper', '--gradle-version', '8.7'], jasmine.any(Object));
        });

        it('CORDOVA_ANDROID_GRADLE_DISTRIBUTION_URL should override gradle version', async () => {
            process.env.CORDOVA_ANDROID_GRADLE_DISTRIBUTION_URL = 'https://dist.local';
            await builder.installGradleWrapper('8.7');
            delete process.env.CORDOVA_ANDROID_GRADLE_DISTRIBUTION_URL;
            expect(execaSpy).toHaveBeenCalledWith('gradle', ['-p', path.normalize('/root/tools'), 'wrapper', '--gradle-distribution-url', 'https://dist.local'], jasmine.any(Object));
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

            let gradle = path.join(rootDir, 'tools', 'gradlew');
            if (isWindows()) {
                gradle += '.bat';
            }

            expect(execaSpy).toHaveBeenCalledWith(gradle, testArgs, jasmine.anything());
        });

        it('should reject if the spawn fails', () => {
            const errorMessage = 'Test error';
            execaSpy.and.rejectWith(new Error(errorMessage));
            builder.getArgs.and.returnValue([]);

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
            builder.getArgs.and.returnValue([]);

            return builder.build({}).then(
                () => fail('Unexpectedly resolved'),
                error => {
                    expect(checkReqsSpy.check_android_target).toHaveBeenCalledWith(rootDir);
                    expect(error).toBe(testError);
                }
            );
        });
    });

    describe('clean', () => {
        beforeEach(() => {
            const marker = ProjectBuilder.__get__('MARKER');
            spyOn(fs, 'readFileSync').and.returnValue(`Some Header Here: ${marker}`);
            spyOn(fs, 'rmSync');
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

            let gradle = path.join(rootDir, 'tools', 'gradlew');
            if (isWindows()) {
                gradle += '.bat';
            }

            return builder.clean(opts).then(() => {
                expect(execaSpy).toHaveBeenCalledWith(gradle, gradleArgs, jasmine.anything());
            });
        });

        it('should remove "out" folder', () => {
            return builder.clean({}).then(() => {
                expect(fs.rmSync).toHaveBeenCalledWith(path.join(rootDir, 'out'));
            });
        });

        it('should remove signing files if they are autogenerated', () => {
            const debugSigningFile = path.join(rootDir, 'debug-signing.properties');
            const releaseSigningFile = path.join(rootDir, 'release-signing.properties');

            spyOn(fs, 'existsSync').and.returnValue(true);

            return builder.clean({}).then(() => {
                expect(fs.rmSync).toHaveBeenCalledWith(debugSigningFile);
                expect(fs.rmSync).toHaveBeenCalledWith(releaseSigningFile);
            });
        });

        it('should not remove signing files if they are not autogenerated', () => {
            const debugSigningFile = path.join(rootDir, 'debug-signing.properties');
            const releaseSigningFile = path.join(rootDir, 'release-signing.properties');

            spyOn(fs, 'existsSync').and.returnValue(false);

            return builder.clean({}).then(() => {
                expect(fs.rmSync).not.toHaveBeenCalledWith(debugSigningFile);
                expect(fs.rmSync).not.toHaveBeenCalledWith(releaseSigningFile);
            });
        });
    });

    describe('outputFileComparator', () => {
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
            const sortedApks = apkArray.sort(ProjectBuilder.__get__('outputFileComparator'));

            expect(sortedApks).toEqual(expectedResult);
        });
    });
});
