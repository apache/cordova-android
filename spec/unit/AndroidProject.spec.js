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

const path = require('path');
const rewire = require('rewire');

describe('AndroidProject', () => {
    const PROJECT_DIR = 'platforms/android';
    let AndroidProject;
    let AndroidStudioSpy;

    beforeEach(() => {
        AndroidProject = rewire('../../bin/templates/cordova/lib/AndroidProject');

        AndroidStudioSpy = jasmine.createSpyObj('AndroidStudio', ['isAndroidStudioProject']);
        AndroidProject.__set__('AndroidStudio', AndroidStudioSpy);
    });

    describe('constructor', () => {
        it('should set the project directory', () => {
            const project = new AndroidProject(PROJECT_DIR);
            expect(project.projectDir).toBe(PROJECT_DIR);
        });

        it('should set www folder correctly if it is an Android Studio project', () => {
            const project = new AndroidProject(PROJECT_DIR);
            expect(project.www).toBe(path.join(PROJECT_DIR, 'app/src/main/assets/www'));
        });
    });

    describe('getProjectFile', () => {
        it('should create and return a new project if one does not exist', () => {
            const newProject = AndroidProject.getProjectFile(PROJECT_DIR);

            expect(newProject).toEqual(jasmine.any(AndroidProject));
        });

        it('should cache created projects', () => {
            const newProject = AndroidProject.getProjectFile(PROJECT_DIR);
            const secondProject = AndroidProject.getProjectFile(PROJECT_DIR);

            expect(newProject).toEqual(jasmine.any(AndroidProject));
            expect(secondProject).toBe(newProject);
        });
    });

    describe('purgeCache', () => {
        beforeEach(() => {
            AndroidProject.__set__('projectFileCache', {
                project1: 'test',
                project2: 'anothertest',
                project3: 'finaltest'
            });
        });

        it('should only remove the specified project from the cache', () => {
            const projectToRemove = 'project2';
            AndroidProject.purgeCache(projectToRemove);

            const cache = AndroidProject.__get__('projectFileCache');
            expect(Object.keys(cache).length).toBe(2);
            expect(cache[projectToRemove]).toBe(undefined);
        });

        it('should remove all projects from cache', () => {
            AndroidProject.purgeCache();

            const cache = AndroidProject.__get__('projectFileCache');
            expect(Object.keys(cache).length).toBe(0);
        });
    });

    describe('getPackageName', () => {
        let AndroidManifestSpy;
        let AndroidManifestFns;
        let androidProject;

        beforeEach(() => {
            AndroidManifestFns = jasmine.createSpyObj('AndroidManifestFns', ['getPackageId']);
            AndroidManifestSpy = jasmine.createSpy('AndroidManifest').and.returnValue(AndroidManifestFns);
            AndroidProject.__set__('AndroidManifest', AndroidManifestSpy);

            androidProject = new AndroidProject(PROJECT_DIR);
        });

        it('should get the package name AndroidManifest', () => {
            androidProject.getPackageName();
            expect(AndroidManifestSpy).toHaveBeenCalledWith(path.join(PROJECT_DIR, 'app/src/main/AndroidManifest.xml'));
        });

        it('should return the package name', () => {
            const packageName = 'io.cordova.unittest';
            AndroidManifestFns.getPackageId.and.returnValue(packageName);

            expect(androidProject.getPackageName()).toBe(packageName);
        });
    });
});
