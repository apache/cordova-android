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
const common = rewire('../../../lib/pluginHandlers');
const android = common.__get__('handlers');
const path = require('node:path');
const fs = require('node:fs');
const os = require('node:os');
const temp = path.join(os.tmpdir(), 'plugman');
const plugins_dir = path.join(temp, 'cordova/plugins');
const dummyplugin = path.join(__dirname, '../../fixtures/org.test.plugins.dummyplugin');
const faultyplugin = path.join(__dirname, '../../fixtures/org.test.plugins.faultyplugin');
const android_studio_project = path.join(__dirname, '../../fixtures/android_studio_project');

const PluginInfo = require('cordova-common').PluginInfo;
const AndroidProject = require('../../../lib/AndroidProject');

const MockCordovaGradleConfigParser = require('../mocks/config/MockCordovaGradleConfigParser');
const CordovaGradleConfigParserFactory = require('../../../lib/config/CordovaGradleConfigParserFactory');

const dummyPluginInfo = new PluginInfo(dummyplugin);
const valid_source = dummyPluginInfo.getSourceFiles('android');
const valid_resources = dummyPluginInfo.getResourceFiles('android');
const valid_libs = dummyPluginInfo.getLibFiles('android');

const faultyPluginInfo = new PluginInfo(faultyplugin);
const invalid_source = faultyPluginInfo.getSourceFiles('android');

describe('android project handler', function () {
    const PROJECT_DIR = 'platforms/android';

    beforeAll(() => {
        spyOn(CordovaGradleConfigParserFactory, 'create').and.returnValue(new MockCordovaGradleConfigParser(PROJECT_DIR));
    });

    describe('installation', function () {
        const copyFileOrig = common.__get__('copyFile');
        const copyFileSpy = jasmine.createSpy('copyFile');
        let dummyProject;

        beforeEach(function () {
            fs.mkdirSync(temp, { recursive: true });
            dummyProject = AndroidProject.getProjectFile(temp);
            copyFileSpy.calls.reset();
            common.__set__('copyFile', copyFileSpy);
        });

        afterEach(function () {
            fs.rmSync(temp, { recursive: true, force: true });
            common.__set__('copyFile', copyFileOrig);
        });

        describe('of <lib-file> elements', function () {
            it('Test#001 : should copy files for Android Studio projects', function () {
                android['lib-file'].install(valid_libs[0], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin, 'src/android/TestLib.jar', temp, path.join('app', 'libs', 'TestLib.jar'), false);
            });
        });

        describe('of <resource-file> elements', function () {
            it('Test#002 : should copy files to the correct location on an Android Studio project', function () {
                android['resource-file'].install(valid_resources[0], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin, 'android-resource.xml', temp, path.join('app', 'src', 'main', 'res', 'xml', 'dummy.xml'), false);
            });
        });

        describe('of <source-file> elements', function () {
            beforeEach(function () {
                fs.cpSync(android_studio_project, temp, { recursive: true });
            });

            it('Test#003 : should copy stuff from one location to another by calling common.copyFile', function () {
                android['source-file'].install(valid_source[0], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/DummyPlugin.java', temp, path.join('app', 'src', 'main', 'java', 'com', 'phonegap', 'plugins', 'dummyplugin', 'DummyPlugin.java'), false);
            });

            it('Test#005 : should throw if source file cannot be found', function () {
                common.__set__('copyFile', copyFileOrig);
                expect(function () {
                    android['source-file'].install(invalid_source[0], faultyPluginInfo, dummyProject);
                }).toThrow(new Error('"' + path.resolve(faultyplugin, 'src/android/NotHere.java') + '" not found!'));
            });

            it('Test#006 : should throw if target file already exists', function () {
                // write out a file
                let target = path.resolve(temp, 'app', 'src', 'main', 'java', 'com', 'phonegap', 'plugins', 'dummyplugin');
                fs.mkdirSync(target, { recursive: true });
                target = path.join(target, 'DummyPlugin.java');
                fs.writeFileSync(target, 'some bs', 'utf-8');

                expect(function () {
                    android['source-file'].install(valid_source[0], dummyPluginInfo, dummyProject);
                }).toThrow(new Error('"' + target + '" already exists!'));
            });

            // TODO: renumber these tests and other tests below
            it('Test#00a6 : should allow installing sources with new app target-dir scheme', function () {
                android['source-file'].install(valid_source[1], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/DummyPlugin2.java', temp, path.join('app/src/main/src/com/phonegap/plugins/dummyplugin/DummyPlugin2.java'), false);
            });

            it('Test#006b : should allow installing jar lib file from sources with new app target-dir scheme', function () {
                android['source-file'].install(valid_source[2], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/TestLib.jar', temp, path.join('app/libs/TestLib.jar'), false);
            });

            it('Test#006c : should allow installing aar lib file from sources with new app target-dir scheme', function () {
                android['source-file'].install(valid_source[3], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/TestAar.aar', temp, path.join('app/libs/TestAar.aar'), false);
            });

            it('Test#006d : should allow installing xml file from sources with old target-dir scheme', function () {
                android['source-file'].install(valid_source[4], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin,
                    'src/android/mysettings.xml', temp,
                    path.join('app/src/main/res/xml/mysettings.xml'), false);
            });

            it('Test#006e : should allow installing file with other extension from sources with old target-dir scheme', function () {
                android['source-file'].install(valid_source[5], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin,
                    'src/android/other.extension', temp,
                    path.join('app/src/main/res/values/other.extension'), false);
            });

            it('Test#006f : should allow installing aidl file from sources with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[6], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin,
                    'src/android/myapi.aidl', temp,
                    path.join('app/src/main/aidl/com/mytest/myapi.aidl'), false);
            });

            it('Test#006g : should allow installing aar lib file from sources with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[7], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin,
                    'src/android/testaar2.aar', temp,
                    path.join('app/libs/testaar2.aar'), false);
            });

            it('Test#006h : should allow installing jar lib file from sources with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[8], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin,
                    'src/android/testjar2.jar', temp,
                    path.join('app/libs/testjar2.jar'), false);
            });

            it('Test#006i : should allow installing .so lib file from sources with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[9], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin,
                    'src/android/jniLibs/x86/libnative.so', temp,
                    path.join('app/src/main/jniLibs/x86/libnative.so'), false);
            });

            it('Test#006j : should allow installing sources with target-dir that includes "app"', function () {
                android['source-file'].install(valid_source[10], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/DummyPlugin2.java', temp, path.join('app/src/main/java/com/appco/DummyPlugin2.java'), false);
            });

            it('Test#006k : should allow installing sources with target-dir that includes "app" in its first directory', function () {
                android['source-file'].install(valid_source[11], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/DummyPlugin2.java', temp, path.join('app/src/main/java/appco/src/DummyPlugin2.java'), false);
            });
        });

        describe('of <framework> elements', function () {
            const someString = jasmine.any(String);

            const copyNewFileOrig = common.__get__('copyNewFile');
            const copyNewFileSpy = jasmine.createSpy('copyNewFile');

            beforeEach(function () {
                fs.cpSync(android_studio_project, temp, { recursive: true });

                spyOn(dummyProject, 'addSystemLibrary');
                spyOn(dummyProject, 'addSubProject');
                spyOn(dummyProject, 'addGradleReference');
                common.__set__('copyNewFile', copyNewFileSpy);
            });

            afterEach(function () {
                common.__set__('copyNewFile', copyNewFileOrig);
            });

            it('Test#007 : should throw if framework doesn\'t have "src" attribute', function () {
                expect(function () { android.framework.install({}, dummyPluginInfo, dummyProject); }).toThrow();
            });

            it('Test#008 : should install framework without "parent" attribute into project root', function () {
                const framework = { src: 'plugin-lib' };
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSystemLibrary).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });

            it('Test#009 : should install framework with "parent" attribute into parent framework dir', function () {
                const childFramework = { src: 'plugin-lib2', parent: 'plugin-lib' };
                android.framework.install(childFramework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSystemLibrary).toHaveBeenCalledWith(path.resolve(dummyProject.projectDir, childFramework.parent), someString);
            });

            it('Test#010 : should not copy anything if "custom" attribute is not set', function () {
                const framework = { src: 'plugin-lib' };
                const cpSpy = spyOn(fs, 'cpSync');
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSystemLibrary).toHaveBeenCalledWith(someString, framework.src);
                expect(cpSpy).not.toHaveBeenCalled();
            });

            it('Test#011 : should copy framework sources if "custom" attribute is set', function () {
                const framework = { src: 'plugin-lib', custom: true };
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSubProject).toHaveBeenCalledWith(dummyProject.projectDir, someString);
                expect(copyNewFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, framework.src, dummyProject.projectDir, someString, false);
            });

            it('Test#012 : should install gradleReference using project.addGradleReference', function () {
                const framework = { src: 'plugin-lib', custom: true, type: 'gradleReference' };
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(copyNewFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, framework.src, dummyProject.projectDir, someString, false);
                expect(dummyProject.addGradleReference).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });
        });

        describe('of <js-module> elements', function () {
            const jsModule = { src: 'www/dummyplugin.js' };
            let wwwDest, platformWwwDest;

            beforeEach(function () {
                spyOn(fs, 'writeFileSync');
                wwwDest = path.resolve(dummyProject.www, 'plugins', dummyPluginInfo.id, jsModule.src);
                platformWwwDest = path.resolve(dummyProject.platformWww, 'plugins', dummyPluginInfo.id, jsModule.src);
            });

            it('Test#013 : should put module to both www and platform_www when options.usePlatformWww flag is specified', function () {
                android['js-module'].install(jsModule, dummyPluginInfo, dummyProject, { usePlatformWww: true });
                expect(fs.writeFileSync).toHaveBeenCalledWith(wwwDest, jasmine.any(String), 'utf-8');
                expect(fs.writeFileSync).toHaveBeenCalledWith(platformWwwDest, jasmine.any(String), 'utf-8');
            });

            it('Test#014 : should put module to www only when options.usePlatformWww flag is not specified', function () {
                android['js-module'].install(jsModule, dummyPluginInfo, dummyProject);
                expect(fs.writeFileSync).toHaveBeenCalledWith(wwwDest, jasmine.any(String), 'utf-8');
                expect(fs.writeFileSync).not.toHaveBeenCalledWith(platformWwwDest, jasmine.any(String), 'utf-8');
            });
        });

        describe('of <asset> elements', function () {
            let asset;

            beforeEach(function () {
                asset = { src: 'www/dummyPlugin.js', target: 'foo/dummy.js' };
            });

            it('Test#015 : should put asset to both www and platform_www when options.usePlatformWww flag is specified', function () {
                android.asset.install(asset, dummyPluginInfo, dummyProject, { usePlatformWww: true });
                expect(copyFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, asset.src, dummyProject.www, asset.target);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, asset.src, dummyProject.platformWww, asset.target);
            });

            it('Test#016 : should put asset to www only when options.usePlatformWww flag is not specified', function () {
                android.asset.install(asset, dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, asset.src, dummyProject.www, asset.target);
                expect(copyFileSpy).not.toHaveBeenCalledWith(dummyPluginInfo.dir, asset.src, dummyProject.platformWww, asset.target);
            });
        });
    });

    describe('uninstallation', function () {
        const deleteJavaOrig = common.__get__('deleteJava');
        const originalRmSync = fs.rmSync;
        const deleteJavaSpy = jasmine.createSpy('deleteJava');
        let dummyProject;
        let rmSyncSpy;

        beforeEach(function () {
            fs.mkdirSync(temp, { recursive: true });
            fs.mkdirSync(plugins_dir, { recursive: true });
            fs.cpSync(android_studio_project, temp, { recursive: true });
            AndroidProject.purgeCache();
            dummyProject = AndroidProject.getProjectFile(temp);
            rmSyncSpy = spyOn(fs, 'rmSync');
            common.__set__('deleteJava', deleteJavaSpy);
        });

        afterEach(function () {
            originalRmSync.call(fs, temp, { recursive: true });
            common.__set__('deleteJava', deleteJavaOrig);
        });

        describe('of <lib-file> elements', function () {
            it('Test#017 : should remove jar files for Android Studio projects', function () {
                android['lib-file'].install(valid_libs[0], dummyPluginInfo, dummyProject);
                android['lib-file'].uninstall(valid_libs[0], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/libs/TestLib.jar'), { recursive: true, force: true });
            });
        });

        describe('of <resource-file> elements', function () {
            it('Test#018 : should remove files for Android Studio projects', function () {
                android['resource-file'].install(valid_resources[0], dummyPluginInfo, dummyProject);
                android['resource-file'].uninstall(valid_resources[0], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app', 'src', 'main', 'res', 'xml', 'dummy.xml'), { recursive: true, force: true });
            });
        });

        describe('of <source-file> elements', function () {
            it('Test#019 : should remove stuff by calling common.deleteJava for Android Studio projects', function () {
                android['source-file'].install(valid_source[0], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[0], dummyPluginInfo, dummyProject);
                expect(deleteJavaSpy).toHaveBeenCalledWith(temp, path.join('app/src/main/java/com/phonegap/plugins/dummyplugin/DummyPlugin.java'));
            });

            it('Test#019a : should remove stuff by calling common.deleteJava for Android Studio projects, with specific app target-dir', function () {
                android['source-file'].install(valid_source[1], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[1], dummyPluginInfo, dummyProject);
                expect(deleteJavaSpy).toHaveBeenCalledWith(temp, path.join('app/src/main/src/com/phonegap/plugins/dummyplugin/DummyPlugin2.java'));
            });

            it('Test#019b : should remove stuff by calling removeFileF for Android Studio projects, of jar with new app target-dir scheme', function () {
                android['source-file'].install(valid_source[2], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[2], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/libs/TestLib.jar'), { recursive: true, force: true });
            });

            it('Test#019c : should remove stuff by calling removeFileF for Android Studio projects, of aar with new app target-dir scheme', function () {
                android['source-file'].install(valid_source[3], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[3], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/libs/TestAar.aar'), { recursive: true, force: true });
            });

            it('Test#019d : should remove stuff by calling removeFileF for Android Studio projects, of xml with old target-dir scheme', function () {
                android['source-file'].install(valid_source[4], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[4], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/src/main/res/xml/mysettings.xml'), { recursive: true, force: true });
            });

            it('Test#019e : should remove stuff by calling removeFileF for Android Studio projects, of file with other extension with old target-dir scheme', function () {
                android['source-file'].install(valid_source[5], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[5], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/src/main/res/values/other.extension'), { recursive: true, force: true });
            });

            it('Test#019f : should remove stuff by calling removeFileF for Android Studio projects, of aidl with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[6], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[6], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/src/main/aidl/com/mytest/myapi.aidl'), { recursive: true, force: true });
            });

            it('Test#019g : should remove stuff by calling removeFileF for Android Studio projects, of aar with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[7], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[7], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/libs/testaar2.aar'), { recursive: true, force: true });
            });

            it('Test#019h : should remove stuff by calling removeFileF for Android Studio projects, of jar with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[8], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[8], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/libs/testjar2.jar'), { recursive: true, force: true });
            });

            it('Test#019i : should remove stuff by calling removeFileF for Android Studio projects, of .so lib file with old target-dir scheme (GH-547)', function () {
                android['source-file'].install(valid_source[9], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[9], dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(path.join(dummyProject.projectDir, 'app/src/main/jniLibs/x86/libnative.so'), { recursive: true, force: true });
            });

            it('Test#019j : should remove stuff by calling common.deleteJava for Android Studio projects, with target-dir that includes "app"', function () {
                android['source-file'].install(valid_source[10], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[10], dummyPluginInfo, dummyProject);
                expect(deleteJavaSpy).toHaveBeenCalledWith(dummyProject.projectDir, path.join('app/src/main/java/com/appco/DummyPlugin2.java'));
            });
        });

        describe('of <framework> elements', function () {
            const someString = jasmine.any(String);

            beforeEach(function () {
                fs.mkdirSync(path.join(dummyProject.projectDir, dummyPluginInfo.id), { recursive: true });

                spyOn(dummyProject, 'removeSystemLibrary');
                spyOn(dummyProject, 'removeSubProject');
                spyOn(dummyProject, 'removeGradleReference');
            });

            it('Test#020 : should throw if framework doesn\'t have "src" attribute', function () {
                expect(function () { android.framework.uninstall({}, dummyPluginInfo, dummyProject); }).toThrow();
            });

            it('Test#021 : should uninstall framework without "parent" attribute into project root', function () {
                const framework = { src: 'plugin-lib' };
                android.framework.uninstall(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.removeSystemLibrary).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });

            it('Test#022 : should uninstall framework with "parent" attribute into parent framework dir', function () {
                const childFramework = { src: 'plugin-lib2', parent: 'plugin-lib' };
                android.framework.uninstall(childFramework, dummyPluginInfo, dummyProject);
                expect(dummyProject.removeSystemLibrary).toHaveBeenCalledWith(path.resolve(dummyProject.projectDir, childFramework.parent), someString);
            });

            it('Test#023 : should remove framework sources if "custom" attribute is set', function () {
                const framework = { src: 'plugin-lib', custom: true };
                android.framework.uninstall(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.removeSubProject).toHaveBeenCalledWith(dummyProject.projectDir, someString);
                expect(rmSyncSpy).toHaveBeenCalledWith(someString, { recursive: true, force: true });
            });

            it('Test#24 : should install gradleReference using project.removeGradleReference', function () {
                const framework = { src: 'plugin-lib', custom: true, type: 'gradleReference' };
                android.framework.uninstall(framework, dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(someString, { recursive: true, force: true });
                expect(dummyProject.removeGradleReference).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });
        });

        describe('of <js-module> elements', function () {
            const jsModule = { src: 'www/dummyPlugin.js' };
            let wwwDest;
            let platformWwwDest;

            beforeEach(function () {
                wwwDest = path.resolve(dummyProject.www, 'plugins', dummyPluginInfo.id, jsModule.src);
                platformWwwDest = path.resolve(dummyProject.platformWww, 'plugins', dummyPluginInfo.id, jsModule.src);

                const existsSyncOrig = fs.existsSync;
                spyOn(fs, 'existsSync').and.callFake(function (file) {
                    if ([wwwDest, platformWwwDest].indexOf(file) >= 0) return true;
                    return existsSyncOrig.call(fs, file);
                });
            });

            it('Test#025 : should put module to both www and platform_www when options.usePlatformWww flag is specified', function () {
                android['js-module'].uninstall(jsModule, dummyPluginInfo, dummyProject, { usePlatformWww: true });
                expect(rmSyncSpy).toHaveBeenCalledWith(wwwDest, { recursive: true, force: true });
                expect(rmSyncSpy).toHaveBeenCalledWith(platformWwwDest, { recursive: true, force: true });
            });

            it('Test#026 : should put module to www only when options.usePlatformWww flag is not specified', function () {
                android['js-module'].uninstall(jsModule, dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(wwwDest, { recursive: true, force: true });
                expect(rmSyncSpy).not.toHaveBeenCalledWith(platformWwwDest, { recursive: true, force: true });
            });
        });

        describe('of <asset> elements', function () {
            const asset = { src: 'www/dummyPlugin.js', target: 'foo/dummy.js' };
            let wwwDest, platformWwwDest;

            beforeEach(function () {
                wwwDest = path.resolve(dummyProject.www, asset.target);
                platformWwwDest = path.resolve(dummyProject.platformWww, asset.target);

                const existsSyncOrig = fs.existsSync;
                spyOn(fs, 'existsSync').and.callFake(function (file) {
                    if ([wwwDest, platformWwwDest].indexOf(file) >= 0) return true;
                    return existsSyncOrig.call(fs, file);
                });
            });

            it('Test#027 : should put module to both www and platform_www when options.usePlatformWww flag is specified', function () {
                android.asset.uninstall(asset, dummyPluginInfo, dummyProject, { usePlatformWww: true });
                expect(rmSyncSpy).toHaveBeenCalledWith(wwwDest, { recursive: true, force: true });
                expect(rmSyncSpy).toHaveBeenCalledWith(platformWwwDest, { recursive: true, force: true });
            });

            it('Test#028 : should put module to www only when options.usePlatformWww flag is not specified', function () {
                android.asset.uninstall(asset, dummyPluginInfo, dummyProject);
                expect(rmSyncSpy).toHaveBeenCalledWith(wwwDest, { recursive: true, force: true });
                expect(rmSyncSpy).not.toHaveBeenCalledWith(platformWwwDest, { recursive: true, force: true });
            });
        });
    });
});
