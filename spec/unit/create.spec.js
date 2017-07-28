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
var create = rewire('../../bin/lib/create');
var check_reqs = require('../../bin/templates/cordova/lib/check_reqs');
var fs = require('fs');
var path = require('path');
var Q = require('q');
var shell = require('shelljs');

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
                it('Test#001 : should accept ' + package_name, function (done) {
                    create.validatePackageName(package_name).fail(fail).done(done);
                });
            });
        });

        describe('failure cases (invalid package names)', function () {
            it('should reject empty package names', function (done) {
                create.validatePackageName('').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
            it('should reject package names containing "class"', function (done) {
                create.validatePackageName('com.class.is.bad').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
            it('should reject package names that do not start with a latin letter', function (done) {
                create.validatePackageName('_un.der.score').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
            it('should reject package names with terms that do not start with a latin letter', function (done) {
                create.validatePackageName('un._der.score').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
            it('should reject package names containing non-alphanumeric or underscore characters', function (done) {
                create.validatePackageName('th!$.!$.b@d').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
            it('should reject package names that do not contain enough dots', function (done) {
                create.validatePackageName('therearenodotshere').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
            it('should reject package names that end with a dot', function (done) {
                create.validatePackageName('this.is.a.complete.sentence.').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Error validating package name');
                }).done(done);
            });
        });
    });
    describe('validateProjectName helper method', function () {
        describe('happy path (valid project names)', function () {
            var valid = [
                'mobilespec',
                'package_name',
                'PackageName',
                'CordovaLib'
            ];
            valid.forEach(function (project_name) {
                it('Test#003 : should accept ' + project_name, function (done) {
                    create.validateProjectName(project_name).fail(fail).done(done);
                });
            });
        });
        describe('failure cases (invalid project names)', function () {
            it('should reject empty project names', function (done) {
                create.validateProjectName('').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Project name cannot be empty');
                }).done(done);
            });
            it('should reject "CordovaActivity" as a project name', function (done) {
                create.validateProjectName('CordovaActivity').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Project name cannot be CordovaActivity');
                }).done(done);
            });
            it('should reject project names that begin with a number', function (done) {
                create.validateProjectName('1337').then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Project name must not begin with a number');
                }).done(done);
            });
        });
    });
    describe('main method', function () {
        var config_mock;
        var events_mock;
        var Manifest_mock = function () {};
        var revert_manifest_mock;
        var project_path = path.join('some', 'path');
        var default_templates = path.join(__dirname, '..', '..', 'bin', 'templates', 'project');
        var fake_android_target = 'android-1337';
        beforeEach(function () {
            Manifest_mock.prototype = jasmine.createSpyObj('AndroidManifest instance mock', ['setPackageId', 'setTargetSdkVersion', 'getActivity', 'setName', 'write']);
            Manifest_mock.prototype.setPackageId.and.returnValue(new Manifest_mock());
            Manifest_mock.prototype.setTargetSdkVersion.and.returnValue(new Manifest_mock());
            Manifest_mock.prototype.getActivity.and.returnValue(new Manifest_mock());
            Manifest_mock.prototype.setName.and.returnValue(new Manifest_mock());
            spyOn(create, 'validatePackageName').and.returnValue(Q());
            spyOn(create, 'validateProjectName').and.returnValue(Q());
            spyOn(create, 'setShellFatal').and.callFake(function (noop, cb) { cb(); });
            spyOn(create, 'copyJsAndLibrary');
            spyOn(create, 'copyScripts');
            spyOn(create, 'copyBuildRules');
            spyOn(create, 'writeProjectProperties');
            spyOn(create, 'prepBuildFiles');
            revert_manifest_mock = create.__set__('AndroidManifest', Manifest_mock);
            spyOn(fs, 'existsSync').and.returnValue(false);
            spyOn(shell, 'cp');
            spyOn(shell, 'mkdir');
            spyOn(shell, 'sed');
            config_mock = jasmine.createSpyObj('ConfigParser mock instance', ['packageName', 'android_packageName', 'name', 'android_activityName']);
            events_mock = jasmine.createSpyObj('EventEmitter mock instance', ['emit']);
            spyOn(check_reqs, 'get_target').and.returnValue(fake_android_target);
        });
        afterEach(function () {
            revert_manifest_mock();
        });
        describe('parameter values and defaults', function () {
            it('should have a default package name of my.cordova.project', function (done) {
                config_mock.packageName.and.returnValue(undefined);
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.validatePackageName).toHaveBeenCalledWith('my.cordova.project');
                }).fail(fail).done(done);
            });
            it('should use the ConfigParser-provided package name, if exists', function (done) {
                config_mock.packageName.and.returnValue('org.apache.cordova');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.validatePackageName).toHaveBeenCalledWith('org.apache.cordova');
                }).fail(fail).done(done);
            });
            it('should have a default project name of CordovaExample', function (done) {
                config_mock.name.and.returnValue(undefined);
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.validateProjectName).toHaveBeenCalledWith('CordovaExample');
                }).fail(fail).done(done);
            });
            it('should use the ConfigParser-provided project name, if exists', function (done) {
                config_mock.name.and.returnValue('MySweetAppName');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.validateProjectName).toHaveBeenCalledWith('MySweetAppName');
                }).fail(fail).done(done);
            });
            it('should replace any non-word characters (including unicode and spaces) in the ConfigParser-provided project name with underscores', function (done) {
                config_mock.name.and.returnValue('応応応応 hello 用用用用');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.validateProjectName).toHaveBeenCalledWith('_____hello_____');
                }).fail(fail).done(done);
            });
            it('should have a default activity name of MainActivity', function (done) {
                config_mock.android_activityName.and.returnValue(undefined);
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(Manifest_mock.prototype.setName).toHaveBeenCalledWith('MainActivity');
                }).fail(fail).done(done);
            });
            it('should use the activityName provided via options parameter, if exists', function (done) {
                config_mock.android_activityName.and.returnValue(undefined);
                create.create(project_path, config_mock, {activityName: 'AwesomeActivity'}, events_mock).then(function () {
                    expect(Manifest_mock.prototype.setName).toHaveBeenCalledWith('AwesomeActivity');
                }).fail(fail).done(done);
            });
            it('should use the ConfigParser-provided activity name, if exists', function (done) {
                config_mock.android_activityName.and.returnValue('AmazingActivity');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(Manifest_mock.prototype.setName).toHaveBeenCalledWith('AmazingActivity');
                }).fail(fail).done(done);
            });
        });
        describe('failure', function () {
            it('should fail if the target path already exists', function (done) {
                fs.existsSync.and.returnValue(true);
                create.create(project_path, config_mock, {}, events_mock).then(fail).fail(function (err) {
                    expect(err).toBeDefined();
                    expect(err.message).toContain('Project already exists!');
                }).done(done);
            });
        });
        describe('happy path', function () {
            it('should copy project templates from a specified custom template', function (done) {
                create.create(project_path, config_mock, {customTemplate: '/template/path'}, events_mock).then(function () {
                    expect(shell.cp).toHaveBeenCalledWith('-r', path.join('/template/path', 'assets'), project_path);
                    expect(shell.cp).toHaveBeenCalledWith('-r', path.join('/template/path', 'res'), project_path);
                    expect(shell.cp).toHaveBeenCalledWith(path.join('/template/path', 'gitignore'), path.join(project_path, '.gitignore'));
                }).fail(fail).done(done);
            });
            it('should copy project templates from the default templates location if no custom template is provided', function (done) {
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(shell.cp).toHaveBeenCalledWith('-r', path.join(default_templates, 'assets'), project_path);
                    expect(shell.cp).toHaveBeenCalledWith('-r', path.join(default_templates, 'res'), project_path);
                    expect(shell.cp).toHaveBeenCalledWith(path.join(default_templates, 'gitignore'), path.join(project_path, '.gitignore'));
                }).fail(fail).done(done);
            });
            it('should copy JS and library assets', function (done) {
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.copyJsAndLibrary).toHaveBeenCalled();
                }).fail(fail).done(done);
            });
            it('should create a java src directory based on the provided project package name', function (done) {
                config_mock.packageName.and.returnValue('org.apache.cordova');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(shell.mkdir).toHaveBeenCalledWith('-p', path.join(project_path, 'src', 'org', 'apache', 'cordova'));
                }).fail(fail).done(done);
            });
            it('should copy, rename and interpolate the template Activity java class with the project-specific activity name and package name', function (done) {
                config_mock.packageName.and.returnValue('org.apache.cordova');
                config_mock.android_activityName.and.returnValue('CEEDEEVEE');
                var activity_path = path.join(project_path, 'src', 'org', 'apache', 'cordova', 'CEEDEEVEE.java');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(shell.cp).toHaveBeenCalledWith('-f', path.join(default_templates, 'Activity.java'), activity_path);
                    expect(shell.sed).toHaveBeenCalledWith('-i', /__ACTIVITY__/, 'CEEDEEVEE', activity_path);
                    expect(shell.sed).toHaveBeenCalledWith('-i', /__ID__/, 'org.apache.cordova', activity_path);
                }).fail(fail).done(done);
            });
            it('should interpolate the project name into strings.xml', function (done) {
                config_mock.name.and.returnValue('IncredibleApp');
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(shell.sed).toHaveBeenCalledWith('-i', /__NAME__/, 'IncredibleApp', path.join(project_path, 'res', 'values', 'strings.xml'));
                }).fail(fail).done(done);
            });
            it('should copy template scripts into generated project', function (done) {
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.copyScripts).toHaveBeenCalledWith(project_path);
                }).fail(fail).done(done);
            });
            it('should copy build rules / gradle files into generated project', function (done) {
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.copyBuildRules).toHaveBeenCalledWith(project_path);
                }).fail(fail).done(done);
            });
            it('should write project.properties file with project details and target API', function (done) {
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.writeProjectProperties).toHaveBeenCalledWith(project_path, fake_android_target);
                }).fail(fail).done(done);
            });
            it('should prepare build files', function (done) {
                create.create(project_path, config_mock, {}, events_mock).then(function () {
                    expect(create.prepBuildFiles).toHaveBeenCalledWith(project_path);
                }).fail(fail).done(done);
            });
        });
    });
});
