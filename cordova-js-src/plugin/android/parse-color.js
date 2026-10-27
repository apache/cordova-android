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

// This <script> element is explicitly used by Cordova's navigationbar for computing color. (Do not use this element)
const parseColorScript = document.createElement('script');
document.head.appendChild(parseColorScript);

/**
 * Parses a CSS color value and returns an array of RGB(A) components.
 * Supports valid CSS color values, e.g. `rebeccapurple`, `#RRGGBBAA`, `rgb(255 0 153)`.
 */
function parseColor (value) {
    parseColorScript.style.color = value;
    var rgbStr = window.getComputedStyle(parseColorScript).getPropertyValue('color');

    if (!rgbStr.match(/^rgb/)) {
        return;
    }

    var rgbVals = rgbStr.match(/[\d.]+/g).map(function (v, i) { return (i < 3) ? parseInt(v, 10) : parseFloat(v); });
    if (rgbVals.length < 3) {
        return;
    }

    return rgbVals;
}

module.exports = parseColor;
