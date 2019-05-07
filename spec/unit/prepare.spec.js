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

const PATH_RESOURCE = path.join('platforms', 'android', 'app', 'src', 'main', 'res');

/**
 * Creates blank resource map object, used for testing.
 *
 * @param {String} target specific resource item
 */
function createResourceMap (target) {
    let resources = {};

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
        let actual = emitSpy.calls.argsFor(0)[1];
        let expected = 'This app does not have launcher icons defined';
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
        let resourceMap = createResourceMap();
        let phaseOneModification = {};
        phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_foreground.png')] = 'res/icon/android/mdpi-foreground.png';
        phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
        let phaseOneUpdatedIconsForAdaptive = Object.assign({}, resourceMap, phaseOneModification);

        updateIconResourceForAdaptiveSpy = jasmine.createSpy('updateIconResourceForAdaptiveSpy');
        prepare.__set__('updateIconResourceForAdaptive', function (preparedIcons, resourceMap, platformResourcesDir) {
            updateIconResourceForAdaptiveSpy();
            return phaseOneUpdatedIconsForAdaptive;
        });

        let phaseTwoModification = {};
        phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-foreground.png';
        phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
        let phaseTwoUpdatedIconsForLegacy = Object.assign({}, phaseOneUpdatedIconsForAdaptive, phaseTwoModification);

        updateIconResourceForLegacySpy = jasmine.createSpy('updateIconResourceForLegacySpy');
        prepare.__set__('updateIconResourceForLegacy', function (preparedIcons, resourceMap, platformResourcesDir) {
            updateIconResourceForLegacySpy();
            return phaseTwoUpdatedIconsForLegacy;
        });

        updateIcons(cordovaProject, platformResourcesDir);

        // The emit was called
        expect(emitSpy).toHaveBeenCalled();

        // The emit message was.
        let actual = emitSpy.calls.argsFor(0)[1];
        let expected = 'Updating icons at ' + PATH_RESOURCE;
        expect(actual).toEqual(expected);

        // Expected to be called.
        expect(updatePathsSpy).toHaveBeenCalled();
        expect(updateIconResourceForAdaptiveSpy).toHaveBeenCalled();
        expect(updateIconResourceForLegacySpy).toHaveBeenCalled();

        let actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
        let expectedResourceMap = phaseTwoUpdatedIconsForLegacy;
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
        let resourceMap = createResourceMap();
        let phaseOneModification = {};
        phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_foreground.png')] = 'res/icon/android/mdpi-foreground.png';
        phaseOneModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
        let phaseOneUpdatedIconsForAdaptive = Object.assign({}, resourceMap, phaseOneModification);

        updateIconResourceForAdaptiveSpy = jasmine.createSpy('updateIconResourceForAdaptiveSpy');
        prepare.__set__('updateIconResourceForAdaptive', function (preparedIcons, resourceMap, platformResourcesDir) {
            updateIconResourceForAdaptiveSpy();
            return phaseOneUpdatedIconsForAdaptive;
        });

        let phaseTwoModification = {};
        phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-foreground.png';
        phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
        let phaseTwoUpdatedIconsForLegacy = Object.assign({}, phaseOneUpdatedIconsForAdaptive, phaseTwoModification);

        updateIconResourceForLegacySpy = jasmine.createSpy('updateIconResourceForLegacySpy');
        prepare.__set__('updateIconResourceForLegacy', function (preparedIcons, resourceMap, platformResourcesDir) {
            updateIconResourceForLegacySpy();
            return phaseTwoUpdatedIconsForLegacy;
        });

        updateIcons(cordovaProject, platformResourcesDir);

        // The emit was called
        expect(emitSpy).toHaveBeenCalled();

        // The emit message was.
        let actual = emitSpy.calls.argsFor(0)[1];
        let expected = 'Updating icons at ' + PATH_RESOURCE;
        expect(actual).toEqual(expected);

        // Expected to be called.
        expect(updatePathsSpy).toHaveBeenCalled();
        expect(updateIconResourceForAdaptiveSpy).toHaveBeenCalled();
        expect(updateIconResourceForLegacySpy).toHaveBeenCalled();

        let actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
        let expectedResourceMap = phaseTwoUpdatedIconsForLegacy;
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
        let phaseOneUpdatedIconsForAdaptive = createResourceMap();

        updateIconResourceForAdaptiveSpy = jasmine.createSpy('updateIconResourceForAdaptiveSpy');
        prepare.__set__('updateIconResourceForAdaptive', function (preparedIcons, resourceMap, platformResourcesDir) {
            updateIconResourceForAdaptiveSpy();
            return phaseOneUpdatedIconsForAdaptive;
        });

        let phaseTwoModification = {};
        phaseTwoModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-icon.png';
        let phaseTwoUpdatedIconsForLegacy = Object.assign({}, phaseOneUpdatedIconsForAdaptive, phaseTwoModification);

        updateIconResourceForLegacySpy = jasmine.createSpy('updateIconResourceForLegacySpy');
        prepare.__set__('updateIconResourceForLegacy', function (preparedIcons, resourceMap, platformResourcesDir) {
            updateIconResourceForLegacySpy();
            return phaseTwoUpdatedIconsForLegacy;
        });

        updateIcons(cordovaProject, platformResourcesDir);

        // The emit was called
        expect(emitSpy).toHaveBeenCalled();

        // The emit message was.
        let actual = emitSpy.calls.argsFor(0)[1];
        let expected = 'Updating icons at ' + PATH_RESOURCE;
        expect(actual).toEqual(expected);

        // Expected to be called.
        expect(updatePathsSpy).toHaveBeenCalled();
        expect(updateIconResourceForAdaptiveSpy).not.toHaveBeenCalled();
        expect(updateIconResourceForLegacySpy).toHaveBeenCalled();

        let actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
        let expectedResourceMap = phaseTwoUpdatedIconsForLegacy;
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
        let ldpi = mockGetIconItem({
            density: 'ldpi',
            background: 'res/icon/android/ldpi-background.png',
            foreground: 'res/icon/android/ldpi-foreground.png'
        });

        let mdpi = mockGetIconItem({
            density: 'mdpi',
            background: 'res/icon/android/mdpi-background.png',
            foreground: 'res/icon/android/mdpi-foreground.png'
        });

        let icons = [ldpi, mdpi];
        let actual = prepareIcons(icons);
        let expected = {
            android_icons: { ldpi, mdpi },
            default_icon: undefined
        };

        expect(expected).toEqual(actual);

    });

    it('Test#002 : should emit extra default icon found for legacy use case.', function () {
        // mock data.
        let ldpi = mockGetIconItem({
            src: 'res/icon/android/ldpi-icon.png',
            density: 'ldpi'
        });

        let mdpi = mockGetIconItem({
            src: 'res/icon/android/mdpi-icon.png',
            density: 'mdpi'
        });

        let icons = [ldpi, mdpi];
        let actual = prepareIcons(icons);
        let expected = {
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
        let expectedModification = {};
        expectedModification[path.join(PATH_RESOURCE, 'mipmap-mdpi', 'ic_launcher.png')] = 'res/icon/android/mdpi-icon.png';
        let expected = Object.assign({}, resourceMap, expectedModification);
        let actual = updateIconResourceForLegacy(preparedIcons, resourceMap, platformResourcesDir);

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
        let expectedModification = {};
        expectedModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_background.png')] = 'res/icon/android/mdpi-background.png';
        expectedModification[path.join(PATH_RESOURCE, 'mipmap-mdpi-v26', 'ic_launcher_foreground.png')] = 'res/icon/android/mdpi-foreground.png';

        let expected = Object.assign({}, resourceMap, expectedModification);
        let actual = updateIconResourceForAdaptive(preparedIcons, resourceMap, platformResourcesDir);

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
        let icons = [];
        let projectRoot = '/mock';
        let projectConfig = {
            getIcons: function () { return icons; },
            path: '/mock/config.xml',
            cdvNamespacePrefix: 'cdv'
        };
        let platformResourcesDir = PATH_RESOURCE;

        const cleanIcons = prepare.__get__('cleanIcons');
        cleanIcons(projectRoot, projectConfig, platformResourcesDir);

        let actualEmitMessage = emitSpy.calls.argsFor(0)[1];
        expect(actualEmitMessage).toContain('This app does not have launcher icons defined');
    });

    it('Test#002 : Should clean paths for adaptive icons.', function () {
        // Mock
        let icons = [mockGetIconItem({
            density: 'mdpi',
            background: 'res/icon/android/mdpi-background.png',
            foreground: 'res/icon/android/mdpi-foreground.png'
        })];
        let projectRoot = '/mock';
        let projectConfig = {
            getIcons: function () { return icons; },
            path: '/mock/config.xml',
            cdvNamespacePrefix: 'cdv'
        };
        let platformResourcesDir = PATH_RESOURCE;

        var expectedResourceMapBackground = createResourceMap('ic_launcher_background.png');

        // mocking initial responses for mapImageResources
        prepare.__set__('mapImageResources', function (rootDir, subDir, type, resourceName) {
            if (resourceName.includes('ic_launcher_background.png')) {
                return expectedResourceMapBackground;
            }
        });

        const cleanIcons = prepare.__get__('cleanIcons');
        cleanIcons(projectRoot, projectConfig, platformResourcesDir);

        let actualResourceMapBackground = updatePathsSpy.calls.argsFor(0)[0];
        expect(actualResourceMapBackground).toEqual(expectedResourceMapBackground);
    });

    it('Test#003 : Should clean paths for legacy icons.', function () {
        // Mock
        let icons = [mockGetIconItem({
            src: 'res/icon/android/mdpi.png',
            density: 'mdpi'
        })];

        let projectRoot = '/mock';
        let projectConfig = {
            getIcons: function () { return icons; },
            path: '/mock/config.xml',
            cdvNamespacePrefix: 'cdv'
        };
        let platformResourcesDir = PATH_RESOURCE;

        var expectedResourceMap = createResourceMap();

        // mocking initial responses for mapImageResources
        prepare.__set__('mapImageResources', function (rootDir, subDir, type, resourceName) {
            return expectedResourceMap;
        });

        const cleanIcons = prepare.__get__('cleanIcons');
        cleanIcons(projectRoot, projectConfig, platformResourcesDir);

        let actualResourceMap = updatePathsSpy.calls.argsFor(0)[0];
        expect(actualResourceMap).toEqual(expectedResourceMap);
    });
});
