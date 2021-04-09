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

    describe('buildTargetSpec', () => {
        it('Test#001 : should select correct target based on the run opts', () => {
            const buildTargetSpec = run.__get__('buildTargetSpec');

            expect(buildTargetSpec({ target: 'emu' })).toEqual({ id: 'emu' });
            expect(buildTargetSpec({ device: true })).toEqual({ type: 'device' });
            expect(buildTargetSpec({ emulator: true })).toEqual({ type: 'emulator' });
            expect(buildTargetSpec({})).toEqual({});
        });
    });

    describe('run method', () => {
        let targetSpyObj, emulatorSpyObj, resolvedTarget;

        beforeEach(() => {
            resolvedTarget = { id: 'dev1', type: 'device', arch: 'atari' };

            targetSpyObj = jasmine.createSpyObj('deviceSpy', ['resolve', 'install']);
            targetSpyObj.resolve.and.resolveTo(resolvedTarget);
            targetSpyObj.install.and.resolveTo();

            emulatorSpyObj = jasmine.createSpyObj('emulatorSpy', ['wait_for_boot']);
            emulatorSpyObj.wait_for_boot.and.resolveTo();

            run.__set__({
                target: targetSpyObj,
                emulator: emulatorSpyObj
            });

            // run needs `this` to behave like an Api instance
            run.run = run.run.bind({
                _builder: builders.getBuilder('FakeRootPath')
            });
        });

        it('should install on target after build', () => {
            return run.run().then(() => {
                expect(targetSpyObj.install).toHaveBeenCalledWith(
                    resolvedTarget,
                    { apkPaths: [], buildType: 'debug' }
                );
            });
        });

        it('should fail with the error message if --packageType=bundle setting is used', () => {
            targetSpyObj.resolve.and.resolveTo(resolvedTarget);
            return expectAsync(run.run({ argv: ['--packageType=bundle'] }))
                .toBeRejectedWith(jasmine.stringMatching(/Package type "bundle" is not supported/));
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
