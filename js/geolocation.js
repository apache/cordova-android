/**
 * This class provides access to device GPS data.
 * @constructor
 */
function Geolocation() {
    /**
     * The last known GPS position.
     */
    this.lastPosition = null;
    this.lastError = null;
    this.callbacks = {
        onLocationChanged: [],
        onError:           []
    };
};

/**
 * Asynchronously aquires the current position.
 * @param {Function} successCallback The function to call when the position
 * data is available
 * @param {Function} errorCallback The function to call when there is an error 
 * getting the position data.
 * @param {PositionOptions} options The options for getting the position data
 * such as timeout.
 */
Geolocation.prototype.getCurrentPosition = function(successCallback, errorCallback, options) {
    var referenceTime = 0;
    if (this.lastPosition)
        referenceTime = this.lastPosition.timeout;
    else
        this.start(options);

    var timeout = 20000;
    var interval = 500;
    if (typeof(options) == 'object' && options.interval)
        interval = options.interval;

    if (typeof(successCallback) != 'function')
        successCallback = function() {};
    if (typeof(errorCallback) != 'function')
        errorCallback = function() {};

    var dis = this;
    var delay = 0;
    var timer = setInterval(function() {
        delay += interval;

        if (typeof(dis.lastPosition) == 'object' && dis.lastPosition.timestamp > referenceTime) {
            successCallback(dis.lastPosition);
            clearInterval(timer);
        } else if (delay >= timeout) {
            errorCallback();
            clearInterval(timer);
        }
    }, interval);
};

/**
 * Asynchronously aquires the position repeatedly at a given interval.
 * @param {Function} successCallback The function to call each time the position
 * data is available
 * @param {Function} errorCallback The function to call when there is an error 
 * getting the position data.
 * @param {PositionOptions} options The options for getting the position data
 * such as timeout and the frequency of the watch.
 */
Geolocation.prototype.watchPosition = function(successCallback, errorCallback, options) {
	// Invoke the appropriate callback with a new Position object every time the implementation 
	// determines that the position of the hosting device has changed. 
	
	this.getCurrentPosition(successCallback, errorCallback, options);
	var frequency = 10000;
        if (typeof(options) == 'object' && options.frequency)
            frequency = options.frequency;
	
	var that = this;
	return setInterval(function() {
		that.getCurrentPosition(successCallback, errorCallback, options);
	}, frequency);
};


/**
 * Clears the specified position watch.
 * @param {String} watchId The ID of the watch returned from #watchPosition.
 */
Geolocation.prototype.clearWatch = function(watchId) {
	clearInterval(watchId);
};

/**
 * Called by the geolocation framework when the current location is found.
 * @param {PositionOptions} position The current position.
 */
Geolocation.prototype.setLocation = function(position) {
    this.lastPosition = position;
    for (var i = 0; i < this.callbacks.onLocationChanged.length; i++) {
        var f = this.callbacks.onLocationChanged.shift();
        f(position);
    }
};

/**
 * Called by the geolocation framework when an error occurs while looking up the current position.
 * @param {String} message The text of the error message.
 */
Geolocation.prototype.setError = function(message) {
    this.lastError = message;
    for (var i = 0; i < this.callbacks.onError.length; i++) {
        var f = this.callbacks.onError.shift();
        f(message);
    }
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.geolocation == "undefined") navigator.geolocation = new Geolocation();
});
/*
* Since we can't guarantee that we will have the most recent, we just try our best!
*
* Also, the API doesn't specify which version is the best version of the API
*/
 
Geolocation.prototype.getCurrentPosition = function(successCallback, errorCallback, options)
{
  var position = Geo.getCurrentLocation();
  this.global_success = successCallback;
  this.fail = errorCallback;
}
 
 
// Run the global callback
Geolocation.prototype.gotCurrentPosition = function(lat, lng, alt, altacc, head, vel, stamp)
{
  if (lat == "undefined" || lng == "undefined")
  {
    this.fail();
  }
  else
  {
    coords = new Coordinates(lat, lng, alt, altacc, head, vel);
    loc = new Position(coords, stamp);
    this.global_success(loc);
  }
}

/*
* This turns on the GeoLocator class, which has two listeners.
* The listeners have their own timeouts, and run independently of this process
* In this case, we return the key to the watch hash
*/
 
Geolocation.prototype.watchPosition = function(successCallback, errorCallback, options)
{
  var frequency = (options != undefined)? options.frequency : 10000;
 
  if (!this.listeners)
  {
      this.listeners = [];
  }
 
  var key = this.listeners.push( {"success" : successCallback, "fail" : failCallback }) - 1;
 
  // TO-DO: Get the names of the method and pass them as strings to the Java.
  return Geolocation.start(frequency, key);
}
 
/*
 * Retrieve and stop this listener from listening to the GPS
 *
 */
Geolocation.prototype.success = function(key, lat, lng, alt, altacc, head, vel, stamp)
{
  var coords = new Coordinates(lat, lng, alt, altacc, head, vel);
  var loc = new Position(coords, stamp);
  this.listeners[key].success(loc);
}

Geolocation.prototype.fail = function(key)
{
  this.listeners[key].fail();
}
 
Geolocation.prototype.clearWatch = function(watchId)
{
  Geo.stop(watchId);
}
