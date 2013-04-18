// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

var ROOT  = WScript.ScriptFullName.split('\\cordova\\lib\\cordova.js').join(''),
    shell = WScript.CreateObject("WScript.Shell"),
    fso   = WScript.CreateObject('Scripting.FileSystemObject');


// log to stdout or stderr
function Log(msg, error) {
    if (error) {
        WScript.StdErr.WriteLine(msg);
    }
    else {
        WScript.StdOut.WriteLine(msg);
    }
} 

// executes a commmand in the shell, returning stdout
function exec(command) {
    var oExec=shell.Exec(command);
    var output = new String();
    while (oExec.Status == 0) {
        if (!oExec.StdOut.AtEndOfStream) {
            var line = oExec.StdOut.ReadLine();
            output += line;
        }
        WScript.sleep(100);
    }
    return output;
}

// executes a command in the shell, returns stdout or stderr if error
function exec_out(command) {
    var oExec=shell.Exec(command);
    var output = new String();
    while (oExec.Status == 0) {
        if (!oExec.StdOut.AtEndOfStream) {
            var line = oExec.StdOut.ReadLine();
            // XXX: Change to verbose mode 
            // WScript.StdOut.WriteLine(line);
            output += line;
        }
        WScript.sleep(100);
    }
    //Check to make sure our scripts did not encounter an error
    if (!oExec.StdErr.AtEndOfStream) {
        var line = oExec.StdErr.ReadAll();
        return {'error' : true, 'output' : line};
    }
    return {'error' : false, 'output' : output};
}

// executes a commmand in the shell and outputs stdout and fails on stderr
function exec_verbose(command) {
    //Log("Command: " + command);
    var oShell=shell.Exec(command);
    while (oShell.Status == 0) {
        //Wait a little bit so we're not super looping
        WScript.sleep(100);
        //Print any stdout output from the script
        if (!oShell.StdOut.AtEndOfStream) {
            var line = oShell.StdOut.ReadLine();
            Log(line);
        }
    }
    //Check to make sure our scripts did not encounter an error
    if (!oShell.StdErr.AtEndOfStream) {
        var line = oShell.StdErr.ReadAll();
        Log(line, true);
        WScript.Quit(2);
    }
}

function get_devices() {
    var device_list = []
    var local_devices = shell.Exec("%comspec% /c adb devices").StdOut.ReadAll();
    if (local_devices.match(/\w+\tdevice/)) {
        devices = local_devices.split('\r\n');
        //format (ID DESCRIPTION)
        for (i in devices) {
            if (devices[i].match(/\w+\tdevice/) && !devices[i].match(/emulator/)) {
                device_list.push(devices[i].replace(/\t/, ' '));
            }
        }
    }
    return device_list
}

function list_devices() {
    var devices = get_devices();
    if (devices.length > 0) {
        for (i in devices) {
            Log(devices[i]);
        }
    }
    else {
        Log('No devices found, if your device is connected and not showing,');
        Log(' then try and install the drivers for your device.');
        Log(' http://developer.android.com/tools/extras/oem-usb.html');
    }

}

function get_emulator_images() {
    // discription contains all data recieved squashed onto one line
    var add_description = true;
    var avd_list = [];
    var local_emulators = shell.Exec("%comspec% /c android list avds").StdOut.ReadAll();
    if (local_emulators.match(/Name\:/)) {
        emulators = local_emulators.split('\n');
        //format (ID DESCRIPTION)
        var count = 0;
        var output = '';
        for (i in emulators) {
            if (emulators[i].match(/Name\:/)) {
                var emulator_name = emulators[i].replace(/\s*Name\:\s/, '') + ' ';
                if (add_description) {
                    count = 1;
                    output += emulator_name
                }
                else {
                    avd_list.push(emulator_name);
                }
            }
            // add description if indicated (all data squeezed onto one line)
            if (count > 0) {
                var emulator_description = emulators[i].replace(/\s*/g, '');
                if (count > 4) {
                    avd_list.push(output + emulator_description);
                    count = 0;
                    output = '';
                }
                else {
                    count++;
                    output += emulator_description + ' '
                }
            }
        }
    }
    return avd_list;
}

function list_emulator_images() {
    var images = get_emulator_images();
    if (images.length > 0) {
        for(i in images) {
            Log(images[i]);
        }
    }
    else {
        Log('No emulators found, if you would like to create an emulator follow the instructions');
        Log(' provided here : http://developer.android.com/tools/devices/index.html');
        Log(' Or run \'android create avd --name <name> --target <targetID>\' in on the command line.');
    }
}

function get_started_emulators() {
    var started_emulators = [];
    var local_devices = shell.Exec("%comspec% /c adb devices").StdOut.ReadAll();
    if (local_devices.match(/emulator/)) {
        devices = local_devices.split('\r\n');
        //format (ID DESCRIPTION)
        for (i in devices) {
            if (devices[i].match(/\w+\tdevice/) && devices[i].match(/emulator/)) {
                started_emulators.push(devices[i].replace(/\t/, ' '));
            }
        }
    }
    return started_emulators
}

function list_started_emulators() {
    var images = get_started_emulators();
    if (images.length > 0) {
        for(i in images) {
            Log(images[i]);
        }
    }
    else {
        Log('No started emulators found, if you would like to start an emulator call \'list-emulator-images\'');
        Log(' to get the name of an emulator and then start the emulator with \'start-emulator <Name>\'');
    }
}

function start_emulator(name) {  
    var emulators = get_emulator_images();
    var started_emulators = get_started_emulators();
    var num_started = started_emulators.length;
    var emulator_name;
    var started = false;
    if (name) {
        for (i in emulators) {
            if (emulators[i].substr(0,name.length) == name) {
                Log("Starting emulator : " + name);
                shell.Run("%comspec% /c start cmd /c emulator -avd " + name);
                //shell.Run("%comspec% /c start cmd /c emulator -cpu-delay 0 -no-boot-anim -cache %Temp%\cache -avd " + name);
                started = true;
            }
        }
    }
    else {
        if (emulators.length > 0 && started_emulators < 1) {
            emulator_name = emulators[0].split(' ', 1)[0];
            start_emulator(emulator_name);
            return;
        } else if (started_emulators.length > 0) {
            Log("Emulator already started : " + started_emulators[0].split(' ', 1));
            return;
        } else {
            Log("Error : unable to start emulator, ensure you have emulators availible by checking \'list-emulator-images\'", true);
            WScript.Quit(2);
        }
    }
    if (!started) {
        Log("Error : unable to start emulator, ensure you have emulators availible by checking \'list-emulator-images\'", true);
        WScript.Quit(2);
    }
    else { // wait for emulator to boot before returning
        WScript.Stdout.Write('Booting up emulator..');
        var boot_anim = null;
        var emulator_ID = null;
        var new_started = get_started_emulators();
        var i = 0;
        // use boot animation property to tell when boot is complete.
        while ((boot_anim == null || !boot_anim.output.match(/stopped/)) && i < 100) {
            if (new_started.length > started_emulators.length && emulator_ID == null) {
                // find new emulator that was just started to get it's ID
                for(var i = 0; i < new_started.length; i++) {
                    if (new_started[i] != started_emulators[i]) {
                        emulator_ID = new_started[i].split(' ', 1)[0];
                        boot_anim = exec_out('%comspec% /c adb -s ' + emulator_ID + ' shell getprop init.svc.bootanim');
                        break;
                    }
                }
            }
            else if (boot_anim == null) {
                new_started = get_started_emulators(); 
            }
            else {
                boot_anim = exec_out('%comspec% /c adb -s ' + emulator_ID + ' shell getprop init.svc.bootanim'); 
            }
            i++;
            WScript.Stdout.Write('.');
            WScript.Sleep(2000);
        }
        if (i < 100) {
            Log('\nBoot Complete!');
        } else {
             Log('\nEmulator boot timed out. Failed to load emulator');
             WScript.Quit(2);
        }
    }
}

function install_device(target) {
    var devices = get_devices();
    var use_target = false;
    if (devices.length < 1) {
        Log("Error : No devices found to install to, make sure there are devices", true);
        Log(" availible by checking \'<project_dir>\\cordova\\lib\\list-devices\'", true);
        WScript.Quit(2);
    }
    if (target) {
        var exists = false;
        for (i in devices) {
            if (devices[i].substr(0,target.length) == target)
            {
                exists = true;
                break;
            }
        }
        if (!exists) {
            Log("Error : Unable to find target " + target, true);
            Log("Please ensure the target exists by checking \'<project>\\cordova\\lib\\list-devices'");
            WScript.Quit(2);
        }
        use_target = true;
    }
    // check if file .apk has been created
    if (fso.FolderExists(ROOT + '\\bin')) {
        var path_to_apk;
        var out_folder = fso.GetFolder(ROOT + '\\bin');
        var out_files = new Enumerator(out_folder.Files);
        for (;!out_files.atEnd(); out_files.moveNext()) {
            var path = out_files.item() + '';
            if (fso.GetExtensionName(path) == 'apk' && !path.match(/unaligned/)) {
                path_to_apk = out_files.item();
                break;
            }
        }
        if (path_to_apk) {
            var launch_name = exec_out("%comspec% /c java -jar "+ROOT+"\\cordova\\appinfo.jar "+ROOT+"\\AndroidManifest.xml");
            if (launch_name.error) {
                Log("Failed to get application name from appinfo.jar + AndroidManifest : ", true);
                Log("Output : " + launch_name.output, true);
                WScript.Quit(2);
            }
            // install on device (-d)
            Log("Installing app on device...");
            var cmd;
            if (use_target) {
                cmd = '%comspec% /c adb -s ' + target + ' install -r ' + path_to_apk;
            } else {
                cmd = '%comspec% /c adb -s ' + devices[0].split(' ', 1)[0] + ' install -r ' + path_to_apk;
            }
            var install = exec_out(cmd);
            if ( install.error && install.output.match(/Failure/)) {
                Log("Error : Could not install apk to device : ", true);
                Log(install.output, true);
                WScript.Quit(2);
            }
            else {
                Log(install.output);
            }
            // run on device
            Log("Launching application...");
            cmd;
            if (use_target) {
                cmd = '%comspec% /c adb -s ' + target + ' shell am start -W -a android.intent.action.MAIN -n ' + launch_name.output;
            } else {
                cmd = '%comspec% /c adb -s ' + devices[0].split(' ', 1)[0] + ' shell am start -W -a android.intent.action.MAIN -n ' + launch_name.output;
            }
            exec_verbose(cmd);
        }
        else {
            Log('Failed to find apk, make sure you project is built and there is an ', true);
            Log(' apk in <project>\\bin\\.  To build your project use \'<project>\\cordova\\build\'', true);
            WScript.Quit(2);
        }
    }
}

function install_emulator(target) {
    var emulators = get_started_emulators();
    var use_target = false;
    if (emulators.length < 1) {
        Log("Error : No emulators found to install to, make sure there are emulators", true);
        Log(" availible by checking \'<project_dir>\\cordova\\lib\\list-started-emulators\'", true);
        WScript.Quit(2);
    }
    if (target) {
        var exists = false;
        for (i in emulators) {
            if (emulators[i].substr(0,target.length) == target)
            {
                exists = true;
                break;
            }
        }
        if (!exists) {
            Log("Error : Unable to find target " + target, true);
            Log("Please ensure the target exists by checking \'<project>\\cordova\\lib\\list-started-emulators'")
        }
        use_target = true;
    } else {
        target = emulators[0].split(' ', 1)[0];
        Log("Deploying to emulator : " + target);
    }
    // check if file .apk has been created
    if (fso.FolderExists(ROOT + '\\bin')) {
        var path_to_apk;
        var out_folder = fso.GetFolder(ROOT + '\\bin');
        var out_files = new Enumerator(out_folder.Files);
        for (;!out_files.atEnd(); out_files.moveNext()) {
            var path = out_files.item() + '';
            if (fso.GetExtensionName(path) == 'apk' && !path.match(/unaligned/)) {
                path_to_apk = out_files.item();
                break;
            }
        }
        if (path_to_apk) {
            var launch_name = exec_out("%comspec% /c java -jar "+ROOT+"\\cordova\\appinfo.jar "+ROOT+"\\AndroidManifest.xml");
            if (launch_name.error) {
                Log("Failed to get application name from appinfo.jar + AndroidManifest : ", true);
                Log("Output : " + launch_name.output, true);
                WScript.Quit(2);
            }
            // install on emulator (-e)
            Log("Installing app on emulator...");
            var cmd = '%comspec% /c adb -s ' + target + ' install -r ' + path_to_apk;
            var install = exec_out(cmd);
            if ( install.error && install.output.match(/Failure/)) {
                Log("Error : Could not install apk to emulator : ", true);
                Log(install.output, true);
                WScript.Quit(2);
            }
            else {
                Log(install.output);
            }
            // run on emulator
            Log("Launching application...");
            cmd;
            if (use_target) {
                cmd = '%comspec% /c adb -s ' + target + ' shell am start -W -a android.intent.action.MAIN -n ' + launch_name.output;
            } else {
                cmd = '%comspec% /c adb -s ' + emulators[0].split(' ', 1)[0] + ' shell am start -W -a android.intent.action.MAIN -n ' + launch_name.output
            }
            exec_verbose(cmd);
        }
        else {
            Log('Failed to find apk, make sure you project is built and there is an ', true);
            Log(' apk in <project>\\bin\\.  To build your project use \'<project>\\cordova\\build\'', true);
            WScript.Quit(2);
        }
    }
    else {
        Log('Failed to find apk, make sure you project is built and there is an ', true);
        Log(' apk in <project>\\bin\\.  To build your project use \'<project>\\cordova\\build\'', true);
        WScript.Quit(2);
    }
}

function clean() {
    Log("Cleaning project...");
    exec("%comspec% /c ant.bat clean -f "+ROOT+"\\build.xml 2>&1");
}

function build(build_type) {
    if (build_type) {
        switch (build_type) {
            case "--debug" :
                clean();
                Log("Building project...");
                exec_verbose("%comspec% /c ant.bat debug -f "+ROOT+"\\build.xml 2>&1");
                break;
            case "--release" :
                clean();
                Log("Building project...");
                exec_verbose("%comspec% /c ant.bat release -f "+ROOT+"\\build.xml 2>&1");
                break;
            case "--nobuild" :
                Log("Skipping build process.");
                break;
            default :
                Log("Build option not recognized: " + build_type, true);
                WScript.Quit(2);
                break;
        }
    }
    else {
        Log("WARNING: [ --debug | --release | --nobuild ] not specified, defaulting to --debug.");
        exec_verbose("%comspec% /c ant.bat debug -f "+ROOT+"\\build.xml 2>&1");
    }
}

function log() {
    // filter out nativeGetEnabledTags spam from latest sdk bug.
    shell.Run("%comspec% /c adb logcat | grep -v nativeGetEnabledTags");
}

function run(target, build_type) {
    var use_target = false;
    if (!target) {
        Log("WARNING: [ --target=<ID> | --emulator | --device ] not specified, using defaults");
    }
    // build application
    build(build_type);
    // attempt to deploy to connected device 
    var devices = get_devices();
    if (devices.length > 0 || target == "--device") {
        if (target) {
            if (target.substr(0,9) == "--target=") {
                install_device(target.split('--target=').join(''))
            } else if (target == "--device") {
                install_device();
            } else {
                Log("Did not regognize " + target + " as a run option.", true);
                WScript.Quit(2);
            }
        }
        else {
            Log("WARNING: [ --target=<ID> | --emulator | --device ] not specified, using defaults");
            install_device();
        }
    }
    else {
        var emulators = get_started_emulators();
        if (emulators.length > 0) {
            install_emulator();
        }
        else {
            var emulator_images = get_emulator_images();
            if (emulator_images.length < 1) {
                Log('No emulators found, if you would like to create an emulator follow the instructions', true);
                Log(' provided here : http://developer.android.com/tools/devices/index.html', true);
                Log(' Or run \'android create avd --name <name> --target <targetID>\' in on the command line.', true);
                WScript.Quit(2);
            }
            start_emulator(emulator_images[0].split(' ')[0]);
            emulators = get_started_emulators();
            if (emulators.length > 0) {
                install_emulator();
            }
            else {
                Log("Error : emulator failed to start.", true);
                WScript.Quit(2);
            }
        }
    }
}

var args = WScript.Arguments;
if (args.count() == 0) {
    Log("Error: no args provided.");
    WScript.Quit(2);
}
else {
    if (args(0) == "build") {
        if (args.Count() > 1) {
            build(args(1))
        } else {
            build();
        }
    } else if (args(0) == "clean") {
        clean();
    } else if (args(0) == "list-devices") {
        list_devices();
    } else if (args(0) == "list-emulator-images") {
        list_emulator_images();
    } else if (args(0) == "list-started-emulators") {
        list_started_emulators();
    } else if (args(0) == "start-emulator") {
        if (args.Count() > 1) {
            start_emulator(args(1))
        } else {
            start_emulator();
        }
    } else if (args(0) == "log") {
        log();
    } else if (args(0) == "install-emulator") {
        if (args.Count() == 2) {
            if (args(1).substr(0,9) == "--target=") {
                install_emulator(args(1).split('--target=').join(''));
            } else {
                Log('Error: \"' + args(1) + '\" is not recognized as an install option', true);
                WScript.Quit(2);
            }
        } else {
            install_emulator();
        }
    } else if (args(0) == "install-device") {
        if (args.Count() == 2) {
            if (args(1).substr(0,9) == "--target=") {
                install_device(args(1).split('--target=').join(''));
            } else {
                Log('Error: \"' + args(1) + '\" is not recognized as an install option', true);
                WScript.Quit(2);
            }
        } else {
            install_device();
        }
    } else if (args(0) == "run") {
        if (args.Count() == 3) {
            run(args(1), args(2));
        }
        else if (args.Count() == 2) {
            if (args(1).substr(0,9) == "--target=" ||
               args(1) == "--emulator" ||
               args(1) == "--device") {
                run(args(1));
            } else if (args(1) == "--debug" ||
                       args(1) == "--release" ||
                       args(1) == "--nobuild") {
                run(null, args(1))
            } else {
                Log('Error: \"' + args(1) + '\" is not recognized as a run option', true);
                WScript.Quit(2);
            }
        }
        else {
            run();
        }
    } else {
        Log('Error: \"' + args(0) + '\" is not recognized as a tooling command', true);
        WScript.Quit(2);
    }
}

