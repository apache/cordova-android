
/*jshint node: true*/

var Q = require('q');
var fs = require('fs');
var et = require('elementtree');
var path = require('path');
var glob = require('glob');
var shell = require('shelljs');
var events = new (require('events').EventEmitter)();

var runImpl = require('./lib/run');
var buildImpl = require('./lib/build');
var requirementsImpl = require('./lib/check_reqs');

var xml = require('./lib/xmlHelper');
var ConfigParser = require('./lib/ConfigParser');

function PlatformApi () {
    // Set up basic properties. They probably will be overridden if this API is used by cordova-lib
    this.root = path.join(__dirname, '..');
    this.platform = 'android';

    this.resources = path.join(this.root, 'res');
    this.strings = path.join(this.root, 'res', 'values', 'strings.xml');
    this.manifest = path.join(this.root, 'AndroidManifest.xml');

    if (this.constructor.super_){
        // This should only happen if this class is being instantiated from cordova-lib
        // In this case the arguments is being passed from cordova-lib as well,
        // so we don't need to care about whether they're correct ot not
        this.constructor.super_.apply(this, arguments);
    }
}

PlatformApi.prototype.build = function(context) {
    var options = context && context.options || [];
    return requirementsImpl
        .run(options)
        .then(function () {
            return buildImpl.run(options);
        });
};

PlatformApi.prototype.run = function(context) {
    var options = context && context.options || [];
    return requirementsImpl
        .run(options)
        .then(function () {
            return runImpl.run(options);
        });
};

PlatformApi.prototype.requirements = function () {
    return requirementsImpl.check_all();
};

PlatformApi.prototype.getWwwDir = function() {
    return path.join(this.root, 'assets', 'www');
};

PlatformApi.prototype.getConfigXml = function () {
    return path.join(this.root, 'res', 'xml', 'config.xml');
};

PlatformApi.prototype.updateProject = function (configSource) {
    var that = this;
    configSource = configSource || this.getConfigXml();
    return that.updateFromConfig(configSource)
    .then(function () {
        var overridesSource = path.join(path.dirname(configSource), 'merges');
        updateOverrides(overridesSource, that.getWwwDir());
        // delete any .svn folders copied over
        shell.rm('-rf', glob.sync(path.join(that.getWwwDir(), '**', '.svn')));
    });
};

PlatformApi.prototype.updateFromConfig = function(configSource) {
    var config = new ConfigParser(configSource);

    // Update app name by editing res/values/strings.xml
    var name = config.name();
    var strings = xml.parseElementtreeSync(this.strings);
    strings.find('string[@name="app_name"]').text = name;
    fs.writeFileSync(this.strings, strings.write({indent: 4}), 'utf-8');
    events.emit('verbose', 'Wrote out Android application name to "' + name + '"');

    handleSplashes(config, this.resources);
    handleIcons(config, this.resources);

    var manifest = xml.parseElementtreeSync(this.manifest);
    // Update the version by changing the AndroidManifest android:versionName
    var version = config.version();
    var versionCode = config.android_versionCode() || default_versionCode(version);
    manifest.getroot().attrib['android:versionName'] = version;
    manifest.getroot().attrib['android:versionCode'] = versionCode;

    // Update package name by changing the AndroidManifest id and moving the entry class around to the proper package directory
    var pkg = config.android_packageName() || config.packageName();
    pkg = pkg.replace(/-/g, '_'); // Java packages cannot support dashes
    var orig_pkg = manifest.getroot().attrib.package;
    manifest.getroot().attrib.package = pkg;

    var act = manifest.getroot().find('./application/activity');

    // TODO: Since PlatformApi can be instantiated outside of cordova project helper property can be unavailable.
    // Might better to move it to ConfigParser class.
    // Set the android:screenOrientation in the AndroidManifest
    var orientation = this.helper.getOrientation(config);
    if (orientation && !this.helper.isDefaultOrientation(orientation)) {
        act.attrib['android:screenOrientation'] = orientation;
    } else {
        delete act.attrib['android:screenOrientation'];
    }

    // Set android:launchMode in AndroidManifest
    var androidLaunchModePref = findAndroidLaunchModePreference(config);
    if (androidLaunchModePref) {
        act.attrib['android:launchMode'] = androidLaunchModePref;
    } else { // User has (explicitly) set an invalid value for AndroidLaunchMode preference
        delete act.attrib['android:launchMode']; // use Android default value (standard)
    }

    // Set min/max/target SDK version
    //<uses-sdk android:minSdkVersion="10" android:targetSdkVersion="19" ... />
    var usesSdk = manifest.getroot().find('./uses-sdk');
    ['minSdkVersion', 'maxSdkVersion', 'targetSdkVersion'].forEach(function(sdkPrefName) {
        var sdkPrefValue = config.getPreference('android-' + sdkPrefName, 'android');
        if (!sdkPrefValue) return;

        if (!usesSdk) { // if there is no required uses-sdk element, we should create it first
            usesSdk = new et.Element('uses-sdk');
            manifest.getroot().append(usesSdk);
        }
        usesSdk.attrib['android:' + sdkPrefName] = sdkPrefValue;
    });

    // Write out AndroidManifest.xml
    fs.writeFileSync(this.manifest, manifest.write({indent: 4}), 'utf-8');

    var orig_pkgDir = path.join(this.root, 'src', path.join.apply(null, orig_pkg.split('.')));

    var java_files = fs.readdirSync(orig_pkgDir)
    .filter(function(f) {
        return f.indexOf('.svn') === -1 && f.indexOf('.java') >= 0 &&
            fs.readFileSync(path.join(orig_pkgDir, f), 'utf-8').match(/extends\s+CordovaActivity/);
    });

    if (java_files.length === 0) {
        throw new Error('No Java files found which extend CordovaActivity.');
    } else if(java_files.length > 1) {
        events.emit('log', 'Multiple candidate Java files (.java files which extend CordovaActivity) found. Guessing at the first one, ' + java_files[0]);
    }

    var orig_java_class = java_files[0];
    var pkgDir = path.join(this.root, 'src', path.join.apply(null, pkg.split('.')));
    shell.mkdir('-p', pkgDir);
    var orig_javs = path.join(orig_pkgDir, orig_java_class);
    var new_javs = path.join(pkgDir, orig_java_class);
    var javs_contents = fs.readFileSync(orig_javs, 'utf-8');
    javs_contents = javs_contents.replace(/package [\w\.]*;/, 'package ' + pkg + ';');
    events.emit('verbose', 'Wrote out Android package name to "' + pkg + '"');
    fs.writeFileSync(new_javs, javs_contents, 'utf-8');

    return Q();
};

module.exports = PlatformApi;
module.exports.PluginHandler = require('./lib/pluginHandler');

function handleSplashes(config, resourcesDir) {
    var resources = config.getSplashScreens('android');
    // if there are "splash" elements in config.xml
    if (resources.length > 0) {
        deleteDefaultResource('screen.png', resourcesDir);
        events.emit('verbose', 'splash screens: ' + JSON.stringify(resources));

        // Since we can't rely on cordova utils here, we're going to resolve
        // splashscreens paths relatively to directory where config is placed
        var projectRoot = path.dirname(config.path);

        var hadMdpi = false;
        resources.forEach(function (resource) {
            if (!resource.density) {
                return;
            }
            if (resource.density === 'mdpi') {
                hadMdpi = true;
            }
            copyImage(path.join(projectRoot, resource.src), resourcesDir, resource.density, 'screen.png');
        });
        // There's no "default" drawable, so assume default == mdpi.
        if (!hadMdpi && resources.defaultResource) {
            copyImage(path.join(projectRoot, resources.defaultResource.src), resourcesDir, 'mdpi', 'screen.png');
        }
    }
}

function handleIcons(config, resourcesDir) {
    var icons = config.getIcons('android');

    // if there are icon elements in config.xml
    if (icons.length === 0) {
        events.emit('verbose', 'This app does not have launcher icons defined');
        return;
    }

    deleteDefaultResource('icon.png', resourcesDir);

    var android_icons = {};
    var default_icon;
    // http://developer.android.com/design/style/iconography.html
    var sizeToDensityMap = {
        36: 'ldpi',
        48: 'mdpi',
        72: 'hdpi',
        96: 'xhdpi',
        144: 'xxhdpi',
        192: 'xxxhdpi'
    };
    // find the best matching icon for a given density or size
    // @output android_icons
    var parseIcon = function(icon, icon_size) {
        // do I have a platform icon for that density already
        var density = icon.density || sizeToDensityMap[icon_size];
        if (!density) {
            // invalid icon defition ( or unsupported size)
            return;
        }
        var previous = android_icons[density];
        if (previous && previous.platform) {
            return;
        }
        android_icons[density] = icon;
    };

    // iterate over all icon elements to find the default icon and call parseIcon
    for (var i=0; i<icons.length; i++) {
        var icon = icons[i];
        var size = icon.width;
        if (!size) {
            size = icon.height;
        }
        if (!size && !icon.density) {
            if (default_icon) {
                events.emit('verbose', 'more than one default icon: ' + JSON.stringify(icon));
            } else {
                default_icon = icon;
            }
        } else {
            parseIcon(icon, size);
        }
    }

    // Since we can't rely on cordova utils here, we're going to resolve
    // splashscreens paths relatively to directory where config is placed
    var projectRoot = path.dirname(config.path);

    for (var density in android_icons) {
        copyImage(path.join(projectRoot, android_icons[density].src), resourcesDir, density, 'icon.png');
    }
    // There's no "default" drawable, so assume default == mdpi.
    if (default_icon && !android_icons.mdpi) {
        copyImage(path.join(projectRoot, default_icon.src), resourcesDir, 'mdpi', 'icon.png');
    }
}

// Consturct the default value for versionCode as
// PATCH + MINOR * 100 + MAJOR * 10000
// see http://developer.android.com/tools/publishing/versioning.html
function default_versionCode(version) {
    var nums = version.split('-')[0].split('.');
    var versionCode = 0;
    if (+nums[0]) {
        versionCode += +nums[0] * 10000;
    }
    if (+nums[1]) {
        versionCode += +nums[1] * 100;
    }
    if (+nums[2]) {
        versionCode += +nums[2];
    }
    return versionCode;
}

function findAndroidLaunchModePreference(config) {
    var launchMode = config.getPreference('AndroidLaunchMode');
    if (!launchMode) {
        // Return a default value
        return 'singleTop';
    }

    var expectedValues = ['standard', 'singleTop', 'singleTask', 'singleInstance'];
    var valid = expectedValues.indexOf(launchMode) !== -1;
    if (!valid) {
        events.emit('warn', 'Unrecognized value for AndroidLaunchMode preference: ' + launchMode);
        events.emit('warn', '  Expected values are: ' + expectedValues.join(', '));
        // Note: warn, but leave the launch mode as developer wanted, in case the list of options changes in the future
    }

    return launchMode;
}

// update the overrides folder into the www folder
function updateOverrides(overridesSource, destination) {
    var merges_path = path.join(overridesSource, 'android');
    if (fs.existsSync(merges_path)) {
        var overrides = path.join(merges_path, '*');
        shell.cp('-rf', overrides, destination);
    }
}

// remove the default resource name from all drawable folders
// return the array of the densities in this project
function deleteDefaultResource(name, resourcesDir) {
    var dirs = fs.readdirSync(resourcesDir);

    for (var i=0; i<dirs.length; i++) {
        var filename = dirs[i];
        if (filename.indexOf('drawable-') === 0) {
            var imgPath = path.join(resourcesDir, filename, name);
            if (fs.existsSync(imgPath)) {
                fs.unlinkSync(imgPath);
                events.emit('verbose', 'deleted: ' + imgPath);
            }
            imgPath = imgPath.replace(/\.png$/, '.9.png');
            if (fs.existsSync(imgPath)) {
                fs.unlinkSync(imgPath);
                events.emit('verbose', 'deleted: ' + imgPath);
            }
        }
    }
}

function copyImage(src, resourcesDir, density, name) {
    var destFolder = path.join(resourcesDir, (density ? 'drawable-': 'drawable') + density);
    var isNinePatch = !!/\.9\.png$/.exec(src);
    var ninePatchName = name.replace(/\.png$/, '.9.png');

    // default template does not have default asset for this density
    if (!fs.existsSync(destFolder)) {
        fs.mkdirSync(destFolder);
    }

    var destFilePath = path.join(destFolder, isNinePatch ? ninePatchName : name);
    events.emit('verbose', 'copying image from ' + src + ' to ' + destFilePath);
    shell.cp('-f', src, destFilePath);
}
