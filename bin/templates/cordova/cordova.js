var ROOT = WScript.ScriptFullName.split('\\cordova\\cordova.js').join(''),
    shell=WScript.CreateObject("WScript.Shell");

function exec(command) {
    var oExec=shell.Exec(command);
    var output = new String();
    while(oExec.Status == 0) {
        if(!oExec.StdOut.AtEndOfStream) {
            var line = oExec.StdOut.ReadLine();
            // XXX: Change to verbose mode 
            // WScript.StdOut.WriteLine(line);
            output += line;
        }
        WScript.sleep(100);
    }

    return output;
}

function emulator_running() {
    var local_devices = shell.Exec("%comspec% /c adb devices").StdOut.ReadAll();
    if(local_devices.match(/emulator/)) {
        return true;
    }
    return false;
}
function emulate() {
    // don't run emulator if a device is plugged in or if emulator is already running
    if(emulator_running()) {
        WScript.Echo("Device or Emulator already running!");
        return;
    }
    var oExec = shell.Exec("%comspec% /c android.bat list avd");
    var avd_list = [];
    var avd_id = -10;
    while(!oExec.StdOut.AtEndOfStream) {
        var output = oExec.StdOut.ReadLine();
        if(output.match(/Name: (.)*/)) {
            avd_list.push(output.replace(/ *Name:\s/, ""));
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

        shell.Run("emulator -cpu-delay 0 -no-boot-anim -cache %Temp%\cache -avd "+avd_list[0]);
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

        shell.Run("emulator -cpu-delay 0 -no-boot-anim -cache %Temp%\\cache -avd "+avd_list[avd_id], 0, false);
    }
}

function clean() {
    exec("%comspec% /c ant.bat clean -f "+ROOT+"\\build.xml 2>&1");
}

function debug() {
   if(emulator_running()) {
        exec("%comspec% /c ant.bat debug install -f "+ROOT+"\\build.xml 2>&1");
   } else {
        exec("%comspec% /c ant.bat debug -f "+ROOT+"\\build.xml 2>&1");
        WScript.Echo("##################################################################");
        WScript.Echo("# Plug in your device or launch an emulator with cordova/emulate #");
        WScript.Echo("##################################################################");
   }
}

function log() {
    shell.Run("%comspec% /c adb logcat");
}

function launch() {
    var launch_str=exec("%comspec% /c java -jar "+ROOT+"\\cordova\\appinfo.jar "+ROOT+"\\AndroidManifest.xml");
    //WScript.Echo(launch_str);
    exec("%comspec% /c adb shell am start -n "+launch_str+" 2>&1");
}

function BOOM() {
   clean();
   debug();
   launch();
}
var args = WScript.Arguments;
if(args.count() != 1) {
    WScript.StdErr.Write("An error has occured!\n");
    WScript.Quit(1);
}
eval(args(0)+"()");
