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

var et = require('elementtree');
var xml = require('../util/xml-helpers');
var CordovaError = require('../CordovaError/CordovaError');
var fs = require('fs');
var events = require('../events');

/** Wraps a config.xml file */
function ConfigParser (path) {
    this.path = path;
    try {
        this.doc = xml.parseElementtreeSync(path);
        this.cdvNamespacePrefix = getCordovaNamespacePrefix(this.doc);
        et.register_namespace(this.cdvNamespacePrefix, 'http://cordova.apache.org/ns/1.0');
    } catch (e) {
        events.emit('error', 'Parsing ' + path + ' failed');
        throw e;
    }
    var r = this.doc.getroot();
    if (r.tag !== 'widget') {
        throw new CordovaError(path + ' has incorrect root node name (expected "widget", was "' + r.tag + '")');
    }
}

function getNodeTextSafe (el) {
    return el && el.text && el.text.trim();
}

function findOrCreate (doc, name) {
    var ret = doc.find(name);
    if (!ret) {
        ret = new et.Element(name);
        doc.getroot().append(ret);
    }
    return ret;
}

function getCordovaNamespacePrefix (doc) {
    var rootAtribs = Object.getOwnPropertyNames(doc.getroot().attrib);
    var prefix = 'cdv';
    for (var j = 0; j < rootAtribs.length; j++) {
        if (rootAtribs[j].indexOf('xmlns:') === 0 &&
            doc.getroot().attrib[rootAtribs[j]] === 'http://cordova.apache.org/ns/1.0') {
            var strings = rootAtribs[j].split(':');
            prefix = strings[1];
            break;
        }
    }
    return prefix;
}

/**
 * Finds the value of an element's attribute
 * @param  {String} attributeName Name of the attribute to search for
 * @param  {Array}  elems         An array of ElementTree nodes
 * @return {String}
 */
function findElementAttributeValue (attributeName, elems) {

    elems = Array.isArray(elems) ? elems : [ elems ];

    var value = elems.filter(function (elem) {
        return elem.attrib.name.toLowerCase() === attributeName.toLowerCase();
    }).map(function (filteredElems) {
        return filteredElems.attrib.value;
    }).pop();

    return value || '';
}

function removeChildren (el, selector) {
    const matches = el.findall(selector);
    matches.forEach(child => el.remove(child));
}

ConfigParser.prototype = {
    getAttribute: function (attr) {
        return this.doc.getroot().attrib[attr];
    },

    packageName: function () {
        return this.getAttribute('id');
    },
    setPackageName: function (id) {
        this.doc.getroot().attrib['id'] = id;
    },
    android_packageName: function () {
        return this.getAttribute('android-packageName');
    },
    android_activityName: function () {
        return this.getAttribute('android-activityName');
    },
    ios_CFBundleIdentifier: function () {
        return this.getAttribute('ios-CFBundleIdentifier');
    },
    name: function () {
        return getNodeTextSafe(this.doc.find('name'));
    },
    setName: function (name) {
        var el = findOrCreate(this.doc, 'name');
        el.text = name;
    },
    shortName: function () {
        return this.doc.find('name').attrib['short'] || this.name();
    },
    setShortName: function (shortname) {
        var el = findOrCreate(this.doc, 'name');
        if (!el.text) {
            el.text = shortname;
        }
        el.attrib['short'] = shortname;
    },
    description: function () {
        return getNodeTextSafe(this.doc.find('description'));
    },
    setDescription: function (text) {
        var el = findOrCreate(this.doc, 'description');
        el.text = text;
    },
    version: function () {
        return this.getAttribute('version');
    },
    windows_packageVersion: function () {
        return this.getAttribute('windows-packageVersion');
    },
    android_versionCode: function () {
        return this.getAttribute('android-versionCode');
    },
    ios_CFBundleVersion: function () {
        return this.getAttribute('ios-CFBundleVersion');
    },
    setVersion: function (value) {
        this.doc.getroot().attrib['version'] = value;
    },
    author: function () {
        return getNodeTextSafe(this.doc.find('author'));
    },
    getGlobalPreference: function (name) {
        return findElementAttributeValue(name, this.doc.findall('preference'));
    },
    setGlobalPreference: function (name, value) {
        var pref = this.doc.find('preference[@name="' + name + '"]');
        if (!pref) {
            pref = new et.Element('preference');
            pref.attrib.name = name;
            this.doc.getroot().append(pref);
        }
        pref.attrib.value = value;
    },
    getPlatformPreference: function (name, platform) {
        return findElementAttributeValue(name, this.doc.findall('./platform[@name="' + platform + '"]/preference'));
    },
    getPreference: function (name, platform) {

        var platformPreference = '';

        if (platform) {
            platformPreference = this.getPlatformPreference(name, platform);
        }

        return platformPreference || this.getGlobalPreference(name);

    },
    /**
     * Returns all resources for the platform specified.
     * @param  {String} platform     The platform.
     * @param {string}  resourceName Type of static resources to return.
     *                               "icon" and "splash" currently supported.
     * @return {Array}               Resources for the platform specified.
     */
    getStaticResources: function (platform, resourceName) {
        var ret = [];
        var staticResources = [];
        if (platform) { // platform specific icons
            this.doc.findall('./platform[@name="' + platform + '"]/' + resourceName).forEach(function (elt) {
                elt.platform = platform; // mark as platform specific resource
                staticResources.push(elt);
            });
        }
        // root level resources
        staticResources = staticResources.concat(this.doc.findall(resourceName));
        // parse resource elements
        var that = this;
        staticResources.forEach(function (elt) {
            var res = {};
            res.src = elt.attrib.src;
            res.target = elt.attrib.target || undefined;
            res.density = elt.attrib['density'] || elt.attrib[that.cdvNamespacePrefix + ':density'] || elt.attrib['gap:density'];
            res.platform = elt.platform || null; // null means icon represents default icon (shared between platforms)
            res.width = +elt.attrib.width || undefined;
            res.height = +elt.attrib.height || undefined;

            // default icon
            if (!res.width && !res.height && !res.density) {
                ret.defaultResource = res;
            }
            ret.push(res);
        });

        /**
         * Returns resource with specified width and/or height.
         * @param  {number} width Width of resource.
         * @param  {number} height Height of resource.
         * @return {Resource} Resource object or null if not found.
         */
        ret.getBySize = function (width, height) {
            return ret.filter(function (res) {
                if (!res.width && !res.height) {
                    return false;
                }
                return ((!res.width || (width === res.width)) &&
                    (!res.height || (height === res.height)));
            })[0] || null;
        };

        /**
         * Returns resource with specified density.
         * @param  {string} density Density of resource.
         * @return {Resource}       Resource object or null if not found.
         */
        ret.getByDensity = function (density) {
            return ret.filter(function (res) {
                return res.density === density;
            })[0] || null;
        };

        /** Returns default icons */
        ret.getDefault = function () {
            return ret.defaultResource;
        };

        return ret;
    },

    /**
     * Returns all icons for specific platform.
     * @param  {string} platform Platform name
     * @return {Resource[]}      Array of icon objects.
     */
    getIcons: function (platform) {
        return this.getStaticResources(platform, 'icon');
    },

    /**
     * Returns all splash images for specific platform.
     * @param  {string} platform Platform name
     * @return {Resource[]}      Array of Splash objects.
     */
    getSplashScreens: function (platform) {
        return this.getStaticResources(platform, 'splash');
    },

    /**
     * Returns all resource-files for a specific platform.
     * @param  {string} platform Platform name
     * @param  {boolean} includeGlobal Whether to return resource-files at the
     *                                 root level.
     * @return {Resource[]}      Array of resource file objects.
     */
    getFileResources: function (platform, includeGlobal) {
        var fileResources = [];

        if (platform) { // platform specific resources
            fileResources = this.doc.findall('./platform[@name="' + platform + '"]/resource-file').map(function (tag) {
                return {
                    platform: platform,
                    src: tag.attrib.src,
                    target: tag.attrib.target,
                    versions: tag.attrib.versions,
                    deviceTarget: tag.attrib['device-target'],
                    arch: tag.attrib.arch
                };
            });
        }

        if (includeGlobal) {
            this.doc.findall('resource-file').forEach(function (tag) {
                fileResources.push({
                    platform: platform || null,
                    src: tag.attrib.src,
                    target: tag.attrib.target,
                    versions: tag.attrib.versions,
                    deviceTarget: tag.attrib['device-target'],
                    arch: tag.attrib.arch
                });
            });
        }

        return fileResources;
    },

    /**
     * Returns all hook scripts for the hook type specified.
     * @param  {String} hook     The hook type.
     * @param {Array}  platforms Platforms to look for scripts into (root scripts will be included as well).
     * @return {Array}               Script elements.
     */
    getHookScripts: function (hook, platforms) {
        var self = this;
        var scriptElements = self.doc.findall('./hook');

        if (platforms) {
            platforms.forEach(function (platform) {
                scriptElements = scriptElements.concat(self.doc.findall('./platform[@name="' + platform + '"]/hook'));
            });
        }

        function filterScriptByHookType (el) {
            return el.attrib.src && el.attrib.type && el.attrib.type.toLowerCase() === hook;
        }

        return scriptElements.filter(filterScriptByHookType);
    },
    /**
    * Returns a list of plugin (IDs).
    *
    * This function also returns any plugin's that
    * were defined using the legacy <feature> tags.
    * @return {string[]} Array of plugin IDs
    */
    getPluginIdList: function () {
        var plugins = this.doc.findall('plugin');
        var result = plugins.map(function (plugin) {
            return plugin.attrib.name;
        });
        var features = this.doc.findall('feature');
        features.forEach(function (element) {
            var idTag = element.find('./param[@name="id"]');
            if (idTag) {
                result.push(idTag.attrib.value);
            }
        });
        return result;
    },
    getPlugins: function () {
        return this.getPluginIdList().map(function (pluginId) {
            return this.getPlugin(pluginId);
        }, this);
    },
    /**
     * Adds a plugin element. Does not check for duplicates.
     * @name addPlugin
     * @function
     * @param {object} attributes name and spec are supported
     * @param {Array|object} variables name, value or arbitary object
     */
    addPlugin: function (attributes, variables) {
        if (!attributes && !attributes.name) return;
        var el = new et.Element('plugin');
        el.attrib.name = attributes.name;
        if (attributes.spec) {
            el.attrib.spec = attributes.spec;
        }

        // support arbitrary object as variables source
        if (variables && typeof variables === 'object' && !Array.isArray(variables)) {
            variables = Object.keys(variables)
                .map(function (variableName) {
                    return {name: variableName, value: variables[variableName]};
                });
        }

        if (variables) {
            variables.forEach(function (variable) {
                el.append(new et.Element('variable', { name: variable.name, value: variable.value }));
            });
        }
        this.doc.getroot().append(el);
    },
    /**
     * Retrives the plugin with the given id or null if not found.
     *
     * This function also returns any plugin's that
     * were defined using the legacy <feature> tags.
     * @name getPlugin
     * @function
     * @param {String} id
     * @returns {object} plugin including any variables
     */
    getPlugin: function (id) {
        if (!id) {
            return undefined;
        }
        var pluginElement = this.doc.find('./plugin/[@name="' + id + '"]');
        if (pluginElement === null) {
            var legacyFeature = this.doc.find('./feature/param[@name="id"][@value="' + id + '"]/..');
            if (legacyFeature) {
                events.emit('log', 'Found deprecated feature entry for ' + id + ' in config.xml.');
                return featureToPlugin(legacyFeature);
            }
            return undefined;
        }
        var plugin = {};

        plugin.name = pluginElement.attrib.name;
        plugin.spec = pluginElement.attrib.spec || pluginElement.attrib.src || pluginElement.attrib.version;
        plugin.variables = {};
        var variableElements = pluginElement.findall('variable');
        variableElements.forEach(function (varElement) {
            var name = varElement.attrib.name;
            var value = varElement.attrib.value;
            if (name) {
                plugin.variables[name] = value;
            }
        });
        return plugin;
    },
    /**
     * Remove the plugin entry with give name (id).
     *
     * This function also operates on any plugin's that
     * were defined using the legacy <feature> tags.
     * @name removePlugin
     * @function
     * @param id name of the plugin
     */
    removePlugin: function (id) {
        if (!id) return;
        const root = this.doc.getroot();
        removeChildren(root, `./plugin/[@name="${id}"]`);
        removeChildren(root, `./feature/param[@name="id"][@value="${id}"]/..`);
    },

    // Add any element to the root
    addElement: function (name, attributes) {
        var el = et.Element(name);
        for (var a in attributes) {
            el.attrib[a] = attributes[a];
        }
        this.doc.getroot().append(el);
    },

    /**
     * Adds an engine. Does not check for duplicates.
     * @param  {String} name the engine name
     * @param  {String} spec engine source location or version (optional)
     */
    addEngine: function (name, spec) {
        if (!name) return;
        var el = et.Element('engine');
        el.attrib.name = name;
        if (spec) {
            el.attrib.spec = spec;
        }
        this.doc.getroot().append(el);
    },
    /**
     * Removes all the engines with given name
     * @param  {String} name the engine name.
     */
    removeEngine: function (name) {
        removeChildren(this.doc.getroot(), `./engine/[@name="${name}"]`);
    },
    getEngines: function () {
        var engines = this.doc.findall('./engine');
        return engines.map(function (engine) {
            var spec = engine.attrib.spec || engine.attrib.version;
            return {
                'name': engine.attrib.name,
                'spec': spec || null
            };
        });
    },
    /* Get all the access tags */
    getAccesses: function () {
        var accesses = this.doc.findall('./access');
        return accesses.map(function (access) {
            var minimum_tls_version = access.attrib['minimum-tls-version']; /* String */
            var requires_forward_secrecy = access.attrib['requires-forward-secrecy']; /* Boolean */
            var requires_certificate_transparency = access.attrib['requires-certificate-transparency']; /* Boolean */
            var allows_arbitrary_loads_in_web_content = access.attrib['allows-arbitrary-loads-in-web-content']; /* Boolean */
            var allows_arbitrary_loads_in_media = access.attrib['allows-arbitrary-loads-in-media']; /* Boolean (DEPRECATED) */
            var allows_arbitrary_loads_for_media = access.attrib['allows-arbitrary-loads-for-media']; /* Boolean */
            var allows_local_networking = access.attrib['allows-local-networking']; /* Boolean */

            return {
                'origin': access.attrib.origin,
                'minimum_tls_version': minimum_tls_version,
                'requires_forward_secrecy': requires_forward_secrecy,
                'requires_certificate_transparency': requires_certificate_transparency,
                'allows_arbitrary_loads_in_web_content': allows_arbitrary_loads_in_web_content,
                'allows_arbitrary_loads_in_media': allows_arbitrary_loads_in_media,
                'allows_arbitrary_loads_for_media': allows_arbitrary_loads_for_media,
                'allows_local_networking': allows_local_networking
            };
        });
    },
    /* Get all the allow-navigation tags */
    getAllowNavigations: function () {
        var allow_navigations = this.doc.findall('./allow-navigation');
        return allow_navigations.map(function (allow_navigation) {
            var minimum_tls_version = allow_navigation.attrib['minimum-tls-version']; /* String */
            var requires_forward_secrecy = allow_navigation.attrib['requires-forward-secrecy']; /* Boolean */
            var requires_certificate_transparency = allow_navigation.attrib['requires-certificate-transparency']; /* Boolean */

            return {
                'href': allow_navigation.attrib.href,
                'minimum_tls_version': minimum_tls_version,
                'requires_forward_secrecy': requires_forward_secrecy,
                'requires_certificate_transparency': requires_certificate_transparency
            };
        });
    },
    /* Get all the allow-intent tags */
    getAllowIntents: function () {
        var allow_intents = this.doc.findall('./allow-intent');
        return allow_intents.map(function (allow_intent) {
            return {
                'href': allow_intent.attrib.href
            };
        });
    },
    /* Get all edit-config tags */
    getEditConfigs: function (platform) {
        var platform_edit_configs = this.doc.findall('./platform[@name="' + platform + '"]/edit-config');
        var edit_configs = this.doc.findall('edit-config').concat(platform_edit_configs);

        return edit_configs.map(function (tag) {
            var editConfig =
                {
                    file: tag.attrib['file'],
                    target: tag.attrib['target'],
                    mode: tag.attrib['mode'],
                    id: 'config.xml',
                    xmls: tag.getchildren()
                };
            return editConfig;
        });
    },

    /* Get all config-file tags */
    getConfigFiles: function (platform) {
        var platform_config_files = this.doc.findall('./platform[@name="' + platform + '"]/config-file');
        var config_files = this.doc.findall('config-file').concat(platform_config_files);

        return config_files.map(function (tag) {
            var configFile =
                {
                    target: tag.attrib['target'],
                    parent: tag.attrib['parent'],
                    after: tag.attrib['after'],
                    xmls: tag.getchildren(),
                    // To support demuxing via versions
                    versions: tag.attrib['versions'],
                    deviceTarget: tag.attrib['device-target']
                };
            return configFile;
        });
    },

    write: function () {
        fs.writeFileSync(this.path, this.doc.write({indent: 4}), 'utf-8');
    }
};

function featureToPlugin (featureElement) {
    var plugin = {};
    plugin.variables = [];
    var pluginVersion,
        pluginSrc;

    var nodes = featureElement.findall('param');
    nodes.forEach(function (element) {
        var n = element.attrib.name;
        var v = element.attrib.value;
        if (n === 'id') {
            plugin.name = v;
        } else if (n === 'version') {
            pluginVersion = v;
        } else if (n === 'url' || n === 'installPath') {
            pluginSrc = v;
        } else {
            plugin.variables[n] = v;
        }
    });

    var spec = pluginSrc || pluginVersion;
    if (spec) {
        plugin.spec = spec;
    }

    return plugin;
}
module.exports = ConfigParser;
