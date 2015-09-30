console.log('Required new Api');

var Q = require('q');
var fs = require('fs');
var path = require('path');
// var unorm = require('unorm');
var shell = require('shelljs');
// var semver = require('semver');

// var superspawn = require('../cordova/superspawn');
var xmlHelpers = require('cordova-common').xmlHelpers;
// var common = require('../plugman/platforms/common');
// var knownPlatforms = require('./platforms');
// var CordovaError = require('../CordovaError');
// var PluginInfo = require('../PluginInfo');
var ConfigParser = require('cordova-common').ConfigParser;
var PlatformJson = require('cordova-common').PlatformJson;
// var ActionStack = require('../plugman/util/action-stack');
var PlatformMunger = require('cordova-common').ConfigChanges.PlatformMunger;
var PluginInfoProvider = require('cordova-common').PluginInfoProvider;

/**
 * Class, that acts as abstraction over particular platform. Encapsulates the
 *   platform's properties and methods.
 *
 * Platform that implements own PlatformApi instance _should implement all
 *   prototype methods_ of this class to be fully compatible with cordova-lib.
 *
 * The PlatformApi instance also should define the following field:
 *
 * * platform: String that defines a platform name.
 */
function PlatformApiPoly(platform, platformRootDir, events) {
    this.platform = 'android';
    this.root = path.resolve(__dirname, '..');
    this.events = events || new (require('events').EventEmitter)();

    // var ParserConstructor = require(knownPlatforms[platform].parser_file);
    // this._parser = new ParserConstructor(this.root);
    // this._handler = require(knownPlatforms[platform].handler_file);

    this._platformJson = PlatformJson.load(this.root, platform);
    this._pluginInfoProvider = new PluginInfoProvider();
    this._munger = new PlatformMunger(this.platform, this.root, this._platformJson, this._pluginInfoProvider);

    var self = this;

    this.locations = {
        www: path.join(self.root, 'assets/www'),
        platformWww: path.join(self.root, 'platform_www'),
        configXml: path.join(self.root, 'res/xml/config.xml'),
        strings: path.join(self.root, 'res/values/strings.xml'),
        manifest: path.join(self.root, 'AndroidManifest.xml'),
        // NOTE: Due to platformApi spec we need to return relative paths here
        cordovaJs: 'bin/templates/project/assets/www/cordova.js',
        cordovaJsSrc: 'cordova-js-src'
    };
}

/**
 * Installs platform to specified directory and creates a platform project.
 *
 * @param  {CordovaProject} cordovaProject A CordovaProject instance, that defines a
 *   project structure and configuration, that should be applied to new platform
 *   (contains platform's target location and ConfigParser instance for
 *   project's config). This argument is optional and if not defined, this means
 *   that platform is used as standalone project and is not a part of cordova
 *   project.
 * @param  {Object}  options  An options object. The most common options are:
 * @param  {String}  options.customTemplate  A path to custom template, that
 *   should override the default one from platform.
 * @param  {Boolean}  options.link  Flag that indicates that platform's sources
 *   will be linked to installed platform instead of copying.
 *
 * @return {Promise<PlatformApi>} Promise either fulfilled with PlatformApi
 *   instance or rejected with CordovaError.
 */
// PlatformApiPoly.createPlatform = function (cordovaProject, options) {
//     if (!options || !options.platformDetails)
//         return Q.reject(CordovaError('Failed to find platform\'s \'create\' script. ' +
//             'Either \'options\' parameter or \'platformDetails\' option is missing'));

//     var command = path.join(options.platformDetails.libDir, 'bin', 'create');
//     var commandArguments = getCreateArgs(cordovaProject, options);

//     return superspawn.spawn(command, commandArguments,
//         { printCommand: true, stdio: 'inherit', chmod: true })
//     .then(function () {
//         var destination = path.join(cordovaProject.locations.platforms, options.platformDetails.platform);
//         var platformApi = knownPlatforms
//             .getPlatformApi(options.platformDetails.platform, destination);
//         copyCordovaSrc(options.platformDetails.libDir, platformApi.getPlatformInfo());
//         return platformApi;
//     });
// };

/**
 * Updates already installed platform.
 *
 * @param   {CordovaProject}  cordovaProject  A CordovaProject instance, that
 *   defines a project structure and configuration, that should be applied to
 *   new platform (contains platform's target location and ConfigParser instance
 *   for project's config). This argument is optional and if not defined, this
 *   means that platform is used as standalone project and is not a part of
 *   cordova project.
 * @param  {Object}  options  An options object. The most common options are:
 * @param  {String}  options.customTemplate  A path to custom template, that
 *   should override the default one from platform.
 * @param  {Boolean}  options.link  Flag that indicates that platform's sources
 *   will be linked to installed platform instead of copying.
 *
 * @return {Promise<PlatformApi>} Promise either fulfilled with PlatformApi
 *   instance or rejected with CordovaError.
 */
// PlatformApiPoly.updatePlatform = function (cordovaProject, options) {
//     if (!options || !options.platformDetails)
//         return Q.reject(CordovaError('Failed to find platform\'s \'create\' script. ' +
//             'Either \'options\' parameter or \'platformDetails\' option is missing'));

//     var command = path.join(options.platformDetails.libDir, 'bin', 'update');
//     var destination = path.join(cordovaProject.locations.platforms, options.platformDetails.platform);

//     return superspawn.spawn(command, [destination],
//         { printCommand: true, stdio: 'inherit', chmod: true })
//     .then(function () {
//         var platformApi = knownPlatforms
//             .getPlatformApi(options.platformDetails.platform, destination);
//         copyCordovaSrc(options.platformDetails.libDir, platformApi.getPlatformInfo());
//         return platformApi;
//     });
// };

/**
 * Gets a CordovaPlatform object, that represents the platform structure.
 *
 * @return  {CordovaPlatform}  A structure that contains the description of
 *   platform's file structure and other properties of platform.
 */
PlatformApiPoly.prototype.getPlatformInfo = function () {
    var self = this;
    var result = {};
    result.locations = this.locations;
    result.root = self.root;
    result.name = self.platform;
    // TODO: replace version with one from package.json
    result.version = '4.2.0-dev';
    result.projectConfig = self._config;

    return result;
};

/**
 * Updates installed platform with provided www assets and new app
 *   configuration. This method is required for CLI workflow and will be called
 *   each time before build, so the changes, made to app configuration and www
 *   code, will be applied to platform.
 *
 * @param {CordovaProject} cordovaProject A CordovaProject instance, that defines a
 *   project structure and configuration, that should be applied to platform
 *   (contains project's www location and ConfigParser instance for project's
 *   config).
 *
 * @return  {Promise}  Return a promise either fulfilled, or rejected with
 *   CordovaError instance.
 */
PlatformApiPoly.prototype.prepare = function (cordovaProject) {
    // First cleanup current config and merge project's one into own
    var defaultConfig = path.join(this.root, 'cordova/defaults.xml');
    // TODO: reuse getPlatformInfo method here
    var ownConfig = path.join(this.root, 'res/xml/config.xml');
    var sourceCfg = cordovaProject.projectConfig.path;
    // If defaults.xml is present, overwrite platform config.xml with it.
    // Otherwise save whatever is there as defaults so it can be
    // restored or copy project config into platform if none exists.
    if (fs.existsSync(defaultConfig)) {
        this.events.emit('verbose', 'Generating config.xml from defaults for platform "' + this.platform + '"');
        shell.cp('-f', defaultConfig, ownConfig);
    } else if (fs.existsSync(ownConfig)) {
        shell.cp('-f', ownConfig, defaultConfig);
    } else {
        shell.cp('-f', sourceCfg.path, ownConfig);
    }

    this._munger.reapply_global_munge().save_all();

    this._config = new ConfigParser(ownConfig);
    xmlHelpers.mergeXml(cordovaProject.projectConfig.doc.getroot(),
        this._config.doc.getroot(), this.platform, true);
    this._config.write();

    // Update own www dir with project's www assets and plugins' assets and js-files
    this._updateWww(cordovaProject.locations.www);

    // update project according to config.xml changes.
    try {
        this._updateProject();
        this._updateOverrides();
        this.events.emit('verbose', 'updated project successfully');
        return Q();
    } catch (err) {
        this.events.emit('error', err);
        return Q.reject(err);
    }
};

/**
 * Installs a new plugin into platform. This method only copies non-www files
 *   (sources, libs, etc.) to platform. It also doesn't resolves the
 *   dependencies of plugin. Both of handling of www files, such as assets and
 *   js-files and resolving dependencies are the responsibility of caller.
 *
 * @param  {PluginInfo}  plugin  A PluginInfo instance that represents plugin
 *   that will be installed.
 * @param  {Object}  installOptions  An options object. Possible options below:
 * @param  {Boolean}  installOptions.link: Flag that specifies that plugin
 *   sources will be symlinked to app's directory instead of copying (if
 *   possible).
 * @param  {Object}  installOptions.variables  An object that represents
 *   variables that will be used to install plugin. See more details on plugin
 *   variables in documentation:
 *   https://cordova.apache.org/docs/en/4.0.0/plugin_ref_spec.md.html
 *
 * @return  {Promise}  Return a promise either fulfilled, or rejected with
 *   CordovaError instance.
 */
// PlatformApiPoly.prototype.addPlugin = function (plugin, installOptions) {

//     if (!plugin || !(plugin instanceof PluginInfo))
//         return Q.reject('The parameter is incorrect. The first parameter ' +
//             'should be valid PluginInfo instance');

//     installOptions = installOptions || {};
//     installOptions.variables = installOptions.variables || {};

//     var self = this;
//     var actions = new ActionStack();
//     var projectFile = this._handler.parseProjectFile && this._handler.parseProjectFile(this.root);

//     // gather all files needs to be handled during install
//     plugin.getFilesAndFrameworks(this.platform)
//         .concat(plugin.getAssets(this.platform))
//         .concat(plugin.getJsModules(this.platform))
//     .forEach(function(item) {
//         actions.push(actions.createAction(
//             self._getInstaller(item.itemType), [item, plugin.dir, plugin.id, installOptions, projectFile],
//             self._getUninstaller(item.itemType), [item, plugin.dir, plugin.id, installOptions, projectFile]));
//     });

//     // run through the action stack
//     return actions.process(this.platform, this.root)
//     .then(function () {
//         if (projectFile) {
//             projectFile.write();
//         }

//         // Add PACKAGE_NAME variable into vars
//         if (!installOptions.variables.PACKAGE_NAME) {
//             installOptions.variables.PACKAGE_NAME = self._handler.package_name(self.root);
//         }

//         self._munger
//             // Ignore passed `is_top_level` option since platform itself doesn't know
//             // anything about managing dependencies - it's responsibility of caller.
//             .add_plugin_changes(plugin, installOptions.variables, /*is_top_level=*/true, /*should_increment=*/true)
//             .save_all();

//         var targetDir = installOptions.usePlatformWww ?
//             self.getPlatformInfo().locations.platformWww :
//             self.getPlatformInfo().locations.www;

//         self._addModulesInfo(plugin, targetDir);
//     });
// };

/**
 * Removes an installed plugin from platform.
 *
 * Since method accepts PluginInfo instance as input parameter instead of plugin
 *   id, caller shoud take care of managing/storing PluginInfo instances for
 *   future uninstalls.
 *
 * @param  {PluginInfo}  plugin  A PluginInfo instance that represents plugin
 *   that will be installed.
 *
 * @return  {Promise}  Return a promise either fulfilled, or rejected with
 *   CordovaError instance.
 */
// PlatformApiPoly.prototype.removePlugin = function (plugin, uninstallOptions) {

//     var self = this;
//     var actions = new ActionStack();
//     var projectFile = this._handler.parseProjectFile && this._handler.parseProjectFile(this.root);

//     // queue up plugin files
//     plugin.getFilesAndFrameworks(this.platform)
//         .concat(plugin.getAssets(this.platform))
//         .concat(plugin.getJsModules(this.platform))
//     .filter(function (item) {
//         // CB-5238 Skip (don't uninstall) non custom frameworks.
//         return !(item.itemType == 'framework' && !item.custom);
//     }).forEach(function(item) {
//         actions.push(actions.createAction(
//             self._getUninstaller(item.itemType), [item, plugin.dir, plugin.id, uninstallOptions, projectFile],
//             self._getInstaller(item.itemType), [item, plugin.dir, plugin.id, uninstallOptions, projectFile]));
//     });

//     // run through the action stack
//     return actions.process(this.platform, this.root)
//     .then(function() {
//         if (projectFile) {
//             projectFile.write();
//         }

//         self._munger
//             // Ignore passed `is_top_level` option since platform itself doesn't know
//             // anything about managing dependencies - it's responsibility of caller.
//             .remove_plugin_changes(plugin, /*is_top_level=*/true)
//             .save_all();

//         var targetDir = uninstallOptions.usePlatformWww ?
//             self.getPlatformInfo().locations.platformWww :
//             self.getPlatformInfo().locations.www;

//         self._removeModulesInfo(plugin, targetDir);
//         // Remove stale plugin directory
//         // TODO: this should be done by plugin files uninstaller
//         shell.rm('-rf', path.resolve(self.root, 'Plugins', plugin.id));
//     });
// };

// PlatformApiPoly.prototype.updatePlugin = function (plugin, updateOptions) {
//     var self = this;

//     // Set up assets installer to copy asset files into platform_www dir instead of www
//     updateOptions = updateOptions || {};
//     updateOptions.usePlatformWww = true;

//     return this.removePlugin(plugin, updateOptions)
//     .then(function () {
//         return  self.addPlugin(plugin, updateOptions);
//     });
// };

/**
 * Builds an application package for current platform.
 *
 * @param  {Object}  buildOptions  A build options. This object's structure is
 *   highly depends on platform's specific. The most common options are:
 * @param  {Boolean}  buildOptions.debug  Indicates that packages should be
 *   built with debug configuration. This is set to true by default unless the
 *   'release' option is not specified.
 * @param  {Boolean}  buildOptions.release  Indicates that packages should be
 *   built with release configuration. If not set to true, debug configuration
 *   will be used.
 * @param   {Boolean}  buildOptions.device  Specifies that built app is intended
 *   to run on device
 * @param   {Boolean}  buildOptions.emulator: Specifies that built app is
 *   intended to run on emulator
 * @param   {String}  buildOptions.target  Specifies the device id that will be
 *   used to run built application.
 * @param   {Boolean}  buildOptions.nobuild  Indicates that this should be a
 *   dry-run call, so no build artifacts will be produced.
 * @param   {String[]}  buildOptions.archs  Specifies chip architectures which
 *   app packages should be built for. List of valid architectures is depends on
 *   platform.
 * @param   {String}  buildOptions.buildConfig  The path to build configuration
 *   file. The format of this file is depends on platform.
 * @param   {String[]} buildOptions.argv Raw array of command-line arguments,
 *   passed to `build` command. The purpose of this property is to pass a
 *   platform-specific arguments, and eventually let platform define own
 *   arguments processing logic.
 *
 * @return {Promise<Object[]>} A promise either fulfilled with an array of build
 *   artifacts (application packages) if package was built successfully,
 *   or rejected with CordovaError. The resultant build artifact objects is not
 *   strictly typed and may conatin arbitrary set of fields as in sample below.
 *
 *     {
 *         architecture: 'x86',
 *         buildType: 'debug',
 *         path: '/path/to/build',
 *         type: 'app'
 *     }
 *
 * The return value in most cases will contain only one item but in some cases
 *   there could be multiple items in output array, e.g. when multiple
 *   arhcitectures is specified.
 */
PlatformApiPoly.prototype.build = function (buildOptions) {
    var self = this;
    return require('./lib/check_reqs').run()
    .then(function () {
        return require('./lib/build').run.call(self, buildOptions);
    })
    .then(function (buildResults) {
        // Cast build result to array of build artifacts
        return buildResults.apkPaths.map(function (apkPath) {
            return {
                buildType: buildResults.buildType,
                buildMethod: buildResults.buildMethod,
                path: apkPath,
                type: 'apk'
            };
        });
    });
};

/**
 * Builds an application package for current platform and runs it on
 *   specified/default device. If no 'device'/'emulator'/'target' options are
 *   specified, then tries to run app on default device if connected, otherwise
 *   runs the app on emulator.
 *
 * @param   {Object}  runOptions  An options object. The structure is the same
 *   as for build options.
 *
 * @return {Promise} A promise either fulfilled if package was built and ran
 *   successfully, or rejected with CordovaError.
 */
PlatformApiPoly.prototype.run = function(runOptions) {
    var self = this;
    // TODO: Ensure that this always rejected with CordovaError
    return require('./lib/check_reqs').run()
    .then(function () {
        return require('./lib/run').run.call(self, runOptions);
    });
};

/**
 * Cleans out the build artifacts from platform's directory.
 *
 * @return  {Promise}  Return a promise either fulfilled, or rejected with
 *   CordovaError.
 */
// PlatformApiPoly.prototype.clean = function() {
//     var cmd = path.join(this.root, 'cordova', 'clean');
//     return superspawn.spawn(cmd, [], { printCommand: true, stdio: 'inherit', chmod: true });
// };

/**
 * Performs a requirements check for current platform. Each platform defines its
 *   own set of requirements, which should be resolved before platform can be
 *   built successfully.
 *
 * @return  {Promise<Requirement[]>}  Promise, resolved with set of Requirement
 *   objects for current platform.
 */
// PlatformApiPoly.prototype.requirements = function() {
//     var modulePath = path.join(this.root, 'cordova', 'lib', 'check_reqs');
//     try {
//         return require(modulePath).check_all();
//     } catch (e) {
//         var errorMsg = 'Failed to check requirements for ' + this.platform + ' platform. ' +
//             'check_reqs module is missing for platfrom. Skipping it...';
//         return Q.reject(errorMsg);
//     }
// };

module.exports = PlatformApiPoly;

/**
 * Converts arguments, passed to createPlatform to command-line args to
 *   'bin/create' script for specific platform.
 *
 * @param   {ProjectInfo}  project  A current project information. The vauest
 *   which this method interested in are project.config - config.xml abstraction
 *   - and platformsLocation - to get install destination.
 * @param   {Object}       options  Set of properties for create script.
 *
 * @return  {String[]}     An array or arguments which can be passed to
 *   'bin/create'.
 */
// function getCreateArgs(project, options) {
//     var platformName = options.platformDetails.platform;
//     var platformVersion = options.platformDetails.version;

//     var args = [];
//     args.push(path.join(project.locations.platforms, platformName)); // output
//     args.push(project.projectConfig.packageName().replace(/[^\w.]/g,'_'));
//     // CB-6992 it is necessary to normalize characters
//     // because node and shell scripts handles unicode symbols differently
//     // We need to normalize the name to NFD form since iOS uses NFD unicode form
//     args.push(platformName == 'ios' ? unorm.nfd(project.projectConfig.name()) : project.projectConfig.name());

//     if (options.customTemplate) {
//         args.push(options.customTemplate);
//     }

//     if (/android|ios/.exec(platformName) &&
//         semver.gt(platformVersion, '3.3.0')) args.push('--cli');

//     if (options.link) args.push('--link');

//     if (platformName === 'android' && semver.gte(platformVersion, '4.0.0-dev')) {
//         var activityName = project.projectConfig.android_activityName();
//         if (activityName) {
//             args.push('--activity-name', activityName.replace(/\W/g, ''));
//         }
//     }

//     return args;
// }

/**
 * Reconstructs the buildOptions tat will be passed along to platform scripts.
 *   This is an ugly temporary fix. The code spawning or otherwise calling into
 *   platform code should be dealing with this based on the parsed args object.
 *
 * @param   {Object}  options  A build options set, passed to `build` method
 *
 * @return  {String[]}         An array or arguments which can be passed to
 *   `create` build script.
 */
// function getBuildArgs(options) {
//     // if no options passed, empty object will be returned
//     if (!options) return [];

//     var downstreamArgs = [];
//     var argNames =[
//         'debug',
//         'release',
//         'device',
//         'emulator',
//         'nobuild',
//         'list'
//     ];

//     argNames.forEach(function(flag) {
//         if (options[flag]) {
//             downstreamArgs.push('--' + flag);
//         }
//     });

//     if (options.buildConfig) {
//         downstreamArgs.push('--buildConfig=' + options.buildConfig);
//     }
//     if (options.target) {
//         downstreamArgs.push('--target=' + options.target);
//     }
//     if (options.archs) {
//         downstreamArgs.push('--archs=' + options.archs);
//     }

//     var unparsedArgs = options.argv || [];
//     return downstreamArgs.concat(unparsedArgs);
// }

/**
 * Removes the specified modules from list of installed modules and updates
 *   platform_json and cordova_plugins.js on disk.
 *
 * @param   {PluginInfo}  plugin  PluginInfo instance for plugin, which modules
 *   needs to be added.
 * @param   {String}  targetDir  The directory, where updated cordova_plugins.js
 *   should be written to.
 */
// PlatformApiPoly.prototype._addModulesInfo = function(plugin, targetDir) {
//     var installedModules = this._platformJson.root.modules || [];

//     var installedPaths = installedModules.map(function (installedModule) {
//         return installedModule.file;
//     });

//     var modulesToInstall = plugin.getJsModules(this.platform)
//     .filter(function (moduleToInstall) {
//         return installedPaths.indexOf(moduleToInstall.file) === -1;
//     }).map(function (moduleToInstall) {
//         var moduleName = plugin.id + '.' + ( moduleToInstall.name || moduleToInstall.src.match(/([^\/]+)\.js/)[1] );
//         var obj = {
//             file: ['plugins', plugin.id, moduleToInstall.src].join('/'),
//             id: moduleName
//         };
//         if (moduleToInstall.clobbers.length > 0) {
//             obj.clobbers = moduleToInstall.clobbers.map(function(o) { return o.target; });
//         }
//         if (moduleToInstall.merges.length > 0) {
//             obj.merges = moduleToInstall.merges.map(function(o) { return o.target; });
//         }
//         if (moduleToInstall.runs) {
//             obj.runs = true;
//         }

//         return obj;
//     });

//     this._platformJson.root.modules = installedModules.concat(modulesToInstall);
//     this._writePluginModules(targetDir);
//     this._platformJson.save();
// };

/**
 * Removes the specified modules from list of installed modules and updates
 *   platform_json and cordova_plugins.js on disk.
 *
 * @param   {PluginInfo}  plugin  PluginInfo instance for plugin, which modules
 *   needs to be removed.
 * @param   {String}  targetDir  The directory, where updated cordova_plugins.js
 *   should be written to.
 */
// PlatformApiPoly.prototype._removeModulesInfo = function(plugin, targetDir) {
//     var installedModules = this._platformJson.root.modules || [];
//     var modulesToRemove = plugin.getJsModules(this.platform)
//     .map(function (jsModule) {
//         return  ['plugins', plugin.id, jsModule.src].join('/');
//     });

//     var updatedModules = installedModules
//     .filter(function (installedModule) {
//         return (modulesToRemove.indexOf(installedModule.file) === -1);
//     });

//     this._platformJson.root.modules = updatedModules;
//     this._writePluginModules(targetDir);
//     this._platformJson.save();
// };

/**
 * Fetches all installed modules, generates cordova_plugins contents and writes
 *   it to file.
 *
 * @param   {String}  targetDir  Directory, where write cordova_plugins.js to.
 *   Ususally it is either <platform>/www or <platform>/platform_www
 *   directories.
 */
// PlatformApiPoly.prototype._writePluginModules = function (targetDir) {
//     var self = this;
//     // Write out moduleObjects as JSON wrapped in a cordova module to cordova_plugins.js
//     var final_contents = 'cordova.define(\'cordova/plugin_list\', function(require, exports, module) {\n';
//     final_contents += 'module.exports = ' + JSON.stringify(this._platformJson.root.modules, null, '    ') + ';\n';
//     final_contents += 'module.exports.metadata = \n';
//     final_contents += '// TOP OF METADATA\n';

//     var pluginMetadata = Object.keys(this._platformJson.root.installed_plugins)
//     .reduce(function (metadata, plugin) {
//         metadata[plugin] = self._platformJson.root.installed_plugins[plugin].version;
//         return metadata;
//     }, {});

//     final_contents += JSON.stringify(pluginMetadata, null, '    ') + '\n';
//     final_contents += '// BOTTOM OF METADATA\n';
//     final_contents += '});'; // Close cordova.define.

//     shell.mkdir('-p', targetDir);
//     fs.writeFileSync(path.join(targetDir, 'cordova_plugins.js'), final_contents, 'utf-8');
// };

// PlatformApiPoly.prototype._getInstaller = function(type) {
//     var self = this;
//     return function (item, plugin_dir, plugin_id, options, project) {
//         var installer = self._handler[type] || common[type];

//         var wwwDest = options.usePlatformWww ?
//             self.getPlatformInfo().locations.platformWww :
//             self._handler.www_dir(self.root);

//         var installerArgs = type === 'asset' ? [wwwDest] :
//             type === 'js-module' ? [plugin_id, wwwDest]:
//             [self.root, plugin_id, options, project];

//         installer.install.apply(null, [item, plugin_dir].concat(installerArgs));
//     };
// };

// PlatformApiPoly.prototype._getUninstaller = function(type) {
//     var self = this;
//     return function (item, plugin_dir, plugin_id, options, project) {
//         var uninstaller = self._handler[type] || common[type];

//         var wwwDest = options.usePlatformWww ?
//             self.getPlatformInfo().locations.platformWww :
//             self._handler.www_dir(self.root);

//         var uninstallerArgs = (type === 'asset' || type === 'js-module') ? [wwwDest, plugin_id] :
//             [self.root, plugin_id, options, project];

//         uninstaller.uninstall.apply(null, [item].concat(uninstallerArgs));
//     };
// };

/**
 * Copies cordova.js itself and cordova-js source into installed/updated
 *   platform's `platform_www` directory.
 *
 * @param   {String}  sourceLib    Path to platform library. Required to acquire
 *   cordova-js sources.
 * @param   {PlatformInfo}  platformInfo  PlatformInfo structure, required for
 *   detecting copied files destination.
 */
// function copyCordovaSrc(sourceLib, platformInfo) {
//     // Copy the cordova.js file to platforms/<platform>/platform_www/
//     // The www dir is nuked on each prepare so we keep cordova.js in platform_www
//     shell.mkdir('-p', platformInfo.locations.platformWww);
//     shell.cp('-f', path.join(platformInfo.locations.www, 'cordova.js'),
//         path.join(platformInfo.locations.platformWww, 'cordova.js'));

//     // Copy cordova-js-src directory into platform_www directory.
//     // We need these files to build cordova.js if using browserify method.
//     var cordovaJsSrcPath = path.resolve(sourceLib, platformInfo.locations.cordovaJsSrc);

//     //only exists for platforms that have shipped cordova-js-src directory
//     if(fs.existsSync(cordovaJsSrcPath)) {
//         shell.cp('-rf', cordovaJsSrcPath, platformInfo.locations.platformWww);
//     }
// }

//TODO: add JSDoc here
// Replace the www dir with contents of platform_www and app www.
PlatformApiPoly.prototype._updateWww = function(sourceWww) {
    var locations = this.getPlatformInfo().locations;
    shell.rm('-rf', locations.www);
    shell.mkdir('-p', locations.www);
    shell.cp('-rf', path.join(sourceWww, '*'), locations.www);
    shell.cp('-rf', path.join(locations.platformWww, '*'), locations.www);
};

// TODO: JSDoc
PlatformApiPoly.prototype._updateProject = function() {
    // Update app name by editing res/values/strings.xml
    var name = this._config.name();
    var strings = xmlHelpers.parseElementtreeSync(this.locations.strings);
    strings.find('string[@name="app_name"]').text = name;
    fs.writeFileSync(this.locations.strings, strings.write({indent: 4}), 'utf-8');
    this.events.emit('verbose', 'Wrote out Android application name to "' + name + '"');

    this.handleSplashes();
    this.handleIcons();

    var manifest = xmlHelpers.parseElementtreeSync(this.locations.manifest);
    // Update the version by changing the AndroidManifest android:versionName
    var version = this._config.version();
    var versionCode = this._config.android_versionCode() || default_versionCode(version);
    manifest.getroot().attrib["android:versionName"] = version;
    manifest.getroot().attrib["android:versionCode"] = versionCode;

    // Update package name by changing the AndroidManifest id and moving the entry class around to the proper package directory
    var pkg = this._config.android_packageName() || this._config.packageName();
    pkg = pkg.replace(/-/g, '_'); // Java packages cannot support dashes
    var orig_pkg = manifest.getroot().attrib.package;
    manifest.getroot().attrib.package = pkg;

    var act = manifest.getroot().find('./application/activity');

    // Set the android:screenOrientation in the AndroidManifest
    // TODO: pick orientationHelper implementation form android_parser
    // var orientation = this.helper.getOrientation(this._config);

    // if (orientation && !this.helper.isDefaultOrientation(orientation)) {
    //     act.attrib['android:screenOrientation'] = orientation;
    // } else {
    //     delete act.attrib['android:screenOrientation'];
    // }

    // Set android:launchMode in AndroidManifest
    var androidLaunchModePref = this.findAndroidLaunchModePreference(this._config);
    if (androidLaunchModePref) {
        act.attrib["android:launchMode"] = androidLaunchModePref;
    } else { // User has (explicitly) set an invalid value for AndroidLaunchMode preference
        delete act.attrib["android:launchMode"]; // use Android default value (standard)
    }

    // Set min/max/target SDK version
    //<uses-sdk android:minSdkVersion="10" android:targetSdkVersion="19" ... />
    var usesSdk = manifest.getroot().find('./uses-sdk');
    var self = this;
    ['minSdkVersion', 'maxSdkVersion', 'targetSdkVersion'].forEach(function(sdkPrefName) {
        var sdkPrefValue = self._config.getPreference('android-' + sdkPrefName, 'android');
        if (!sdkPrefValue) return;

        if (!usesSdk) { // if there is no required uses-sdk element, we should create it first
            usesSdk = new et.Element('uses-sdk');
            manifest.getroot().append(usesSdk);
        }
        usesSdk.attrib['android:' + sdkPrefName] = sdkPrefValue;
    });

    // Write out AndroidManifest.xml
    fs.writeFileSync(this.locations.manifest, manifest.write({indent: 4}), 'utf-8');

    var orig_pkgDir = path.join(this.root, 'src', path.join.apply(null, orig_pkg.split('.')));
    var java_files = fs.readdirSync(orig_pkgDir)
    .filter(function(f) {
      return f.indexOf('.svn') == -1 && f.indexOf('.java') >= 0 &&
        fs.readFileSync(path.join(orig_pkgDir, f), 'utf-8').match(/extends\s+CordovaActivity/);
    });

    if (java_files.length === 0) {
      throw new Error('No Java files found which extend CordovaActivity.');
    } else if(java_files.length > 1) {
      events.emit('log', 'Multiple candidate Java files (.java files which extend CordovaActivity) found. Guessing at the first one, ' + java_files[0]);
    }

    var orig_java_class = java_files[0];
    var pkgDir = path.join(this.root, 'src', path.join.apply(null, pkg.split('.')));
    shell.mkdir('-p', pkgDir);
    var orig_javs = path.join(orig_pkgDir, orig_java_class);
    var new_javs = path.join(pkgDir, orig_java_class);
    var javs_contents = fs.readFileSync(orig_javs, 'utf-8');
    javs_contents = javs_contents.replace(/package [\w\.]*;/, 'package ' + pkg + ';');
    this.events.emit('verbose', 'Wrote out Android package name to "' + pkg + '"');
    fs.writeFileSync(new_javs, javs_contents, 'utf-8');
};


PlatformApiPoly.prototype.copyImage = function(src, density, name) {
    var destFolder = path.join(this.path, 'res', (density ? 'drawable-': 'drawable') + density);
    var isNinePatch = !!/\.9\.png$/.exec(src);
    var ninePatchName = name.replace(/\.png$/, '.9.png');

    // default template does not have default asset for this density
    if (!fs.existsSync(destFolder)) {
        fs.mkdirSync(destFolder);
    }

    var destFilePath = path.join(destFolder, isNinePatch ? ninePatchName : name);
    events.emit('verbose', 'copying image from ' + src + ' to ' + destFilePath);
    shell.cp('-f', src, destFilePath);
};

PlatformApiPoly.prototype.handleSplashes = function() {
    var resources = this._config.getSplashScreens('android');
    var me = this;
    // if there are "splash" elements in config.xml
    if (resources.length > 0) {
        this.deleteDefaultResource('screen.png');
        this.events.emit('verbose', 'splash screens: ' + JSON.stringify(resources));

        var projectRoot = util.isCordova(this.path);

        var hadMdpi = false;
        resources.forEach(function (resource) {
            if (!resource.density) {
                return;
            }
            if (resource.density == 'mdpi') {
                hadMdpi = true;
            }
            me.copyImage(path.join(projectRoot, resource.src), resource.density, 'screen.png');
        });
        // There's no "default" drawable, so assume default == mdpi.
        if (!hadMdpi && resources.defaultResource) {
            me.copyImage(path.join(projectRoot, resources.defaultResource.src), 'mdpi', 'screen.png');
        }
    }
};

PlatformApiPoly.prototype.handleIcons = function(config) {
    var icons = this._config.getIcons('android');

    // if there are icon elements in config.xml
    if (icons.length === 0) {
        this.events.emit('verbose', 'This app does not have launcher icons defined');
        return;
    }

    this.deleteDefaultResource('icon.png');

    var android_icons = {};
    var default_icon;
    // http://developer.android.com/design/style/iconography.html
    var sizeToDensityMap = {
        36: 'ldpi',
        48: 'mdpi',
        72: 'hdpi',
        96: 'xhdpi',
        144: 'xxhdpi',
        192: 'xxxhdpi'
    };
    // find the best matching icon for a given density or size
    // @output android_icons
    var parseIcon = function(icon, icon_size) {
        // do I have a platform icon for that density already
        var density = icon.density || sizeToDensityMap[icon_size];
        if (!density) {
            // invalid icon defition ( or unsupported size)
            return;
        }
        var previous = android_icons[density];
        if (previous && previous.platform) {
            return;
        }
        android_icons[density] = icon;
    };

    // iterate over all icon elements to find the default icon and call parseIcon
    for (var i=0; i<icons.length; i++) {
        var icon = icons[i];
        var size = icon.width;
        if (!size) {
            size = icon.height;
        }
        if (!size && !icon.density) {
            if (default_icon) {
                events.emit('verbose', 'more than one default icon: ' + JSON.stringify(icon));
            } else {
                default_icon = icon;
            }
        } else {
            parseIcon(icon, size);
        }
    }
    var projectRoot = util.isCordova(this.path);
    for (var density in android_icons) {
        this.copyImage(path.join(projectRoot, android_icons[density].src), density, 'icon.png');
    }
    // There's no "default" drawable, so assume default == mdpi.
    if (default_icon && !android_icons.mdpi) {
        this.copyImage(path.join(projectRoot, default_icon.src), 'mdpi', 'icon.png');
    }
};

// remove the default resource name from all drawable folders
// return the array of the densities in this project
PlatformApiPoly.prototype.deleteDefaultResource = function(name) {
    var res = path.join(this.path, 'res');
    var dirs = fs.readdirSync(res);

    for (var i=0; i<dirs.length; i++) {
        var filename = dirs[i];
        if (filename.indexOf('drawable-') === 0) {
            var imgPath = path.join(res, filename, name);
            if (fs.existsSync(imgPath)) {
                fs.unlinkSync(imgPath);
                events.emit('verbose', 'deleted: ' + imgPath);
            }
            imgPath = imgPath.replace(/\.png$/, '.9.png');
            if (fs.existsSync(imgPath)) {
                fs.unlinkSync(imgPath);
                events.emit('verbose', 'deleted: ' + imgPath);
            }
        }
    }
};

// Consturct the default value for versionCode as
// PATCH + MINOR * 100 + MAJOR * 10000
// see http://developer.android.com/tools/publishing/versioning.html
function default_versionCode(version) {
    var nums = version.split('-')[0].split('.');
    var versionCode = 0;
    if (+nums[0]) {
        versionCode += +nums[0] * 10000;
    }
    if (+nums[1]) {
        versionCode += +nums[1] * 100;
    }
    if (+nums[2]) {
        versionCode += +nums[2];
    }
    return versionCode;
}

PlatformApiPoly.prototype.findAndroidLaunchModePreference = function() {
    var launchMode = this._config.getPreference('AndroidLaunchMode');
    if (!launchMode) {
        // Return a default value
        return 'singleTop';
    }

    var expectedValues = ['standard', 'singleTop', 'singleTask', 'singleInstance'];
    var valid = expectedValues.indexOf(launchMode) !== -1;
    if (!valid) {
        events.emit('warn', 'Unrecognized value for AndroidLaunchMode preference: ' + launchMode);
        events.emit('warn', '  Expected values are: ' + expectedValues.join(', '));
        // Note: warn, but leave the launch mode as developer wanted, in case the list of options changes in the future
    }

    return launchMode;
};


// update the overrides folder into the www folder
// TODO: this is still rely on cordova project structure and need to
// be reworked to accept source directory to merge into www dir.
// TODO: Probably move into _updateWww?
PlatformApiPoly.prototype._updateOverrides = function() {
//     var projectRoot = util.isCordova(this.path);
//     var merges_path = path.join(util.appDir(projectRoot), 'merges', 'android');
//     if (fs.existsSync(merges_path)) {
//         var overrides = path.join(merges_path, '*');
//         shell.cp('-rf', overrides, this.www_dir());
//     }
};
