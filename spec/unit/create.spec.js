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
var utils = require('../../lib/utils');
var create = rewire('../../lib/create');
var check_reqs = require('../../lib/check_reqs');
var fs = require('fs-extra');
var path = require('path');

describe('create', function () {
    describe('validatePackageName helper method', function () {
        describe('happy path (valid package names)', function () {
            var valid = [
                'org.apache.mobilespec',
                'com.example',
                'com.floors42.package',
                'ball8.ball8.ball8ball'
            ];
            valid.forEach(function (package_name) {
                it('Test#001 : should accept ' + package_name, () => {
                    return create.validatePackageName(package_name);
                });
            });
        });

        describe('failure cases (invalid package names)', function () {
            function expectPackageNameToBeRejected (name) {
                return create.validatePackageName(name).then(() => {
                    fail('Expected promise to be rejected');
                }, err => {
                    expect(err).toEqual(jasmine.any(Error));
                    expect(err.message).toContain('Error validating package name');
                });
            }

            it('should reject empty package names', () => {
                return expectPackageNameToBeRejected('');
            });

            it('should reject package names containing "class"', () => {
                return expectPackageNameToBeRejected('com.class.is.bad');
            });

            it('should reject package names that do not start with a latin letter', () => {
                return expectPackageNameToBeRejected('_un.der.score');
            });

            it('should reject package names with terms that do not start with a latin letter', () => {
                return expectPackageNameToBeRejected('un._der.score');
            });

            it('should reject package names containing non-alphanumeric or underscore characters', () => {
                return expectPackageNameToBeRejected('th!$.!$.b@d');
            });

            it('should reject package names that do not contain enough dots', () => {
                return expectPackageNameToBeRejected('therearenodotshere');
            });

            it('should reject package names that end with a dot', () => {
                return expectPackageNameToBeRejected('this.is.a.complete.sentence.');
            });
        });
    });

    describe('validateProjectName helper method', function () {
        describe('happy path (valid project names)', function () {
            var valid = [
                'mobilespec',
                'package_name',
                'PackageName',
                'CordovaLib',
                '1337',
                '3 Little Pigs',
                'CordovaActivity'
            ];
            valid.forEach(function (project_name) {
                it('Test#003 : should accept ' + project_name, () => {
                    return create.validateProjectName(project_name);
                });
            });
        });

        describe('failure cases (invalid project names)', function () {
            it('should reject empty project names', () => {
                return create.validateProjectName('').then(() => {
                    fail('Expected promise to be rejected');
                }, err => {
                    expect(err).toEqual(jasmine.any(Error));
                    expect(err.message).toContain('Project name cannot be empty');
                });
            });
        });
    });

    describe('main method', function () {
        var config_mock;
        var events_mock;
        var Manifest_mock = function () {};
        var revert_manifest_mock;
        var project_path = path.join('some', 'path');
        var app_path = path.join(project_path, 'app', 'src', 'main');
        var default_templates = path.join(__dirname, '..', '..', 'templates', 'project');
        var fake_android_target = 'android-1337';

        beforeEach(function () {
            Manifest_mock.prototype = jasmine.createSpyObj('AndroidManifest instance mock', ['setPackageId', 'getActivity', 'setName', 'write']);
            Manifest_mock.prototype.setPackageId.and.returnValue(new Manifest_mock());
            Manifest_mock.prototype.getActivity.and.returnValue(new Manifest_mock());
            Manifest_mock.prototype.setName.and.returnValue(new Manifest_mock());
            spyOn(create, 'validatePackageName').and.resolveTo();
            spyOn(create, 'validateProjectName').and.resolveTo();
            spyOn(create, 'copyJsAndLibrary');
            spyOn(create, 'copyScripts');
            spyOn(create, 'copyBuildRules');
            spyOn(create, 'writeProjectProperties');
            spyOn(create, 'prepBuildFiles');
            revert_manifest_mock = create.__set__('AndroidManifest', Manifest_mock);
            spyOn(fs, 'existsSync').and.returnValue(false);
            spyOn(fs, 'copySync');
            spyOn(fs, 'ensureDirSync');
            spyOn(utils, 'replaceFileContents');
            config_mock = jasmine.createSpyObj('ConfigParser mock instance', ['packageName', 'android_packageName', 'name', 'android_activityName']);
            events_mock = jasmine.createSpyObj('EventEmitter mock instance', ['emit']);
            spyOn(check_reqs, 'get_target').and.returnValue(fake_android_target);
        });

        afterEach(function () {
            revert_manifest_mock();
        });

        describe('parameter values and defaults', function () {
            it('should have a default package name of my.cordova.project', () => {
                config_mock.packageName.and.returnValue(undefined);
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.validatePackageName).toHaveBeenCalledWith('my.cordova.project');
                });
            });

            it('should use the ConfigParser-provided package name, if exists', () => {
                config_mock.packageName.and.returnValue('org.apache.cordova');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.validatePackageName).toHaveBeenCalledWith('org.apache.cordova');
                });
            });

            it('should have a default project name of CordovaExample', () => {
                config_mock.name.and.returnValue(undefined);
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.validateProjectName).toHaveBeenCalledWith('CordovaExample');
                });
            });

            it('should use the ConfigParser-provided project name, if exists', () => {
                config_mock.name.and.returnValue('MySweetAppName');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.validateProjectName).toHaveBeenCalledWith('MySweetAppName');
                });
            });

            it('should replace any non-word characters (including unicode and spaces) in the ConfigParser-provided project name with underscores', () => {
                config_mock.name.and.returnValue('応応応応 hello 用用用用');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.validateProjectName).toHaveBeenCalledWith('_____hello_____');
                });
            });

            it('should have a default activity name of MainActivity', () => {
                config_mock.android_activityName.and.returnValue(undefined);
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(Manifest_mock.prototype.setName).toHaveBeenCalledWith('MainActivity');
                });
            });

            it('should use the activityName provided via options parameter, if exists', () => {
                config_mock.android_activityName.and.returnValue(undefined);
                return create.create(project_path, config_mock, { activityName: 'AwesomeActivity' }, events_mock).then(() => {
                    expect(Manifest_mock.prototype.setName).toHaveBeenCalledWith('AwesomeActivity');
                });
            });

            it('should use the ConfigParser-provided activity name, if exists', () => {
                config_mock.android_activityName.and.returnValue('AmazingActivity');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(Manifest_mock.prototype.setName).toHaveBeenCalledWith('AmazingActivity');
                });
            });
        });

        describe('failure', function () {
            it('should fail if the target path already exists', () => {
                fs.existsSync.and.returnValue(true);
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    fail('Expected promise to be rejected');
                }, err => {
                    expect(err).toEqual(jasmine.any(Error));
                    expect(err.message).toContain('Project already exists!');
                });
            });

            it('should fail if validateProjectName rejects', () => {
                const fakeError = new Error();
                create.validateProjectName.and.callFake(() => Promise.reject(fakeError));

                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    fail('Expected promise to be rejected');
                }, err => {
                    expect(err).toBe(fakeError);
                });
            });
        });

        describe('happy path', function () {
            it('should copy project templates from a specified custom template', () => {
                return create.create(project_path, config_mock, { customTemplate: '/template/path' }, events_mock).then(() => {
                    expect(fs.copySync).toHaveBeenCalledWith(path.join('/template/path', 'assets'), path.join(app_path, 'assets'));
                    expect(fs.copySync).toHaveBeenCalledWith(path.join('/template/path', 'res'), path.join(app_path, 'res'));
                    expect(fs.copySync).toHaveBeenCalledWith(path.join('/template/path', 'gitignore'), path.join(project_path, '.gitignore'));
                });
            });

            it('should copy project templates from the default templates location if no custom template is provided', () => {
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(fs.copySync).toHaveBeenCalledWith(path.join(default_templates, 'assets'), path.join(app_path, 'assets'));
                    expect(fs.copySync).toHaveBeenCalledWith(path.join(default_templates, 'res'), path.join(app_path, 'res'));
                    expect(fs.copySync).toHaveBeenCalledWith(path.join(default_templates, 'gitignore'), path.join(project_path, '.gitignore'));
                });
            });

            it('should copy JS and library assets', () => {
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.copyJsAndLibrary).toHaveBeenCalled();
                });
            });

            it('should create a java src directory based on the provided project package name', () => {
                config_mock.packageName.and.returnValue('org.apache.cordova');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(fs.ensureDirSync).toHaveBeenCalledWith(path.join(app_path, 'java', 'org', 'apache', 'cordova'));
                });
            });

            it('should copy, rename and interpolate the template Activity java class with the project-specific activity name and package name', () => {
                config_mock.packageName.and.returnValue('org.apache.cordova');
                config_mock.android_activityName.and.returnValue('CEEDEEVEE');
                var activity_path = path.join(app_path, 'java', 'org', 'apache', 'cordova', 'CEEDEEVEE.java');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(fs.copySync).toHaveBeenCalledWith(path.join(default_templates, 'Activity.java'), activity_path);
                    expect(utils.replaceFileContents).toHaveBeenCalledWith(activity_path, /__ACTIVITY__/, 'CEEDEEVEE');
                    expect(utils.replaceFileContents).toHaveBeenCalledWith(activity_path, /__ID__/, 'org.apache.cordova');
                });
            });

            it('should interpolate the project name into strings.xml', () => {
                config_mock.name.and.returnValue('IncredibleApp');
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(utils.replaceFileContents).toHaveBeenCalledWith(path.join(app_path, 'res', 'values', 'strings.xml'), /__NAME__/, 'IncredibleApp');
                });
            });

            it('should copy template scripts into generated project', () => {
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.copyScripts).toHaveBeenCalledWith(project_path);
                });
            });

            it('should copy build rules / gradle files into generated project', () => {
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.copyBuildRules).toHaveBeenCalledWith(project_path);
                });
            });

            it('should write project.properties file with project details and target API', () => {
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.writeProjectProperties).toHaveBeenCalledWith(project_path, fake_android_target);
                });
            });

            it('should prepare build files', () => {
                return create.create(project_path, config_mock, {}, events_mock).then(() => {
                    expect(create.prepBuildFiles).toHaveBeenCalledWith(project_path);
                });
            });
        });
    });
});
