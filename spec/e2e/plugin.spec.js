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
const fs = require('fs');
const path = require('path');
const shell = require('shelljs');
const execa = require('execa');
const { PluginInfoProvider } = require('cordova-common');

const createBin = path.join(__dirname, '../../bin/create');
const fakePluginPath = path.join(__dirname, 'fixtures/cordova-plugin-fake');

describe('plugin add', function () {
    let tmpDir;
    beforeEach(() => {
        const tmpDirTemplate = path.join(os.tmpdir(), `cordova-android-test-`);
        tmpDir = fs.realpathSync(fs.mkdtempSync(tmpDirTemplate));
    });
    afterEach(() => {
        shell.rm('-rf', tmpDir);
    });

    it('Test#001 : create project and add a plugin with framework', function () {
        const projectname = 'testpluginframework';
        const projectid = 'com.test.plugin.framework';

        const projectPath = path.join(tmpDir, projectname);
        const pluginInfo = new PluginInfoProvider().get(fakePluginPath);

        return Promise.resolve()
            .then(() => execa(createBin, [projectPath, projectid, projectname]))
            .then(() => {
                const Api = require(path.join(projectPath, 'cordova/Api.js'));
                return new Api('android', projectPath).addPlugin(pluginInfo);
            });
    }, 90000);
});
