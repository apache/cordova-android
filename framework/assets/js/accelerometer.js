function Acceleration(x, y, z)
{
  this.x = x;
  this.y = y;
  this.z = z;
  this.timestamp = new Date().getTime();
}

var accelListeners = [];

/**
 * This class provides access to device accelerometer data.
 * @constructor
 */
function Accelerometer() {
	/**
	 * The last known acceleration.
	 */
	this.lastAcceleration = null;
}

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
	// If the acceleration is available then call success
	// If the acceleration is not available then call error

	// Created for iPhone, Iphone passes back _accel obj litteral
	if (typeof successCallback == "function") {
		if(this.lastAcceleration)
		  successCallback(accel);
		else
		{
			watchAcceleration(this.gotCurrentAcceleration, this.fail);
		}
	}
}


Accelerometer.prototype.gotCurrentAcceleration = function(key, x, y, z)
{
    var a = new Acceleration(x,y,z);
    a.x = x;
    a.y = y;
    a.z = z;
    a.win = accelListeners[key].win;
    a.fail = accelListeners[key].fail;
    this.timestamp = new Date().getTime();
    this.lastAcceleration = a;
    accelListeners[key] = a;
    if (typeof a.win == "function") {
      a.win(a);
    }
}


/**
 * Asynchronously aquires the acceleration repeatedly at a given interval.
 * @param {Function} successCallback The function to call each time the acceleration
 * data is available
 * @param {Function} errorCallback The function to call when there is an error 
 * getting the acceleration data.
 * @param {AccelerationOptions} options The options for getting the accelerometer data
 * such as timeout.
 */

Accelerometer.prototype.watchAcceleration = function(successCallback, errorCallback, options) {
	// TODO: add the interval id to a list so we can clear all watches
  var frequency = (options != undefined)? options.frequency : 10000;
  var accel = new Acceleration(0,0,0);
  accel.win = successCallback;
  accel.fail = errorCallback;
  accel.opts = options;
  var key = accelListeners.push( accel ) - 1;
  Accel.start(frequency, key);
}

/**
 * Clears the specified accelerometer watch.
 * @param {String} watchId The ID of the watch returned from #watchAcceleration.
 */
Accelerometer.prototype.clearWatch = function(watchId) {
	Accel.stop(watchId);
}

Accelerometer.prototype.epicFail = function(watchId, message) {
  accelWatcher[key].fail();
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
});
