/*
 * create a cordova/android project
 *
 * USAGE
 *  ./create [path package activity]
 */

function read(filename) {
    var fso=WScript.CreateObject("Scripting.FileSystemObject");
    var f=fso.OpenTextFile(filename, 1, true);
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
function exec(s) {
    var o=shell.Exec(s);
}

var args = WScript.Arguments, PROJECT_PATH="example", 
    PACKAGE="org.apache.cordova.example", ACTIVITY="cordovaExample",
    shell=WScript.CreateObject("WScript.Shell");

if (args.Count() == 3) {
    WScript.Echo('Found expected arguments');
    PROJECT_PATH=args(0);
    PACKAGE=args(1);
    ACTIVITY=args(2);
}

var PACKAGE_AS_PATH=PACKAGE.replace(/\./g, '\\');
var ACTIVITY_PATH=PROJECT_PATH+'\\src\\'+PACKAGE_AS_PATH+'\\'+ACTIVITY+'.java';
var MANIFEST_PATH=PROJECT_PATH+'\\AndroidManifest.xml';
var TARGET=shell.Exec('android.bat list targets').StdOut.ReadAll().match(/id:\s([0-9]).*/)[1];
var VERSION=read('VERSION').replace(/\r\n/,'').replace(/\n/,'');

// clobber any existing example

/*
if [ $# -eq 0 ]
then
    rm -rf $PROJECT_PATH
fi
*/

// create the project
exec('android.bat create project --target '+TARGET+' --path '+PROJECT_PATH+' --package '+PACKAGE+' --activity '+ACTIVITY);

// update the cordova framework project to a target that exists on this machine
exec('android.bat update project --target '+TARGET+' --path framework');

// compile cordova.js and cordova.jar
// if you see an error about "Unable to resolve target" then you may need to 
// update your android tools or install an additional Android platform version
exec('ant.bat -f framework\\build.xml jar');

// copy in the project template
exec('cmd /c xcopy bin\\templates\\project '+PROJECT_PATH+' /S /Y');

// copy in cordova.js
exec('cmd /c copy framework\\assets\\www\\cordova-'+VERSION+'.js '+PROJECT_PATH+'\\assets\\www\\cordova-'+VERSION+'.js /Y');

// copy in cordova.jar
exec('cmd /c copy framework\\cordova-'+VERSION+'.jar '+PROJECT_PATH+'\\libs\\cordova-'+VERSION+'.jar /Y');

// copy in default activity
exec('cmd /c copy bin\\templates\\Activity.java '+ACTIVITY_PATH+' /Y');

// interpolate the activity name and package
replaceInFile(ACTIVITY_PATH, /__ACTIVITY__/, ACTIVITY);
replaceInFile(ACTIVITY_PATH, /__ID__/, PACKAGE);

replaceInFile(MANIFEST_PATH, /__ACTIVITY__/, ACTIVITY);
replaceInFile(MANIFEST_PATH, /__PACKAGE__/, PACKAGE);

/*
# leave the id for launching
touch $PROJECT_PATH/package-activity
echo $PACKAGE/$PACKAGE.$ACTIVITY >  $PROJECT_PATH/package-activity 
*/