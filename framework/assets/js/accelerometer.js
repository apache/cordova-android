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
     * List of accelerometer watch listeners
     */
    this.accelListeners = {};

    /**
     * List of accelerometer watch timers
     */
    this.accelTimers = {};

    /**
     * Next id to use
     */
    this.listenerId = 0;

    /**
     * Timer that turns off accelerometer when it reaches maximum time.
     * This timer is only used for getCurrentAcceleration.
     */
    this.turnOffTimer = 0;

    // Turn off accelerometer if not accessed for certain amount of time
    setInterval(function() {
        navigator.accelerometer.turnOffTimer += 10;
        if (navigator.accelerometer.turnOffTimer > Accelerometer.MAX_TIMER) {
            Accel.stop("timer");
            navigator.accelerometer.turnOffTimer = 0;
        }
    }, 10000);
}

Accelerometer.STOPPED = 0;
Accelerometer.STARTING = 1;
Accelerometer.RUNNING = 2;
Accelerometer.ERROR_FAILED_TO_START = 3;
Accelerometer.ERROR_NOT_FOUND = 4;
Accelerometer.ERROR_MSG = ["Not running", "Starting", "", "Failed to start", "Listener not found"];

/**
 * Time (in seconds) to turn off accelerometer if getCurrentAcceleration() hasn't been called.
 */
Accelerometer.MAX_TIMER = 30;

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
        Accel.start("timer");
        navigator.accelerometer.turnOffTimer = 0;
        var obj = this;

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

    var id = ""+(navigator.accelerometer.listenerId++);
    navigator.accelerometer.accelListeners[id] = id;
    Accel.start(id);

    // Start watch timer
    navigator.accelerometer.accelTimers[id] = setInterval(function() {
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
    if (id && navigator.accelerometer.accelTimers[id]) {
        clearInterval(navigator.accelerometer.accelTimers[id]);
        delete navigator.accelerometer.accelTimers[id];
    }

    // Remove from watch list
    if (id && navigator.accelerometer.accelListeners[id]) {
        delete navigator.accelerometer.accelListeners[id];
    }

    // Stop accelerometer for this watch listener
    if (id) {
        try {
            Accel.stop(id);
        } catch (e) {
        }
    }
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
});
