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

var PluginInfoProvider = require('cordova-common').PluginInfoProvider;
var shell = require('shelljs');
var cp = require('child_process');
var path = require('path');
var util = require('util');

var cordova_bin = path.join(__dirname, '../../../bin');

/**
 * Creates a project using platform create script with given parameters
 * @param {string} projectname - name of the project
 * @param {string} projectid - id of the project
 * @param {string} platformpath - path to the platform
 * @param {function} callback - function which is called (without arguments) when the project is created or (with error object) when error occurs
 */
module.exports.createProject = function (projectname, projectid, platformpath, callback) {
    // platformpath is optional
    if (!callback && typeof platformpath === 'function') {
        callback = platformpath;
        platformpath = null;
    }
    var projectDirName = getDirName(projectid);
    var createScriptPath = platformpath ? path.join(platformpath, 'bin/create') : path.join(cordova_bin, 'create');

    // remove existing folder
    module.exports.removeProject(projectid);

    // create the project
    var command = util.format('"%s" %s %s "%s"', createScriptPath, projectDirName, projectid, projectname);
    cp.exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(stdout);
            console.error(stderr);
        }
        callback(error);
    });
};

/**
 * Updates a project using platform update script with given parameters
 * @param {string} projectid - id of the project
 * @param {string} platformpath - path to the platform
 * @param {function} callback - function which is called (without arguments) when the project is updated or (with error object) when error occurs
 */
module.exports.updateProject = function (projectid, platformpath, callback) {
    // platformpath is optional
    if (!callback && typeof platformpath === 'function') {
        callback = platformpath;
        platformpath = null;
    }
    var projectDirName = getDirName(projectid);
    var updateScriptPath = platformpath ? path.join(platformpath, 'bin/update') : path.join(cordova_bin, 'update');
    var command = util.format('"%s" %s', updateScriptPath, projectDirName);
    cp.exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(stdout);
            console.error(stderr);
        }
        callback(error);
    });

};

/**
 * Builds a project using platform build script with given parameters
 * @param {string} projectid - id of the project
 * @param {function} callback - function which is called (without arguments) when the project is built or (with error object) when error occurs
 */
module.exports.buildProject = function (projectid, callback) {
    var projectDirName = getDirName(projectid);
    var command = path.join(projectDirName, 'cordova/build');

    cp.exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(stdout);
            console.error(stderr);
        }
        callback(error);
    });
};

/**
 * Removes a project
 * @param {string} projectid - id of the project
 */
module.exports.removeProject = function (projectid) {
    var projectDirName = getDirName(projectid);
    shell.rm('-rf', projectDirName);
};

/**
 * Add a plugin to a project using platform api
 * @param {string} projectid - id of the project
 * @param {string} plugindir - path to a plugin
 * @param {function} callback - function which is called (without arguments) when the plugin is added or (with error object) when error occurs
 */
module.exports.addPlugin = function (projectid, plugindir, callback) {
    var projectDirName = getDirName(projectid);
    var pip = new PluginInfoProvider();
    var pluginInfo = pip.get(plugindir);
    var Api = require(path.join(__dirname, '../../..', projectDirName, 'cordova', 'Api.js'));
    var api = new Api('android', projectDirName);

    api.addPlugin(pluginInfo).then(function () {
        callback(null);
    }, function (error) {
        console.error(error);
        callback(error);
    });
};

/**
 * Gets a version number from project using platform script
 * @param {string} projectid - id of the project
 * @param {function} callback - function which is called with platform version as an argument
 */
module.exports.getPlatformVersion = function (projectid, callback) {
    var command = path.join(getDirName(projectid), 'cordova/version');

    cp.exec(command, function (error, stdout, stderr) {
        if (error) {
            console.log(stdout);
            console.error(stderr);
        }
        callback(stdout.trim());
    });
};

function getDirName (projectid) {
    return 'test-' + projectid;
}
