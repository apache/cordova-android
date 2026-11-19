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

const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const STATUSBAR_SRC = path.join(__dirname, '../../../cordova-js-src/plugin/android/statusbar.js');

/**
 * Loads statusbar.js in a minimal browser-like context.
 * `computedColor` is what getComputedStyle reports for the color that was set.
 */
function loadStatusBar (computedColor, StatusBar) {
    const exec = jasmine.createSpy('exec');
    const window = {
        StatusBar,
        getComputedStyle: () => ({ getPropertyValue: () => computedColor })
    };
    const document = {
        head: { appendChild: () => {} },
        createElement: () => ({ style: {} })
    };
    const module = { exports: {} };
    const require = (id) => {
        if (id === 'cordova/exec') return exec;
        throw new Error(`Unexpected require: ${id}`);
    };

    vm.runInNewContext(fs.readFileSync(STATUSBAR_SRC, 'utf8'), { window, document, module, require });
    return { statusBar: module.exports, exec };
}

describe('cordova-js-src/plugin/android/statusbar', () => {
    describe('setBackgroundColor with cordova-plugin-statusbar', () => {
        let StatusBar;

        beforeEach(() => {
            StatusBar = { backgroundColorByHexString: jasmine.createSpy('backgroundColorByHexString') };
        });

        it('forwards an opaque color as #AARRGGBB', () => {
            const { statusBar } = loadStatusBar('rgb(255, 0, 153)', StatusBar);
            statusBar.setBackgroundColor('rgb(255, 0, 153)');
            expect(StatusBar.backgroundColorByHexString).toHaveBeenCalledWith('#ffff0099');
        });

        it('rounds a fractional alpha to a whole byte', () => {
            const { statusBar } = loadStatusBar('rgba(255, 0, 153, 0.5)', StatusBar);
            statusBar.setBackgroundColor('rgba(255, 0, 153, 0.5)');
            expect(StatusBar.backgroundColorByHexString).toHaveBeenCalledWith('#80ff0099');
        });

        it('pads a small rounded alpha to two digits', () => {
            const { statusBar } = loadStatusBar('rgba(255, 0, 153, 0.02)', StatusBar);
            statusBar.setBackgroundColor('rgba(255, 0, 153, 0.02)');
            expect(StatusBar.backgroundColorByHexString).toHaveBeenCalledWith('#05ff0099');
        });
    });
});
