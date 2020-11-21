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
const builders = require('../../bin/templates/cordova/lib/builders/builders');

describe('run', () => {
    let run;

    beforeEach(() => {
        run = rewire('../../bin/templates/cordova/lib/run');
    });

    describe('getInstallTarget', () => {
        const targetOpts = { target: 'emu' };
        const deviceOpts = { device: true };
        const emulatorOpts = { emulator: true };
        const emptyOpts = {};

        it('Test#001 : should select correct target based on the run opts', () => {
            const getInstallTarget = run.__get__('getInstallTarget');
            expect(getInstallTarget(targetOpts)).toBe('emu');
            expect(getInstallTarget(deviceOpts)).toBe('--device');
            expect(getInstallTarget(emulatorOpts)).toBe('--emulator');
            expect(getInstallTarget(emptyOpts)).toBeUndefined();
        });
    });

    describe('run method', () => {
        let deviceSpyObj;
        let emulatorSpyObj;
        let targetSpyObj;
        let eventsSpyObj;
        let getInstallTargetSpy;

        beforeEach(() => {
            deviceSpyObj = jasmine.createSpyObj('deviceSpy', ['list', 'resolveTarget']);
            emulatorSpyObj = jasmine.createSpyObj('emulatorSpy', ['list_images', 'list_started', 'resolveTarget', 'start', 'wait_for_boot']);
            eventsSpyObj = jasmine.createSpyObj('eventsSpy', ['emit']);
            getInstallTargetSpy = jasmine.createSpy('getInstallTargetSpy');

            targetSpyObj = jasmine.createSpyObj('target', ['install']);
            targetSpyObj.install.and.resolveTo();

            run.__set__({
                device: deviceSpyObj,
                emulator: emulatorSpyObj,
                target: targetSpyObj,
                events: eventsSpyObj,
                getInstallTarget: getInstallTargetSpy
            });

            // run needs `this` to behave like an Api instance
            run.run = run.run.bind({
                _builder: builders.getBuilder('FakeRootPath')
            });
        });

        it('should run on default device when no target arguments are specified', () => {
            const deviceList = ['testDevice1', 'testDevice2'];

            getInstallTargetSpy.and.returnValue(null);
            deviceSpyObj.list.and.returnValue(Promise.resolve(deviceList));

            return run.run().then(() => {
                expect(deviceSpyObj.resolveTarget).toHaveBeenCalledWith(deviceList[0]);
            });
        });

        it('should run on emulator when no target arguments are specified, and no devices are found', () => {
            const deviceList = [];

            getInstallTargetSpy.and.returnValue(null);
            deviceSpyObj.list.and.returnValue(Promise.resolve(deviceList));
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve([]));

            return run.run().then(() => {
                expect(emulatorSpyObj.list_started).toHaveBeenCalled();
            });
        });

        it('should run on default device when device is requested, but none specified', () => {
            getInstallTargetSpy.and.returnValue('--device');

            return run.run().then(() => {
                // Default device is selected by calling device.resolveTarget(null)
                expect(deviceSpyObj.resolveTarget).toHaveBeenCalledWith(null);
            });
        });

        it('should run on a running emulator if one exists', () => {
            const emulatorList = ['emulator1', 'emulator2'];

            getInstallTargetSpy.and.returnValue('--emulator');
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve(emulatorList));

            return run.run().then(() => {
                expect(emulatorSpyObj.resolveTarget).toHaveBeenCalledWith(emulatorList[0]);
            });
        });

        it('should start an emulator and run on that if none is running', () => {
            const emulatorList = [];
            const defaultEmulator = 'default-emu';

            getInstallTargetSpy.and.returnValue('--emulator');
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve(emulatorList));
            emulatorSpyObj.start.and.returnValue(Promise.resolve(defaultEmulator));

            return run.run().then(() => {
                expect(emulatorSpyObj.resolveTarget).toHaveBeenCalledWith(defaultEmulator);
            });
        });

        it('should run on a named device if it is specified', () => {
            const deviceList = ['device1', 'device2', 'device3'];
            getInstallTargetSpy.and.returnValue(deviceList[1]);

            deviceSpyObj.list.and.returnValue(Promise.resolve(deviceList));

            return run.run().then(() => {
                expect(deviceSpyObj.resolveTarget).toHaveBeenCalledWith(deviceList[1]);
            });
        });

        it('should run on a named emulator if it is specified', () => {
            const startedEmulatorList = ['emu1', 'emu2', 'emu3'];
            getInstallTargetSpy.and.returnValue(startedEmulatorList[2]);

            deviceSpyObj.list.and.returnValue(Promise.resolve([]));
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve(startedEmulatorList));

            return run.run().then(() => {
                expect(emulatorSpyObj.resolveTarget).toHaveBeenCalledWith(startedEmulatorList[2]);
            });
        });

        it('should start named emulator and then run on it if it is specified', () => {
            const emulatorList = [
                { name: 'emu1', id: 1 },
                { name: 'emu2', id: 2 },
                { name: 'emu3', id: 3 }
            ];
            getInstallTargetSpy.and.returnValue(emulatorList[2].name);

            deviceSpyObj.list.and.returnValue(Promise.resolve([]));
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve([]));
            emulatorSpyObj.list_images.and.returnValue(Promise.resolve(emulatorList));
            emulatorSpyObj.start.and.returnValue(Promise.resolve(emulatorList[2].id));

            return run.run().then(() => {
                expect(emulatorSpyObj.start).toHaveBeenCalledWith(emulatorList[2].name);
                expect(emulatorSpyObj.resolveTarget).toHaveBeenCalledWith(emulatorList[2].id);
            });
        });

        it('should throw an error if target is specified but does not exist', () => {
            const emulatorList = [{ name: 'emu1', id: 1 }];
            const deviceList = ['device1'];
            const target = 'nonexistentdevice';
            getInstallTargetSpy.and.returnValue(target);

            deviceSpyObj.list.and.returnValue(Promise.resolve(deviceList));
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve([]));
            emulatorSpyObj.list_images.and.returnValue(Promise.resolve(emulatorList));

            return run.run().then(
                () => fail('Expected error to be thrown'),
                err => expect(err.message).toContain(target)
            );
        });

        it('should install on device after build', () => {
            const deviceTarget = { target: 'device1', isEmulator: false };
            getInstallTargetSpy.and.returnValue('--device');
            deviceSpyObj.resolveTarget.and.returnValue(deviceTarget);

            return run.run().then(() => {
                expect(targetSpyObj.install).toHaveBeenCalledWith(deviceTarget, { apkPaths: [], buildType: 'debug' });
            });
        });

        it('should install on emulator after build', () => {
            const emulatorTarget = { target: 'emu1', isEmulator: true };
            getInstallTargetSpy.and.returnValue('--emulator');
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve([emulatorTarget.target]));
            emulatorSpyObj.resolveTarget.and.returnValue(emulatorTarget);
            emulatorSpyObj.wait_for_boot.and.returnValue(Promise.resolve());

            return run.run().then(() => {
                expect(targetSpyObj.install).toHaveBeenCalledWith(emulatorTarget, { apkPaths: [], buildType: 'debug' });
            });
        });

        it('should fail with the error message if --packageType=bundle setting is used', () => {
            const deviceList = ['testDevice1', 'testDevice2'];
            getInstallTargetSpy.and.returnValue(null);

            deviceSpyObj.list.and.returnValue(Promise.resolve(deviceList));

            return run.run({ argv: ['--packageType=bundle'] }).then(
                () => fail('Expected error to be thrown'),
                err => expect(err).toContain('Package type "bundle" is not supported during cordova run.')
            );
        });
    });

    describe('help', () => {
        it('should print out usage and help', () => {
            spyOn(console, 'log');
            spyOn(process, 'exit');

            run.help();
            expect(console.log).toHaveBeenCalledWith(jasmine.stringMatching(/^Usage:/));
        });
    });
});
