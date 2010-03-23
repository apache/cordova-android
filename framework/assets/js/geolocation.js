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
    this.listeners = null;
};

var geoListeners = [];

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
	this.lastPosition = loc;
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
   
  var key = geoListeners.push( {"success" : successCallback, "fail" : errorCallback }) - 1;
 
  // TO-DO: Get the names of the method and pass them as strings to the Java.
  return Geo.start(frequency, key);
}
 
/*
 * Retrieve and stop this listener from listening to the GPS
 *
 */
Geolocation.prototype.success = function(key, lat, lng, alt, altacc, head, vel, stamp)
{
  var coords = new Coordinates(lat, lng, alt, altacc, head, vel);
  var loc = new Position(coords, stamp);
  geoListeners[key].success(loc);
}

Geolocation.prototype.fail = function(key)
{
  geoListeners[key].fail();
}
 
Geolocation.prototype.clearWatch = function(watchId)
{
  Geo.stop(watchId);
}

PhoneGap.addConstructor(function() {
	// Taken from Jesse's geo fix (similar problem) in PhoneGap iPhone. Go figure, same browser!
	function __proxyObj(origObj, proxyObj, funkList) {
		for (var v in funkList) {
			origObj[funkList[v]] = proxyObj[funkList[v]];
		}
	}
	// In case a native geolocation object exists, proxy the native one over to a diff object so that we can overwrite the native implementation.
	if (typeof navigator.geolocation != 'undefined') {
		navigator._geo = new Geolocation();
		__proxyObj(navigator.geolocation, navigator._geo, ["setLocation", "getCurrentPosition", "watchPosition", "clearWatch", "setError", "start", "stop", "gotCurrentPosition"]);
	} else {
		navigator.geolocation = new Geolocation();
	}
});