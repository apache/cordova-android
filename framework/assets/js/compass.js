/**
 * This class provides access to device Compass data.
 * @constructor
 */
function Compass() {
    /**
     * The last known Compass position.
     */
    this.lastHeading = null;

    /**
     * List of compass watch timers
     */
    this.timers = {};
};

Compass.STOPPED = 0;
Compass.STARTING = 1;
Compass.RUNNING = 2;
Compass.ERROR_FAILED_TO_START = 3;
Compass.ERROR_MSG = ["Not running", "Starting", "", "Failed to start"];

/**
 * Asynchronously aquires the current heading.
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data.
 * @param {PositionOptions} options The options for getting the heading data such as timeout.
 */
Compass.prototype.getCurrentHeading = function(successCallback, errorCallback, options) {

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Compass Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Compass Error: errorCallback is not a function");
        return;
    }

    // Get current compass status
    var status = CompassHook.getStatus();

    // If running, then call successCallback
    if (status == Compass.RUNNING) {
        try {
            var heading = CompassHook.getHeading();
            successCallback(heading);
        } catch (e) {
            console.log("Compass Error in successCallback: " + e);
        }
    }

    // If not running, then start it
    else {
        CompassHook.start();

        // Wait until started
        var timer = setInterval(function() {
            var status = CompassHook.getStatus();
            if (status != Compass.STARTING) {
                clearInterval(timer);

                // If compass is running
                if (status == Compass.RUNNING) {
                    try {
                        var heading = CompassHook.getHeading();
                        successCallback(heading);
                    } catch (e) {
                        console.log("Compass Error in successCallback: " + e);
                    }
                }

                // If compass error
                else {
                    console.log("Compass Error: "+ Compass.ERROR_MSG[status]);
                    try {
                        if (errorCallback) {
                            errorCallback(status);
                        }
                    } catch (e) {
                        console.log("Compass Error in errorCallback: " + e);
                    }
                }
            }
        }, 10);
    }
}

/**
 * Asynchronously aquires the heading repeatedly at a given interval.
 *
 * @param {Function} successCallback    The function to call each time the heading data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the heading data.
 * @param {HeadingOptions} options      The options for getting the heading data such as timeout and the frequency of the watch.
 * @return String                       The watch id that must be passed to #clearWatch to stop watching.
 */
Compass.prototype.watchHeading= function(successCallback, errorCallback, options) {

    // Default interval (100 msec)
    var frequency = (options != undefined) ? options.frequency : 100;

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Compass Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Compass Error: errorCallback is not a function");
        return;
    }

    // Make sure compass timeout > frequency + 10 sec
    var timeout = CompassHook.getTimeout();
    if (timeout < (frequency + 10000)) {
        CompassHook.setTimeout(frequency + 10000); // set to frequency + 10 sec
    }

    var id = PhoneGap.createUUID();
    CompassHook.start();

    // Start watch timer
    navigator.compass.timers[id] = setInterval(function() {
        var status = CompassHook.getStatus();

        // If compass is running
        if (status == Compass.RUNNING) {
            try {
                var heading = CompassHook.getHeading();
                successCallback(heading);
            } catch (e) {
                console.log("Compass Error in successCallback: " + e);
            }
        }

        // If compass had error
        else if (status != Compass.STARTING) {
            console.log("Compass Error: "+ Compass.ERROR_MSG[status]);
            try {
                navigator.compass.clearWatch(id);
                if (errorCallback) {
                    errorCallback(status);
                }
            } catch (e) {
                console.log("Compass Error in errorCallback: " + e);
            }
        }
    }, (frequency ? frequency : 1));

    return id;
}


/**
 * Clears the specified heading watch.
 *
 * @param {String} id       The ID of the watch returned from #watchHeading.
 */
Compass.prototype.clearWatch = function(id) {

    // Stop javascript timer & remove from timer list
    if (id && navigator.compass.timers[id]) {
        clearInterval(navigator.compass.timers[id]);
        delete navigator.compass.timers[id];
    }
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.compass == "undefined") navigator.compass = new Compass();
});
