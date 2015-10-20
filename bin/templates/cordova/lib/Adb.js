
var Q = require('q');
var os = require('os');
var events = require('cordova-common').events;
var spawn = require('cordova-common').superspawn.spawn;
var CordovaError = require('cordova-common').CordovaError;

var Adb = {};

function isDevice(line) {
    return line.match(/\w+\tdevice/) && !line.match(/emulator/);
}

function isEmulator(line) {
    return line.match(/device/) && line.match(/emulator/);
}

/**
 * Lists available/connected devices and emulators
 *
 * @param   {Object}   opts            Various options
 * @param   {Boolean}  opts.emulators  Specifies whether this method returns
 *   emulators only
 *
 * @return  {Promise<String[]>}        list of available/connected
 *   devices/emulators
 */
Adb.devices = function (opts) {
    return spawn('adb', ['devices'], {cwd: os.tmpdir()})
    .then(function(output) {
        return output.split('\n').filter(function (line) {
            // Filter out either real devices or emulators, depending on options
            return (line && opts && opts.emulators) ? isEmulator(line) : isDevice(line);
        }).map(function (line) {
            return line.replace(/\tdevice/, '').replace('\r', '');
        });
    });
};

Adb.install = function (target, packagePath, opts) {
    events.emit('verbose', 'Installing apk ' + packagePath + ' on ' + target + '...');
    var args = ['-s', target, 'install'];
    if (opts && opts.replace) args.push('-r');
    return spawn('adb', args.concat(packagePath), {cwd: os.tmpdir()})
    .then(function(output) {
        // 'adb install' seems to always returns no error, even if installation fails
        // so we catching output to detect installation failure
        if (output.match(/Failure/))
            return Q.reject(new CordovaError('Failed to install apk to device: ' + output));
    });
};

Adb.uninstall = function (target, packageId) {
    events.emit('verbose', 'Uninstalling ' + packageId + ' from ' + target + '...');
    return spawn('adb', ['-s', target, 'uninstall', packageId], {cwd: os.tmpdir()});
};

Adb.shell = function (target, shellCommand) {
    events.emit('verbose', 'Running command "' + shellCommand + '" on ' + target + '...');
    var args = ['-s', target, 'shell'];
    shellCommand = shellCommand.split(/\s+/);
    return spawn('adb', args.concat(shellCommand), {cwd: os.tmpdir()})
    .catch(function (output) {
        return Q.reject(new CordovaError('Failed to execute shell command "' +
            shellCommand + '"" on device: ' + output));
    });
};

Adb.start = function (target, activityName) {
    events.emit('verbose', 'Starting application "' + activityName + '" on ' + target + '...');
    return Adb.shell(target, 'am start -W -a android.intent.action.MAIN -n' + activityName)
    .catch(function (output) {
        return Q.reject(new CordovaError('Failed to start application "' +
            activityName + '"" on device: ' + output));
    });
};

module.exports = Adb;
