var build_path = __dirname + '/../..'
    project_path = process.env.Temp + '\\example',
    package_name = 'org.apache.cordova.example',
    package_as_path = 'org/apache/cordova/example',
    project_name = 'cordovaExample';

var path = require('path'),
    fs = require('fs'),
    util = require('util'),
    assert = require('assert'),
    exec = require('child_process').exec,
    spawn = require('child_process').spawn;

var version = fs.readFileSync(build_path + '/VERSION').toString().replace('\r\n', '');

assert(version !== undefined);
assert(version !== '');

process.on('uncaughtException', function (err) {
    console.log('Caught exception: ' + err);
    exec('rd /s /q ' + project_path);
});

var create_project = spawn('cscript',
                           [build_path + '/bin/create.js',
                            project_path,
                            package_name,
                            project_name]
                          );

create_project.stderr.on('data', function (data) {
  console.log('ps stderr: ' + data);
});

create_project.stderr.on('data', function(data) {
    console.log(data.toString());
});

create_project.stdout.on('data', function(data) {
    console.log(data.toString());
});

create_project.on('exit', function(code) {
    assert.equal(code, 0, 'Project did not get created');

    // make sure the project was created
    path.exists(project_path, function(exists) {
        assert(exists, 'Project path does not exist');
    });

    // make sure the build directory was cleaned up
   // path.exists(build_path + '/framework/libs', function(exists) {
   //     assert(!exists, 'libs directory did not get cleaned up');
   // });
    path.exists(build_path + util.format('/framework/assets/cordova-%s.js', version), function(exists) {
        assert(!exists, 'javascript file did not get cleaned up');
    });
    path.exists(build_path + util.format('/framework/cordova-%s.jar', version), function(exists) {
        assert(!exists, 'jar file did not get cleaned up');
    });

    // make sure AndroidManifest.xml was added
    path.exists(util.format('%s/AndroidManifest.xml', project_path), function(exists) {
        assert(exists, 'AndroidManifest.xml did not get created');
        // TODO check that the activity name was properly substituted
    });

    // make sure main Activity was added 
    path.exists(util.format('%s/src/%s/%s.java', project_path, package_as_path, project_name), function(exists) {
        assert(exists, 'Activity did not get created');
        // TODO check that package name and activity name were substitued properly
    });
   
    // make sure plugins.xml was added
    path.exists(util.format('%s/res/xml/plugins.xml', project_path), function(exists) {
        assert(exists, 'plugins.xml did not get created');
    });
    
    // make sure cordova.xml was added
    path.exists(util.format('%s/res/xml/cordova.xml', project_path), function(exists) {
        assert(exists, 'plugins.xml did not get created');
    });
    
    // make sure cordova.jar was added
    path.exists(util.format('%s/libs/cordova-%s.jar', project_path, version), function(exists) {
        assert(exists, 'cordova.jar did not get added');
    });
    
    // make sure cordova.js was added
    path.exists(util.format('%s/assets/www/cordova-%s.js', project_path, version), function(exists) {
        assert(exists, 'cordova.js did not get added');
    });
    
    // check that project compiles && creates a cordovaExample-debug.apk
    // XXX: !@##!@# WINDOWS
    exec('ant debug -f ' + project_path + "\\build.xml", function(error, stdout, stderr) {
        assert(error == null, "Cordova Android Project does not compile");
        path.exists(util.format('%s/bin/%s-debug.apk', project_path, project_name), 
                    function(exists) {
                        assert(exists, 'Package did not get created');
                        // if project compiles properly just AXE it
                        exec('rd /s /q ' + project_path);
                    });
    });


});

