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

var rewire = require('rewire');
var common  = rewire('../../../bin/templates/cordova/lib/pluginHandlers');
var android = common.__get__('handlers');
var path    = require('path');
var fs      = require('fs');
var shell   = require('shelljs');
var os      = require('os');
var temp    = path.join(os.tmpdir(), 'plugman');
var plugins_dir = path.join(temp, 'cordova/plugins');
var dummyplugin = path.join(__dirname, '../../fixtures/org.test.plugins.dummyplugin');
var faultyplugin = path.join(__dirname, '../../fixtures/org.test.plugins.faultyplugin');
var android_project = path.join(__dirname, '../../fixtures/android_project/*');

var PluginInfo = require('cordova-common').PluginInfo;
var AndroidProject = require('../../../bin/templates/cordova/lib/AndroidProject');

var dummyPluginInfo = new PluginInfo(dummyplugin);
var valid_source = dummyPluginInfo.getSourceFiles('android'),
    valid_resources = dummyPluginInfo.getResourceFiles('android'),
    valid_libs = dummyPluginInfo.getLibFiles('android');

var faultyPluginInfo = new PluginInfo(faultyplugin);
var invalid_source = faultyPluginInfo.getSourceFiles('android');

describe('android project handler', function() {
    describe('installation', function() {
        var copyFileOrig = common.__get__('copyFile');
        var copyFileSpy = jasmine.createSpy('copyFile');
        var dummyProject;

        beforeEach(function() {
            shell.mkdir('-p', temp);
            dummyProject = AndroidProject.getProjectFile(temp);
            common.__set__('copyFile', copyFileSpy);
        });

        afterEach(function() {
            shell.rm('-rf', temp);
            common.__set__('copyFile', copyFileOrig);
        });

        describe('of <lib-file> elements', function() {
            it('should copy files', function () {
                android['lib-file'].install(valid_libs[0], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin, 'src/android/TestLib.jar', temp, path.join('libs', 'TestLib.jar'), false);
            });
        });

        describe('of <resource-file> elements', function() {
            it('should copy files', function () {
                android['resource-file'].install(valid_resources[0], dummyPluginInfo, dummyProject);
                expect(copyFileSpy).toHaveBeenCalledWith(dummyplugin, 'android-resource.xml', temp, path.join('res', 'xml', 'dummy.xml'), false);
            });
        });

        describe('of <source-file> elements', function() {
            beforeEach(function() {
                shell.cp('-rf', android_project, temp);
            });

            it('should copy stuff from one location to another by calling common.copyFile', function() {
                android['source-file'].install(valid_source[0], dummyPluginInfo, dummyProject);
                expect(copyFileSpy)
                    .toHaveBeenCalledWith(dummyplugin, 'src/android/DummyPlugin.java', temp, path.join('src/com/phonegap/plugins/dummyplugin/DummyPlugin.java'), false);
            });

            it('should throw if source file cannot be found', function() {
                common.__set__('copyFile', copyFileOrig);
                expect(function() {
                    android['source-file'].install(invalid_source[0], faultyPluginInfo, dummyProject);
                }).toThrow('"' + path.resolve(faultyplugin, 'src/android/NotHere.java') + '" not found!');
            });

            it('should throw if target file already exists', function() {
                // write out a file
                var target = path.resolve(temp, 'src/com/phonegap/plugins/dummyplugin');
                shell.mkdir('-p', target);
                target = path.join(target, 'DummyPlugin.java');
                fs.writeFileSync(target, 'some bs', 'utf-8');

                expect(function() {
                    android['source-file'].install(valid_source[0], dummyPluginInfo, dummyProject);
                }).toThrow('"' + target + '" already exists!');
            });
        });

        describe('of <framework> elements', function() {

            var someString = jasmine.any(String);

            var copyNewFileOrig = common.__get__('copyNewFile');
            var copyNewFileSpy = jasmine.createSpy('copyNewFile');

            beforeEach(function() {
                shell.cp('-rf', android_project, temp);

                spyOn(dummyProject, 'addSystemLibrary');
                spyOn(dummyProject, 'addSubProject');
                spyOn(dummyProject, 'addGradleReference');
                common.__set__('copyNewFile', copyNewFileSpy);
            });

            afterEach(function() {
                common.__set__('copyNewFile', copyNewFileOrig);
            });

            it('should throw if framework doesn\'t have "src" attribute', function() {
                expect(function() { android.framework.install({}, dummyPluginInfo, dummyProject); }).toThrow();
            });

            it('should install framework without "parent" attribute into project root', function() {
                var framework = {src: 'plugin-lib'};
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSystemLibrary).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });

            it('should install framework with "parent" attribute into parent framework dir', function() {
                var childFramework = {src: 'plugin-lib2', parent: 'plugin-lib'};
                android.framework.install(childFramework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSystemLibrary).toHaveBeenCalledWith(path.resolve(dummyProject.projectDir, childFramework.parent), someString);
            });

            it('should not copy anything if "custom" attribute is not set', function() {
                var framework = {src: 'plugin-lib'};
                var cpSpy = spyOn(shell, 'cp');
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSystemLibrary).toHaveBeenCalledWith(someString, framework.src);
                expect(cpSpy).not.toHaveBeenCalled();
            });

            it('should copy framework sources if "custom" attribute is set', function() {
                var framework = {src: 'plugin-lib', custom: true};
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.addSubProject).toHaveBeenCalledWith(dummyProject.projectDir, someString);
                expect(copyNewFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, framework.src, dummyProject.projectDir, someString, false);
            });

            it('should install gradleReference using project.addGradleReference', function() {
                var framework = {src: 'plugin-lib', custom: true, type: 'gradleReference'};
                android.framework.install(framework, dummyPluginInfo, dummyProject);
                expect(copyNewFileSpy).toHaveBeenCalledWith(dummyPluginInfo.dir, framework.src, dummyProject.projectDir, someString, false);
                expect(dummyProject.addGradleReference).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });
        });
    });

    describe('uninstallation', function() {

        var removeFileOrig = common.__get__('removeFile');
        var deleteJavaOrig = common.__get__('deleteJava');

        var removeFileSpy = jasmine.createSpy('removeFile');
        var deleteJavaSpy = jasmine.createSpy('deleteJava');
        var dummyProject;

        beforeEach(function() {
            shell.mkdir('-p', temp);
            shell.mkdir('-p', plugins_dir);
            shell.cp('-rf', android_project, temp);
            AndroidProject.purgeCache();
            dummyProject = AndroidProject.getProjectFile(temp);
            common.__set__('removeFile', removeFileSpy);
            common.__set__('deleteJava', deleteJavaSpy);
        });

        afterEach(function() {
            shell.rm('-rf', temp);
            common.__set__('removeFile', removeFileOrig);
            common.__set__('deleteJava', deleteJavaOrig);
        });

        describe('of <lib-file> elements', function(done) {
            it('should remove jar files', function () {
                android['lib-file'].install(valid_libs[0], dummyPluginInfo, dummyProject);
                android['lib-file'].uninstall(valid_libs[0], dummyPluginInfo, dummyProject);
                expect(removeFileSpy).toHaveBeenCalledWith(temp, path.join('libs/TestLib.jar'));
            });
        });

        describe('of <resource-file> elements', function(done) {
            it('should remove files', function () {
                android['resource-file'].install(valid_resources[0], dummyPluginInfo, dummyProject);
                android['resource-file'].uninstall(valid_resources[0], dummyPluginInfo, dummyProject);
                expect(removeFileSpy).toHaveBeenCalledWith(temp, path.join('res/xml/dummy.xml'));
            });
        });

        describe('of <source-file> elements', function() {
            it('should remove stuff by calling common.deleteJava', function() {
                android['source-file'].install(valid_source[0], dummyPluginInfo, dummyProject);
                android['source-file'].uninstall(valid_source[0], dummyPluginInfo, dummyProject);
                expect(deleteJavaSpy).toHaveBeenCalledWith(temp, path.join('src/com/phonegap/plugins/dummyplugin/DummyPlugin.java'));
            });
        });

        describe('of <framework> elements', function() {

            var someString = jasmine.any(String);

            beforeEach(function() {
                shell.mkdir(path.join(dummyProject.projectDir, dummyPluginInfo.id));

                spyOn(dummyProject, 'removeSystemLibrary');
                spyOn(dummyProject, 'removeSubProject');
                spyOn(dummyProject, 'removeGradleReference');
            });

            it('should throw if framework doesn\'t have "src" attribute', function() {
                expect(function() { android.framework.uninstall({}, dummyPluginInfo, dummyProject); }).toThrow();
            });

            it('should uninstall framework without "parent" attribute into project root', function() {
                var framework = {src: 'plugin-lib'};
                android.framework.uninstall(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.removeSystemLibrary).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });

            it('should uninstall framework with "parent" attribute into parent framework dir', function() {
                var childFramework = {src: 'plugin-lib2', parent: 'plugin-lib'};
                android.framework.uninstall(childFramework, dummyPluginInfo, dummyProject);
                expect(dummyProject.removeSystemLibrary).toHaveBeenCalledWith(path.resolve(dummyProject.projectDir, childFramework.parent), someString);
            });

            it('should remove framework sources if "custom" attribute is set', function() {
                var framework = {src: 'plugin-lib', custom: true};
                android.framework.uninstall(framework, dummyPluginInfo, dummyProject);
                expect(dummyProject.removeSubProject).toHaveBeenCalledWith(dummyProject.projectDir, someString);
                expect(removeFileSpy).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });

            it('should install gradleReference using project.removeGradleReference', function() {
                var framework = {src: 'plugin-lib', custom: true, type: 'gradleReference'};
                android.framework.uninstall(framework, dummyPluginInfo, dummyProject);
                expect(removeFileSpy).toHaveBeenCalledWith(dummyProject.projectDir, someString);
                expect(dummyProject.removeGradleReference).toHaveBeenCalledWith(dummyProject.projectDir, someString);
            });
        });
    });
});
