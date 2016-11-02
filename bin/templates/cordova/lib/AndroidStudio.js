/*
 *  This is a simple routine that checks if project is an Android Studio Project
 *
 *  @param {String} root Root folder of the project
 */

/*jshint esnext: false */

var path = require('path');
var fs = require('fs');

module.exports.isAndroidStudioProject = function isAndroidStudioProject(root) {
    var eclipseFiles = ['AndroidManifest.xml', 'libs', 'res', 'project.properties', 'platform_www'];
    var androidStudioFiles = ['app', 'gradle', 'build', 'app/src/main/assets'];

    // assume it is an AS project and not an Eclipse project
    var isEclipse = false;
    var isAS = true;

    // if any of the following exists, then we are not an ASProj
    eclipseFiles.forEach(function(file) {
        if(fs.existsSync(path.join(root, file))) {
            isEclipse = true;
        }
    });

    // if it is NOT an eclipse project, check that all required files exist
    if(!isEclipse) {
        androidStudioFiles.forEach(function(file){
            if(!fs.existsSync(path.join(root, file))) {
                isAS = false;
            }
        });
    }
    return (!isEclipse && isAS);
};
