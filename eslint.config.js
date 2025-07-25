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

const { defineConfig, globalIgnores } = require('eslint/config');
const nodeConfig = require('@cordova/eslint-config/node');
const nodeTestConfig = require('@cordova/eslint-config/node-tests');
const browserConfig = require('@cordova/eslint-config/browser');

module.exports = defineConfig([
    globalIgnores([
        '**/coverage/',
        'spec/fixtures/',
        'templates/project/assets/www/cordova.js',
        'test/android/app',
        'test/androidx/app'
    ]),
    ...nodeConfig.map(config => ({
        files: [
            'lib/**/*.js',
            'templates/cordova/**/*.js',
            'templates/cordova/version',
            'templates/cordova/android_sdk_version',
            'templates/cordova/lib/list-devices',
            'templates/cordova/lib/list-emulator-images',
            'test/**/*.js',
            '*.js' // Root files that are JavaScript
        ],
        ...config
    })),
    ...nodeTestConfig.map(config => ({
        files: ['spec/**/*.js'],
        ...config,
        rules: {
            ...(config.rules || {}),
            'prefer-promise-reject-errors': 'off'
        }
    })),
    ...browserConfig.map(config => ({
        files: [
            'cordova-js-src/**/*.js',
            'templates/project/assets/**/*.js'
        ],
        ...config,
        languageOptions: {
            ...(config?.languageOptions || {}),
            globals: {
                ...(config.languageOptions?.globals || {}),
                require: 'readonly',
                module: 'readonly'
            }
        }
    }))
]);
