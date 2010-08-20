function Acceleration(x, y, z) {
  this.x = x;
  this.y = y;
  this.z = z;
  this.timestamp = new Date().getTime();
}

/**
 * This class provides access to device accelerometer data.
 * @constructor
 */
function Accelerometer() {

    /**
     * The last known acceleration.  type=Acceleration()
     */
    this.lastAcceleration = null;

    /**
     * List of accelerometer watch timers
     */
    this.timers = {};
}

Accelerometer.STOPPED = 0;
Accelerometer.STARTING = 1;
Accelerometer.RUNNING = 2;
Accelerometer.ERROR_FAILED_TO_START = 3;
Accelerometer.ERROR_MSG = ["Not running", "Starting", "", "Failed to start"];

/**
 * Asynchronously aquires the current acceleration.
 *
 * @param {Function} successCallback    The function to call when the acceleration data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the acceleration data.
 * @param {AccelerationOptions} options The options for getting the accelerometer data such as timeout.
 */
Accelerometer.prototype.getCurrentAcceleration = function(successCallback, errorCallback, options) {

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Accelerometer Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Accelerometer Error: errorCallback is not a function");
        return;
    }

    // Get current acceleration status
    var status = Accel.getStatus();

    // If running, then call successCallback
    if (status == Accelerometer.RUNNING) {
        try {
            navigator.accelerometer.turnOffTimer = 0;
            var accel = new Acceleration(Accel.getX(), Accel.getY(), Accel.getZ());
            successCallback(accel);
        } catch (e) {
            console.log("Accelerometer Error in successCallback: " + e);
        }
    }

    // If not running, then start it
    else {
        Accel.start();

        // Wait until started
        var timer = setInterval(function() {
            var status = Accel.getStatus();
            if (status != Accelerometer.STARTING) {
                clearInterval(timer);

                // If accelerometer is running
                if (status == Accelerometer.RUNNING) {
                    try {
                        var accel = new Acceleration(Accel.getX(), Accel.getY(), Accel.getZ());
                        successCallback(accel);
                    } catch (e) {
                        console.log("Accelerometer Error in successCallback: " + e);
                    }
                }

                // If accelerometer error
                else {
                    console.log("Accelerometer Error: "+ Accelerometer.ERROR_MSG[status]);
                    try {
                        if (errorCallback) {
                            errorCallback(status);
                        }
                    } catch (e) {
                        console.log("Accelerometer Error in errorCallback: " + e);
                    }
                }
            }
        }, 10);
    }
}

/**
 * Asynchronously aquires the acceleration repeatedly at a given interval.
 *
 * @param {Function} successCallback    The function to call each time the acceleration data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the acceleration data.
 * @param {AccelerationOptions} options The options for getting the accelerometer data such as timeout.
 * @return String                       The watch id that must be passed to #clearWatch to stop watching.
 */
Accelerometer.prototype.watchAcceleration = function(successCallback, errorCallback, options) {

    // Default interval (10 sec)
    var frequency = (options != undefined)? options.frequency : 10000;

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Accelerometer Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Accelerometer Error: errorCallback is not a function");
        return;
    }

    // Make sure accelerometer timeout > frequency + 10 sec
    var timeout = Accel.getTimeout();
    if (timeout < (frequency + 10000)) {
        Accel.setTimeout(frequency + 10000); // set to frequency + 10 sec
    }

    var id = PhoneGap.createUUID();
    Accel.start();

    // Start watch timer
    navigator.accelerometer.timers[id] = setInterval(function() {
        var status = Accel.getStatus();

        // If accelerometer is running
        if (status == Accelerometer.RUNNING) {
            try {
                var accel = new Acceleration(Accel.getX(), Accel.getY(), Accel.getZ());
                successCallback(accel);
            } catch (e) {
                console.log("Accelerometer Error in successCallback: " + e);
            }
        }

        // If accelerometer had error
        else if (status != Accelerometer.STARTING) {
            console.log("Accelerometer Error: "+ Accelerometer.ERROR_MSG[status]);
            try {
                navigator.accelerometer.clearWatch(id);
                if (errorCallback) {
                    errorCallback(status);
                }
            } catch (e) {
                console.log("Accelerometer Error in errorCallback: " + e);
            }
        }
    }, (frequency ? frequency : 1));

    return id;
}

/**
 * Clears the specified accelerometer watch.
 *
 * @param {String} id       The id of the watch returned from #watchAcceleration.
 */
Accelerometer.prototype.clearWatch = function(id) {

    // Stop javascript timer & remove from timer list
    if (id && navigator.accelerometer.timers[id]) {
        clearInterval(navigator.accelerometer.timers[id]);
        delete navigator.accelerometer.timers[id];
    }
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
});
