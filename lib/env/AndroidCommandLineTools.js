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

const { events } = require('cordova-common');
const fs = require('node:fs');
const path = require('node:path');
const semver = require('semver');

/**
 * Utility collection for resolving the Android SDK command line tools installed
 * on the workstation.
 */
const AndroidCommandLineTools = {
    /**
     * Gets a sorted list of available versions found on the system.
     *
     * If the command line tools is not resolvable, then an empty array will be returned.
     *
     * This function depends on ANDROID_HOME environment variable.
     *
     * @returns {String[]}
     */
    getAvailableVersions: () => {
        const androidHome = path.resolve(AndroidCommandLineTools.__getAndroidHome());

        if (!fs.existsSync(androidHome)) {
            events.emit('warn', 'ANDROID_HOME is not resolvable.');
            return [];
        }

        const cmdLineToolsContainer = path.join(androidHome, 'cmdline-tools');
        if (!fs.existsSync(cmdLineToolsContainer)) {
            events.emit('warn', 'Android SDK is missing cmdline-tools directory.');
            return [];
        }

        const cmdLineVersions = fs.readdirSync(cmdLineToolsContainer)
            .filter((value) => {
                // expected directory paths are semver-like version strings or literally "latest"
                return value === 'latest' || semver.coerce(value) !== null;
            })
            .sort((a, b) => {
                // "latest" directory always comes first
                if (a === 'latest') return -1;
                if (b === 'latest') return 1;

                const av = semver.coerce(a, {
                    includePrerelease: true
                });
                const bv = semver.coerce(b, {
                    includePrerelease: true
                });

                // Descending (highest version first)
                return semver.rcompare(av, bv);
            });

        return cmdLineVersions;
    },

    /**
     * Gets the bin path of the cmd line tools using the latest available that
     * is installed on the workstation.
     *
     * Returns null if there are no versions fond
     *
     * @returns {String | null}
     */
    getBinPath: () => {
        const versions = AndroidCommandLineTools.getAvailableVersions();

        if (versions.length === 0) {
            return null;
        }

        const version = versions[0];
        return path.resolve(AndroidCommandLineTools.__getAndroidHome(), 'cmdline-tools', version, 'bin');
    },

    /**
     * @internal
     */
    __getAndroidHome: () => {
        return process.env.ANDROID_HOME;
    }
};

module.exports = AndroidCommandLineTools;
