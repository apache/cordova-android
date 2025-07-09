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
        var script = document.querySelector('script[src$="cordova.js"]');
        script.style.color = value;
        var rgbStr = window.getComputedStyle(script).getPropertyValue('color');

        if (!rgbStr.match(/^rgb/)) { return; }

        var rgbVals = rgbStr.match(/\d+/g).map(function (v) { return parseInt(v, 10); });

        if (rgbVals.length < 3) {
            return;
        } else if (rgbVals.length === 3) {
            rgbVals = [255].concat(rgbVals);
        }

        const padRgb = (val) => val.toString(16).padStart(2, '0');
        const a = padRgb(rgbVals[0]);
        const r = padRgb(rgbVals[1]);
        const g = padRgb(rgbVals[2]);
        const b = padRgb(rgbVals[3]);
        const hexStr = '#' + a + r + g + b;

        if (window.StatusBar) {
            window.StatusBar.backgroundColorByHexString(hexStr);
        } else {
            exec(null, null, 'SystemBarPlugin', 'setStatusBarBackgroundColor', rgbVals);
        }
    }
});

module.exports = statusBar;
