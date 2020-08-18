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
const { CordovaError } = require('cordova-common');

describe('target', () => {
    let target;

    beforeEach(() => {
        target = rewire('../../bin/templates/cordova/lib/target');
    });

    describe('list', () => {
        it('should return available targets from Adb.devices', () => {
            const AdbSpy = jasmine.createSpyObj('Adb', ['devices']);
            AdbSpy.devices.and.resolveTo(['emulator-5556', '123a76565509e124']);
            target.__set__('Adb', AdbSpy);

            return target.list().then(emus => {
                expect(emus).toEqual([
                    { id: 'emulator-5556', type: 'emulator' },
                    { id: '123a76565509e124', type: 'device' }
                ]);
            });
        });
    });

    describe('resolveToOnlineTarget', () => {
        let resolveToOnlineTarget, emus, devs;

        beforeEach(() => {
            resolveToOnlineTarget = target.__get__('resolveToOnlineTarget');

            emus = [
                { id: 'emu1', type: 'emulator' },
                { id: 'emu2', type: 'emulator' }
            ];
            devs = [
                { id: 'dev1', type: 'device' },
                { id: 'dev2', type: 'device' }
            ];

            spyOn(target, 'list').and.returnValue([...emus, ...devs]);
        });

        it('should return first device when no target arguments are specified', async () => {
            return resolveToOnlineTarget().then(result => {
                expect(result.id).toBe('dev1');
            });
        });

        it('should return first emulator when no target arguments are specified and no devices are found', async () => {
            target.list.and.resolveTo(emus);
            return resolveToOnlineTarget().then(result => {
                expect(result.id).toBe('emu1');
            });
        });

        it('should return first device when type device is specified', async () => {
            return resolveToOnlineTarget({ type: 'device' }).then(result => {
                expect(result.id).toBe('dev1');
            });
        });

        it('should return first running emulator when type emulator is specified', async () => {
            return resolveToOnlineTarget({ type: 'emulator' }).then(result => {
                expect(result.id).toBe('emu1');
            });
        });

        it('should return a device that matches given ID', async () => {
            return resolveToOnlineTarget({ id: 'dev2' }).then(result => {
                expect(result.id).toBe('dev2');
            });
        });

        it('should return a running emulator that matches given ID', async () => {
            return resolveToOnlineTarget({ id: 'emu2' }).then(result => {
                expect(result.id).toBe('emu2');
            });
        });

        it('should return null if there are no online targets', async () => {
            target.list.and.resolveTo([]);
            return expectAsync(resolveToOnlineTarget())
                .toBeResolvedTo(null);
        });

        it('should return null if no target matches given ID', async () => {
            return expectAsync(resolveToOnlineTarget({ id: 'foo' }))
                .toBeResolvedTo(null);
        });

        it('should return null if no target matches given type', async () => {
            target.list.and.resolveTo(devs);
            return expectAsync(resolveToOnlineTarget({ type: 'emulator' }))
                .toBeResolvedTo(null);
        });
    });

    describe('resolveToOfflineEmulator', () => {
        const emuId = 'emulator-5554';
        let resolveToOfflineEmulator, emulatorSpyObj;

        beforeEach(() => {
            resolveToOfflineEmulator = target.__get__('resolveToOfflineEmulator');

            emulatorSpyObj = jasmine.createSpyObj('emulatorSpy', ['start']);
            emulatorSpyObj.start.and.resolveTo(emuId);

            target.__set__({
                emulator: emulatorSpyObj,
                isEmulatorName: name => name.startsWith('emu')
            });
        });

        it('should start an emulator and run on that if none is running', () => {
            return resolveToOfflineEmulator().then(result => {
                expect(result).toEqual({ id: emuId, type: 'emulator' });
                expect(emulatorSpyObj.start).toHaveBeenCalled();
            });
        });

        it('should start named emulator and then run on it if it is specified', () => {
            return resolveToOfflineEmulator({ id: 'emu3' }).then(result => {
                expect(result).toEqual({ id: emuId, type: 'emulator' });
                expect(emulatorSpyObj.start).toHaveBeenCalledWith('emu3');
            });
        });

        it('should return null if given ID is not an avd name', () => {
            return resolveToOfflineEmulator({ id: 'dev1' }).then(result => {
                expect(result).toBe(null);
                expect(emulatorSpyObj.start).not.toHaveBeenCalled();
            });
        });

        it('should return null if given type is not emulator', () => {
            return resolveToOfflineEmulator({ type: 'device' }).then(result => {
                expect(result).toBe(null);
                expect(emulatorSpyObj.start).not.toHaveBeenCalled();
            });
        });
    });

    describe('resolve', () => {
        let resolveToOnlineTarget, resolveToOfflineEmulator;

        beforeEach(() => {
            resolveToOnlineTarget = jasmine.createSpy('resolveToOnlineTarget')
                .and.resolveTo(null);

            resolveToOfflineEmulator = jasmine.createSpy('resolveToOfflineEmulator')
                .and.resolveTo(null);

            target.__set__({
                resolveToOnlineTarget,
                resolveToOfflineEmulator,
                build: { detectArchitecture: id => id + '-arch' }
            });
        });

        it('should delegate to resolveToOnlineTarget', () => {
            const spec = { type: 'device' };
            resolveToOnlineTarget.and.resolveTo({ id: 'dev1', type: 'device' });

            return target.resolve(spec).then(result => {
                expect(result.id).toBe('dev1');
                expect(resolveToOnlineTarget).toHaveBeenCalledWith(spec);
                expect(resolveToOfflineEmulator).not.toHaveBeenCalled();
            });
        });

        it('should delegate to resolveToOfflineEmulator if resolveToOnlineTarget fails', () => {
            const spec = { type: 'emulator' };
            resolveToOfflineEmulator.and.resolveTo({ id: 'emu1', type: 'emulator' });

            return target.resolve(spec).then(result => {
                expect(result.id).toBe('emu1');
                expect(resolveToOnlineTarget).toHaveBeenCalledWith(spec);
                expect(resolveToOfflineEmulator).toHaveBeenCalledWith(spec);
            });
        });

        it('should add the target arch', () => {
            const spec = { type: 'device' };
            resolveToOnlineTarget.and.resolveTo({ id: 'dev1', type: 'device' });

            return target.resolve(spec).then(result => {
                expect(result.arch).toBe('dev1-arch');
            });
        });

        it('should throw an error if target cannot be resolved', () => {
            return expectAsync(target.resolve())
                .toBeRejectedWithError(/Could not find target matching/);
        });
    });

    describe('install', () => {
        let AndroidManifestSpy;
        let AndroidManifestFns;
        let AndroidManifestGetActivitySpy;
        let AdbSpy;
        let buildSpy;
        let installTarget;

        beforeEach(() => {
            installTarget = { id: 'emulator-5556', type: 'emulator', arch: 'atari' };

            buildSpy = jasmine.createSpyObj('build', ['findBestApkForArchitecture']);
            target.__set__('build', buildSpy);

            AndroidManifestFns = jasmine.createSpyObj('AndroidManifestFns', ['getPackageId', 'getActivity']);
            AndroidManifestGetActivitySpy = jasmine.createSpyObj('getActivity', ['getName']);
            AndroidManifestFns.getActivity.and.returnValue(AndroidManifestGetActivitySpy);
            AndroidManifestSpy = jasmine.createSpy('AndroidManifest').and.returnValue(AndroidManifestFns);
            target.__set__('AndroidManifest', AndroidManifestSpy);

            AdbSpy = jasmine.createSpyObj('Adb', ['shell', 'start', 'install', 'uninstall']);
            AdbSpy.shell.and.returnValue(Promise.resolve());
            AdbSpy.start.and.returnValue(Promise.resolve());
            AdbSpy.install.and.returnValue(Promise.resolve());
            AdbSpy.uninstall.and.returnValue(Promise.resolve());
            target.__set__('Adb', AdbSpy);

            // Silence output during test
            spyOn(target.__get__('events'), 'emit');
        });

        it('should install to the passed target', () => {
            return target.install(installTarget, {}).then(() => {
                expect(AdbSpy.install.calls.argsFor(0)[0]).toBe(installTarget.id);
            });
        });

        it('should install the correct apk based on the architecture and build results', () => {
            const buildResults = {
                apkPaths: 'path/to/apks',
                buildType: 'debug',
                buildMethod: 'foo'
            };

            const apkPath = 'my/apk/path/app.apk';
            buildSpy.findBestApkForArchitecture.and.returnValue(apkPath);

            return target.install(installTarget, buildResults).then(() => {
                expect(buildSpy.findBestApkForArchitecture).toHaveBeenCalledWith(buildResults, installTarget.arch);

                expect(AdbSpy.install.calls.argsFor(0)[1]).toBe(apkPath);
            });
        });

        it('should uninstall and reinstall app if failure is due to different certificates', () => {
            AdbSpy.install.and.returnValues(
                Promise.reject('Failure: INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES'),
                Promise.resolve()
            );

            return target.install(installTarget, {}).then(() => {
                expect(AdbSpy.install).toHaveBeenCalledTimes(2);
                expect(AdbSpy.uninstall).toHaveBeenCalled();
            });
        });

        it('should throw any error not caused by different certificates', () => {
            const errorMsg = 'Failure: Failed to install';
            AdbSpy.install.and.rejectWith(new CordovaError(errorMsg));

            return target.install(installTarget, {}).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toContain(errorMsg);
                }
            );
        });

        it('should unlock the screen on device', () => {
            return target.install(installTarget, {}).then(() => {
                expect(AdbSpy.shell).toHaveBeenCalledWith(installTarget.id, 'input keyevent 82');
            });
        });

        it('should start the newly installed app on the device', () => {
            const packageId = 'unittestapp';
            const activityName = 'TestActivity';
            AndroidManifestFns.getPackageId.and.returnValue(packageId);
            AndroidManifestGetActivitySpy.getName.and.returnValue(activityName);

            return target.install(installTarget, {}).then(() => {
                expect(AdbSpy.start).toHaveBeenCalledWith(installTarget.id, `${packageId}/.${activityName}`);
            });
        });
    });
});
