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

const execa = require('execa');
const path = require('node:path');
const fs = require('node:fs');
const { forgivingWhichSync, isWindows, isDarwin } = require('./utils');
const java = require('./env/java');
const { CordovaError, ConfigParser, events } = require('cordova-common');
const android_sdk = require('./android_sdk');
const { SDK_VERSION } = require('./gradle-config-defaults');

// Re-exporting these for backwards compatibility and for unit testing.
// TODO: Remove uses and use the ./utils module directly.
Object.assign(module.exports, { isWindows, isDarwin });

/**
 * @param {string} projectRoot
 * @returns {string} The android target in format "android-${target}"
 */
module.exports.get_target = function (projectRoot) {
    const userTargetSdkVersion = getUserTargetSdkVersion(projectRoot);

    if (userTargetSdkVersion && userTargetSdkVersion < SDK_VERSION) {
        events.emit('warn', `android-targetSdkVersion should be greater than or equal to ${SDK_VERSION}.`);
    }

    return `android-${Math.max(userTargetSdkVersion, SDK_VERSION)}`;
};

/**
 * @param {string} projectRoot
 * @returns {string} The android target in format "android-${target}"
 */
module.exports.get_compile = function (projectRoot) {
    const userTargetSdkVersion = getUserTargetSdkVersion(projectRoot) || SDK_VERSION;
    const userCompileSdkVersion = getUserCompileSdkVersion(projectRoot) || userTargetSdkVersion;

    module.exports.isCompileSdkValid(userCompileSdkVersion, userTargetSdkVersion);

    return userCompileSdkVersion;
};

module.exports.isCompileSdkValid = (compileSdk, targetSdk) => {
    targetSdk = (targetSdk || SDK_VERSION);
    compileSdk = (compileSdk || targetSdk);
    const isValid = compileSdk >= targetSdk;

    if (!isValid) {
        events.emit('warn', `The "android-compileSdkVersion" (${compileSdk}) should be greater than or equal to the "android-targetSdkVersion" (${targetSdk}).`);
    }

    return isValid;
};

/**
 * @param {string} projectRoot
 * @returns {number} target sdk or 0 if undefined
 */
function getUserTargetSdkVersion (projectRoot) {
    // If the repo config.xml file exists, find the desired targetSdkVersion.
    // We need to use the cordova project's config.xml here, since the platform
    // project's config.xml does not yet have the user's preferences when this
    // function is called during `Api.createPlatform`.
    const configFile = path.join(projectRoot, '../../config.xml');
    if (!fs.existsSync(configFile)) return 0;

    const configParser = new ConfigParser(configFile);
    const targetSdkVersion = parseInt(configParser.getPreference('android-targetSdkVersion', 'android'), 10);
    return isNaN(targetSdkVersion) ? 0 : targetSdkVersion;
}

/**
 * @param {string} projectRoot
 * @returns {number} target sdk or 0 if undefined
 */
function getUserCompileSdkVersion (projectRoot) {
    // If the repo config.xml file exists, find the desired compileSdkVersion.
    // We need to use the cordova project's config.xml here, since the platform
    // project's config.xml does not yet have the user's preferences when this
    // function is called during `Api.createPlatform`.
    const configFile = path.join(projectRoot, '../../config.xml');
    if (!fs.existsSync(configFile)) return 0;

    const configParser = new ConfigParser(configFile);
    const compileSdkVersion = parseInt(configParser.getPreference('android-compileSdkVersion', 'android'), 10);
    return isNaN(compileSdkVersion) ? 0 : compileSdkVersion;
}

module.exports.get_gradle_wrapper = function () {
    let androidStudioPath;
    let i = 0;
    let foundStudio = false;
    let program_dir;
    // OK, This hack only works on Windows, not on Mac OS or Linux.  We will be deleting this eventually!
    if (module.exports.isWindows()) {
        // "spawn" option enabled for CVE-2024-27980 (Windows) Mitigation
        // See https://nodejs.org/en/blog/vulnerability/april-2024-security-releases-2 for more details
        const result = execa.sync(path.join(__dirname, 'getASPath.bat'), { shell: true });
        // console.log('result.stdout =' + result.stdout.toString());
        // console.log('result.stderr =' + result.stderr.toString());

        if (result.stderr.toString().length > 0) {
            const androidPath = path.join(process.env.ProgramFiles, 'Android') + '/';
            if (fs.existsSync(androidPath)) {
                program_dir = fs.readdirSync(androidPath);
                while (i < program_dir.length && !foundStudio) {
                    if (program_dir[i].startsWith('Android Studio')) {
                        foundStudio = true;
                        androidStudioPath = path.join(process.env.ProgramFiles, 'Android', program_dir[i], 'gradle');
                    } else { ++i; }
                }
            }
        } else {
            // console.log('got android studio path from registry');
            // remove the (os independent) new line char at the end of stdout
            // add gradle to match the above.
            androidStudioPath = path.join(result.stdout.toString().split('\r\n')[0], 'gradle');
        }
    }

    if (androidStudioPath !== null && fs.existsSync(androidStudioPath)) {
        const dirs = fs.readdirSync(androidStudioPath);
        if (dirs[0].split('-')[0] === 'gradle') {
            return path.join(androidStudioPath, dirs[0], 'bin', 'gradle');
        }
    } else {
        // OK, let's try to check for Gradle!
        return forgivingWhichSync('gradle');
    }
};

// Returns a promise. Called only by build and clean commands.
module.exports.check_gradle = function () {
    const sdkDir = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
    if (!sdkDir) {
        return Promise.reject(new CordovaError('Could not find gradle wrapper within Android SDK. Could not find Android SDK directory.\n' +
            'Might need to install Android SDK or set up \'ANDROID_HOME\' env variable.'));
    }

    const gradlePath = module.exports.get_gradle_wrapper();

    if (gradlePath.length !== 0) return Promise.resolve(gradlePath);

    return Promise.reject(new CordovaError('Could not find an installed version of Gradle either in Android Studio,\n' +
                            'or on your system to install the gradle wrapper. Please include gradle \n' +
                            'in your path, or install Android Studio'));
};

/**
 * Checks for the java installation and correct version
 *
 * Despite the name, it should return the Java version value, it's used by the Cordova CLI.
 */
module.exports.check_java = async function () {
    const javaVersion = await java.getVersion();
    return javaVersion;
};

// Returns a promise.
module.exports.check_android = function () {
    return Promise.resolve().then(function () {
        let hasAndroidHome = false;

        function maybeSetAndroidHome (value) {
            if (!hasAndroidHome && fs.existsSync(value)) {
                hasAndroidHome = true;
                process.env.ANDROID_HOME = value;
            }
        }

        const adbInPath = forgivingWhichSync('adb');
        const avdmanagerInPath = forgivingWhichSync('avdmanager');

        if (process.env.ANDROID_HOME) {
            maybeSetAndroidHome(path.resolve(process.env.ANDROID_HOME));
        }

        // First ensure ANDROID_HOME is set
        // If we have no hints (nothing in PATH), try a few default locations
        if (!hasAndroidHome && !adbInPath && !avdmanagerInPath) {
            if (process.env.ANDROID_HOME) {
                // Fallback to deprecated `ANDROID_HOME` variable
                maybeSetAndroidHome(path.join(process.env.ANDROID_HOME));
            }
            if (module.exports.isWindows()) {
                // Android Studio 1.0 installer
                if (process.env.LOCALAPPDATA) {
                    maybeSetAndroidHome(path.join(process.env.LOCALAPPDATA, 'Android', 'sdk'));
                }
                if (process.env.ProgramFiles) {
                    maybeSetAndroidHome(path.join(process.env.ProgramFiles, 'Android', 'sdk'));
                }

                // Android Studio pre-1.0 installer
                if (process.env.LOCALAPPDATA) {
                    maybeSetAndroidHome(path.join(process.env.LOCALAPPDATA, 'Android', 'android-studio', 'sdk'));
                }
                if (process.env.ProgramFiles) {
                    maybeSetAndroidHome(path.join(process.env.ProgramFiles, 'Android', 'android-studio', 'sdk'));
                }

                // Stand-alone installer
                if (process.env.LOCALAPPDATA) {
                    maybeSetAndroidHome(path.join(process.env.LOCALAPPDATA, 'Android', 'android-sdk'));
                }
                if (process.env.ProgramFiles) {
                    maybeSetAndroidHome(path.join(process.env.ProgramFiles, 'Android', 'android-sdk'));
                }
            } else if (module.exports.isDarwin()) {
                // Android Studio 1.0 installer
                if (process.env.HOME) {
                    maybeSetAndroidHome(path.join(process.env.HOME, 'Library', 'Android', 'sdk'));
                }
                // Android Studio pre-1.0 installer
                maybeSetAndroidHome('/Applications/Android Studio.app/sdk');
                // Stand-alone zip file that user might think to put under /Applications
                maybeSetAndroidHome('/Applications/android-sdk-macosx');
                maybeSetAndroidHome('/Applications/android-sdk');
            }
            if (process.env.HOME) {
                // Stand-alone zip file that user might think to put under their home directory
                maybeSetAndroidHome(path.join(process.env.HOME, 'android-sdk-macosx'));
                maybeSetAndroidHome(path.join(process.env.HOME, 'android-sdk'));
            }
        }

        if (!hasAndroidHome) {
            // If we dont have ANDROID_HOME, but we do have some tools on the PATH, try to infer from the tooling PATH.
            let parentDir, grandParentDir;
            if (adbInPath) {
                parentDir = path.dirname(adbInPath);
                grandParentDir = path.dirname(parentDir);
                if (path.basename(parentDir) === 'platform-tools') {
                    maybeSetAndroidHome(grandParentDir);
                } else {
                    throw new CordovaError('Failed to find \'ANDROID_HOME\' environment variable. Try setting it manually.\n' +
                        'Detected \'adb\' command at ' + parentDir + ' but no \'platform-tools\' directory found near.\n' +
                        'Try reinstall Android SDK or update your PATH to include valid path to SDK' + path.sep + 'platform-tools directory.');
                }
            }
            if (avdmanagerInPath) {
                parentDir = path.dirname(avdmanagerInPath);
                grandParentDir = path.dirname(parentDir);
                if (path.basename(parentDir) === 'bin' && path.basename(grandParentDir) === 'tools') {
                    maybeSetAndroidHome(path.dirname(grandParentDir));
                } else {
                    throw new CordovaError('Failed to find \'ANDROID_HOME\' environment variable. Try setting it manually.\n' +
                        'Detected \'avdmanager\' command at ' + parentDir + ' but no \'tools' + path.sep + 'bin\' directory found near.\n' +
                        'Try reinstall Android SDK or update your PATH to include valid path to SDK' + path.sep + 'tools' + path.sep + 'bin directory.');
                }
            }
        }
        if (!process.env.ANDROID_HOME) {
            throw new CordovaError('Failed to find \'ANDROID_HOME\' environment variable. Try setting it manually.\n' +
                'Failed to find \'android\' command in your \'PATH\'. Try update your \'PATH\' to include path to valid SDK directory.');
        }
        if (!fs.existsSync(process.env.ANDROID_HOME)) {
            throw new CordovaError('\'ANDROID_HOME\' environment variable is set to non-existent path: ' + process.env.ANDROID_HOME +
                '\nTry update it manually to point to valid SDK directory.');
        }
        // Next let's make sure relevant parts of the SDK tooling is in our PATH
        if (hasAndroidHome && !adbInPath) {
            process.env.PATH += path.delimiter + path.join(process.env.ANDROID_HOME, 'platform-tools');
        }
        if (hasAndroidHome && !avdmanagerInPath) {
            process.env.PATH += path.delimiter + path.join(process.env.ANDROID_HOME, 'tools', 'bin');
        }
        return hasAndroidHome;
    });
};

module.exports.check_android_target = function (projectRoot) {
    // valid_target can look like:
    //   android-19
    //   android-L
    //   Google Inc.:Google APIs:20
    //   Google Inc.:Glass Development Kit Preview:20
    const desired_api_level = module.exports.get_target(projectRoot);
    return android_sdk.list_targets().then(function (targets) {
        if (targets.indexOf(desired_api_level) >= 0) {
            return targets;
        }
        throw new CordovaError(`Please install the Android SDK Platform "platforms;${desired_api_level}"`);
    });
};

// Returns a promise.
module.exports.run = function () {
    console.log('Checking Java JDK and Android SDK versions');
    console.log('ANDROID_HOME=' + process.env.ANDROID_HOME + ' (recommended setting)');
    console.log('ANDROID_SDK_ROOT=' + process.env.ANDROID_SDK_ROOT + ' (DEPRECATED)');

    return Promise.all([this.check_java(), this.check_android()]).then(function (values) {
        console.log('Using Android SDK: ' + (process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT));

        if (!values[1]) {
            throw new CordovaError('Requirements check failed for Android SDK! Android SDK was not detected.');
        }
    });
};

/**
 * Object thar represents one of requirements for current platform.
 * @param {String} id         The unique identifier for this requirements.
 * @param {String} name       The name of requirements. Human-readable field.
 * @param {String} version    The version of requirement installed. In some cases could be an array of strings
 *                            (for example, check_android_target returns an array of android targets installed)
 * @param {Boolean} installed Indicates whether the requirement is installed or not
 */
const Requirement = function (id, name, version, installed) {
    this.id = id;
    this.name = name;
    this.installed = installed || false;
    this.metadata = {
        version
    };
};

/**
 * Methods that runs all checks one by one and returns a result of checks
 * as an array of Requirement objects. This method intended to be used by cordova-lib check_reqs method
 *
 * @param {string} projectRoot
 * @return Promise<Requirement[]> Array of requirements. Due to implementation, promise is always fulfilled.
 */
module.exports.check_all = function (projectRoot) {
    const requirements = [
        new Requirement('java', 'Java JDK'),
        new Requirement('androidSdk', 'Android SDK'),
        new Requirement('androidTarget', 'Android target'),
        new Requirement('gradle', 'Gradle')
    ];

    const checkFns = [
        this.check_java,
        this.check_android,
        this.check_android_target.bind(this, projectRoot),
        this.check_gradle
    ];

    // Then execute requirement checks one-by-one
    return checkFns.reduce(function (promise, checkFn, idx) {
        // Update each requirement with results
        const requirement = requirements[idx];
        return promise.then(checkFn).then(function (version) {
            requirement.installed = true;
            requirement.metadata.version = version;
        }, function (err) {
            requirement.metadata.reason = err instanceof Error ? err.message : err;
        });
    }, Promise.resolve()).then(function () {
        // When chain is completed, return requirements array to upstream API
        return requirements;
    });
};
