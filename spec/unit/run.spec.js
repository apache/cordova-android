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
        run.__set__({
            events: jasmine.createSpyObj('eventsSpy', ['emit'])
        });
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

    describe('resolveInstallTarget', () => {
        let resolveInstallTarget;
        let deviceSpyObj;
        let emulatorSpyObj;

        beforeEach(() => {
            resolveInstallTarget = run.__get__('resolveInstallTarget');

            deviceSpyObj = jasmine.createSpyObj('deviceSpy', ['list', 'resolveTarget']);
            emulatorSpyObj = jasmine.createSpyObj('emulatorSpy', ['list_images', 'list_started', 'resolveTarget', 'start']);

            async function fakeResolveTarget (target) {
                const list = await (this.list || this.list_started)();
                target = target || list[0];
                if (!list.length || !list.includes(target)) throw new Error('Unable to find target');
                return { target };
            }

            deviceSpyObj.resolveTarget.and.callFake(fakeResolveTarget);
            emulatorSpyObj.resolveTarget.and.callFake(fakeResolveTarget);

            deviceSpyObj.list.and.resolveTo(['dev1', 'dev2']);

            emulatorSpyObj.list_started.and.resolveTo(['running-emu']);
            emulatorSpyObj.list_images.and.resolveTo([1, 2, 3].map(i => ({ name: 'emu' + i })));
            emulatorSpyObj.start.and.callFake(async function (name = 'emu1') {
                (await this.list_started()).push(name);
                return name;
            });

            run.__set__({
                device: deviceSpyObj,
                emulator: emulatorSpyObj
            });
        });

        it('should run on default device when no target arguments are specified', () => {
            return resolveInstallTarget().then(result => {
                expect(result.target).toBe('dev1');
            });
        });

        it('should run on emulator when no target arguments are specified, and no devices are found', () => {
            deviceSpyObj.list.and.resolveTo([]);
            return resolveInstallTarget().then(result => {
                expect(result.target).toBe('running-emu');
            });
        });

        it('should run on default device when device is requested, but none specified', () => {
            return resolveInstallTarget('--device').then(result => {
                expect(result.target).toBe('dev1');
            });
        });

        it('should run on a running emulator if one exists', () => {
            return resolveInstallTarget('--emulator').then(result => {
                expect(result.target).toBe('running-emu');
            });
        });

        it('should start an emulator and run on that if none is running', () => {
            emulatorSpyObj.list_started.and.resolveTo([]);

            return resolveInstallTarget('--emulator').then(result => {
                expect(result.target).toBe('emu1');
                expect(emulatorSpyObj.start).toHaveBeenCalled();
            });
        });

        it('should run on a named device if it is specified', () => {
            return resolveInstallTarget('dev2').then(result => {
                expect(result.target).toBe('dev2');
            });
        });

        it('should run on a named emulator if it is specified', () => {
            emulatorSpyObj.list_started.and.resolveTo(['emu1', 'emu2']);

            return resolveInstallTarget('emu2').then(result => {
                expect(result.target).toBe('emu2');
            });
        });

        it('should start named emulator and then run on it if it is specified', () => {
            return resolveInstallTarget('emu3').then(result => {
                expect(result.target).toBe('emu3');
                expect(emulatorSpyObj.start).toHaveBeenCalledWith('emu3');
            });
        });

        it('should throw an error if target is specified but does not exist', () => {
            return expectAsync(resolveInstallTarget('nonexistent'))
                .toBeRejectedWithError(/nonexistent/);
        });
    });

    describe('run method', () => {
        let deviceSpyObj;
        let emulatorSpyObj;
        let getInstallTargetSpy;

        beforeEach(() => {
            deviceSpyObj = jasmine.createSpyObj('deviceSpy', ['install', 'list', 'resolveTarget']);
            emulatorSpyObj = jasmine.createSpyObj('emulatorSpy', ['install', 'list_images', 'list_started', 'resolveTarget', 'start', 'wait_for_boot']);
            getInstallTargetSpy = jasmine.createSpy('getInstallTargetSpy');

            run.__set__({
                device: deviceSpyObj,
                emulator: emulatorSpyObj,
                getInstallTarget: getInstallTargetSpy
            });

            // run needs `this` to behave like an Api instance
            run.run = run.run.bind({
                _builder: builders.getBuilder('FakeRootPath')
            });
        });

        it('should install on device after build', () => {
            const deviceTarget = { target: 'device1', isEmulator: false };
            getInstallTargetSpy.and.returnValue('--device');
            deviceSpyObj.resolveTarget.and.returnValue(deviceTarget);

            return run.run().then(() => {
                expect(deviceSpyObj.install).toHaveBeenCalledWith(deviceTarget, { apkPaths: [], buildType: 'debug' });
            });
        });

        it('should install on emulator after build', () => {
            const emulatorTarget = { target: 'emu1', isEmulator: true };
            getInstallTargetSpy.and.returnValue('--emulator');
            emulatorSpyObj.list_started.and.returnValue(Promise.resolve([emulatorTarget.target]));
            emulatorSpyObj.resolveTarget.and.returnValue(emulatorTarget);
            emulatorSpyObj.wait_for_boot.and.returnValue(Promise.resolve());

            return run.run().then(() => {
                expect(emulatorSpyObj.install).toHaveBeenCalledWith(emulatorTarget, { apkPaths: [], buildType: 'debug' });
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
