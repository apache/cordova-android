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

const os = require('os');
const fs = require('fs-extra');
const path = require('path');
const { EventEmitter } = require('events');
const { ConfigParser, PluginInfoProvider } = require('cordova-common');

const Api = require('../../bin/templates/cordova/Api');

const fakePluginPath = path.join(__dirname, 'fixtures/cordova-plugin-fake');
const configXmlPath = path.join(__dirname, '../../bin/templates/project/res/xml/config.xml');

describe('plugin add', function () {
    let tmpDir;
    beforeEach(() => {
        const tmpDirTemplate = path.join(os.tmpdir(), 'cordova-android-test-');
        tmpDir = fs.realpathSync(fs.mkdtempSync(tmpDirTemplate));
    });
    afterEach(() => {
        fs.removeSync(tmpDir);
    });

    it('Test#001 : create project and add a plugin with framework', function () {
        const projectname = 'testpluginframework';
        const projectid = 'com.test.plugin.framework';

        const config = new ConfigParser(configXmlPath);
        config.setPackageName(projectid);
        config.setName(projectname);

        const projectPath = path.join(tmpDir, projectname);
        const pluginInfo = new PluginInfoProvider().get(fakePluginPath);
        const noopEvents = new EventEmitter();

        return Promise.resolve()
            .then(() => Api.createPlatform(projectPath, config, {}, noopEvents))
            .then(() => {
                // Allow test project to find the `cordova-android` module
                fs.ensureSymlinkSync(
                    path.join(__dirname, '../..'),
                    path.join(projectPath, 'node_modules/cordova-android'),
                    'junction'
                );

                // We need to use the API from the project or some paths break
                // TODO remove this and use the API instance returned from
                // createPlatform once we fixed the platform
                const Api = require(path.join(projectPath, 'cordova/Api.js'));
                const api = new Api('android', projectPath, noopEvents);

                return api.addPlugin(pluginInfo);
            });
    }, 90000);
});
