/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

var exec = require('cordova/exec');

var statusBarVisible = true;
var statusBar = {};

Object.defineProperty(statusBar, 'visible', {
    configurable: false,
    enumerable: true,
    get: function () {
        if (window.StatusBar) {
            // try to let the StatusBar plugin handle it
            return window.StatusBar.isVisible;
        }

        return statusBarVisible;
    },
    set: function (value) {
        if (window.StatusBar) {
            // try to let the StatusBar plugin handle it
            if (value) {
                window.StatusBar.show();
            } else {
                window.StatusBar.hide();
            }
        } else {
            statusBarVisible = value;
            exec(null, null, 'SystemBarPlugin', 'setStatusBarVisible', [!!value]);
        }
    }
});

Object.defineProperty(statusBar, 'setBackgroundColor', {
    configurable: false,
    enumerable: false,
    writable: false,
    value: function (value) {
        // Confirm if value is a valid hex code is before passing to native-side
        if (!(/^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/.test(value))) {
            console.error('Invalid color hex code. Valid format: #RRGGBB or #AARRGGBB');
            return;
        }

        if (window.StatusBar) {
            window.StatusBar.backgroundColorByHexString(value);
        } else {
            exec(null, null, 'SystemBarPlugin', 'setStatusBarBackgroundColor', [value]);
        }
    }
});

module.exports = statusBar;
