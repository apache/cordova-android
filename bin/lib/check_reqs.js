#!/usr/bin/env node

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

var shelljs = require('shelljs'),
    child_process = require('child_process'),
    Q     = require('q'),
    path  = require('path'),
    fs    = require('fs'),
    which = require('which'),
    ROOT  = path.join(__dirname, '..', '..');

var isWindows = process.platform == 'win32';

function forgivingWhichSync(cmd) {
    try {
        return which.sync(cmd);
    } catch (e) {
        return '';
    }
}

function tryCommand(cmd, errMsg) {
    var d = Q.defer();
    child_process.exec(cmd, function(err, stdout, stderr) {
        if (err) d.reject(new Error(errMsg));
        else d.resolve(stdout);
    });
    return d.promise;
}

// Get valid target from framework/project.properties
module.exports.get_target = function() {
    if(fs.existsSync(path.join(ROOT, 'framework', 'project.properties'))) {
        var target = shelljs.grep(/target=android-[\d+]/, path.join(ROOT, 'framework', 'project.properties'));
        return target.split('=')[1].replace('\n', '').replace('\r', '').replace(' ', '');
    } else if (fs.existsSync(path.join(ROOT, 'project.properties'))) {
        // if no target found, we're probably in a project and project.properties is in ROOT.
        // this is called on the project itself, and can support Google APIs AND Vanilla Android
        var target = shelljs.grep(/target=android-[\d+]/, path.join(ROOT, 'project.properties')) ||
          shelljs.grep(/target=Google Inc.:Google APIs:[\d+]/, path.join(ROOT, 'project.properties'));
        if(target == "" || !target) {
          // Try Google Glass APIs
          target = shelljs.grep(/target=Google Inc.:Glass Development Kit Preview:[\d+]/, path.join(ROOT, 'project.properties'));
        }
        return target.split('=')[1].replace('\n', '').replace('\r', '');
    }
}

// Returns a promise. Called only by build and clean commands.
module.exports.check_ant = function() {
    return tryCommand('ant -version', 'Failed to run "ant -version", make sure you have ant installed and added to your PATH.');
};

// Returns a promise. Called only by build and clean commands.
module.exports.check_gradle = function() {
    var sdkDir = process.env['ANDROID_HOME'];
    var wrapperDir = path.join(sdkDir, 'tools', 'templates', 'gradle', 'wrapper');
    if (!fs.existsSync(wrapperDir)) {
        return Q.reject(new Error('Could not find gradle wrapper within android sdk. Might need to update your Android SDK.\n' +
            'Looked here: ' + wrapperDir));
    }
    return Q.when();
};

// Returns a promise.
module.exports.check_java = function() {
    var javacPath = forgivingWhichSync('javac');
    var hasJavaHome = !!process.env['JAVA_HOME'];
    return Q().then(function() {
        if (hasJavaHome) {
            // Windows java installer doesn't add javac to PATH, nor set JAVA_HOME (ugh).
            if (!javacPath) {
                process.env['PATH'] += path.delimiter + path.join(process.env['JAVA_HOME'], 'bin');
            }
        } else {
            if (javacPath) {
                // OS X has a command for finding JAVA_HOME.
                if (fs.existsSync('/usr/libexec/java_home')) {
                    return tryCommand('/usr/libexec/java_home', 'Failed to run: /usr/libexec/java_home')
                    .then(function(stdout) {
                        process.env['JAVA_HOME'] = stdout.trim();
                    });
                } else {
                    // See if we can derive it from javac's location.
                    // fs.realpathSync is require on Ubuntu, which symplinks from /usr/bin -> JDK
                    var maybeJavaHome = path.dirname(path.dirname(fs.realpathSync(javacPath)));
                    if (fs.existsSync(path.join(maybeJavaHome, 'lib', 'tools.jar'))) {
                        process.env['JAVA_HOME'] = maybeJavaHome;
                    } else {
                        throw new Error('Could not find JAVA_HOME. Try setting the environment variable manually');
                    }
                }
            } else if (isWindows) {
                // Try to auto-detect java in the default install paths.
                var firstJdkDir =
                    shelljs.ls(process.env['ProgramFiles'] + '\\java\\jdk*')[0] ||
                    shelljs.ls('C:\\Program Files\\java\\jdk*')[0] ||
                    shelljs.ls('C:\\Program Files (x86)\\java\\jdk*')[0];
                if (firstJdkDir) {
                    // shelljs always uses / in paths.
                    firstJdkDir = firstJdkDir.replace(/\//g, path.sep);
                    if (!javacPath) {
                        process.env['PATH'] += path.delimiter + path.join(firstJdkDir, 'bin');
                    }
                    process.env['JAVA_HOME'] = firstJdkDir;
                }
            }
        }
    }).then(function() {
        var msg =
            'Failed to run "java -version", make sure your java environment is set up\n' +
            'including JDK and JRE.\n' +
            'Your JAVA_HOME variable is: ' + process.env['JAVA_HOME'];
        return tryCommand('java -version', msg)
    }).then(function() {
        msg = 'Failed to run "javac -version", make sure you have a Java JDK (not just a JRE) installed.';
        return tryCommand('javac -version', msg)
    });
}

// Returns a promise.
module.exports.check_android = function() {
    return Q().then(function() {
        var androidCmdPath = forgivingWhichSync('android');
        var adbInPath = !!forgivingWhichSync('adb');
        var hasAndroidHome = !!process.env['ANDROID_HOME'] && fs.existsSync(process.env['ANDROID_HOME']);
        if (hasAndroidHome && !androidCmdPath) {
            process.env['PATH'] += path.delimiter + path.join(process.env['ANDROID_HOME'], 'tools');
        }
        if (androidCmdPath && !hasAndroidHome) {
            var parentDir = path.dirname(androidCmdPath);
            if (path.basename(parentDir) == 'tools') {
                process.env['ANDROID_HOME'] = path.dirname(parentDir);
                hasAndroidHome = true;
            }
        }
        if (hasAndroidHome && !adbInPath) {
            process.env['PATH'] += path.delimiter + path.join(process.env['ANDROID_HOME'], 'platform-tools');
        }
        if (!process.env['ANDROID_HOME']) {
            throw new Error('ANDROID_HOME is not set and "android" command not in your PATH. You must fulfill at least one of these conditions.');
        }
        if (!fs.existsSync(process.env['ANDROID_HOME'])) {
            throw new Error('ANDROID_HOME is set to a non-existant path: ' + process.env['ANDROID_HOME']);
        }
        // Check that the target sdk level is installed.
        return module.exports.check_android_target(module.exports.get_target());
    });
};

module.exports.check_android_target = function(valid_target) {
    var msg = 'Failed to run "android". Make sure you have the latest Android SDK installed, and that the "android" command (inside the tools/ folder) is added to your PATH.';
    return tryCommand('android list targets', msg)
    .then(function(output) {
        if (!output.match(valid_target)) {
            throw new Error('Please install Android target "' + valid_target + '".\n' +
                'Hint: Run "android" from your command-line to open the SDK manager.');
        }
    });
};

// Returns a promise.
module.exports.run = function() {
    return Q.all([this.check_java(), this.check_android()]);
}

