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

const rewire = require('rewire');

const CordovaError = require('cordova-common').CordovaError;

describe('device', () => {
    const DEVICE_LIST = ['device1', 'device2', 'device3'];
    let AdbSpy;
    let device;

    beforeEach(() => {
        device = rewire('../../bin/templates/cordova/lib/device');
        AdbSpy = jasmine.createSpyObj('Adb', ['devices', 'install', 'shell', 'start', 'uninstall']);
        device.__set__('Adb', AdbSpy);
    });

    describe('list', () => {
        it('should return the list from adb devices', () => {
            AdbSpy.devices.and.returnValue(Promise.resolve(DEVICE_LIST));

            return device.list().then(list => {
                expect(list).toEqual(DEVICE_LIST);
            });
        });
    });

    describe('resolveTarget', () => {
        let buildSpy;

        beforeEach(() => {
            buildSpy = jasmine.createSpyObj('build', ['detectArchitecture']);
            buildSpy.detectArchitecture.and.returnValue(Promise.resolve());
            device.__set__('build', buildSpy);

            spyOn(device, 'list').and.returnValue(Promise.resolve(DEVICE_LIST));
        });

        it('should select the first device to be the target if none is specified', () => {
            return device.resolveTarget().then(deviceInfo => {
                expect(deviceInfo.target).toBe(DEVICE_LIST[0]);
            });
        });

        it('should use the given target instead of the default', () => {
            return device.resolveTarget(DEVICE_LIST[2]).then(deviceInfo => {
                expect(deviceInfo.target).toBe(DEVICE_LIST[2]);
            });
        });

        it('should set emulator to false', () => {
            return device.resolveTarget().then(deviceInfo => {
                expect(deviceInfo.isEmulator).toBe(false);
            });
        });

        it('should throw an error if there are no devices', () => {
            device.list.and.returnValue(Promise.resolve([]));

            return device.resolveTarget().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                }
            );
        });

        it('should throw an error if the specified target does not exist', () => {
            return device.resolveTarget('nonexistent-target').then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                }
            );
        });

        it('should detect the architecture and return it with the device info', () => {
            const target = DEVICE_LIST[1];
            const arch = 'unittestarch';

            buildSpy.detectArchitecture.and.returnValue(Promise.resolve(arch));

            return device.resolveTarget(target).then(deviceInfo => {
                expect(buildSpy.detectArchitecture).toHaveBeenCalledWith(target);
                expect(deviceInfo.arch).toBe(arch);
            });
        });
    });

    describe('install', () => {
        let AndroidManifestSpy;
        let AndroidManifestFns;
        let AndroidManifestGetActivitySpy;
        let buildSpy;
        let target;

        beforeEach(() => {
            target = { target: DEVICE_LIST[0], arch: 'arm7', isEmulator: false };

            buildSpy = jasmine.createSpyObj('build', ['findBestApkForArchitecture']);
            device.__set__('build', buildSpy);

            AndroidManifestFns = jasmine.createSpyObj('AndroidManifestFns', ['getPackageId', 'getActivity']);
            AndroidManifestGetActivitySpy = jasmine.createSpyObj('getActivity', ['getName']);
            AndroidManifestFns.getActivity.and.returnValue(AndroidManifestGetActivitySpy);
            AndroidManifestSpy = jasmine.createSpy('AndroidManifest').and.returnValue(AndroidManifestFns);
            device.__set__('AndroidManifest', AndroidManifestSpy);

            AdbSpy.install.and.returnValue(Promise.resolve());
            AdbSpy.shell.and.returnValue(Promise.resolve());
            AdbSpy.start.and.returnValue(Promise.resolve());
        });

        it('should get the full target object if only id is specified', () => {
            const targetId = DEVICE_LIST[0];
            spyOn(device, 'resolveTarget').and.returnValue(Promise.resolve(target));

            return device.install(targetId).then(() => {
                expect(device.resolveTarget).toHaveBeenCalledWith(targetId);
            });
        });

        it('should install to the passed target', () => {
            return device.install(target).then(() => {
                expect(AdbSpy.install).toHaveBeenCalledWith(target.target, undefined, jasmine.anything());
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

            return device.install(target, buildResults).then(() => {
                expect(buildSpy.findBestApkForArchitecture).toHaveBeenCalledWith(buildResults, target.arch);
                expect(AdbSpy.install).toHaveBeenCalledWith(jasmine.anything(), apkPath, jasmine.anything());
            });
        });

        it('should uninstall and reinstall app if failure is due to different certificates', () => {
            AdbSpy.install.and.returnValues(
                Promise.reject('Failed to install: INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES'),
                Promise.resolve()
            );

            AdbSpy.uninstall.and.callFake(() => {
                expect(AdbSpy.install).toHaveBeenCalledTimes(1);
                return Promise.resolve();
            });

            return device.install(target).then(() => {
                expect(AdbSpy.install).toHaveBeenCalledTimes(2);
                expect(AdbSpy.uninstall).toHaveBeenCalled();
            });
        });

        it('should throw any error not caused by different certificates', () => {
            const errorMsg = new CordovaError('Failed to install');
            AdbSpy.install.and.returnValues(Promise.reject(errorMsg));

            return device.install(target).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toBe(errorMsg);
                }
            );
        });

        it('should unlock the screen on device', () => {
            return device.install(target).then(() => {
                expect(AdbSpy.shell).toHaveBeenCalledWith(target.target, 'input keyevent 82');
            });
        });

        it('should start the newly installed app on the device', () => {
            const packageId = 'unittestapp';
            const activityName = 'TestActivity';
            AndroidManifestFns.getPackageId.and.returnValue(packageId);
            AndroidManifestGetActivitySpy.getName.and.returnValue(activityName);

            return device.install(target).then(() => {
                expect(AdbSpy.start).toHaveBeenCalledWith(target.target, `${packageId}/.${activityName}`);
            });
        });
    });
});
