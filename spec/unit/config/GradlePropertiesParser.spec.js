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
const GradlePropertiesParser = rewire('../../../lib/config/GradlePropertiesParser');

describe('Gradle Builder', () => {
    describe('_initializeEditor method', () => {
        let parser;
        let writeFileSyncSpy;
        let existsSyncSpy;
        let createEditorSpy;
        let emitSpy;

        beforeEach(() => {
            createEditorSpy = jasmine.createSpy('createEditor');
            emitSpy = jasmine.createSpy('emit');

            GradlePropertiesParser.__set__('propertiesParser', {
                createEditor: createEditorSpy
            });

            GradlePropertiesParser.__set__('events', {
                emit: emitSpy
            });

            parser = new GradlePropertiesParser('/root');
        });

        it('should not detect an existing gradle.properties file and create new file', () => {
            existsSyncSpy = jasmine.createSpy('existsSync').and.returnValue(false);
            writeFileSyncSpy = jasmine.createSpy('writeFileSync');
            GradlePropertiesParser.__set__('fs', {
                existsSync: existsSyncSpy,
                writeFileSync: writeFileSyncSpy
            });

            parser._initializeEditor();

            expect(emitSpy).toHaveBeenCalled();
            expect(emitSpy.calls.argsFor(0)[1]).toContain('File missing, creating file with Cordova defaults');
            expect(writeFileSyncSpy).toHaveBeenCalled();
            expect(createEditorSpy).toHaveBeenCalled();
        });

        it('should detect an existing gradle.properties file', () => {
            existsSyncSpy = jasmine.createSpy('existsSync').and.returnValue(true);
            writeFileSyncSpy = jasmine.createSpy('writeFileSync');
            GradlePropertiesParser.__set__('fs', {
                existsSync: existsSyncSpy,
                writeFileSync: writeFileSyncSpy
            });

            parser._initializeEditor();

            expect(writeFileSyncSpy).not.toHaveBeenCalled();
            expect(createEditorSpy).toHaveBeenCalled();
        });
    });

    describe('_configureProperties method', () => {
        let parser;
        let emitSpy;

        beforeEach(() => {
            emitSpy = jasmine.createSpy('emit');

            GradlePropertiesParser.__set__('events', {
                emit: emitSpy
            });

            parser = new GradlePropertiesParser('/root');

            parser._defaults = { 'org.gradle.jvmargs': '-Xmx2048m' };
        });

        it('should detect missing default property and sets the property.', () => {
            const setSpy = jasmine.createSpy('set');
            const getSpy = jasmine.createSpy('get').and.returnValue(false);

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureProperties(parser._defaults);

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).toHaveBeenCalled();
            expect(emitSpy.calls.argsFor(0)[1]).toContain('Appending configuration item');
        });

        it('should not detect missing defaults and call set.', () => {
            const setSpy = jasmine.createSpy('set');
            const getSpy = jasmine.createSpy('get').and.returnValue(true);

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureProperties(parser._defaults);

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).toHaveBeenCalled();
        });

        it('should detect default with changed value to match default and set.', () => {
            const setSpy = jasmine.createSpy('set');
            const getSpy = jasmine.createSpy('get').and.returnValue('-Xmx512m');

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureProperties(parser._defaults);

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).toHaveBeenCalled();
            expect(emitSpy.calls.argsFor(0)[1]).toContain('Updating Gradle property');
        });

        it('should detect default with changed value different from default and set.', () => {
            const setSpy = jasmine.createSpy('set');
            const getSpy = jasmine.createSpy('get').and.returnValue('-Xmx2048m');

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureProperties({ 'org.gradle.jvmargs': '-Xmx512m' });

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).toHaveBeenCalled();
            expect(emitSpy.calls.argsFor(0)[1]).toContain('Cordova\'s recommended value is');
        });
    });

    describe('_save method', () => {
        let parser;
        let emitSpy;
        let saveSpy;

        beforeEach(() => {
            emitSpy = jasmine.createSpy('emit');
            GradlePropertiesParser.__set__('events', {
                emit: emitSpy
            });

            parser = new GradlePropertiesParser('/root');

            saveSpy = jasmine.createSpy('save');
            parser.gradleFile = {
                save: saveSpy
            };
        });

        it('should detect save being called.', () => {
            parser._save();

            expect(saveSpy).toHaveBeenCalled();
            expect(emitSpy.calls.argsFor(0)[1]).toContain('Updating and Saving File');
        });
    });

    describe('JVM Settings detection', () => {
        const parser = new GradlePropertiesParser('/root');

        describe('_getBaseJVMSize', () => {
            it('1024k = 1048576', () => {
                expect(parser._getBaseJVMSize(1024, 'k')).toBe(1048576);
                expect(parser._getBaseJVMSize(1024, 'K')).toBe(1048576);
            });

            it('1024m = 1073741824', () => {
                expect(parser._getBaseJVMSize(1024, 'm')).toBe(1073741824);
                expect(parser._getBaseJVMSize(1024, 'M')).toBe(1073741824);
            });

            it('2g = 2097152', () => {
                expect(parser._getBaseJVMSize(2, 'g')).toBe(2147483648);
                expect(parser._getBaseJVMSize(2, 'G')).toBe(2147483648);
            });

            it('unknown units should warn', () => {
                const emitSpy = jasmine.createSpy('emit');
                GradlePropertiesParser.__set__('events', {
                    emit: emitSpy
                });

                parser._getBaseJVMSize(1024, 'bad unit');
                expect(emitSpy.calls.argsFor(0)[1]).toContain('Unknown memory size unit');
            });
        });

        describe('JVM recommended tests', () => {
            const recommended = '-Xmx2048m';

            const tests = {
                // kb
                '1024k': true,
                '2097152k': false,
                '2097151k': true,
                '2097153k': false,

                // mb
                '1024m': true,
                '2048m': false,
                '2047m': true,
                '2049m': false,

                // gb
                '1g': true,
                '3g': false,
                '2g': false
            };

            for (const i in tests) {
                it(i + ' should return ' + tests[i], () => {
                    expect(parser._isJVMMemoryLessThanRecommended('-Xmx' + i, recommended)).toBe(tests[i]);
                });
            }
        });
    });
});
