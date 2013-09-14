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

var shell = require('shelljs'),
    path  = require('path'),
    fs    = require('fs'),
    ROOT  = path.join(__dirname, '..', '..');

// Get valid target from framework/project.properties
module.exports.get_target = function() {
    if(fs.existsSync(path.join(ROOT, 'framework', 'project.properties'))) {
        var target = shell.grep(/target=android-[\d+]/, path.join(ROOT, 'framework', 'project.properties'));
        return target.split('=')[1].replace('\n', '').replace('\r', '').replace(' ', '');
    } else if (fs.existsSync(path.join(ROOT, 'project.properties'))) {
        // if no target found, we're probably in a project and project.properties is in ROOT.
        var target = shell.grep(/target=android-[\d+]/, path.join(ROOT, 'project.properties'));
        return target.split('=')[1].replace('\n', '').replace('\r', '').replace(' ', '');
    }
}

module.exports.check_ant = function() {
    var test = shell.exec('ant -version', {silent:true, async:false});
    if(test.code > 0) {
        console.error('ERROR : executing command \'ant\', make sure you have ant installed and added to your path.');
        return false;
    }
    return true;
}

module.exports.check_java = function() {
    if(process.env.JAVA_HOME) {
        var test = shell.exec('java', {silent:true, async:false});
        if(test.code > 0) {
            console.error('ERROR : executing command \'java\', make sure you java environment is set up. Including your JDK and JRE.');
            return false;
        }
        return true;
    } else {
        console.error('ERROR : Make sure JAVA_HOME is set, as well as paths to your JDK and JRE for java.');
        return false;
    }
}

module.exports.check_android = function() {
    var valid_target = this.get_target();
    var targets = shell.exec('android list targets', {silent:true, async:false});

    if(targets.code > 0 && targets.output.match(/command\snot\sfound/)) {
        console.error('The command \"android\" failed. Make sure you have the latest Android SDK installed, and the \"android\" command (inside the tools/ folder) is added to your path.');
        return false;
    } else if(!targets.output.match(valid_target)) {
        console.error('Please install Android target ' + valid_target.split('-')[1] + ' (the Android newest SDK). Make sure you have the latest Android tools installed as well. Run \"android\" from your command-line to install/update any missing SDKs or tools.');
        return false;
    }
    return true;
}

module.exports.run = function() {
    return this.check_ant() && this.check_java && this.check_android();
}
