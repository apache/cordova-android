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
const path = require('node:path');
const properties_parser = require('properties-parser');
const pluginHandlers = require('./pluginHandlers');
const CordovaGradleConfigParserFactory = require('./config/CordovaGradleConfigParserFactory');

let projectFileCache = {};

function addToPropertyList (projectProperties, key, value) {
    let i = 1;
    while (projectProperties.get(key + '.' + i)) { i++; }

    projectProperties.set(key + '.' + i, value);
    projectProperties.dirty = true;
}

function removeFromPropertyList (projectProperties, key, value) {
    let i = 1;
    let currentValue;
    while ((currentValue = projectProperties.get(key + '.' + i))) {
        if (currentValue === value) {
            while ((currentValue = projectProperties.get(key + '.' + (i + 1)))) {
                projectProperties.set(key + '.' + i, currentValue);
                i++;
            }
            projectProperties.set(key + '.' + i);
            break;
        }
        i++;
    }
    projectProperties.dirty = true;
}

function getRelativeLibraryPath (parentDir, subDir) {
    const libraryPath = path.relative(parentDir, subDir);
    return (path.sep === '\\') ? libraryPath.replace(/\\/g, '/') : libraryPath;
}

class AndroidProject {
    constructor (projectDir) {
        this._propertiesEditors = {};
        this._subProjectDirs = {};
        this._dirty = false;
        this.projectDir = projectDir;
        this.platformWww = path.join(this.projectDir, 'platform_www');
        this.www = path.join(this.projectDir, 'app/src/main/assets/www');
        this.cordovaGradleConfigParser = CordovaGradleConfigParserFactory.create(this.projectDir);
    }

    /**
     * Reads the package name out of the Cordova's Gradle Config file
     *
     * @param   {String}  projectDir  The absolute path to the directory containing the project
     * @return  {String}              The name of the package
     */
    getPackageName () {
        return this.cordovaGradleConfigParser.getPackageName();
    }

    getCustomSubprojectRelativeDir (plugin_id, src) {
        // All custom subprojects are prefixed with the last portion of the package id.
        // This is to avoid collisions when opening multiple projects in Eclipse that have subprojects with the same name.
        const packageName = this.getPackageName();
        const lastDotIndex = packageName.lastIndexOf('.');
        const prefix = packageName.substring(lastDotIndex + 1);
        const subRelativeDir = path.join(plugin_id, prefix + '-' + path.basename(src));
        return subRelativeDir;
    }

    addSubProject (parentDir, subDir) {
        const parentProjectFile = path.resolve(parentDir, 'project.properties');
        const subProjectFile = path.resolve(subDir, 'project.properties');
        const parentProperties = this._getPropertiesFile(parentProjectFile);
        // TODO: Setting the target needs to happen only for pre-3.7.0 projects
        if (fs.existsSync(subProjectFile)) {
            const subProperties = this._getPropertiesFile(subProjectFile);
            subProperties.set('target', parentProperties.get('target'));
            subProperties.dirty = true;
            this._subProjectDirs[subDir] = true;
        }
        addToPropertyList(parentProperties, 'android.library.reference', getRelativeLibraryPath(parentDir, subDir));

        this._dirty = true;
    }

    removeSubProject (parentDir, subDir) {
        const parentProjectFile = path.resolve(parentDir, 'project.properties');
        const parentProperties = this._getPropertiesFile(parentProjectFile);
        removeFromPropertyList(parentProperties, 'android.library.reference', getRelativeLibraryPath(parentDir, subDir));
        delete this._subProjectDirs[subDir];
        this._dirty = true;
    }

    addGradleReference (parentDir, subDir) {
        const parentProjectFile = path.resolve(parentDir, 'project.properties');
        const parentProperties = this._getPropertiesFile(parentProjectFile);
        addToPropertyList(parentProperties, 'cordova.gradle.include', getRelativeLibraryPath(parentDir, subDir));
        this._dirty = true;
    }

    removeGradleReference (parentDir, subDir) {
        const parentProjectFile = path.resolve(parentDir, 'project.properties');
        const parentProperties = this._getPropertiesFile(parentProjectFile);
        removeFromPropertyList(parentProperties, 'cordova.gradle.include', getRelativeLibraryPath(parentDir, subDir));
        this._dirty = true;
    }

    addSystemLibrary (parentDir, value) {
        const parentProjectFile = path.resolve(parentDir, 'project.properties');
        const parentProperties = this._getPropertiesFile(parentProjectFile);
        addToPropertyList(parentProperties, 'cordova.system.library', value);
        this._dirty = true;
    }

    removeSystemLibrary (parentDir, value) {
        const parentProjectFile = path.resolve(parentDir, 'project.properties');
        const parentProperties = this._getPropertiesFile(parentProjectFile);
        removeFromPropertyList(parentProperties, 'cordova.system.library', value);
        this._dirty = true;
    }

    write () {
        if (!this._dirty) {
            return;
        }
        this._dirty = false;

        for (const filename in this._propertiesEditors) {
            const editor = this._propertiesEditors[filename];
            if (editor.dirty) {
                fs.writeFileSync(filename, editor.toString());
                editor.dirty = false;
            }
        }
    }

    getInstaller (type) {
        return pluginHandlers.getInstaller(type);
    }

    getUninstaller (type) {
        return pluginHandlers.getUninstaller(type);
    }

    /*
     * This checks if an Android project is clean or has old build artifacts
     */
    isClean () {
        const build_path = path.join(this.projectDir, 'build');
        // If the build directory doesn't exist, it's clean
        return !(fs.existsSync(build_path));
    }

    _getPropertiesFile (filename) {
        if (!this._propertiesEditors[filename]) {
            if (fs.existsSync(filename)) {
                this._propertiesEditors[filename] = properties_parser.createEditor(filename);
            } else {
                this._propertiesEditors[filename] = properties_parser.createEditor();
            }
        }

        return this._propertiesEditors[filename];
    }

    static getProjectFile (projectDir) {
        if (!projectFileCache[projectDir]) {
            projectFileCache[projectDir] = new AndroidProject(projectDir);
        }

        return projectFileCache[projectDir];
    }

    static purgeCache (projectDir) {
        if (projectDir) {
            delete projectFileCache[projectDir];
        } else {
            projectFileCache = {};
        }
    }
}

module.exports = AndroidProject;
