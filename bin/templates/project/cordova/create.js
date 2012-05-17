var shell=WScript.CreateObject("WScript.Shell");

function exec(s, output) {
    WScript.Echo('Executing ' + s);
    var o=shell.Exec(s);
    while (o.Status == 0) {
        WScript.Sleep(100);
    }
    WScript.Echo("Command exited with code " + o.Status);
}
function read(filename) {
    var fso=WScript.CreateObject("Scripting.FileSystemObject");
    var f=fso.OpenTextFile(filename, 1);
    var s=f.ReadAll();
    f.Close();
    return s;
}
function write(filename, contents) {
    var fso=WScript.CreateObject("Scripting.FileSystemObject");
    var f=fso.OpenTextFile(filename, 2, true);
    f.Write(contents);
    f.Close();
}
function replaceInFile(filename, regexp, replacement) {
    write(filename, read(filename).replace(regexp, replacement));
}

// working dir
var PWD = WScript.ScriptFullName.split('\\cordova\\create.js').join('');

var fso=WScript.CreateObject("Scripting.FileSystemObject");
var f=fso.OpenTextFile(PWD + '\\.cordova\\config', 1);
while (!f.AtEndOfStream) {
  var prop = f.ReadLine().split('=');
  var line = 'var ' + prop[0] + '=' + "'" + prop[1] + "';";
  eval(line); // hacky shit to load config but whatevs
}

var PACKAGE_AS_PATH=PACKAGE.replace(/\./g, '\\');
var ACTIVITY_PATH=PWD+'\\src\\'+PACKAGE_AS_PATH+'\\'+ACTIVITY+'.java';
var MANIFEST_PATH=PWD+'\\AndroidManifest.xml';

exec('android.bat create project --target ' + TARGET + ' --path ' + PWD + ' --package ' + PACKAGE + ' --activity ' + ACTIVITY);

// copy in activity and other android assets
exec('cmd /c xcopy ' + PWD + '\\cordova\\templates\\project\\* ' + PWD +' /Y /S');

// copy in cordova.js
exec('cmd /c copy ' + PWD + '\\.cordova\\android\\cordova-' + VERSION + '.js ' + PWD + '\\assets\\www /Y');

// copy in cordova.jar
exec('cmd /c copy ' + PWD + '\\.cordova\\android\\cordova-' + VERSION + '.jar ' + PWD + '\\libs /Y');

// copy in res/xml
exec('cmd /c md ' + PWD + '\\res\\xml');
exec('cmd /c copy ' + PWD + '\\.cordova\\android\\cordova.xml ' + PWD + '\\res\\xml /Y');
exec('cmd /c copy ' + PWD + '\\.cordova\\android\\plugins.xml ' + PWD + '\\res\\xml /Y');

// copy in default activity
exec('cmd /c copy ' + PWD + '\\cordova\\templates\\Activity.java ' + ACTIVITY_PATH + ' /Y');

// interpolate the activity name and package
replaceInFile(ACTIVITY_PATH, /__ACTIVITY__/, ACTIVITY);
replaceInFile(ACTIVITY_PATH, /__ID__/, PACKAGE);

replaceInFile(MANIFEST_PATH, /__ACTIVITY__/, ACTIVITY);
replaceInFile(MANIFEST_PATH, /__PACKAGE__/, PACKAGE);

WScript.Echo('Create completed successfully.');