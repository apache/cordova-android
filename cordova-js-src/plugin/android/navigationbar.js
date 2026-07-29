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

var exec = require('cordova/exec');

var navigationBar = {};

// This <script> element is explicitly used by Cordova's navigationbar for computing color. (Do not use this element)
const navigationBarScript = document.createElement('script');
document.head.appendChild(navigationBarScript);

/**
 * Sets the background color of the navigation bar, this will only work on old Android devices or if gesture navigation is disabled (3 button navigation).
 * Supports valid CSS color values, e.g. `rebeccapurple`, `#RRGGBBAA`, `rgb(255 0 153)`.
 */
Object.defineProperty(navigationBar, 'setBackgroundColor', {
    configurable: false,
    enumerable: false,
    writable: false,
    value: function (value) {
        navigationBarScript.style.color = value;
        var rgbStr = window.getComputedStyle(navigationBarScript).getPropertyValue('color');

        if (!rgbStr.match(/^rgb/)) {
            return;
        }

        var rgbVals = rgbStr.match(/[\d.]+/g).map(function (v, i) { return (i < 3) ? parseInt(v, 10) : parseFloat(v); });
        if (rgbVals.length < 3) {
            return;
        }

        exec(null, null, 'SystemBarPlugin', 'setNavigationBarBackgroundColor', rgbVals);
    }
});

module.exports = navigationBar;