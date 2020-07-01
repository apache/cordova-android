/**
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    'License'); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

var os = require('os');
var path = require('path');
var common = require('cordova-common');
var rewire = require('rewire');

var AndroidProject = require('../../bin/templates/cordova/lib/AndroidProject');

var PluginInfo = common.PluginInfo;

var FIXTURES = path.join(__dirname, '../e2e/fixtures');
var FAKE_PROJECT_DIR = path.join(os.tmpdir(), 'plugin-test-project');

describe('addPlugin method', function () {
    var api, Api;

    beforeEach(function () {
        Api = rewire('../../bin/templates/cordova/Api');

        var pluginManager = jasmine.createSpyObj('pluginManager', ['addPlugin']);
        pluginManager.addPlugin.and.resolveTo();
        spyOn(common.PluginManager, 'get').and.returnValue(pluginManager);

        var projectSpy = jasmine.createSpyObj('AndroidProject', ['getPackageName', 'write', 'isClean']);
        spyOn(AndroidProject, 'getProjectFile').and.returnValue(projectSpy);

        Api.__set__('Api.prototype.clean', async () => {});

        // Prevent logging to avoid polluting the test reports
        Api.__set__('selfEvents.emit', jasmine.createSpy());

        api = new Api('android', FAKE_PROJECT_DIR);
        spyOn(api._builder, 'prepBuildFiles');
    });

    const getPluginFixture = name => new PluginInfo(path.join(FIXTURES, name));

    it('Test#001 : should call gradleBuilder.prepBuildFiles for every plugin with frameworks', () => {
        return api.addPlugin(getPluginFixture('cordova-plugin-fake')).then(() => {
            expect(api._builder.prepBuildFiles).toHaveBeenCalled();
        });
    });

    it('Test#002 : shouldn\'t trigger gradleBuilder.prepBuildFiles for plugins without android frameworks', () => {
        return api.addPlugin(getPluginFixture('cordova-plugin-fake-ios-frameworks')).then(() => {
            expect(api._builder.prepBuildFiles).not.toHaveBeenCalled();
        });
    });
});
