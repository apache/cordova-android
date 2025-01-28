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

const os = require('node:os');
const fs = require('node:fs');
const path = require('node:path');
const { EventEmitter } = require('events');
const { ConfigParser, PluginInfoProvider } = require('cordova-common');
const Api = require('../../lib/Api');

function makeTempDir () {
    const tmpDirTemplate = path.join(os.tmpdir(), 'cordova-android-test-');
    return fs.realpathSync(fs.mkdtempSync(tmpDirTemplate));
}

async function makeProject (projectPath) {
    const configXmlPath = path.join(__dirname, '../../templates/project/res/xml/config.xml');
    const config = new ConfigParser(configXmlPath);
    config.setPackageName('io.cordova.testapp');
    config.setName('TestApp');

    const noopEvents = new EventEmitter();

    return Api.createPlatform(projectPath, config, {}, noopEvents);
}

describe('E2E', function () {
    let tmpDir, projectPath, api;
    beforeEach(async () => {
        tmpDir = makeTempDir();

        projectPath = path.join(tmpDir, 'project');
        api = await makeProject(projectPath);
    });
    afterEach(() => {
        fs.rmSync(tmpDir, { recursive: true, force: true });
    });

    it('loads the API from a project directory', async () => {
        // Allow test project to find the `cordova-android` module
        fs.mkdirSync(path.join(tmpDir, 'node_modules'), { recursive: true });
        fs.symlinkSync(
            path.join(__dirname, '..', '..'),
            path.join(tmpDir, 'node_modules', 'cordova-android'),
            'junction'
        );

        expect(() => {
            require(path.join(projectPath, 'cordova/Api.js'));
        }).not.toThrow();
    });

    it('adds a plugin with framework', async () => {
        const fakePluginPath = path.join(__dirname, 'fixtures/cordova-plugin-fake');
        const pluginInfo = new PluginInfoProvider().get(fakePluginPath);

        await expectAsync(api.addPlugin(pluginInfo)).toBeResolved();
    });
});
