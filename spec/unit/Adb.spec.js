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

const adbOutput = `List of devices attached
emulator-5554\tdevice
emulator-5556\toffline
123a76565509e124\tdevice
123a76565509e123\tbootloader
`;

describe('Adb', () => {
    const deviceId = '123a76565509e124';

    const alreadyExistsError = 'adb: failed to install app.apk: Failure[INSTALL_FAILED_ALREADY_EXISTS]';
    const certificateError = 'adb: failed to install app.apk: Failure[INSTALL_PARSE_FAILED_NO_CERTIFICATES]';
    const downgradeError = 'adb: failed to install app.apk: Failure[INSTALL_FAILED_VERSION_DOWNGRADE]';

    let Adb;
    let execaSpy;

    beforeEach(() => {
        Adb = rewire('../../lib/Adb');
        execaSpy = jasmine.createSpy('execa');
        Adb.__set__('execa', execaSpy);
    });

    describe('devices', () => {
        it('should return the IDs of all fully booted devices & emulators', () => {
            execaSpy.and.resolveTo({ stdout: adbOutput });

            return Adb.devices().then(devices => {
                expect(devices).toEqual([
                    'emulator-5554',
                    '123a76565509e124'
                ]);
            });
        });
    });

    describe('install', () => {
        beforeEach(() => {
            execaSpy.and.returnValue(Promise.resolve({ stdout: '' }));
        });

        it('should target the passed device id to adb', () => {
            return Adb.install(deviceId).then(() => {
                const args = execaSpy.calls.argsFor(0);
                expect(args[0]).toBe('adb');

                const adbArgs = args[1].join(' ');
                expect(adbArgs).toMatch(`-s ${deviceId}`);
            });
        });

        it('should add the -r flag if opts.replace is set', () => {
            return Adb.install(deviceId, '', { replace: true }).then(() => {
                const adbArgs = execaSpy.calls.argsFor(0)[1];
                expect(adbArgs).toContain('-r');
            });
        });

        it('should pass the correct package path to adb', () => {
            const packagePath = 'build/test/app.apk';

            return Adb.install(deviceId, packagePath).then(() => {
                const adbArgs = execaSpy.calls.argsFor(0)[1];
                expect(adbArgs).toContain(packagePath);
            });
        });

        it('should reject with a CordovaError if the adb output suggests a failure', () => {
            execaSpy.and.returnValue(Promise.resolve({ stdout: alreadyExistsError }));

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
            execaSpy.and.returnValue(Promise.resolve({ stdout: certificateError }));

            return Adb.install(deviceId, '').then(
                () => fail('Unexpectedly resolved'),
                err => {
                    expect(err).toEqual(jasmine.any(CordovaError));
                    expect(err.message).toMatch('Sign the build');
                }
            );
        });

        it('should give a more specific error message if there is a downgrade error', () => {
            execaSpy.and.returnValue(Promise.resolve({ stdout: downgradeError }));

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
            execaSpy.and.returnValue(Promise.resolve({ stdout: '' }));

            return Adb.uninstall(deviceId, packageId).then(() => {
                const args = execaSpy.calls.argsFor(0);
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
            execaSpy.and.returnValue(Promise.resolve({ stdout: '' }));

            return Adb.shell(deviceId, shellCommand).then(() => {
                const args = execaSpy.calls.argsFor(0);
                expect(args[0]).toBe('adb');

                const adbArgs = args[1].join(' ');
                expect(adbArgs).toContain('shell');
                expect(adbArgs).toContain(`-s ${deviceId}`);
                expect(adbArgs).toMatch(new RegExp(`${shellCommand}$`));
            });
        });

        it('should reject with a CordovaError on failure', () => {
            const errorMessage = 'shell error';
            execaSpy.and.rejectWith(new Error(errorMessage));

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
            spyOn(Adb, 'shell').and.rejectWith(new CordovaError(errorMessage));

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
