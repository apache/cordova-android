/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

var fs = require('fs');
var path = require('path');
var shelljs = require('shelljs');
var mungeutil = require('./ConfigChanges/munge-util');
var pluginMappernto = require('cordova-registry-mapper').newToOld;
var pluginMapperotn = require('cordova-registry-mapper').oldToNew;

function PlatformJson (filePath, platform, root) {
    this.filePath = filePath;
    this.platform = platform;
    this.root = fix_munge(root || {});
}

PlatformJson.load = function (plugins_dir, platform) {
    var filePath = path.join(plugins_dir, platform + '.json');
    var root = null;
    if (fs.existsSync(filePath)) {
        root = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
    }
    return new PlatformJson(filePath, platform, root);
};

PlatformJson.prototype.save = function () {
    shelljs.mkdir('-p', path.dirname(this.filePath));
    fs.writeFileSync(this.filePath, JSON.stringify(this.root, null, 2), 'utf-8');
};

/**
 * Indicates whether the specified plugin is installed as a top-level (not as
 *  dependency to others)
 * @method function
 * @param  {String} pluginId A plugin id to check for.
 * @return {Boolean} true if plugin installed as top-level, otherwise false.
 */
PlatformJson.prototype.isPluginTopLevel = function (pluginId) {
    var installedPlugins = this.root.installed_plugins;
    return installedPlugins[pluginId] ||
        installedPlugins[pluginMappernto[pluginId]] ||
        installedPlugins[pluginMapperotn[pluginId]];
};

/**
 * Indicates whether the specified plugin is installed as a dependency to other
 *  plugin.
 * @method function
 * @param  {String} pluginId A plugin id to check for.
 * @return {Boolean} true if plugin installed as a dependency, otherwise false.
 */
PlatformJson.prototype.isPluginDependent = function (pluginId) {
    var dependentPlugins = this.root.dependent_plugins;
    return dependentPlugins[pluginId] ||
        dependentPlugins[pluginMappernto[pluginId]] ||
        dependentPlugins[pluginMapperotn[pluginId]];
};

/**
 * Indicates whether plugin is installed either as top-level or as dependency.
 * @method function
 * @param  {String} pluginId A plugin id to check for.
 * @return {Boolean} true if plugin installed, otherwise false.
 */
PlatformJson.prototype.isPluginInstalled = function (pluginId) {
    return this.isPluginTopLevel(pluginId) ||
        this.isPluginDependent(pluginId);
};

PlatformJson.prototype.addPlugin = function (pluginId, variables, isTopLevel) {
    var pluginsList = isTopLevel ?
        this.root.installed_plugins :
        this.root.dependent_plugins;

    pluginsList[pluginId] = variables;

    return this;
};

/**
 * @chaining
 * Generates and adds metadata for provided plugin into associated <platform>.json file
 *
 * @param   {PluginInfo}  pluginInfo  A pluginInfo instance to add metadata from
 * @returns {this} Current PlatformJson instance to allow calls chaining
 */
PlatformJson.prototype.addPluginMetadata = function (pluginInfo) {

    var installedModules = this.root.modules || [];

    var installedPaths = installedModules.map(function (installedModule) {
        return installedModule.file;
    });

    var modulesToInstall = pluginInfo.getJsModules(this.platform)
        .map(function (module) {
            return new ModuleMetadata(pluginInfo.id, module);
        })
        .filter(function (metadata) {
            // Filter out modules which are already added to metadata
            return installedPaths.indexOf(metadata.file) === -1;
        });

    this.root.modules = installedModules.concat(modulesToInstall);

    this.root.plugin_metadata = this.root.plugin_metadata || {};
    this.root.plugin_metadata[pluginInfo.id] = pluginInfo.version;

    return this;
};

PlatformJson.prototype.removePlugin = function (pluginId, isTopLevel) {
    var pluginsList = isTopLevel ?
        this.root.installed_plugins :
        this.root.dependent_plugins;

    delete pluginsList[pluginId];

    return this;
};

/**
 * @chaining
 * Removes metadata for provided plugin from associated file
 *
 * @param   {PluginInfo}  pluginInfo A PluginInfo instance to which modules' metadata
 *   we need to remove
 *
 * @returns {this} Current PlatformJson instance to allow calls chaining
 */
PlatformJson.prototype.removePluginMetadata = function (pluginInfo) {
    var modulesToRemove = pluginInfo.getJsModules(this.platform)
        .map(function (jsModule) {
            return ['plugins', pluginInfo.id, jsModule.src].join('/');
        });

    var installedModules = this.root.modules || [];
    this.root.modules = installedModules
        .filter(function (installedModule) {
            // Leave only those metadatas which 'file' is not in removed modules
            return (modulesToRemove.indexOf(installedModule.file) === -1);
        });

    if (this.root.plugin_metadata) {
        delete this.root.plugin_metadata[pluginInfo.id];
    }

    return this;
};

PlatformJson.prototype.addInstalledPluginToPrepareQueue = function (pluginDirName, vars, is_top_level, force) {
    this.root.prepare_queue.installed.push({'plugin': pluginDirName, 'vars': vars, 'topLevel': is_top_level, 'force': force});
};

PlatformJson.prototype.addUninstalledPluginToPrepareQueue = function (pluginId, is_top_level) {
    this.root.prepare_queue.uninstalled.push({'plugin': pluginId, 'id': pluginId, 'topLevel': is_top_level});
};

/**
 * Moves plugin, specified by id to top-level plugins. If plugin is top-level
 *  already, then does nothing.
 * @method function
 * @param  {String} pluginId A plugin id to make top-level.
 * @return {PlatformJson} PlatformJson instance.
 */
PlatformJson.prototype.makeTopLevel = function (pluginId) {
    var plugin = this.root.dependent_plugins[pluginId];
    if (plugin) {
        delete this.root.dependent_plugins[pluginId];
        this.root.installed_plugins[pluginId] = plugin;
    }
    return this;
};

/**
 * Generates a metadata for all installed plugins and js modules. The resultant
 *   string is ready to be written to 'cordova_plugins.js'
 *
 * @returns {String} cordova_plugins.js contents
 */
PlatformJson.prototype.generateMetadata = function () {
    return [
        'cordova.define(\'cordova/plugin_list\', function(require, exports, module) {',
        'module.exports = ' + JSON.stringify(this.root.modules, null, 2) + ';',
        'module.exports.metadata = ',
        '// TOP OF METADATA',
        JSON.stringify(this.root.plugin_metadata, null, 2) + ';',
        '// BOTTOM OF METADATA',
        '});' // Close cordova.define.
    ].join('\n');
};

/**
 * @chaining
 * Generates and then saves metadata to specified file. Doesn't check if file exists.
 *
 * @param {String} destination  File metadata will be written to
 * @return {PlatformJson} PlatformJson instance
 */
PlatformJson.prototype.generateAndSaveMetadata = function (destination) {
    var meta = this.generateMetadata();
    shelljs.mkdir('-p', path.dirname(destination));
    fs.writeFileSync(destination, meta, 'utf-8');

    return this;
};

// convert a munge from the old format ([file][parent][xml] = count) to the current one
function fix_munge (root) {
    root.prepare_queue = root.prepare_queue || {installed: [], uninstalled: []};
    root.config_munge = root.config_munge || {files: {}};
    root.installed_plugins = root.installed_plugins || {};
    root.dependent_plugins = root.dependent_plugins || {};

    var munge = root.config_munge;
    if (!munge.files) {
        var new_munge = { files: {} };
        for (var file in munge) {
            for (var selector in munge[file]) {
                for (var xml_child in munge[file][selector]) {
                    var val = parseInt(munge[file][selector][xml_child]);
                    for (var i = 0; i < val; i++) {
                        mungeutil.deep_add(new_munge, [file, selector, { xml: xml_child, count: val }]);
                    }
                }
            }
        }
        root.config_munge = new_munge;
    }

    return root;
}

/**
 * @constructor
 * @class ModuleMetadata
 *
 * Creates a ModuleMetadata object that represents module entry in 'cordova_plugins.js'
 *   file at run time
 *
 * @param {String}  pluginId  Plugin id where this module installed from
 * @param (JsModule|Object)  jsModule  A js-module entry from PluginInfo class to generate metadata for
 */
function ModuleMetadata (pluginId, jsModule) {

    if (!pluginId) throw new TypeError('pluginId argument must be a valid plugin id');
    if (!jsModule.src && !jsModule.name) throw new TypeError('jsModule argument must contain src or/and name properties');

    this.id = pluginId + '.' + (jsModule.name || jsModule.src.match(/([^\/]+)\.js/)[1]); /* eslint no-useless-escape: 0 */
    this.file = ['plugins', pluginId, jsModule.src].join('/');
    this.pluginId = pluginId;

    if (jsModule.clobbers && jsModule.clobbers.length > 0) {
        this.clobbers = jsModule.clobbers.map(function (o) { return o.target; });
    }
    if (jsModule.merges && jsModule.merges.length > 0) {
        this.merges = jsModule.merges.map(function (o) { return o.target; });
    }
    if (jsModule.runs) {
        this.runs = true;
    }
}

module.exports = PlatformJson;
