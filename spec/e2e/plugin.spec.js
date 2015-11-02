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

var path    = require('path'),
    actions = require('./helpers/projectActions.js');

var PLUGIN_ADD_TIMEOUT = 60000;

describe('plugin add', function() {

    it('create project and add a plugin with framework', function(done) {
        var projectname = 'testpluginframework';
        var projectid = 'com.test.plugin.framework';
        var fakePluginPath = path.join(__dirname, 'fixtures/cordova-plugin-fake');

        actions.createProject(projectname, projectid, function () {
            actions.addPlugin(projectid, fakePluginPath, function (error) {
                actions.removeProject(projectid);
                if (error) {
                    console.error(error.stack);
                }
                expect(error).toBe(null);
                done();
            });
        });
    }, PLUGIN_ADD_TIMEOUT);

});
