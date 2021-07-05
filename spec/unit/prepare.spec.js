/**
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    'License'); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

var rewire = require('rewire');
var path = require('path');
var CordovaError = require('cordova-common').CordovaError;
const GradlePropertiesParser = require('../../bin/templates/cordova/lib/config/GradlePropertiesParser');

const PATH_RESOURCE = path.join('platforms', 'android', 'app', 'src', 'main', 'res');

/**
 * Creates blank resource map object, used for testing.
 *
 * @param {String} target specific resource item
 */
function createResourceMap (target) {
    const resources = {};

    [
        'mipmap-ldpi',
        'mipmap-mdpi',
        'mipmap-hdpi',
        'mipmap-xhdpi',
        'mipmap-xxhdpi',
        'mipmap-xxxhdpi',
        'mipmap-ldpi-v26',
        'mipmap-mdpi-v26',
        'mipmap-hdpi-v26',
        'mipmap-xhdpi-v26',
        'mipmap-xxhdpi-v26',
        'mipmap-xxxhdpi-v26'
    ].forEach((mipmap) => {
        if (!target || target === 'ic_launcher.png') resources[path.join(PATH_RESOURCE, mipmap, 'ic_launcher.png')] = null;
        if (!target || target === 'ic_launcher_foreground.png') resources[path.join(PATH_RESOURCE, mipmap, 'ic_launcher_foreground.png')] = null;
        if (!target || target === 'ic_launcher_background.png') resources[path.join(PATH_RESOURCE, mipmap, 'ic_launcher_background.png')] = null;
        if (!target || target === 'ic_launcher_foreground.xml') resources[path.join(PATH_RESOURCE, mipmap, 'ic_launcher_foreground.xml')] = null;
        if (!target || target === 'ic_launcher_background.xml') resources[path.join(PATH_RESOURCE, mipmap, 'ic_launcher_background.xml')] = null;

        if (
            !mipmap.includes('-v26') &&
            (!target || target === 'ic_launcher.xml')
        ) {
            resources[path.join(PATH_RESOURCE, mipmap, 'ic_launcher.xml')] = null;
        }
    });

    return resources;
}

/**
 * Create a mock item from the getIcon collection with the supplied updated data.
 *
 * @param {Object} data Changes to apply to the mock getIcon item
 */
function mockGetIconItem (data) {
    return Object.assign({}, {
        src: undefined,
        target: undefined,
        density: undefined,
        platform: 'android',
        width: undefined,
        height: undefined,
        background: undefined,
        foreground: undefined
    }, data);
}

/**
 * Create a mock item from the getSplashScreen collection with the supplied updated data.
 *
 * @param {Object} data Changes to apply to the mock getSplashScreen item
 */
function mockGetSplashScreenItem (data) {
    return Object.assign({}, {
        src: undefined,
        density: undefined
    }, data);
}

describe('prepare', () => {
    describe('updateIcons method', function () {
    // Rewire
        let prepare;

        // Spies
        let updateIconResourceForAdaptiveSpy;
        let updateIconResourceForLegacySpy;
        let emitSpy;
        let updatePathsSpy;

        // Mock Data
        let cordovaProject;
        let platformResourcesDir;

        beforeEach(function () {
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            cordovaProject = {
                root: '/mock',
                projectConfig: {
                    path: '/mock/config.xml',
                    cdvNamespacePrefix: 'cdv'
                },
                locations: {
                    plugins: '/mock/plugins',
                    www: '/mock/www'
                }
            };
            platformResourcesDir = PATH_RESOURCE;

            emitSpy = jasmine.createSpy('emit');
            prepare.__set__('events', {
                emit: emitSpy
            });

            updatePathsSpy = jasmine.createSpy('updatePaths');
            prepare.__set__('FileUpdater', {
                updatePaths: updatePathsSpy
            });

            // mocking initial responses for mapImageResources
            prepare.__set__('mapImageResources', function (rootDir, subDir, type, resourceName) {
                if (resourceName.includes('ic_launcher.png')) {
                    return createResourceMap('ic_launcher.png');
                } else if (resourceName.includes('ic_launcher_foreground.png')) {
                    return createResourceMap('ic_launcher_foreground.png');
                } else if (resourceName.includes('ic_launcher_background.png')) {
                    return createResourceMap('ic_launcher_background.png');
                } else if (resourceName.includes('ic_launcher_foreground.xml')) {
                    return createResourceMap('ic_launcher_foreground.xml');
                } else if (resourceName.includes('ic_launcher_background.xml')) {
                    return createResourceMap('ic_launcher_background.xml');
                } else if (resourceName.includes('ic_launcher.xml')) {
                    return createResourceMap('ic_launcher.xml');
                }
            });
        });

        it('Test#001 : Should detect no defined icons.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [];
            };

            updateIcons(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'This app does not have launcher icons defined';
            expect(actual).toEqual(expected);
        });

        it('Test#002 : Should detech incorrect configrations for adaptive icon and throws error.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    background: 'res/icon/android/mdpi-background.png'
                })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: mdpi. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#003 : Should detech incorrect configrations (missing foreground) for adaptive icon and throw an error.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    background: 'res/icon/android/mdpi-background.png'
                })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: mdpi. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#004 : Should detech incorrect configrations (missing background) for adaptive icon and throw an error.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    foreground: 'res/icon/android/mdpi-foreground.png'
                })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: mdpi. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#005 : Should detech incorrect configrations and throw an error.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({ density: 'mdpi' })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: mdpi. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#006 : Should display incorrect configuration with density in message.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({ density: 'mdpi' })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: mdpi. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#007 : Should display incorrect configuration with size in message from height.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({ height: '192' })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: size=192. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#008 : Should display incorrect configuration with size in message from width.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({ width: '192' })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: size=192. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#009 : Should detech incorrect configrations (missing background) for adaptive icon and throw an error.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    foreground: 'res/icon/android/mdpi-foreground.png'
                })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('One of the following attributes are set but missing the other for the density type: mdpi. Please ensure that all require attributes are defined.')
            );
        });

        it('Test#010 : Should detech adaptive icon with vector foreground and throws error for missing backwards compatability settings.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    background: 'res/icon/android/mdpi-background.png',
                    foreground: 'res/icon/android/mdpi-foreground.xml'
                })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('For the following icons with the density of: mdpi, adaptive foreground with a defined color or vector can not be used as a standard fallback icon for older Android devices. To support older Android environments, please provide a value for the src attribute.')
            );
        });

        it('Test#011 : Should detech adaptive icon with color foreground and throws error for missing backwards compatability settings.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    background: 'res/icon/android/mdpi-background.png',
                    foreground: '@color/background'
                })];
            };

            expect(function () {
                updateIcons(cordovaProject, platformResourcesDir);
            }).toThrow(
                new CordovaError('For the following icons with the density of: mdpi, adaptive foreground with a defined color or vector can not be used as a standard fallback icon for older Android devices. To support older Android environments, please provide a value for the src attribute.')
            );
        });

        it('Test#012 : Should update paths with adaptive and standard icons. Standard icon comes from adaptive foreground', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    background: 'res/icon/android/mdpi-background.png',
                    foreground: 'res/icon/android/mdpi-foreground.png'
                })];
            };

            // Creating Spies
            const resourceMap = createResourceMap();
            const phaseOneModification = {};
            phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_foreground.png')] = 'res/icon/android/mdpi-foreground.png';
            phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
            const phaseOneUpdatedIconsForAdaptive = Object.assign({}, resourceMap, phaseOneModification);

            updateIconResourceForAdaptiveSpy = jasmine.createSpy('updateIconResourceForAdaptiveSpy');
            prepare.__set__('updateIconResourceForAdaptive', function (preparedIcons, resourceMap, platformResourcesDir) {
                updateIconResourceForAdaptiveSpy();
                return phaseOneUpdatedIconsForAdaptive;
            });

            const phaseTwoModification = {};
            phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-foreground.png';
            phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
            const phaseTwoUpdatedIconsForLegacy = Object.assign({}, phaseOneUpdatedIconsForAdaptive, phaseTwoModification);

            updateIconResourceForLegacySpy = jasmine.createSpy('updateIconResourceForLegacySpy');
            prepare.__set__('updateIconResourceForLegacy', function (preparedIcons, resourceMap, platformResourcesDir) {
                updateIconResourceForLegacySpy();
                return phaseTwoUpdatedIconsForLegacy;
            });

            updateIcons(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'Updating icons at ' + PATH_RESOURCE;
            expect(actual).toEqual(expected);

            // Expected to be called.
            expect(updatePathsSpy).toHaveBeenCalled();
            expect(updateIconResourceForAdaptiveSpy).toHaveBeenCalled();
            expect(updateIconResourceForLegacySpy).toHaveBeenCalled();

            const actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
            const expectedResourceMap = phaseTwoUpdatedIconsForLegacy;
            expect(actualResourceMap).toEqual(expectedResourceMap);
        });

        it('Test#013 : Should update paths with adaptive and standard icons.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    src: 'res/icon/android/mdpi-icon.png',
                    background: 'res/icon/android/mdpi-background.png',
                    foreground: 'res/icon/android/mdpi-foreground.png'
                })];
            };

            // Creating Spies
            const resourceMap = createResourceMap();
            const phaseOneModification = {};
            phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_foreground.png')] = 'res/icon/android/mdpi-foreground.png';
            phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
            const phaseOneUpdatedIconsForAdaptive = Object.assign({}, resourceMap, phaseOneModification);

            updateIconResourceForAdaptiveSpy = jasmine.createSpy('updateIconResourceForAdaptiveSpy');
            prepare.__set__('updateIconResourceForAdaptive', function (preparedIcons, resourceMap, platformResourcesDir) {
                updateIconResourceForAdaptiveSpy();
                return phaseOneUpdatedIconsForAdaptive;
            });

            const phaseTwoModification = {};
            phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-foreground.png';
            phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
            const phaseTwoUpdatedIconsForLegacy = Object.assign({}, phaseOneUpdatedIconsForAdaptive, phaseTwoModification);

            updateIconResourceForLegacySpy = jasmine.createSpy('updateIconResourceForLegacySpy');
            prepare.__set__('updateIconResourceForLegacy', function (preparedIcons, resourceMap, platformResourcesDir) {
                updateIconResourceForLegacySpy();
                return phaseTwoUpdatedIconsForLegacy;
            });

            updateIcons(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'Updating icons at ' + PATH_RESOURCE;
            expect(actual).toEqual(expected);

            // Expected to be called.
            expect(updatePathsSpy).toHaveBeenCalled();
            expect(updateIconResourceForAdaptiveSpy).toHaveBeenCalled();
            expect(updateIconResourceForLegacySpy).toHaveBeenCalled();

            const actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
            const expectedResourceMap = phaseTwoUpdatedIconsForLegacy;
            expect(actualResourceMap).toEqual(expectedResourceMap);
        });

        it('Test#014 : Should update paths with standard icons.', function () {
            const updateIcons = prepare.__get__('updateIcons');

            // mock data.
            cordovaProject.projectConfig.getIcons = function () {
                return [mockGetIconItem({
                    density: 'mdpi',
                    src: 'res/icon/android/mdpi-icon.png'
                })];
            };

            // Creating Spies
            const phaseOneUpdatedIconsForAdaptive = createResourceMap();

            updateIconResourceForAdaptiveSpy = jasmine.createSpy('updateIconResourceForAdaptiveSpy');
            prepare.__set__('updateIconResourceForAdaptive', function (preparedIcons, resourceMap, platformResourcesDir) {
                updateIconResourceForAdaptiveSpy();
                return phaseOneUpdatedIconsForAdaptive;
            });

            const phaseTwoModification = {};
            phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-icon.png';
            const phaseTwoUpdatedIconsForLegacy = Object.assign({}, phaseOneUpdatedIconsForAdaptive, phaseTwoModification);

            updateIconResourceForLegacySpy = jasmine.createSpy('updateIconResourceForLegacySpy');
            prepare.__set__('updateIconResourceForLegacy', function (preparedIcons, resourceMap, platformResourcesDir) {
                updateIconResourceForLegacySpy();
                return phaseTwoUpdatedIconsForLegacy;
            });

            updateIcons(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'Updating icons at ' + PATH_RESOURCE;
            expect(actual).toEqual(expected);

            // Expected to be called.
            expect(updatePathsSpy).toHaveBeenCalled();
            expect(updateIconResourceForAdaptiveSpy).not.toHaveBeenCalled();
            expect(updateIconResourceForLegacySpy).toHaveBeenCalled();

            const actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
            const expectedResourceMap = phaseTwoUpdatedIconsForLegacy;
            expect(actualResourceMap).toEqual(expectedResourceMap);
        });
    });

    describe('prepareIcons method', function () {
        let prepare;
        let emitSpy;
        let prepareIcons;

        beforeEach(function () {
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            prepareIcons = prepare.__get__('prepareIcons');

            // Creating Spies
            emitSpy = jasmine.createSpy('emit');
            prepare.__set__('events', {
                emit: emitSpy
            });
        });

        it('Test#001 : should emit extra default icon found for adaptive use case.', function () {
        // mock data.
            const ldpi = mockGetIconItem({
                density: 'ldpi',
                background: 'res/icon/android/ldpi-background.png',
                foreground: 'res/icon/android/ldpi-foreground.png'
            });

            const mdpi = mockGetIconItem({
                density: 'mdpi',
                background: 'res/icon/android/mdpi-background.png',
                foreground: 'res/icon/android/mdpi-foreground.png'
            });

            const icons = [ldpi, mdpi];
            const actual = prepareIcons(icons);
            const expected = {
                android_icons: { ldpi, mdpi },
                default_icon: undefined
            };

            expect(expected).toEqual(actual);
        });

        it('Test#002 : should emit extra default icon found for legacy use case.', function () {
        // mock data.
            const ldpi = mockGetIconItem({
                src: 'res/icon/android/ldpi-icon.png',
                density: 'ldpi'
            });

            const mdpi = mockGetIconItem({
                src: 'res/icon/android/mdpi-icon.png',
                density: 'mdpi'
            });

            const icons = [ldpi, mdpi];
            const actual = prepareIcons(icons);
            const expected = {
                android_icons: { ldpi, mdpi },
                default_icon: undefined
            };

            expect(expected).toEqual(actual);
        });
    });

    describe('updateIconResourceForLegacy method', function () {
        let prepare;

        // Spies
        let fsWriteFileSyncSpy;

        // Mock Data
        let platformResourcesDir;
        let preparedIcons;
        let resourceMap;

        beforeEach(function () {
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            // Mocked Data
            platformResourcesDir = PATH_RESOURCE;
            preparedIcons = {
                android_icons: {
                    mdpi: mockGetIconItem({
                        src: 'res/icon/android/mdpi-icon.png',
                        density: 'mdpi'
                    })
                },
                default_icon: undefined
            };

            resourceMap = createResourceMap();

            fsWriteFileSyncSpy = jasmine.createSpy('writeFileSync');
            prepare.__set__('fs', {
                writeFileSync: fsWriteFileSyncSpy
            });
        });

        it('Test#001 : Should update resource map with prepared icons.', function () {
        // Get method for testing
            const updateIconResourceForLegacy = prepare.__get__('updateIconResourceForLegacy');

            // Run Test
            const expectedModification = {};
            expectedModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-icon.png';
            const expected = Object.assign({}, resourceMap, expectedModification);
            const actual = updateIconResourceForLegacy(preparedIcons, resourceMap, platformResourcesDir);

            expect(actual).toEqual(expected);
        });
    });

    describe('updateIconResourceForAdaptive method', function () {
        let prepare;

        // Spies
        let fsWriteFileSyncSpy;

        // Mock Data
        let platformResourcesDir;
        let preparedIcons;
        let resourceMap;

        beforeEach(function () {
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            // Mocked Data
            platformResourcesDir = PATH_RESOURCE;
            preparedIcons = {
                android_icons: {
                    mdpi: mockGetIconItem({
                        density: 'mdpi',
                        background: 'res/icon/android/mdpi-background.png',
                        foreground: 'res/icon/android/mdpi-foreground.png'
                    })
                },
                default_icon: undefined
            };

            resourceMap = createResourceMap();

            fsWriteFileSyncSpy = jasmine.createSpy('writeFileSync');
            prepare.__set__('fs', {
                writeFileSync: fsWriteFileSyncSpy
            });
        });

        it('Test#001 : Should update resource map with prepared icons.', function () {
        // Get method for testing
            const updateIconResourceForAdaptive = prepare.__get__('updateIconResourceForAdaptive');

            // Run Test
            const expectedModification = {};
            expectedModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
            expectedModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_foreground.png')] = 'res/icon/android/mdpi-foreground.png';

            const expected = Object.assign({}, resourceMap, expectedModification);
            const actual = updateIconResourceForAdaptive(preparedIcons, resourceMap, platformResourcesDir);

            expect(actual).toEqual(expected);
        });
    });

    describe('cleanIcons method', function () {
        let prepare;
        let emitSpy;
        let updatePathsSpy;

        beforeEach(function () {
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            emitSpy = jasmine.createSpy('emit');
            prepare.__set__('events', {
                emit: emitSpy
            });

            updatePathsSpy = jasmine.createSpy('updatePaths');
            prepare.__set__('FileUpdater', {
                updatePaths: updatePathsSpy
            });
        });

        it('Test#001 : should detect that the app does not have defined icons.', function () {
        // Mock
            const icons = [];
            const projectRoot = '/mock';
            const projectConfig = {
                getIcons: function () { return icons; },
                path: '/mock/config.xml',
                cdvNamespacePrefix: 'cdv'
            };
            const platformResourcesDir = PATH_RESOURCE;

            const cleanIcons = prepare.__get__('cleanIcons');
            cleanIcons(projectRoot, projectConfig, platformResourcesDir);

            const actualEmitMessage = emitSpy.calls.argsFor(0)[1];
            expect(actualEmitMessage).toContain('This app does not have launcher icons defined');
        });

        it('Test#002 : Should clean paths for adaptive icons.', function () {
        // Mock
            const icons = [mockGetIconItem({
                density: 'mdpi',
                background: 'res/icon/android/mdpi-background.png',
                foreground: 'res/icon/android/mdpi-foreground.png'
            })];
            const projectRoot = '/mock';
            const projectConfig = {
                getIcons: function () { return icons; },
                path: '/mock/config.xml',
                cdvNamespacePrefix: 'cdv'
            };
            const platformResourcesDir = PATH_RESOURCE;

            var expectedResourceMapBackground = createResourceMap('ic_launcher_background.png');

            // mocking initial responses for mapImageResources
            prepare.__set__('mapImageResources', function (rootDir, subDir, type, resourceName) {
                if (resourceName.includes('ic_launcher_background.png')) {
                    return expectedResourceMapBackground;
                }
            });

            const cleanIcons = prepare.__get__('cleanIcons');
            cleanIcons(projectRoot, projectConfig, platformResourcesDir);

            const actualResourceMapBackground = updatePathsSpy.calls.argsFor(0)[0];
            expect(actualResourceMapBackground).toEqual(expectedResourceMapBackground);
        });

        it('Test#003 : Should clean paths for legacy icons.', function () {
        // Mock
            const icons = [mockGetIconItem({
                src: 'res/icon/android/mdpi.png',
                density: 'mdpi'
            })];

            const projectRoot = '/mock';
            const projectConfig = {
                getIcons: function () { return icons; },
                path: '/mock/config.xml',
                cdvNamespacePrefix: 'cdv'
            };
            const platformResourcesDir = PATH_RESOURCE;

            var expectedResourceMap = createResourceMap();

            // mocking initial responses for mapImageResources
            prepare.__set__('mapImageResources', function (rootDir, subDir, type, resourceName) {
                return expectedResourceMap;
            });

            const cleanIcons = prepare.__get__('cleanIcons');
            cleanIcons(projectRoot, projectConfig, platformResourcesDir);

            const actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
            expect(actualResourceMap).toEqual(expectedResourceMap);
        });
    });

    describe('prepare arguments', () => {
    // Rewire
        let Api;
        let api;
        let prepare;

        // Spies
        let gradlePropertiesParserSpy;

        // Mock Data
        let cordovaProject;
        let options;

        beforeEach(function () {
            Api = rewire('../../bin/templates/cordova/Api');
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            prepare.__set__('require.resolve', (file) => {
                if (file === 'cordova-android/framework/cdv-gradle-config-defaults.json') {
                    return path.resolve('./framework/cdv-gradle-config-defaults.json');
                }
                return path.resolve(file);
            });

            const defaults = require('../../framework/cdv-gradle-config-defaults.json');

            prepare.__set__('fs', {
                readJSONSync: () => defaults,
                writeJSONSync: () => {}
            });

            cordovaProject = {
                root: '/mock',
                projectConfig: {
                    path: '/mock/config.xml',
                    cdvNamespacePrefix: 'cdv'
                },
                locations: {
                    plugins: '/mock/plugins',
                    www: '/mock/www'
                }
            };

            options = {
                options: {}
            };

            Api.__set__('ConfigParser',
                jasmine.createSpy('ConfigParser')
                    .and.returnValue(cordovaProject.projectConfig)
            );

            Api.__set__('prepare', prepare.prepare);

            prepare.__set__('events', {
                emit: jasmine.createSpy('emit')
            });
            prepare.__set__('updateWww', jasmine.createSpy());
            prepare.__set__('updateProjectAccordingTo', jasmine.createSpy('updateProjectAccordingTo')
                .and.returnValue(Promise.resolve()));
            prepare.__set__('updateIcons', jasmine.createSpy('updateIcons').and.returnValue(Promise.resolve()));
            prepare.__set__('updateSplashes', jasmine.createSpy('updateSplashes').and.returnValue(Promise.resolve()));
            prepare.__set__('updateFileResources', jasmine.createSpy('updateFileResources').and.returnValue(Promise.resolve()));
            prepare.__set__('updateConfigFilesFrom',
                jasmine.createSpy('updateConfigFilesFrom')
                    .and.returnValue({
                        getPreference: jasmine.createSpy('getPreference')
                    }));

            gradlePropertiesParserSpy = spyOn(GradlePropertiesParser.prototype, 'configure');

            api = new Api();
        });

        it('runs without arguments', async () => {
            await expectAsync(
                api.prepare(cordovaProject, options).then(() => {
                    expect(gradlePropertiesParserSpy).toHaveBeenCalledWith({});
                })
            ).toBeResolved();
        });

        it('runs with jvmargs', async () => {
            options.options.argv = ['--jvmargs=-Xmx=4096m'];
            await expectAsync(
                api.prepare(cordovaProject, options).then(() => {
                    expect(gradlePropertiesParserSpy).toHaveBeenCalledWith({
                        'org.gradle.jvmargs': '-Xmx=4096m'
                    });
                })
            ).toBeResolved();
        });
    });

    describe('updateSplashes method', function () {
        // Rewire
        let prepare;

        // Spies
        let emitSpy;
        let updatePathsSpy;

        // Mock Data
        let cordovaProject;
        let platformResourcesDir;

        beforeEach(function () {
            prepare = rewire('../../bin/templates/cordova/lib/prepare');

            cordovaProject = {
                root: '/mock',
                projectConfig: {
                    path: '/mock/config.xml',
                    cdvNamespacePrefix: 'cdv'
                },
                locations: {
                    plugins: '/mock/plugins',
                    www: '/mock/www'
                }
            };
            platformResourcesDir = PATH_RESOURCE;

            emitSpy = jasmine.createSpy('emit');
            prepare.__set__('events', {
                emit: emitSpy
            });

            updatePathsSpy = jasmine.createSpy('updatePaths');
            prepare.__set__('FileUpdater', {
                updatePaths: updatePathsSpy
            });

            // mocking initial responses for mapImageResources
            prepare.__set__('makeSplashCleanupMap', (rootDir, resourcesDir) => ({
                [path.join(resourcesDir, 'drawable-mdpi/screen.png')]: null,
                [path.join(resourcesDir, 'drawable-mdpi/screen.webp')]: null
            }));
        });

        it('Test#001 : Should detect no defined splash screens.', function () {
            const updateSplashes = prepare.__get__('updateSplashes');

            // mock data.
            cordovaProject.projectConfig.getSplashScreens = function (platform) {
                return [];
            };

            updateSplashes(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'This app does not have splash screens defined';
            expect(actual).toEqual(expected);
        });

        it('Test#02 : Should update splash png icon.', function () {
            const updateSplashes = prepare.__get__('updateSplashes');

            // mock data.
            cordovaProject.projectConfig.getSplashScreens = function (platform) {
                return [mockGetSplashScreenItem({
                    density: 'mdpi',
                    src: 'res/splash/android/mdpi-screen.png'
                })];
            };

            updateSplashes(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'Updating splash screens at ' + PATH_RESOURCE;
            expect(actual).toEqual(expected);

            const actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
            const expectedResourceMap = {};
            expectedResourceMap[path.join(PATH_RESOURCE, 'drawable-mdpi', 'screen.png')] = 'res/splash/android/mdpi-screen.png';
            expectedResourceMap[path.join(PATH_RESOURCE, 'drawable-mdpi', 'screen.webp')] = null;

            expect(actualResourceMap).toEqual(expectedResourceMap);
        });

        it('Test#03 : Should update splash webp icon.', function () {
            const updateSplashes = prepare.__get__('updateSplashes');

            // mock data.
            cordovaProject.projectConfig.getSplashScreens = function (platform) {
                return [mockGetSplashScreenItem({
                    density: 'mdpi',
                    src: 'res/splash/android/mdpi-screen.webp'
                })];
            };

            // Creating Spies
            updateSplashes(cordovaProject, platformResourcesDir);

            // The emit was called
            expect(emitSpy).toHaveBeenCalled();

            // The emit message was.
            const actual = emitSpy.calls.argsFor(0)[1];
            const expected = 'Updating splash screens at ' + PATH_RESOURCE;
            expect(actual).toEqual(expected);

            const actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];

            const expectedResourceMap = {};
            expectedResourceMap[path.join(PATH_RESOURCE, 'drawable-mdpi', 'screen.webp')] = 'res/splash/android/mdpi-screen.webp';
            expectedResourceMap[path.join(PATH_RESOURCE, 'drawable-mdpi', 'screen.png')] = null;

            expect(actualResourceMap).toEqual(expectedResourceMap);
        });
    });
});
