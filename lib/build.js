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

const path = require('node:path');
const fs = require('fs');
const nopt = require('nopt');
const untildify = require('untildify');
const { parseArgsStringToArgv } = require('string-argv');

const Adb = require('./Adb');

const events = require('cordova-common').events;
const PackageType = require('./PackageType');

module.exports.parseBuildOptions = parseOpts;
function parseOpts (options, resolvedTarget, projectRoot) {
    options = options || {};
    options.argv = nopt({
        prepenv: Boolean,
        versionCode: String,
        minSdkVersion: String,
        maxSdkVersion: String,
        targetSdkVersion: String,

        // This needs to be an array since nopts will parse its entries as further options for this process
        // It will be an array of 1 string: [ "string args" ]
        gradleArg: [String, Array],

        keystore: path,
        alias: String,
        storePassword: String,
        password: String,
        keystoreType: String,
        packageType: String
    }, {}, options.argv, 0);

    // Android Studio Build method is the default
    const ret = {
        buildType: options.release ? 'release' : 'debug',
        prepEnv: options.argv.prepenv,
        arch: resolvedTarget && resolvedTarget.arch,
        extraArgs: []
    };

    if (options.argv.versionCode) { ret.extraArgs.push('-PcdvVersionCode=' + options.argv.versionCode); }
    if (options.argv.minSdkVersion) { ret.extraArgs.push('-PcdvMinSdkVersion=' + options.argv.minSdkVersion); }
    if (options.argv.maxSdkVersion) { ret.extraArgs.push('-PcdvMaxSdkVersion=' + options.argv.maxSdkVersion); }
    if (options.argv.targetSdkVersion) { ret.extraArgs.push('-PcdvTargetSdkVersion=' + options.argv.targetSdkVersion); }
    if (options.argv.gradleArg) {
        const gradleArgs = parseArgsStringToArgv(options.argv.gradleArg[0]);
        ret.extraArgs = ret.extraArgs.concat(gradleArgs);
    }

    const packageArgs = {};

    if (options.argv.keystore) { packageArgs.keystore = path.relative(projectRoot, path.resolve(options.argv.keystore)); }

    ['alias', 'storePassword', 'password', 'keystoreType', 'packageType'].forEach(function (flagName) {
        if (options.argv[flagName]) { packageArgs[flagName] = options.argv[flagName]; }
    });

    const buildConfig = options.buildConfig;

    // If some values are not specified as command line arguments - use build config to supplement them.
    // Command line arguments have precedence over build config.
    if (buildConfig) {
        if (!fs.existsSync(buildConfig)) {
            throw new Error('Specified build config file does not exist: ' + buildConfig);
        }
        events.emit('log', 'Reading build config file: ' + path.resolve(buildConfig));
        const buildjson = fs.readFileSync(buildConfig, 'utf8');
        const config = JSON.parse(buildjson.replace(/^\ufeff/, '')); // Remove BOM
        if (config.android && config.android[ret.buildType]) {
            const androidInfo = config.android[ret.buildType];
            if (androidInfo.keystore && !packageArgs.keystore) {
                androidInfo.keystore = untildify(androidInfo.keystore);
                packageArgs.keystore = path.resolve(path.dirname(buildConfig), androidInfo.keystore);
                events.emit('log', 'Reading the keystore from: ' + packageArgs.keystore);
            }

            ['alias', 'storePassword', 'password', 'keystoreType', 'packageType'].forEach(function (key) {
                packageArgs[key] = packageArgs[key] || androidInfo[key];
            });
        }
    }

    if (packageArgs.keystore && packageArgs.alias) {
        ret.packageInfo = new PackageInfo(packageArgs.keystore, packageArgs.alias, packageArgs.storePassword,
            packageArgs.password, packageArgs.keystoreType);
    }

    if (!ret.packageInfo) {
        // The following loop is to decide whether to print a warning about generating a signed archive
        // We only want to produce a warning if they are using a config property that is related to signing, but
        // missing the required properties for signing. We don't want to produce a warning if they are simply
        // using a build property that isn't related to signing, such as --packageType
        let shouldWarn = false;
        const signingKeys = ['keystore', 'alias', 'storePassword', 'password', 'keystoreType'];

        for (const key in packageArgs) {
            if (!shouldWarn && signingKeys.indexOf(key) > -1) {
                // If we enter this condition, we have a key used for signing a build,
                // but we are missing some required signing properties
                shouldWarn = true;
            }
        }

        if (shouldWarn) {
            events.emit('warn', '\'keystore\' and \'alias\' need to be specified to generate a signed archive.');
        }
    }

    if (packageArgs.packageType) {
        const VALID_PACKAGE_TYPES = [PackageType.APK, PackageType.BUNDLE];
        if (VALID_PACKAGE_TYPES.indexOf(packageArgs.packageType) === -1) {
            events.emit('warn', '"' + packageArgs.packageType + '" is an invalid packageType. Valid values are: ' + VALID_PACKAGE_TYPES.join(', ') + '\nDefaulting packageType to ' + PackageType.APK);
            ret.packageType = PackageType.APK;
        } else {
            ret.packageType = packageArgs.packageType;
        }
    } else {
        if (ret.buildType === 'release') {
            ret.packageType = PackageType.BUNDLE;
        } else {
            ret.packageType = PackageType.APK;
        }
    }

    return ret;
}

/*
 * Builds the project with the specifed options
 * Returns a promise.
 */
module.exports.runClean = function (options) {
    const opts = parseOpts(options, null, this.root);
    const builder = this._builder;

    return builder.prepEnv(opts).then(function () {
        return builder.clean(opts);
    });
};

/**
 * Builds the project with the specifed options.
 *
 * @param   {BuildOptions}  options      A set of options. See PlatformApi.build
 *   method documentation for reference.
 * @param   {Object}  optResolvedTarget  A deployment target. Used to pass
 *   target architecture from upstream 'run' call. TODO: remove this option in
 *   favor of setting buildOptions.archs field.
 *
 * @return  {Promise<Object>}            Promise, resolved with built packages
 *   information.
 */
module.exports.run = function (options, optResolvedTarget) {
    const opts = parseOpts(options, optResolvedTarget, this.root);
    const builder = this._builder;

    return builder.prepEnv(opts).then(function () {
        if (opts.prepEnv) {
            events.emit('verbose', 'Build file successfully prepared.');
            return;
        }
        return builder.build(opts).then(function () {
            let paths;
            if (opts.packageType === PackageType.BUNDLE) {
                paths = builder.findOutputBundles(opts.buildType);
                events.emit('log', 'Built the following bundle(s): \n\t' + paths.join('\n\t'));
            } else {
                paths = builder.findOutputApks(opts.buildType, opts.arch);
                events.emit('log', 'Built the following apk(s): \n\t' + paths.join('\n\t'));
            }

            return {
                paths,
                buildType: opts.buildType
            };
        });
    });
};

/*
 * Detects the architecture of a device/emulator
 * Returns "arm" or "x86".
 */
module.exports.detectArchitecture = function (target) {
    return Adb.shell(target, 'cat /proc/cpuinfo').then(function (output) {
        return /intel/i.exec(output) ? 'x86' : 'arm';
    });
};

module.exports.findBestApkForArchitecture = function (buildResults, arch) {
    const paths = buildResults.apkPaths.filter(function (p) {
        const apkName = path.basename(p);
        if (buildResults.buildType === 'debug') {
            return /-debug/.exec(apkName);
        }
        return !/-debug/.exec(apkName);
    });
    const archPattern = new RegExp('-' + arch);
    const hasArchPattern = /-x86|-arm/;
    for (let i = 0; i < paths.length; ++i) {
        const apkName = path.basename(paths[i]);
        if (hasArchPattern.exec(apkName)) {
            if (archPattern.exec(apkName)) {
                return paths[i];
            }
        } else {
            return paths[i];
        }
    }
    throw new Error('Could not find apk architecture: ' + arch + ' build-type: ' + buildResults.buildType);
};

function PackageInfo (keystore, alias, storePassword, password, keystoreType) {
    const createNameKeyObject = (name, value) => ({ name, value: value.replace(/\\/g, '\\\\') });

    this.data = [
        createNameKeyObject('key.store', keystore),
        createNameKeyObject('key.alias', alias)
    ];

    if (storePassword) this.data.push(createNameKeyObject('key.store.password', storePassword));
    if (password) this.data.push(createNameKeyObject('key.alias.password', password));
    if (keystoreType) this.data.push(createNameKeyObject('key.store.type', keystoreType));
}

PackageInfo.prototype = {
    appendToProperties: function (propertiesParser) {
        for (const { name, value } of this.data) propertiesParser.set(name, value);

        propertiesParser.save();
    }
};
