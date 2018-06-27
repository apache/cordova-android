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

describe('GenericBuilder', () => {
    let GenericBuilder;
    let genericBuilder;

    beforeEach(() => {
        GenericBuilder = rewire('../../../bin/templates/cordova/lib/builders/GenericBuilder');
        GenericBuilder.__set__('__dirname', 'projecttest/platforms/android/app');

        genericBuilder = new GenericBuilder();
    });

    describe('constructor', () => {
        it('should set the project directory to the passed argument', () => {
            const projectDir = 'unit/test/dir';
            const genericBuilder = new GenericBuilder(projectDir);

            expect(genericBuilder.root).toBe(projectDir);
        });

        it('should set the project directory to three folders back', () => {
            expect(genericBuilder.root).toMatch(/projecttest$/);
        });
    });

    describe('prepEnv', () => {
        it('should return a promise', () => {
            expect(genericBuilder.prepEnv().then).toEqual(jasmine.any(Function));
        });
    });

    describe('build', () => {
        it('should return a promise', () => {
            expect(genericBuilder.build().then).toEqual(jasmine.any(Function));
        });
    });

    describe('clean', () => {
        it('should return a promise', () => {
            expect(genericBuilder.clean().then).toEqual(jasmine.any(Function));
        });
    });

    describe('apkSorter', () => {
        it('should sort APKs from most recent to oldest, deprioritising unsigned arch-specific builds', () => {
            const APKs = {
                'app-debug.apk': new Date('2018-04-20'),
                'app-release.apk': new Date('2018-05-20'),
                'app-debug-x86.apk': new Date('2018-06-01'),
                'app-release-x86.apk': new Date('2018-06-20'),
                'app-debug-arm.apk': new Date('2018-05-24'),
                'app-release-arm.apk': new Date('2018-06-24'),
                'app-release-unsigned.apk': new Date('2018-06-28')
            };

            const expectedResult = ['app-release.apk', 'app-debug.apk', 'app-release-unsigned.apk',
                'app-release-arm.apk', 'app-debug-arm.apk', 'app-release-x86.apk', 'app-debug-x86.apk'];

            const fsSpy = jasmine.createSpyObj('fs', ['statSync']);
            fsSpy.statSync.and.callFake(filename => {
                return { mtime: APKs[filename].getTime() };
            });
            GenericBuilder.__set__('fs', fsSpy);

            const apkArray = Object.keys(APKs);
            const sortedApks = apkArray.sort(GenericBuilder.__get__('apkSorter'));

            expect(sortedApks).toEqual(expectedResult);
        });
    });
});
