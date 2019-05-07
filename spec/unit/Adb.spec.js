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

const CordovaError = require('cordova-common').CordovaError;
const rewire = require('rewire');

describe('Adb', () => {
    const adbOutput = `List of devices attached
emulator-5554\tdevice
123a76565509e124\tdevice`;
    const [, emulatorLine, deviceLine] = adbOutput.split('\n');
    const emulatorId = emulatorLine.split('\t')[0];
    const deviceId = deviceLine.split('\t')[0];

    const alreadyExistsError = 'adb: failed to install app.apk: Failure[INSTALL_FAILED_ALREADY_EXISTS]';
    const certificateError = 'adb: failed to install app.apk: Failure[INSTALL_PARSE_FAILED_NO_CERTIFICATES]';
    const downgradeError = 'adb: failed to install app.apk: Failure[INSTALL_FAILED_VERSION_DOWNGRADE]';

    let Adb;
    let spawnSpy;

    beforeEach(() => {
        Adb = rewire('../../bin/templates/cordova/lib/Adb');
        spawnSpy = jasmine.createSpy('spawn');
        Adb.__set__('spawn', spawnSpy);
    });

    describe('isDevice', () => {
        it('should return true for a real device', () => {
            const isDevice = Adb.__get__('isDevice');

            expect(isDevice(deviceLine)).toBeTruthy();
            expect(isDevice(emulatorLine)).toBeFalsy();
        });
    });

    describe('isEmulator', () => {
        it('should return true for an emulator', () => {
            const isEmulator = Adb.__get__('isEmulator');

            expect(isEmulator(emulatorLine)).toBeTruthy();
            expect(isEmulator(deviceLine)).toBeFalsy();
        });
    });

    describe('devices', () => {
        beforeEach(() => {
            spawnSpy.and.returnValue(Promise.resolve(adbOutput));
        });

        it('should return only devices if no options are specified', () => {
            return Adb.devices().then(devices => {
                expect(devices.length).toBe(1);
                expect(devices[0]).toBe(deviceId);
            });
        });

        it('should return only emulators if opts.emulators is true', () => {
            return Adb.devices({ emulators: true }).then(devices => {
                expect(devices.length).toBe(1);
                expect(devices[0]).toBe(emulatorId);
            });
        });
    });

    describe('install', () => {
        beforeEach(() => {
            spawnSpy.and.returnValue(Promise.resolve(''));
        });

        it('should target the passed device id to adb', () => {
            return Adb.install(deviceId).then(() => {
                const args = spawnSpy.calls.argsFor(0);
                expect(args[0]).toBe('adb');

                const adbArgs = args[1].join(' ');
                expect(adbArgs).toMatch(`-s ${deviceId}`);
            });
        });

        it('should add the -r flag if opts.replace is set', () => {
            return Adb.install(deviceId, '', { replace: true }).then(() => {
                const adbArgs = spawnSpy.calls.argsFor(0)[1];
                expect(adbArgs).toContain('-r');
            });
        });

        it('should pass the correct package path to adb', () => {
            const packagePath = 'build/test/app.apk';

            return Adb.install(deviceId, packagePath).then(() => {
                const adbArgs = spawnSpy.calls.argsFor(0)[1];
                expect(adbArgs).toContain(packagePath);
            });
        });

        it('should reject with a CordovaError if the adb output suggests a failure', () => {
            spawnSpy.and.returnValue(Promise.resolve(alreadyExistsError));

            return Adb.install(deviceId, '').then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                }
            );
        });

        // The following two tests are somewhat brittle as they are dependent on the
        // exact message returned. But it is better to have them tested than not at all.
        it('should give a more specific error message if there is a certificate failure', () => {
            spawnSpy.and.returnValue(Promise.resolve(certificateError));

            return Adb.install(deviceId, '').then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toMatch('Sign the build');
                }
            );
        });

        it('should give a more specific error message if there is a downgrade error', () => {
            spawnSpy.and.returnValue(Promise.resolve(downgradeError));

            return Adb.install(deviceId, '').then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toMatch('lower versionCode');
                }
            );
        });
    });

    describe('uninstall', () => {
        it('should call adb uninstall with the correct arguments', () => {
            const packageId = 'io.cordova.test';
            spawnSpy.and.returnValue(Promise.resolve(''));

            return Adb.uninstall(deviceId, packageId).then(() => {
                const args = spawnSpy.calls.argsFor(0);
                expect(args[0]).toBe('adb');

                const adbArgs = args[1];
                expect(adbArgs).toContain('uninstall');
                expect(adbArgs.join(' ')).toContain(`-s ${deviceId}`);
                expect(adbArgs[adbArgs.length - 1]).toBe(packageId);
            });
        });
    });

    describe('shell', () => {
        const shellCommand = 'ls -l /sdcard';

        it('should run the passed command on the target device', () => {
            spawnSpy.and.returnValue(Promise.resolve(''));

            return Adb.shell(deviceId, shellCommand).then(() => {
                const args = spawnSpy.calls.argsFor(0);
                expect(args[0]).toBe('adb');

                const adbArgs = args[1].join(' ');
                expect(adbArgs).toContain('shell');
                expect(adbArgs).toContain(`-s ${deviceId}`);
                expect(adbArgs).toMatch(new RegExp(`${shellCommand}$`));
            });
        });

        it('should reject with a CordovaError on failure', () => {
            const errorMessage = 'shell error';
            spawnSpy.and.returnValue(Promise.reject(errorMessage));

            return Adb.shell(deviceId, shellCommand).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toMatch(errorMessage);
                }
            );
        });
    });

    describe('start', () => {
        const activityName = 'io.cordova.test/.MainActivity';

        it('should start an activity using the shell activity manager', () => {
            const shellSpy = spyOn(Adb, 'shell').and.returnValue(Promise.resolve(''));

            return Adb.start(deviceId, activityName).then(() => {
                expect(shellSpy).toHaveBeenCalled();

                const [target, command] = shellSpy.calls.argsFor(0);
                expect(target).toBe(deviceId);
                expect(command).toContain('am start');
                expect(command).toContain(`-n${activityName}`);
            });
        });

        it('should reject with a CordovaError on a shell error', () => {
            const errorMessage = 'Test Start error';
            spyOn(Adb, 'shell').and.returnValue(Promise.reject(errorMessage));

            return Adb.start(deviceId, activityName).then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toMatch(errorMessage);
                }
            );
        });
    });

});
