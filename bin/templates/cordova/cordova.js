var ROOT = WScript.ScriptFullName.split('\\bin\\templates\\cordova\\cordova.js').join(''),
    shell=WScript.CreateObject("WScript.Shell");

function exec(command) {
    var oExec=shell.Exec(command);
    var output = '';
    while (!oExec.StdOut.AtEndOfStream) {
        output += oExec.StdOut.ReadLine();
    }
    return output;
}

function devices_running() {
    var local_devices = exec("adb devices");
    if(local_devices.match(/device |emulator /)) {
        return true;
    }
    return false;
}
function emulate() {
    // don't run emulator if a device is plugged in or if emulator is already running
    if(devices_running()) {
        WScript.Echo("Device or Emulator already running!");
        return;
    }
    var oExec = shell.Exec("android.bat list avd");
    var avd_list = [];
    var avd_id = -10;
    while(!oExec.StdOut.AtEndOfStream) {
        var output = oExec.StdOut.ReadLine();
        if(output.match(/Name: (.)*/)) {
            avd_list.push(output.replace(/\sName:\s/, ""));
        }
    }
    // user has no AVDs
    if(avd_list.length == 0) {
        WScript.Echo("You don't have any Android Virtual Devices. Please create at least one AVD.");
        WScript.Echo("android");
        WScript.Quit(1);
    }
    // user has only one AVD so we launch that one
    if(avd_list.length == 1) {

        exec("emulator.bat -cpu-delay 0 -no-boot-anim -cache /tmp/cache -avd "+avd_list[0]+" > NUL");
    }

    // user has more than one avd so we ask them to choose
    if(avd_list.length > 1) {
        while(!avd_list[avd_id]) {
            WScript.Echo("Choose from one of the following Android Virtual Devices [0 to "+(avd_list.length - 1)+"]:")
            for(i = 0, j = avd_list.length ; i < j ; i++) {
                WScript.Echo((i)+") "+avd_list[i]);
            }
            WScript.StdOut.Write("> ");
            avd_id = new Number(WScript.StdIn.ReadLine());
        }

        WScript.Echo("emulator.bat -cpu-delay 0 -no-boot-anim -cache /tmp/cache -avd "+avd_list[avd_id]+" > NUL");
    }
}

function clean() {
    WScript.Echo(exec("ant.bat clean"));
}

function debug() {
    WScript.Echo(exec("ant.bat debug"));
}

function debug_install() {
    WScript.Echo(exec("ant.bat debug install"));
}

function log() {
    WScript.Echo(exec("adb.bat logcat"));
}

function launch() {
    var launch_str=exec("java -jar "+ROOT+"\\cordova\\appinfo.jar "+ROOT+"\\AndroidManifest.xml")
    exec("adb.bat shell am start -n "+launch_str);
}

function BOOM() {
   clean(); 
   if(devices_running()) {
        debug_install();
        launch();
   } else {
        debug();
        WScript.Echo("##################################################################");
        WScript.Echo("# Plug in your device or launch an emulator with cordova/emulate #");
        WScript.Echo("##################################################################");
   }
}

emulate();
