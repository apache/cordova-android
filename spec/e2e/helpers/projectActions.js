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

var PluginInfoProvider = require('cordova-common').PluginInfoProvider,
    shell = require('shelljs'),
    cp = require('child_process'),
    path  = require('path'),
    util  = require('util');

var cordova_bin = path.join(__dirname, '../../../bin');

module.exports.createProject = function (projectname, projectid, callback) {
    var projectDirName = getDirName(projectid);
    var createScriptPath = path.join(cordova_bin, 'create');

    // remove existing folder
    module.exports.removeProject(projectid);

    // create the project
    var command = util.format('%s %s %s "%s"', createScriptPath, projectDirName, projectid, projectname);
    cp.exec(command, function (error, stdout, stderr) {
		if (error) {
			throw 'An error occured while creating the project. Stderr:\n' + stderr;
		}
        callback();
    });
};

module.exports.buildProject = function (projectid, callback) {
    var projectDirName = getDirName(projectid);
    var command = path.join(projectDirName, 'cordova/build');
    cp.exec(command, function (error, stdout, stderr) {
		if (error) {
			throw 'An error occured while building the project. Stderr:\n' + stderr;
		}
        callback(error);
    });
};

module.exports.removeProject = function (projectid) {
    var projectDirName = getDirName(projectid);
    shell.rm('-rf', projectDirName);
};

module.exports.addPlugin = function (projectid, plugindir, callback) {
	var projectDirName = getDirName(projectid);
    var pip = new PluginInfoProvider();
    var pluginInfo = pip.get(plugindir);
    var Api = require(path.join(__dirname, '../../..', projectDirName, 'cordova', 'Api.js'));
    var api = new Api('android', projectDirName);

    api.addPlugin(pluginInfo).then(function () {
        callback(null);
    }, function (data) {
        console.log(data);
        callback(data);
    });
    
};

function getDirName(projectid) {
    return 'test-' + projectid;
}
