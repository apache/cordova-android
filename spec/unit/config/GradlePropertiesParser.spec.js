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
let GradlePropertiesParser = rewire('../../../bin/templates/cordova/lib/config/GradlePropertiesParser');

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

    describe('_configureDefaults method', () => {
        let parser;
        let emitSpy;

        beforeEach(() => {
            emitSpy = jasmine.createSpy('emit');

            GradlePropertiesParser.__set__('events', {
                emit: emitSpy
            });

            parser = new GradlePropertiesParser('/root');

            parser._defaults = {'org.gradle.jvmargs': '-Xmx2048m'};
        });

        it('should detect missing default property and sets the property.', () => {
            let setSpy = jasmine.createSpy('set');
            let getSpy = jasmine.createSpy('get').and.returnValue(false);

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureDefaults();

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).toHaveBeenCalled();
            expect(emitSpy.calls.argsFor(0)[1]).toContain('Appended missing default');
        });

        it('should not detect missing defaults and not call set.', () => {
            let setSpy = jasmine.createSpy('set');
            let getSpy = jasmine.createSpy('get').and.returnValue(true);

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureDefaults();

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).not.toHaveBeenCalled();
        });

        it('should detect default with changed value.', () => {
            let setSpy = jasmine.createSpy('set');
            let getSpy = jasmine.createSpy('get').and.returnValue('-Xmx512m');

            parser.gradleFile = {
                set: setSpy,
                get: getSpy
            };

            parser._configureDefaults();

            expect(getSpy).toHaveBeenCalled();
            expect(setSpy).not.toHaveBeenCalled();
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
});
