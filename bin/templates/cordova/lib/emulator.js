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
    appinfo = require('./appinfo'),
    build = require('./build'),
    ROOT  = path.join(__dirname, '..', '..'),
    new_emulator = 'cordova_emulator';

/**
 * Returns a list of emulator images in the form of objects
 * {
       name   : <emulator_name>,
       path   : <path_to_emulator_image>,
       target : <api_target>,
       abi    : <cpu>,
       skin   : <skin>
   }
 */
module.exports.list_images = function() {
    var cmd = 'android list avds';
    var result = shell.exec(cmd, {silent:true, async:false});
    if (result.code > 0) {
        console.error('Failed to execute android command \'' + cmd + '\'.');
        process.exit(2);
    } else {
        var response = result.output.split('\n');
        var emulator_list = [];
        for (var i = 1; i < response.length; i++) {
            // To return more detailed information use img_obj
            var img_obj = {};
            if (response[i].match(/Name:\s/)) {
                img_obj['name'] = response[i].split('Name: ')[1].replace('\r', '');
                if (response[i + 1].match(/Path:\s/)) {
                    i++;
                    img_obj['path'] = response[i].split('Path: ')[1].replace('\r', '');
                }
                if (response[i + 1].match(/\(API\slevel\s/)) {
                    i++;
                    img_obj['target'] = response[i].replace('\r', '');
                }
                if (response[i + 1].match(/ABI:\s/)) {
                    i++;
                    img_obj['abi'] = response[i].split('ABI: ')[1].replace('\r', '');
                }
                if (response[i + 1].match(/Skin:\s/)) {
                    i++;
                    img_obj['skin'] = response[i].split('Skin: ')[1].replace('\r', '');
                }

                emulator_list.push(img_obj);
            }
            /* To just return a list of names use this
            if (response[i].match(/Name:\s/)) {
                emulator_list.push(response[i].split('Name: ')[1].replace('\r', '');
            }*/

        }
        return emulator_list;
    }
}

/**
 * Will return the closest avd to the projects target
 * or undefined if no avds exist.
 */
module.exports.best_image = function() {
    var project_target = this.get_target().replace('android-', '');
    var images = this.list_images();
    var closest = 9999;
    var best = images[0];
    for (i in images) {
        var target = images[i].target;
        if(target) {
            var num = target.split('(API level ')[1].replace(')', '');
            if (num == project_target) {
                return images[i];
            } else if (project_target - num < closest && project_target > num) {
                var closest = project_target - num;
                best = images[i];
            }
        }
    }
    return best;
}

module.exports.list_started = function() {
    var cmd = 'adb devices';
    var result = shell.exec(cmd, {silent:true, async:false});
    if (result.code > 0) {
        console.error('Failed to execute android command \'' + cmd + '\'.');
        process.exit(2);
    } else {
        var response = result.output.split('\n');
        var started_emulator_list = [];
        for (var i = 1; i < response.length; i++) {
            if (response[i].match(/device/) && response[i].match(/emulator/)) {
                started_emulator_list.push(response[i].replace(/\tdevice/, '').replace('\r', ''));
            }
        }
        return started_emulator_list;
    }
}

module.exports.get_target = function() {
    var target = shell.grep(/target=android-[\d+]/, path.join(ROOT, 'project.properties'));
    return target.split('=')[1].replace('\n', '').replace('\r', '').replace(' ', '');
}

module.exports.list_targets = function() {
    var target_out = shell.exec('android list targets', {silent:true, async:false}).output.split('\n');
    var targets = [];
    for (var i = target_out.length; i >= 0; i--) {
        if(target_out[i].match(/id:/)) {
            targets.push(targets[i].split(' ')[1]);
        }
    }
    return targets;
}

/*
 * Starts an emulator with the given ID,
 * and returns the started ID of that emulator.
 * If no ID is given it will used the first image availible,
 * if no image is availible it will error out (maybe create one?).
 */
module.exports.start = function(emulator_ID) {
    var started_emulators = this.list_started();
    var num_started = started_emulators.length;
    if (typeof emulator_ID === 'undefined') {
        var emulator_list = this.list_images();
        if (emulator_list.length > 0) {
            emulator_ID = this.best_image().name;
            console.log('WARNING : no emulator specified, defaulting to ' + emulator_ID);
        } else {
            console.error('ERROR : No emulator images (avds) found, if you would like to create an');
            console.error(' avd follow the instructions provided here : ');
            console.error(' http://developer.android.com/tools/devices/index.html')
            console.error(' Or run \'android create avd --name <name> --target <targetID>\' ');
            console.error(' in on the command line.');
            process.exit(2);
            /*console.log('WARNING : no emulators availible, creating \'' + new_emulator + '\'.');
            this.create_image(new_emulator, this.get_target());
            emulator_ID = new_emulator;*/
        }
    }

    var pipe_null = (process.platform == 'win32' || process.platform == 'win64'? '> NUL' : '> /dev/null');
    var cmd = 'emulator -avd ' + emulator_ID + ' ' + pipe_null + ' &';
    if(process.platform == 'win32' || process.platform == 'win64') {
        cmd = '%comspec% /c start cmd /c ' + cmd;
    }
    var result = shell.exec(cmd, {silent:true, async:false}, function(code, output) {
        if (code > 0) {
            console.error('Failed to execute android command \'' + cmd + '\'.');
            console.error(output);
            process.exit(2);
        }
    });

    // wait for emulator to start
    console.log('Waiting for emulator...');
    var new_started = this.wait_for_emulator(num_started);
    var emulator_id;
    if (new_started.length > 1) {
        for (i in new_started) {
            console.log(new_started[i]);
            console.log(started_emulators.indexOf(new_started[i]));
            if (started_emulators.indexOf(new_started[i]) < 0) {
                emulator_id = new_started[i];
            }
        }
    } else {
        emulator_id = new_started[0];
    }
    if (!emulator_id) {
        console.error('ERROR :  Failed to start emulator, could not find new emulator');
        process.exit(2);
    }

    //wait for emulator to boot up
    process.stdout.write('Booting up emulator (this may take a while)...');
    this.wait_for_boot(emulator_id);
    console.log('BOOT COMPLETE');

    //unlock screen
    cmd = 'adb -s ' + emulator_id + ' shell input keyevent 82';
    shell.exec(cmd, {silent:false, async:false});

    //return the new emulator id for the started emulators
    return emulator_id;
}

/*
 * Waits for the new emulator to apear on the started-emulator list.
 */
module.exports.wait_for_emulator = function(num_running) {
    var new_started = this.list_started();
    if (new_started.length > num_running) {
        return new_started;
    } else {
        this.sleep(1);
        return this.wait_for_emulator(num_running);
    }
}

/*
 * Waits for the boot animation property of the emulator to switch to 'stopped'
 */
module.exports.wait_for_boot = function(emulator_id) {
    var cmd;
    // ShellJS opens a lot of file handles, and the default on OS X is too small.
    // TODO : This is not working, need to find a better way to increese the ulimit.
    if(process.platform == 'win32' || process.platform == 'win64') {
        cmd = 'adb -s ' + emulator_id + ' shell getprop init.svc.bootanim';
    } else {
        cmd = 'ulimit -S -n 4096; adb -s ' + emulator_id + ' shell getprop init.svc.bootanim';
    }
    var boot_anim = shell.exec(cmd, {silent:true, async:false});
    if (boot_anim.output.match(/stopped/)) {
        return;
    } else {
        process.stdout.write('.');
        this.sleep(3);
        return this.wait_for_boot(emulator_id);
    }
}

/*
 * TODO : find a better way to wait for the emulator (maybe using async methods?)
 */
module.exports.sleep = function(time_sec) {
    if (process.platform == 'win32' || process.platform == 'win64') {
        shell.exec('ping 127.0.0.1 -n ' + time_sec, {silent:true, async:false});
    } else {
        shell.exec('sleep ' + time_sec, {silent:true, async:false});
    }
}

/*
 * Create avd
 * TODO : Enter the stdin input required to complete the creation of an avd.
 */
module.exports.create_image = function(name, target) {
    console.log('Creating avd named ' + name);
    if (target) {
        var cmd = 'android create avd --name ' + name + ' --target ' + target;
        var create = shell.exec(cmd, {sient:false, async:false});
        if (create.error) {
            console.error('ERROR : Failed to create emulator image : ');
            console.error(' Do you have the latest android targets including ' + target + '?');
            console.error(create.output);
            process.exit(2);
        }
    } else {
        console.log('WARNING : Project target not found, creating avd with a different target but the project may fail to install.');
        var cmd = 'android create avd --name ' + name + ' --target ' + this.list_targets()[0];
        var create = shell.exec(cmd, {sient:false, async:false});
        if (create.error) {
            console.error('ERROR : Failed to create emulator image : ');
            console.error(create.output);
            process.exit(2);
        }
        console.error('ERROR : Unable to create an avd emulator, no targets found.');
        console.error('Please insure you have targets availible by runing the "android" command').
        process.exit(2);
    }
}

/*
 * Installs a previously built application on the emulator and launches it.
 * If no target is specified, then it picks one.
 * If no started emulators are found, error out.
 */
module.exports.install = function(target) {
    var emulator_list = this.list_started();
    if (emulator_list.length < 1) {
        console.error('ERROR : No started emulators found, please start an emultor before deploying your project.');
        process.exit(2);
        /*console.log('WARNING : No started emulators found, attemting to start an avd...');
        this.start(this.best_image().name);*/
    }
    // default emulator
    target = typeof target !== 'undefined' ? target : emulator_list[0];
    if (emulator_list.indexOf(target) > -1) {
        console.log('Installing app on emulator...');
        var apk_path = build.get_apk();
        var cmd = 'adb -s ' + target + ' install -r ' + apk_path;
        var install = shell.exec(cmd, {sient:false, async:false});
        if (install.error || install.output.match(/Failure/)) {
            console.error('ERROR : Failed to install apk to emulator : ');
            console.error(install.output);
            process.exit(2);
        }

        //unlock screen
        cmd = 'adb -s ' + target + ' shell input keyevent 82';
        shell.exec(cmd, {silent:true, async:false});

        // launch the application
        console.log('Launching application...');
        var launchName = appinfo.getActivityName();
        cmd = 'adb -s ' + target + ' shell am start -W -a android.intent.action.MAIN -n ' + launchName;
        console.log(cmd);
        var launch = shell.exec(cmd, {silent:false, async:false});
        if(launch.code > 0) {
            console.error('ERROR : Failed to launch application on emulator : ' + launch.error);
            console.error(launch.output);
            process.exit(2);
        } else {
            console.log('LANCH SUCCESS');
        }
    } else {
        console.error('ERROR : Unable to find target \'' + target + '\'.');
        console.error('Failed to deploy to emulator.');
        process.exit(2);
    }
}
