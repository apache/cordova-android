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

var addProperty = require('./src/util/addProperty');

module.exports = { };

addProperty(module, 'events', './src/events');
addProperty(module, 'superspawn', './src/superspawn');

addProperty(module, 'ActionStack', './src/ActionStack');
addProperty(module, 'CordovaError', './src/CordovaError/CordovaError');
addProperty(module, 'CordovaLogger', './src/CordovaLogger');
addProperty(module, 'CordovaExternalToolErrorContext', './src/CordovaError/CordovaExternalToolErrorContext');
addProperty(module, 'PlatformJson', './src/PlatformJson');
addProperty(module, 'ConfigParser', './src/ConfigParser/ConfigParser');
addProperty(module, 'FileUpdater', './src/FileUpdater');

addProperty(module, 'PluginInfo', './src/PluginInfo/PluginInfo');
addProperty(module, 'PluginInfoProvider', './src/PluginInfo/PluginInfoProvider');

addProperty(module, 'PluginManager', './src/PluginManager');

addProperty(module, 'ConfigChanges', './src/ConfigChanges/ConfigChanges');
addProperty(module, 'ConfigKeeper', './src/ConfigChanges/ConfigKeeper');
addProperty(module, 'ConfigFile', './src/ConfigChanges/ConfigFile');
addProperty(module, 'mungeUtil', './src/ConfigChanges/munge-util');

addProperty(module, 'xmlHelpers', './src/util/xml-helpers');

