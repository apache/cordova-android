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

const path = require('path');
const rewire = require('rewire');
const { CordovaError } = require('cordova-common');
const utils = require('../../bin/templates/cordova/lib/utils');
const glob = require('fast-glob');

describe('Java', () => {
    const Java = rewire('../../bin/templates/cordova/lib/env/java');

    describe('getVersion', () => {
        let originalEnsureFunc = null;

        beforeEach(() => {
            /*
                This is to avoid changing/polluting the
                process environment variables
                as a result of running these java tests; which could produce
                unexpected side effects to other tests.
             */
            originalEnsureFunc = Java._ensure;
            spyOn(Java, '_ensure').and.callFake(() => {
                return originalEnsureFunc({});
            });
        });

        it('runs', async () => {
            Java.__set__('execa', () => Promise.resolve({
                all: 'javac 1.8.0_275'
            }));

            console.log('BEFORE', process.env.JAVA_HOME);
            const result = await Java.getVersion();
            console.log('AFTER', process.env.JAVA_HOME);
            expect(result.major).toBe(1);
            expect(result.minor).toBe(8);
            expect(result.patch).toBe(0);
            expect(result.version).toBe('1.8.0');
        });

        it('produces a CordovaError on error', async () => {
            Java.__set__('execa', () => Promise.reject({
                shortMessage: 'test error'
            }));
            const emitSpy = jasmine.createSpy('events.emit');
            Java.__set__('events', {
                emit: emitSpy
            });

            await expectAsync(Java.getVersion())
                .toBeRejectedWithError(CordovaError, /Failed to run "javac -version"/);
            expect(emitSpy).toHaveBeenCalledWith('verbose', 'test error');
        });
    });

    describe('_ensure', () => {
        beforeEach(() => {
            Java.__set__('javaIsEnsured', false);
        });

        it('with JAVA_HOME / without javac', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('');

            const env = {
                JAVA_HOME: '/tmp/jdk'
            };

            await Java._ensure(env);

            expect(env.PATH.split(path.delimiter))
                .toContain(path.join(env.JAVA_HOME, 'bin'));
        });

        it('detects JDK in default location on windows', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('');
            spyOn(utils, 'isWindows').and.returnValue(true);

            const root = 'C:\\Program Files';
            const env = {
                ProgramFiles: root
            };

            spyOn(glob, 'sync').and.returnValue(`${root}\\java\\jdk1.8.0_275`);

            const jdkDir = `${root}\\java\\jdk1.8.0_275`;

            await Java._ensure(env);

            expect(env.JAVA_HOME).withContext('JAVA_HOME').toBe(jdkDir);
            expect(env.PATH).toContain(jdkDir);
        });

        it('detects JDK in default location on windows (x86)', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('');
            spyOn(utils, 'isWindows').and.returnValue(true);

            const root = 'C:\\Program Files (x86)';
            const env = {
                'ProgramFiles(x86)': root
            };

            spyOn(glob, 'sync').and.returnValue(`${root}\\java\\jdk1.8.0_275`);

            const jdkDir = `${root}\\java\\jdk1.8.0_275`;

            await Java._ensure(env);

            expect(env.JAVA_HOME).withContext('JAVA_HOME').toBe(jdkDir);
            expect(env.PATH).toContain(jdkDir);
        });

        it('without JAVA_HOME / with javac - Mac OS X - success', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('/tmp/jdk/bin');
            const fsSpy = jasmine.createSpy('fs').and.returnValue(true);
            Java.__set__('fs', {
                existsSync: fsSpy
            });
            Java.__set__('execa', async () => ({ stdout: '/tmp/jdk' }));

            const env = {};

            await Java._ensure(env);

            expect(fsSpy).toHaveBeenCalledWith('/usr/libexec/java_home');
            expect(env.JAVA_HOME).toBe('/tmp/jdk');
        });

        it('without JAVA_HOME / with javac - Mac OS X - error', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('/tmp/jdk/bin');
            Java.__set__('fs', { existsSync: () => true });
            Java.__set__('execa', jasmine.createSpy('execa').and.returnValue(Promise.reject({
                shortMessage: 'test error'
            })));
            const emitSpy = jasmine.createSpy('events.emit');
            Java.__set__('events', {
                emit: emitSpy
            });

            await expectAsync(Java._ensure({}))
                .toBeRejectedWithError(CordovaError, /Failed to find 'JAVA_HOME' environment variable/);

            expect(emitSpy).toHaveBeenCalledWith('verbose', 'test error');
        });

        it('derive from javac location - success', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('/tmp/jdk/bin');
            Java.__set__('fs', { existsSync: path => !/java_home$/.test(path) });
            const env = {};
            await Java._ensure(env);
            expect(env.JAVA_HOME).toBe('/tmp');
        });

        it('derive from javac location - error', async () => {
            spyOn(utils, 'forgivingWhichSync').and.returnValue('/tmp/jdk/bin');
            Java.__set__('fs', { existsSync: () => false });
            await expectAsync(Java._ensure({})).toBeRejectedWithError(CordovaError, /Failed to find 'JAVA_HOME' environment variable/);
        });
    });
});
