/*
 *  This is a simple routine that checks if project is an Android Studio Project
 *
 *  @param {String} root Root folder of the project
 */

/*jshint esversion: 6 */

var path = require('path');
var fs = require('fs');

function isAndroidStudioProject(root) {
    var eclipseFiles = ['AndroidManifest.xml', 'libs', 'res', 'project.properties', 'platform_www'];
    var androidStudioFiles = ['app', 'gradle', 'build', 'app/src/main/assets'];
    var file;
    for(file of eclipseFiles) {
      if(fs.existsSync(path.join(root, file))) {
        return false;
      }
    }
    for(file of androidStudioFiles) {
      if(!fs.existsSync(path.join(root, file))) {
        return false;
      }
    }
    return true;
}

module.exports.isAndroidStudioProject = isAndroidStudioProject;
