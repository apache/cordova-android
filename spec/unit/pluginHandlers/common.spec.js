/*
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

var rewire = require('rewire');
var common = rewire('../../../bin/templates/cordova/lib/pluginHandlers');
var path = require('path');
var fs = require('fs-extra');
var osenv = require('os');
var test_dir = path.join(osenv.tmpdir(), 'test_plugman');
var project_dir = path.join(test_dir, 'project');
var src = path.join(project_dir, 'src');
var dest = path.join(project_dir, 'dest');
var java_dir = path.join(src, 'one', 'two', 'three');
var java_file = path.join(java_dir, 'test.java');
var symlink_file = path.join(java_dir, 'symlink');
var non_plugin_file = path.join(osenv.tmpdir(), 'non_plugin_file');

var copyFile = common.__get__('copyFile');
var deleteJava = common.__get__('deleteJava');
var copyNewFile = common.__get__('copyNewFile');

describe('common platform handler', function () {
    describe('copyFile', function () {
        it('Test#001 : should throw if source path not found', function () {
            fs.removeSync(src);
            expect(function () { copyFile(test_dir, src, project_dir, dest); })
                .toThrow(new Error('"' + src + '" not found!'));
        });

        it('Test#002 : should throw if src not in plugin directory', function () {
            fs.ensureDirSync(project_dir);
            fs.writeFileSync(non_plugin_file, 'contents', 'utf-8');
            var outside_file = '../non_plugin_file';
            expect(function () { copyFile(test_dir, outside_file, project_dir, dest); })
                .toThrow(new Error('File "' + path.resolve(test_dir, outside_file) + '" is located outside the plugin directory "' + test_dir + '"'));
            fs.removeSync(test_dir);
        });

        it('Test#003 : should allow symlink src, if inside plugin', function () {
            fs.ensureDirSync(java_dir);
            fs.writeFileSync(java_file, 'contents', 'utf-8');

            // This will fail on windows if not admin - ignore the error in that case.
            if (ignoreEPERMonWin32(java_file, symlink_file)) {
                return;
            }

            copyFile(test_dir, symlink_file, project_dir, dest);
            fs.removeSync(project_dir);
        });

        it('Test#004 : should throw if symlink is linked to a file outside the plugin', function () {
            fs.ensureDirSync(java_dir);
            fs.writeFileSync(non_plugin_file, 'contents', 'utf-8');

            // This will fail on windows if not admin - ignore the error in that case.
            if (ignoreEPERMonWin32(non_plugin_file, symlink_file)) {
                return;
            }

            expect(function () { copyFile(test_dir, symlink_file, project_dir, dest); })
                .toThrow(new Error('File "' + path.resolve(test_dir, symlink_file) + '" is located outside the plugin directory "' + test_dir + '"'));
            fs.removeSync(project_dir);
        });

        it('Test#005 : should throw if dest is outside the project directory', function () {
            fs.ensureDirSync(java_dir);
            fs.writeFileSync(java_file, 'contents', 'utf-8');
            expect(function () { copyFile(test_dir, java_file, project_dir, non_plugin_file); })
                .toThrow(new Error('Destination "' + path.resolve(project_dir, non_plugin_file) + '" for source file "' + path.resolve(test_dir, java_file) + '" is located outside the project'));
            fs.removeSync(project_dir);
        });

        it('Test#006 : should call mkdir -p on target path', function () {
            fs.ensureDirSync(java_dir);
            fs.writeFileSync(java_file, 'contents', 'utf-8');

            var s = spyOn(fs, 'ensureDirSync').and.callThrough();
            var resolvedDest = path.resolve(project_dir, dest);

            copyFile(test_dir, java_file, project_dir, dest);

            expect(s).toHaveBeenCalled();
            expect(s).toHaveBeenCalledWith(path.dirname(resolvedDest));
            fs.removeSync(project_dir);
        });

        it('Test#007 : should call cp source/dest paths', function () {
            fs.ensureDirSync(java_dir);
            fs.writeFileSync(java_file, 'contents', 'utf-8');

            var s = spyOn(fs, 'copySync').and.callThrough();
            var resolvedDest = path.resolve(project_dir, dest);

            copyFile(test_dir, java_file, project_dir, dest);

            expect(s).toHaveBeenCalled();
            expect(s).toHaveBeenCalledWith(java_file, resolvedDest);

            fs.removeSync(project_dir);
        });
    });

    describe('copyNewFile', function () {
        it('Test#008 : should throw if target path exists', function () {
            fs.ensureDirSync(dest);
            expect(function () { copyNewFile(test_dir, src, project_dir, dest); })
                .toThrow(new Error('"' + dest + '" already exists!'));
            fs.removeSync(dest);
        });
    });

    describe('deleteJava', function () {
        beforeEach(function () {
            fs.ensureDirSync(java_dir);
            fs.writeFileSync(java_file, 'contents', 'utf-8');
        });

        afterEach(function () {
            fs.removeSync(java_dir);
        });

        it('Test#009 : should call fs.unlinkSync on the provided paths', function () {
            var s = spyOn(fs, 'removeSync').and.callThrough();
            deleteJava(project_dir, java_file);
            expect(s).toHaveBeenCalled();
            expect(s).toHaveBeenCalledWith(path.resolve(project_dir, java_file));
        });

        it('Test#010 : should delete empty directories after removing source code in a java src path hierarchy', function () {
            deleteJava(project_dir, java_file);
            expect(fs.existsSync(java_file)).not.toBe(true);
            expect(fs.existsSync(java_dir)).not.toBe(true);
            expect(fs.existsSync(path.join(src, 'one'))).not.toBe(true);
        });

        it('Test#011 : should never delete the top-level src directory, even if all plugins added were removed', function () {
            deleteJava(project_dir, java_file);
            expect(fs.existsSync(src)).toBe(true);
        });
    });
});

function ignoreEPERMonWin32 (symlink_src, symlink_dest) {
    try {
        fs.symlinkSync(symlink_src, symlink_dest);
    } catch (e) {
        if (process.platform === 'win32' && e.message.indexOf('Error: EPERM, operation not permitted' > -1)) {
            return true;
        }
        throw e;
    }
    return false;
}
