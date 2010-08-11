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
}

Accelerometer.STOPPED = 0;
Accelerometer.STARTING = 1;
Accelerometer.RUNNING = 2;
Accelerometer.ERROR_FAILED_TO_START = 3;
Accelerometer.ERROR_NOT_FOUND = 4;
Accelerometer.ERROR_MSG = ["Not running", "Starting", "", "Failed to start", "Listener not found"];

/**
 * Asynchronously aquires the current acceleration.
 * @param {Function} successCallback The function to call when the acceleration
 * data is available
 * @param {Function} errorCallback The function to call when there is an error
 * getting the acceleration data.
 * @param {AccelerationOptions} options The options for getting the accelerometer data
 * such as timeout.
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

    // Get current acceleration from native
    var status = Accel.getStatus();
    //console.log("getCurrentAcceleration: status="+status);

    // If running, then call successCallback
    if (status == Accelerometer.RUNNING) {
        try {
            var accel = new Acceleration(Accel.getX(), Accel.getY(), Accel.getZ());
            successCallback(accel);
        } catch (e) {
            console.log("Accelerometer Error in successCallback: " + e);
        }
    }

    // If not running, then start it
    else {

        Accel.start();

        var status = Accel.getStatus();
        //console.log("getAcceleration: status="+status);

        // Wait until sensor has 1 reading
        if (status == Accelerometer.STARTING) {
            Accel.getX();
            status = Accel.getStatus(); // get status again to see if started or error
        }

        // If sensor is running
        if (status == Accelerometer.RUNNING) {
            try {
                var accel = new Acceleration(Accel.getX(), Accel.getY(), Accel.getZ());
                successCallback(accel);
            } catch (e) {
                console.log("Accelerometer Error in successCallback: " + e);
            }
        }

        // If sensor error
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

        //@todo: don't clear now, wait until x seconds since last request
        this.clearWatch("");
    }
}

/**
 * Asynchronously aquires the acceleration repeatedly at a given interval.
 *
 * @param {Function} successCallback The function to call each time the acceleration
 * data is available
 * @param {Function} errorCallback The function to call when there is an error
 * getting the acceleration data.
 * @param {AccelerationOptions} options The options for getting the accelerometer data
 * such as timeout.
 */
Accelerometer.prototype.watchAcceleration = function(successCallback, errorCallback, options) {

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

    var key = this.listenerId++;
    this.accelListeners[key] = key;
    var obj = this;
    Accel.start();

    // Start watch timer
    this.accelTimers[key] = setInterval(function() {
        //console.log("Interval timer: key="+key+"  timer="+obj.accelTimers[key]+" freq="+frequency);
        var status = Accel.getStatus();
        //console.log("watchAcceleration: status="+status);

        if (status == Accelerometer.RUNNING) {
            try {
                // If getCurrentAcceleration(), then clear this watch
                if (frequency == 0) {
                    obj.clearWatch(key);
                }
                var accel = new Acceleration(Accel.getX(), Accel.getY(), Accel.getZ());
                successCallback(accel);
            } catch (e) {
                console.log("Accelerometer Error in successCallback: " + e);
            }
        }
        else {
            console.log("Accelerometer Error: "+ Accelerometer.ERROR_MSG[status]);
            try {
                obj.clearWatch(key);
                if (errorCallback) {
                    errorCallback(status);
                }
            } catch (e) {
                console.log("Accelerometer Error in errorCallback: " + e);
            }
        }
    }, (frequency ? frequency : 1));

    return key;
}

/**
 * Clears the specified accelerometer watch.
 *
 * @param {String} watchId The ID of the watch returned from #watchAcceleration.
 */
Accelerometer.prototype.clearWatch = function(watchId) {

    // Stop javascript timer & remove from timer list
    if (watchId && this.accelTimers[watchId]) {
        clearInterval(this.accelListeners[watchId]);
        delete this.accelTimers[watchId];
    }

    // Remove from watch list
    if (watchId && this.accelListeners[watchId]) {
        delete this.accelListeners[watchId];
    }

    // Stop native sensor if no more listeners
    var size = 0;
    var key;
    for (key in this.accelListeners) {
        if (this.accelListeners.hasOwnProperty(key)) size++;
    }
    if (size == 0) {
        Accel.stop();
    }
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
});
