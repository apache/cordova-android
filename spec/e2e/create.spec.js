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

var actions = require('./helpers/projectActions.js');

var CREATE_TIMEOUT = 60000;

function createAndBuild(projectname, projectid, done) {
    actions.createProject(projectname, projectid, function (error) {
        expect(error).toBe(null);
        actions.buildProject(projectid, function (error) {
            expect(error).toBe(null);
            actions.removeProject(projectid);
            done();   
        });
    });
}


describe('create', function() {

    it('create project with ascii name, no spaces', function(done) {
        var projectname = 'testcreate';
        var projectid = 'com.test.create.app1';

        createAndBuild(projectname, projectid, done);
    }, CREATE_TIMEOUT);

    it('create project with ascii name, and spaces', function(done) {
        var projectname = 'test create';
        var projectid = 'com.test.create.app2';

        createAndBuild(projectname, projectid, done);
    }, CREATE_TIMEOUT);

    it('create project with unicode name, no spaces', function(done) {
        var projectname = '応応応応用用用用';
        var projectid = 'com.test.create.app3';

        createAndBuild(projectname, projectid, done);
    }, CREATE_TIMEOUT);

    it('create project with unicode name, and spaces', function(done) {
        var projectname = '応応応応 用用用用';
        var projectid = 'com.test.create.app4';

        createAndBuild(projectname, projectid, done);
    }, CREATE_TIMEOUT);

    it('create project with ascii+unicode name, no spaces', function(done) {
        var projectname = '応応応応hello用用用用';
        var projectid = 'com.test.create.app5';

        createAndBuild(projectname, projectid, done);
    }, CREATE_TIMEOUT);

    it('create project with ascii+unicode name, and spaces', function(done) {
        var projectname = '応応応応 hello 用用用用';
        var projectid = 'com.test.create.app6';

        createAndBuild(projectname, projectid, done);
    }, CREATE_TIMEOUT);

});
