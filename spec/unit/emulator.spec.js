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

const fs = require('fs-extra');
const path = require('path');
const rewire = require('rewire');
const which = require('which');

const CordovaError = require('cordova-common').CordovaError;
const check_reqs = require('../../bin/templates/cordova/lib/check_reqs');

describe('emulator', () => {
    const EMULATOR_LIST = ['emulator-5555', 'emulator-5556', 'emulator-5557'];
    let emu;

    beforeEach(() => {
        emu = rewire('../../bin/templates/cordova/lib/emulator');
    });

    describe('list_images_using_avdmanager', () => {
        it('should properly parse details of SDK Tools 25.3.1 `avdmanager` output', () => {
            const avdList = fs.readFileSync(path.join('spec', 'fixtures', 'sdk25.3-avdmanager_list_avd.txt'), 'utf-8');

            const execaSpy = jasmine.createSpy('execa').and.returnValue(Promise.resolve({ stdout: avdList }));
            emu.__set__('execa', execaSpy);

            return emu.list_images_using_avdmanager().then(list => {
                expect(list).toBeDefined();
                expect(list[0].name).toEqual('nexus5-5.1');
                expect(list[0].target).toEqual('Android 5.1 (API level 22)');
                expect(list[1].device).toEqual('pixel (Google)');
                expect(list[2].abi).toEqual('default/x86_64');
            });
        });
    });

    describe('list_images', () => {
        beforeEach(() => {
            spyOn(fs, 'realpathSync').and.callFake(cmd => cmd);
        });

        it('should try to parse AVD information using `avdmanager` first', () => {
            spyOn(which, 'sync').and.callFake(cmd => cmd === 'avdmanager');

            const avdmanager_spy = spyOn(emu, 'list_images_using_avdmanager').and.returnValue(Promise.resolve([]));

            return emu.list_images().then(() => {
                expect(avdmanager_spy).toHaveBeenCalled();
            });
        });

        it('should correct api level information and fill in the blanks about api level if exists', () => {
            spyOn(which, 'sync').and.callFake(cmd => cmd === 'avdmanager');
            spyOn(emu, 'list_images_using_avdmanager').and.returnValue(Promise.resolve([
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
            ]));

            return emu.list_images().then(avds => {
                expect(avds[1].target).toContain('Android 8');
                expect(avds[1].target).toContain('API level 26');
            });
        });

        it('should throw an error if neither `avdmanager` nor `android` are able to be found', () => {
            spyOn(which, 'sync').and.returnValue(false);

            return emu.list_images().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Could not find `avdmanager`');
                }
            );
        });
    });

    describe('best_image', () => {
        let target_mock;

        beforeEach(() => {
            spyOn(emu, 'list_images');
            target_mock = spyOn(check_reqs, 'get_target').and.returnValue('android-26');
        });

        it('should return undefined if there are no defined AVDs', () => {
            emu.list_images.and.returnValue(Promise.resolve([]));

            return emu.best_image().then(best_avd => {
                expect(best_avd).toBeUndefined();
            });
        });

        it('should return the first available image if there is no available target information for existing AVDs', () => {
            const fake_avd = { name: 'MyFakeAVD' };
            const second_fake_avd = { name: 'AnotherAVD' };
            emu.list_images.and.returnValue(Promise.resolve([fake_avd, second_fake_avd]));

            return emu.best_image().then(best_avd => {
                expect(best_avd).toBe(fake_avd);
            });
        });

        it('should return the first AVD for the API level that matches the project target', () => {
            target_mock.and.returnValue('android-25');
            const fake_avd = { name: 'MyFakeAVD', target: 'Android 7.0 (API level 24)' };
            const second_fake_avd = { name: 'AnotherAVD', target: 'Android 7.1 (API level 25)' };
            const third_fake_avd = { name: 'AVDThree', target: 'Android 8.0 (API level 26)' };
            emu.list_images.and.returnValue(Promise.resolve([fake_avd, second_fake_avd, third_fake_avd]));

            return emu.best_image().then(best_avd => {
                expect(best_avd).toBe(second_fake_avd);
            });
        });

        it('should return the AVD with API level that is closest to the project target API level, without going over', () => {
            target_mock.and.returnValue('android-26');
            const fake_avd = { name: 'MyFakeAVD', target: 'Android 7.0 (API level 24)' };
            const second_fake_avd = { name: 'AnotherAVD', target: 'Android 7.1 (API level 25)' };
            const third_fake_avd = { name: 'AVDThree', target: 'Android 99.0 (API level 134)' };
            emu.list_images.and.returnValue(Promise.resolve([fake_avd, second_fake_avd, third_fake_avd]));

            return emu.best_image().then(best_avd => {
                expect(best_avd).toBe(second_fake_avd);
            });
        });

        it('should not try to compare API levels when an AVD definition is missing API level info', () => {
            emu.list_images.and.returnValue(Promise.resolve([{
                name: 'Samsung_S8_API_26',
                device: 'Samsung S8+ (User)',
                path: '/Users/daviesd/.android/avd/Samsung_S8_API_26.avd',
                abi: 'google_apis/x86',
                target: 'Android 8.0'
            }]));

            return emu.best_image().then(best_avd => {
                expect(best_avd).toBeDefined();
            });
        });
    });

    describe('list_started', () => {
        it('should return a list of all online emulators', () => {
            const AdbSpy = jasmine.createSpyObj('Adb', ['devices']);
            AdbSpy.devices.and.resolveTo(['emulator-5556', '123a76565509e124']);
            emu.__set__('Adb', AdbSpy);

            return emu.list_started().then(emus => {
                expect(emus).toEqual(['emulator-5556']);
            });
        });
    });

    describe('get_available_port', () => {
        let emus;

        beforeEach(() => {
            emus = [];
            spyOn(emu, 'list_started').and.returnValue(Promise.resolve(emus));
        });

        it('should find the closest available port below 5584 for the emulator', () => {
            const lowestUsedPort = 5565;
            for (let i = 5584; i >= lowestUsedPort; i--) {
                emus.push(`emulator-${i}`);
            }

            return emu.get_available_port().then(port => {
                expect(port).toBe(lowestUsedPort - 1);
            });
        });

        it('should throw an error if no port is available between 5554 and 5584', () => {
            for (let i = 5584; i >= 5554; i--) {
                emus.push(`emulator-${i}`);
            }

            return emu.get_available_port().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                }
            );
        });
    });

    describe('start', () => {
        const port = 5555;
        let emulator;
        let AdbSpy;
        let checkReqsSpy;
        let execaSpy;
        let whichSpy;

        beforeEach(() => {
            emulator = {
                name: 'Samsung_S8_API_26',
                device: 'Samsung S8+ (User)',
                path: '/Users/daviesd/.android/avd/Samsung_S8_API_26.avd',
                abi: 'google_apis/x86',
                target: 'Android 8.0'
            };

            AdbSpy = jasmine.createSpyObj('Adb', ['shell']);
            AdbSpy.shell.and.returnValue(Promise.resolve());
            emu.__set__('Adb', AdbSpy);

            checkReqsSpy = jasmine.createSpyObj('create_reqs', ['getAbsoluteAndroidCmd']);
            emu.__set__('check_reqs', checkReqsSpy);

            execaSpy = jasmine.createSpy('execa').and.returnValue(
                jasmine.createSpyObj('spawnFns', ['unref'])
            );
            emu.__set__('execa', execaSpy);

            spyOn(emu, 'get_available_port').and.returnValue(Promise.resolve(port));
            spyOn(emu, 'wait_for_emulator').and.returnValue(Promise.resolve('randomname'));
            spyOn(emu, 'wait_for_boot').and.returnValue(Promise.resolve());

            // Prevent pollution of the test logs
            const proc = emu.__get__('process');
            spyOn(proc.stdout, 'write').and.stub();

            whichSpy = jasmine.createSpyObj('which', ['sync']);
            whichSpy.sync.and.returnValue('/dev/android-sdk/tools');
            emu.__set__('which', whichSpy);
        });

        it('should find an emulator if an id is not specified', () => {
            spyOn(emu, 'best_image').and.returnValue(Promise.resolve(emulator));

            return emu.start().then(() => {
                // This is the earliest part in the code where we can hook in and check
                // the emulator that has been selected.
                const spawnArgs = execaSpy.calls.argsFor(0);
                expect(spawnArgs[1]).toContain(emulator.name);
            });
        });

        it('should use the specified emulator', () => {
            spyOn(emu, 'best_image');

            return emu.start(emulator.name).then(() => {
                expect(emu.best_image).not.toHaveBeenCalled();

                const spawnArgs = execaSpy.calls.argsFor(0);
                expect(spawnArgs[1]).toContain(emulator.name);
            });
        });

        it('should throw an error if no emulator is specified and no default is found', () => {
            spyOn(emu, 'best_image').and.returnValue(Promise.resolve());

            return emu.start().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                }
            );
        });

        it('should unlock the screen after the emulator has booted', () => {
            emu.wait_for_emulator.and.returnValue(Promise.resolve(emulator.name));
            emu.wait_for_boot.and.returnValue(Promise.resolve(true));

            return emu.start(emulator.name).then(() => {
                expect(emu.wait_for_emulator).toHaveBeenCalledBefore(AdbSpy.shell);
                expect(emu.wait_for_boot).toHaveBeenCalledBefore(AdbSpy.shell);
                expect(AdbSpy.shell).toHaveBeenCalledWith(emulator.name, 'input keyevent 82');
            });
        });

        it('should resolve with the emulator id after the emulator has started', () => {
            emu.wait_for_emulator.and.returnValue(Promise.resolve(emulator.name));
            emu.wait_for_boot.and.returnValue(Promise.resolve(true));

            return emu.start(emulator.name).then(emulatorId => {
                expect(emulatorId).toBe(emulator.name);
            });
        });

        it('should resolve with null if starting the emulator times out', () => {
            emu.wait_for_emulator.and.returnValue(Promise.resolve(emulator.name));
            emu.wait_for_boot.and.returnValue(Promise.resolve(false));

            return emu.start(emulator.name).then(emulatorId => {
                expect(emulatorId).toBe(null);
            });
        });
    });

    describe('wait_for_emulator', () => {
        const port = 5656;
        const expectedEmulatorId = `emulator-${port}`;
        let AdbSpy;

        beforeEach(() => {
            AdbSpy = jasmine.createSpyObj('Adb', ['shell']);
            AdbSpy.shell.and.returnValue(Promise.resolve());
            emu.__set__('Adb', AdbSpy);

            spyOn(emu, 'wait_for_emulator').and.callThrough();
        });

        it('should resolve with the emulator id if the emulator has completed boot', () => {
            AdbSpy.shell.and.callFake((emulatorId, shellArgs) => {
                expect(emulatorId).toBe(expectedEmulatorId);
                expect(shellArgs).toContain('getprop dev.bootcomplete');

                return Promise.resolve('1'); // 1 means boot is complete
            });

            return emu.wait_for_emulator(port).then(emulatorId => {
                expect(emulatorId).toBe(expectedEmulatorId);
            });
        });

        it('should call itself again if the emulator is not ready', () => {
            AdbSpy.shell.and.returnValues(
                Promise.resolve('0'),
                Promise.resolve('0'),
                Promise.resolve('1')
            );

            return emu.wait_for_emulator(port).then(() => {
                expect(emu.wait_for_emulator).toHaveBeenCalledTimes(3);
            });
        });

        it('should call itself again if shell fails for a known reason', () => {
            AdbSpy.shell.and.returnValues(
                Promise.reject({ message: 'device not found' }),
                Promise.reject({ message: 'device offline' }),
                Promise.reject({ message: 'device still connecting' }),
                Promise.resolve('1')
            );

            return emu.wait_for_emulator(port).then(() => {
                expect(emu.wait_for_emulator).toHaveBeenCalledTimes(4);
            });
        });

        it('should throw an error if shell fails for an unknown reason', () => {
            const errorMessage = { message: 'Some unknown error' };
            AdbSpy.shell.and.returnValue(Promise.reject(errorMessage));

            return emu.wait_for_emulator(port).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toBe(errorMessage);
                }
            );
        });
    });

    describe('wait_for_boot', () => {
        const port = 5656;
        const emulatorId = `emulator-${port}`;
        const psOutput = `
            root      1     0     8504   1512  SyS_epoll_ 00000000 S /init
            u0_a1     2044  1350  1423452 47256 SyS_epoll_ 00000000 S android.process.acore
            u0_a51    2963  1350  1417724 37492 SyS_epoll_ 00000000 S com.google.process.gapps
        `;

        let AdbSpy;

        beforeEach(() => {
            // If we use Jasmine's fake clock, we need to re-require the target module,
            // or else it will not work.
            jasmine.clock().install();
            emu = rewire('../../bin/templates/cordova/lib/emulator');

            AdbSpy = jasmine.createSpyObj('Adb', ['shell']);
            emu.__set__('Adb', AdbSpy);

            spyOn(emu, 'wait_for_boot').and.callThrough();

            // Stop the logs from being polluted
            const proc = emu.__get__('process');
            spyOn(proc.stdout, 'write').and.stub();
        });

        afterEach(() => {
            jasmine.clock().uninstall();
        });

        it('should resolve with true if the system has booted', () => {
            AdbSpy.shell.and.callFake((emuId, shellArgs) => {
                expect(emuId).toBe(emulatorId);
                expect(shellArgs).toContain('getprop sys.boot_completed');

                return Promise.resolve(psOutput);
            });

            return emu.wait_for_boot(emulatorId).then(isReady => {
                expect(isReady).toBe(true);
            });
        });

        it('should should check boot status at regular intervals until ready', () => {
            const retryInterval = emu.__get__('CHECK_BOOTED_INTERVAL');
            const RETRIES = 10;

            let shellPromise = Promise.resolve('');
            AdbSpy.shell.and.returnValue(shellPromise);

            const waitPromise = emu.wait_for_boot(emulatorId);

            let attempts = 0;
            function tickTimer () {
                shellPromise.then(() => {
                    if (attempts + 1 === RETRIES) {
                        AdbSpy.shell.and.returnValue(Promise.resolve(psOutput));
                        jasmine.clock().tick(retryInterval);
                    } else {
                        attempts++;
                        shellPromise = Promise.resolve('');
                        AdbSpy.shell.and.returnValue(shellPromise);
                        jasmine.clock().tick(retryInterval);
                        tickTimer();
                    }
                });
            }

            tickTimer();

            // After all the retries and eventual success, this is called
            return waitPromise.then(isReady => {
                expect(isReady).toBe(true);
                expect(emu.wait_for_boot).toHaveBeenCalledTimes(RETRIES + 1);
            });
        });

        it('should should check boot status at regular intervals until timeout', () => {
            const retryInterval = emu.__get__('CHECK_BOOTED_INTERVAL');
            const TIMEOUT = 9000;
            const expectedRetries = Math.floor(TIMEOUT / retryInterval);

            let shellPromise = Promise.resolve('');
            AdbSpy.shell.and.returnValue(shellPromise);

            const waitPromise = emu.wait_for_boot(emulatorId, TIMEOUT);

            let attempts = 0;
            function tickTimer () {
                shellPromise.then(() => {
                    attempts++;
                    shellPromise = Promise.resolve('');
                    AdbSpy.shell.and.returnValue(shellPromise);
                    jasmine.clock().tick(retryInterval);

                    if (attempts < expectedRetries) {
                        tickTimer();
                    }
                });
            }

            tickTimer();

            // After all the retries and eventual success, this is called
            return waitPromise.then(isReady => {
                expect(isReady).toBe(false);
                expect(emu.wait_for_boot).toHaveBeenCalledTimes(expectedRetries + 1);
            });
        });
    });

    describe('resolveTarget', () => {
        const arch = 'arm7-test';

        beforeEach(() => {
            const buildSpy = jasmine.createSpyObj('build', ['detectArchitecture']);
            buildSpy.detectArchitecture.and.returnValue(Promise.resolve(arch));
            emu.__set__('build', buildSpy);

            spyOn(emu, 'list_started').and.returnValue(Promise.resolve(EMULATOR_LIST));
        });

        it('should throw an error if there are no running emulators', () => {
            emu.list_started.and.returnValue(Promise.resolve([]));

            return emu.resolveTarget().then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                }
            );
        });

        it('should throw an error if the requested emulator is not running', () => {
            const targetEmulator = 'unstarted-emu';

            return emu.resolveTarget(targetEmulator).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err.message).toContain(targetEmulator);
                }
            );
        });

        it('should return info on the first running emulator if none is specified', () => {
            return emu.resolveTarget().then(emulatorInfo => {
                expect(emulatorInfo.target).toBe(EMULATOR_LIST[0]);
            });
        });

        it('should return the emulator info', () => {
            return emu.resolveTarget(EMULATOR_LIST[1]).then(emulatorInfo => {
                expect(emulatorInfo).toEqual({ target: EMULATOR_LIST[1], arch, isEmulator: true });
            });
        });
    });

    describe('install', () => {
        let AndroidManifestSpy;
        let AndroidManifestFns;
        let AndroidManifestGetActivitySpy;
        let AdbSpy;
        let buildSpy;
        let target;

        beforeEach(() => {
            target = { target: EMULATOR_LIST[1], arch: 'arm7', isEmulator: true };

            buildSpy = jasmine.createSpyObj('build', ['findBestApkForArchitecture']);
            emu.__set__('build', buildSpy);

            AndroidManifestFns = jasmine.createSpyObj('AndroidManifestFns', ['getPackageId', 'getActivity']);
            AndroidManifestGetActivitySpy = jasmine.createSpyObj('getActivity', ['getName']);
            AndroidManifestFns.getActivity.and.returnValue(AndroidManifestGetActivitySpy);
            AndroidManifestSpy = jasmine.createSpy('AndroidManifest').and.returnValue(AndroidManifestFns);
            emu.__set__('AndroidManifest', AndroidManifestSpy);

            AdbSpy = jasmine.createSpyObj('Adb', ['shell', 'start', 'install', 'uninstall']);
            AdbSpy.shell.and.returnValue(Promise.resolve());
            AdbSpy.start.and.returnValue(Promise.resolve());
            AdbSpy.install.and.returnValue(Promise.resolve());
            AdbSpy.uninstall.and.returnValue(Promise.resolve());
            emu.__set__('Adb', AdbSpy);
        });

        it('should get the full target object if only id is specified', () => {
            const targetId = target.target;
            spyOn(emu, 'resolveTarget').and.returnValue(Promise.resolve(target));

            return emu.install(targetId, {}).then(() => {
                expect(emu.resolveTarget).toHaveBeenCalledWith(targetId);
            });
        });

        it('should install to the passed target', () => {
            return emu.install(target, {}).then(() => {
                expect(AdbSpy.install.calls.argsFor(0)[0]).toBe(target.target);
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

            return emu.install(target, buildResults).then(() => {
                expect(buildSpy.findBestApkForArchitecture).toHaveBeenCalledWith(buildResults, target.arch);

                expect(AdbSpy.install.calls.argsFor(0)[1]).toBe(apkPath);
            });
        });

        it('should uninstall and reinstall app if failure is due to different certificates', () => {
            AdbSpy.install.and.returnValues(
                Promise.reject('Failure: INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES'),
                Promise.resolve()
            );

            return emu.install(target, {}).then(() => {
                expect(AdbSpy.install).toHaveBeenCalledTimes(2);
                expect(AdbSpy.uninstall).toHaveBeenCalled();
            });
        });

        it('should throw any error not caused by different certificates', () => {
            const errorMsg = 'Failure: Failed to install';
            AdbSpy.install.and.rejectWith(new CordovaError(errorMsg));

            return emu.install(target, {}).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toContain(errorMsg);
                }
            );
        });

        it('should unlock the screen on device', () => {
            return emu.install(target, {}).then(() => {
                expect(AdbSpy.shell).toHaveBeenCalledWith(target.target, 'input keyevent 82');
            });
        });

        it('should start the newly installed app on the device', () => {
            const packageId = 'unittestapp';
            const activityName = 'TestActivity';
            AndroidManifestFns.getPackageId.and.returnValue(packageId);
            AndroidManifestGetActivitySpy.getName.and.returnValue(activityName);

            return emu.install(target, {}).then(() => {
                expect(AdbSpy.start).toHaveBeenCalledWith(target.target, `${packageId}/.${activityName}`);
            });
        });
    });
});
