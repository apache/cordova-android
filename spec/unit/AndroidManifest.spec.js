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

const fs = require('fs');
const os = require('os');
const path = require('path');
const rewire = require('rewire');

describe('AndroidManifest', () => {
    const VERSION_CODE = '50407';
    const VERSION_NAME = '5.4.7';
    const PACKAGE_ID = 'io.cordova.test';
    const ACTIVITY_LAUNCH_MODE = 'singleTop';
    const ACTIVITY_NAME = 'MainActivity';
    const ACTIVITY_ORIENTATION = 'portrait';
    const MIN_SDK_VERSION = '12';
    const MAX_SDK_VERSION = '88';
    const TARGET_SDK_VERSION = '27';

    const DEFAULT_MANIFEST = `<?xml version='1.0' encoding='utf-8'?>
<manifest android:hardwareAccelerated="true" android:versionCode="${VERSION_CODE}" android:versionName="${VERSION_NAME}"
    package="${PACKAGE_ID}" xmlns:android="http://schemas.android.com/apk/res/android">
    <supports-screens android:anyDensity="true" android:largeScreens="true" android:normalScreens="true"
        android:resizeable="true" android:smallScreens="true" android:xlargeScreens="true" />
    <uses-permission android:name="android.permission.INTERNET" />
    <application android:hardwareAccelerated="true" android:icon="@mipmap/icon" android:label="@string/app_name"
        android:supportsRtl="true" android:debuggable="true">
        <activity android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale"
            android:label="@string/activity_name" android:launchMode="${ACTIVITY_LAUNCH_MODE}"
            android:name="${ACTIVITY_NAME}" android:theme="@android:style/Theme.DeviceDefault.NoActionBar"
            android:windowSoftInputMode="adjustResize" android:screenOrientation="${ACTIVITY_ORIENTATION}">
            <intent-filter android:label="@string/launcher_name">
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
    <uses-sdk android:minSdkVersion="${MIN_SDK_VERSION}" android:maxSdkVersion="${MAX_SDK_VERSION}" android:targetSdkVersion="${TARGET_SDK_VERSION}" />
</manifest>`;

    const manifestPath = path.join(os.tmpdir(), `AndroidManifest${Date.now()}.xml`);

    function createTempManifestFile (xml) {
        fs.writeFileSync(manifestPath, xml);
    }

    function removeTempManifestFile () {
        fs.unlinkSync(manifestPath);
    }

    let AndroidManifest;
    let manifest;

    beforeEach(() => {
        createTempManifestFile(DEFAULT_MANIFEST);

        AndroidManifest = rewire('../../bin/templates/cordova/lib/AndroidManifest');
        manifest = new AndroidManifest(manifestPath);
    });

    afterEach(() => {
        removeTempManifestFile();
    });

    describe('constructor', () => {
        it('should parse the manifest', () => {
            expect(manifest.doc.getroot().tag).toBe('manifest');
        });

        it('should throw an error if not a valid manifest', () => {
            createTempManifestFile(`<?xml version='1.0' encoding='utf-8'?><notamanifest></notamanifest>`);

            expect(() => new AndroidManifest(manifestPath)).toThrowError();
        });
    });

    describe('versionName', () => {
        it('should get the version name', () => {
            expect(manifest.getVersionName()).toBe(VERSION_NAME);
        });

        it('should set the version name', () => {
            const newVersionName = `${VERSION_NAME}55555`;
            manifest.setVersionName(newVersionName);
            expect(manifest.getVersionName()).toBe(newVersionName);
        });
    });

    describe('versionCode', () => {
        it('should get the version code', () => {
            expect(manifest.getVersionCode()).toBe(VERSION_CODE);
        });

        it('should set the version code', () => {
            const newVersionName = `${VERSION_CODE}12345`;
            manifest.setVersionCode(newVersionName);
            expect(manifest.getVersionCode()).toBe(newVersionName);
        });
    });

    describe('packageId', () => {
        it('should get the package ID', () => {
            expect(manifest.getPackageId()).toBe(PACKAGE_ID);
        });

        it('should set the package ID', () => {
            const newPackageId = `${PACKAGE_ID}new`;
            manifest.setPackageId(newPackageId);
            expect(manifest.getPackageId()).toBe(newPackageId);
        });
    });

    describe('activity', () => {
        let activity;

        beforeEach(() => {
            activity = manifest.getActivity();
        });

        describe('name', () => {
            it('should get the activity name', () => {
                expect(activity.getName()).toBe(ACTIVITY_NAME);
            });

            it('should set the activity name', () => {
                const newActivityName = `${ACTIVITY_NAME}New`;
                activity.setName(newActivityName);
                expect(activity.getName()).toBe(newActivityName);
            });

            it('should remove the activity name if set to empty', () => {
                activity.setName();
                expect(activity.getName()).toBe(undefined);
            });
        });

        describe('orientation', () => {
            it('should get the activity orientation', () => {
                expect(activity.getOrientation()).toBe(ACTIVITY_ORIENTATION);
            });

            it('should set the activity orienation', () => {
                const newOrientation = 'landscape';
                activity.setOrientation(newOrientation);
                expect(activity.getOrientation()).toBe(newOrientation);
            });

            it('should remove the orientation if set to default', () => {
                activity.setOrientation(AndroidManifest.__get__('DEFAULT_ORIENTATION'));
                expect(activity.getOrientation()).toBe(undefined);
            });

            it('should remove the orientation if set to empty', () => {
                activity.setOrientation();
                expect(activity.getOrientation()).toBe(undefined);
            });
        });

        describe('launch mode', () => {
            it('should get the activity launch mode', () => {
                expect(activity.getLaunchMode()).toBe(ACTIVITY_LAUNCH_MODE);
            });

            it('should set the activity launch mode', () => {
                const newLaunchMode = 'standard';
                activity.setLaunchMode(newLaunchMode);
                expect(activity.getLaunchMode()).toBe(newLaunchMode);
            });

            it('should remove the launch mode if set to empty', () => {
                activity.setLaunchMode();
                expect(activity.getLaunchMode()).toBe(undefined);
            });
        });
    });

    describe('debuggable', () => {
        it('should get debuggable', () => {
            expect(manifest.getDebuggable()).toBe(true);
        });

        it('should remove debuggable if set to a falsy value', () => {
            manifest.setDebuggable(false);
            expect(manifest.doc.getroot().find('./application').attrib['android:debuggable']).toBe(undefined);
        });

        it('should set debuggable to true', () => {
            const NO_DEBUGGABLE_MANIFEST = DEFAULT_MANIFEST.replace('android:debuggable="true"', '');
            createTempManifestFile(NO_DEBUGGABLE_MANIFEST);
            manifest = new AndroidManifest(manifestPath);

            expect(manifest.getDebuggable()).toBe(false);

            manifest.setDebuggable(true);
            expect(manifest.getDebuggable()).toBe(true);
        });
    });

    describe('write', () => {
        let fsSpy;

        beforeEach(() => {
            fsSpy = jasmine.createSpyObj('fs', ['writeFileSync']);
            AndroidManifest.__set__('fs', fsSpy);
        });

        it('should overwrite existing manifest if path not specified', () => {
            manifest.write();

            expect(fsSpy.writeFileSync).toHaveBeenCalledWith(manifestPath, jasmine.any(String), jasmine.any(String));
        });

        it('should save to the specified path', () => {
            const testPath = 'NewAndroidManifest.xml';
            manifest.write(testPath);

            expect(fsSpy.writeFileSync).toHaveBeenCalledWith(testPath, jasmine.any(String), jasmine.any(String));
        });

        it('should write the manifest from the parsed XML as utf-8', () => {
            const newXml = '<test></test>';
            spyOn(manifest.doc, 'write').and.returnValue(newXml);

            manifest.write();

            expect(fsSpy.writeFileSync).toHaveBeenCalledWith(jasmine.any(String), newXml, 'utf-8');
        });
    });

});
