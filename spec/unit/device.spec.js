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
        it('should return a list of all connected devices', () => {
            AdbSpy.devices.and.resolveTo(['emulator-5556', '123a76565509e124']);

            return device.list().then(list => {
                expect(list).toEqual(['123a76565509e124']);
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
});
