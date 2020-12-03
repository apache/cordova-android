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

    describe('install', () => {
        let AndroidManifestSpy;
        let AndroidManifestFns;
        let AndroidManifestGetActivitySpy;
        let AdbSpy;
        let buildSpy;
        let installTarget;

        beforeEach(() => {
            installTarget = { target: 'emulator-5556', isEmulator: true, arch: 'atari' };

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
                expect(AdbSpy.install.calls.argsFor(0)[0]).toBe(installTarget.target);
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
                expect(AdbSpy.shell).toHaveBeenCalledWith(installTarget.target, 'input keyevent 82');
            });
        });

        it('should start the newly installed app on the device', () => {
            const packageId = 'unittestapp';
            const activityName = 'TestActivity';
            AndroidManifestFns.getPackageId.and.returnValue(packageId);
            AndroidManifestGetActivitySpy.getName.and.returnValue(activityName);

            return target.install(installTarget, {}).then(() => {
                expect(AdbSpy.start).toHaveBeenCalledWith(installTarget.target, `${packageId}/.${activityName}`);
            });
        });
    });
});
