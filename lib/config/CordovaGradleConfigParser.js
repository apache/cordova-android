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

const fs = require('fs-extra');
const path = require('node:path');
const events = require('cordova-common').events;

class CordovaGradleConfigParser {
    /**
    * Loads and Edits Gradle Properties File.
    *
    * Do not construct this directly. Use CordovaGradleConfigParserFactory instead.
    *
    * @param {String} platformDir is the path of the Android platform directory
    */
    constructor (platformDir) {
        this._cdvGradleConfigFilePath = path.join(platformDir, 'cdv-gradle-config.json');
        this._cdvGradleConfig = this._readConfig(this._cdvGradleConfigFilePath);
    }

    /**
     * Reads and parses the configuration JSON file
     *
     * @param {String} configPath
     * @returns {Record<any, any>} The parsed JSON object representing the gradle config.
     */
    _readConfig (configPath) {
        return fs.readJSONSync(configPath, 'utf-8');
    }

    setPackageName (packageName) {
        events.emit('verbose', '[Cordova Gradle Config] Setting "PACKAGE_NAMESPACE" to ' + packageName);
        this._cdvGradleConfig.PACKAGE_NAMESPACE = packageName;
        return this;
    }

    getPackageName () {
        return this._cdvGradleConfig.PACKAGE_NAMESPACE;
    }

    getProjectNameFromPackageName () {
        const packageName = this._cdvGradleConfig.PACKAGE_NAMESPACE;
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * Saves any changes that has been made to the properties file.
     */
    write () {
        events.emit('verbose', '[Cordova Gradle Config] Saving File');
        fs.writeJSONSync(this._cdvGradleConfigFilePath, this._cdvGradleConfig, 'utf-8');
    }
}

module.exports = CordovaGradleConfigParser;
