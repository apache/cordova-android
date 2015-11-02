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

var actions      = require('./helpers/projectActions.js'),
    shell        = require('shelljs'),
    fs           = require('fs'),
    util         = require('util'),
    platformOld  = { version: '4.0.0', path: 'cordova-android-old' },
    platformEdge = { version: getCurrentVersion(), path: '.' };

var DOWNLOAD_TIMEOUT = 2 * 60 * 1000,
    UPDATE_TIMEOUT   = 60 * 1000,
    PLATFORM_GIT_URL = 'https://github.com/apache/cordova-android';

function getCurrentVersion() {
    return fs.readFileSync('VERSION').toString().trim();
}

function testUpdate(projectname, projectid, createfrom, updatefrom, doBuild, done) {
    actions.createProject(projectname, projectid, createfrom.path, function (error) {
        expect(error).toBe(null);
        actions.updateProject(projectid, updatefrom.path, function (error) {
            expect(error).toBe(null);
            actions.getPlatformVersion(projectid, function (v) {
                expect(v).toEqual(updatefrom.version);
                if (doBuild) {
                    actions.buildProject(projectid, function (error) {
                        expect(error).toBe(null);
                        actions.removeProject(projectid);
                        done();
                    });
                } else {
                    actions.removeProject(projectid);
                    done();
                }
            });
        });
    });
}

describe('preparing fixtures', function () {

    it('cloning old platform', function (done) {
        var command = util.format('git clone %s --depth=1 --branch %s %s',
            PLATFORM_GIT_URL, platformOld.version, platformOld.path);
        shell.rm('-rf', platformOld.path);
        shell.exec(command, { silent: true }, function (err) {
            expect(err).toBe(0);
            done();
        });
    }, DOWNLOAD_TIMEOUT);

});

describe('update', function() {

    it('should update major version and build the project', function(done) {
        var projectname = 'testupdate';
        var projectid = 'com.test.update.app1';

        testUpdate(projectname, projectid, platformOld, platformEdge, true, done);

    }, UPDATE_TIMEOUT);

    it('should downgrade major version and build the project', function(done) {
        var projectname = 'testupdate';
        var projectid = 'com.test.update.app2';

        testUpdate(projectname, projectid, platformEdge, platformOld, true, done);
    }, UPDATE_TIMEOUT);

    // TODO: After next Android release, add tests for minor/patch version update
});

describe('cleanup', function () {

    it('remove cloned old platform', function() {
        shell.rm('-rf', platformOld.path);
    });

});
