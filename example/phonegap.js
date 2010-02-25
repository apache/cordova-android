if (typeof(DeviceInfo) != 'object')
    DeviceInfo = {};

/**
 * This represents the PhoneGap API itself, and provides a global namespace for accessing
 * information about the state of PhoneGap.
 * @class
 */
PhoneGap = {
    queue: {
        ready: true,
        commands: [],
        timer: null
    },
    _constructors: []
};

/**
 * Boolean flag indicating if the PhoneGap API is available and initialized.
 */ // TODO: Remove this, it is unused here ... -jm
PhoneGap.available = DeviceInfo.uuid != undefined;

/**
 * Add an initialization function to a queue that ensures it will run and initialize
 * application constructors only once PhoneGap has been initialized.
 * @param {Function} func The function callback you want run once PhoneGap is initialized
 */
PhoneGap.addConstructor = function(func) {
    var state = document.readyState;
    if ( state == 'loaded' || state == 'complete' )
	  {
		  func();
	  }
    else
	  {
        PhoneGap._constructors.push(func);
	  }
};

(function() 
 {
    var timer = setInterval(function()
	{
							
		var state = document.readyState;
							
    if ( state == 'loaded' || state == 'complete' )
		{
			clearInterval(timer); // stop looking
			// run our constructors list
			while (PhoneGap._constructors.length > 0) 
			{
				var constructor = PhoneGap._constructors.shift();
				try 
				{
					constructor();
				} 
				catch(e) 
				{
					if (typeof(debug['log']) == 'function')
					{
						debug.log("Failed to run constructor: " + debug.processMessage(e));
					}
					else
					{
						alert("Failed to run constructor: " + e.message);
					}
				}
            }
			// all constructors run, now fire the deviceready event
			var e = document.createEvent('Events'); 
			e.initEvent('deviceready');
			document.dispatchEvent(e);
		}
    }, 5);
})();


/**
 * Execute a PhoneGap command in a queued fashion, to ensure commands do not
 * execute with any race conditions, and only run when PhoneGap is ready to
 * recieve them.
 * @param {String} command Command to be run in PhoneGap, e.g. "ClassName.method"
 * @param {String[]} [args] Zero or more arguments to pass to the method
 */
PhoneGap.exec = function() {
    PhoneGap.queue.commands.push(arguments);
    if (PhoneGap.queue.timer == null)
        PhoneGap.queue.timer = setInterval(PhoneGap.run_command, 10);
};

/**
 * Internal function used to dispatch the request to PhoneGap.  It processes the
 * command queue and executes the next command on the list.  If one of the
 * arguments is a JavaScript object, it will be passed on the QueryString of the
 * url, which will be turned into a dictionary on the other end.
 * @private
 */
PhoneGap.run_command = function() {
    if (!PhoneGap.available || !PhoneGap.queue.ready)
        return;

    PhoneGap.queue.ready = false;

    var args = PhoneGap.queue.commands.shift();
    if (PhoneGap.queue.commands.length == 0) {
        clearInterval(PhoneGap.queue.timer);
        PhoneGap.queue.timer = null;
    }

    var uri = [];
    var dict = null;
    for (var i = 1; i < args.length; i++) {
        var arg = args[i];
        if (arg == undefined || arg == null)
            arg = '';
        if (typeof(arg) == 'object')
            dict = arg;
        else
            uri.push(encodeURIComponent(arg));
    }
    var url = "gap://" + args[0] + "/" + uri.join("/");
    if (dict != null) {
        var query_args = [];
        for (var name in dict) {
            if (typeof(name) != 'string')
                continue;
            query_args.push(encodeURIComponent(name) + "=" + encodeURIComponent(dict[name]));
        }
        if (query_args.length > 0)
            url += "?" + query_args.join("&");
    }
    document.location = url;

};
function Acceleration(x, y, z)
{
  this.x = x;
  this.y = y;
  this.z = z;
  this.timestamp = new Date().getTime();
}

// Need to define these for android
_accel = {};
_accel.x = 0;
_accel.y = 0;
_accel.z = 0;

function gotAccel(x, y, z)
{
	_accel.x = x;
	_accel.y = y;
	_accel.z = z;
}

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
		var accel = new Acceleration(_accel.x,_accel.y,_accel.z);
		Accelerometer.lastAcceleration = accel;
		successCallback(accel);
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
	
	Accel.start(frequency);
	return setInterval(function() {
		navigator.accelerometer.getCurrentAcceleration(successCallback, errorCallback, options);
	}, frequency);
}

/**
 * Clears the specified accelerometer watch.
 * @param {String} watchId The ID of the watch returned from #watchAcceleration.
 */
Accelerometer.prototype.clearWatch = function(watchId) {
	Accel.stop();
	clearInterval(watchId);
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
});
/**
 * This class provides access to the device camera.
 * @constructor
 */
function Camera() {
	
}

/**
 * 
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
Camera.prototype.getPicture = function(successCallback, errorCallback, options) {

  this.winCallback = successCallback;
  this.failCallback = errorCallback;
  if (options.quality)
  {
    GapCam.takePicture(options.quality);
  }
  else 
  {
    GapCam.takePicture(80);
  }
}

Camera.prototype.win = function(picture)
{
  this.winCallback(picture);
}

Camera.prototype.fail = function(err)
{
  this.failCallback(err);
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.camera == "undefined") navigator.camera = new Camera();
});
/**
 * This class provides access to device Compass data.
 * @constructor
 */
function Compass() {
    /**
     * The last known Compass position.
     */
	this.lastHeading = null;
    this.lastError = null;
	this.callbacks = {
		onHeadingChanged: [],
        onError:           []
    };
};

/**
 * Asynchronously aquires the current heading.
 * @param {Function} successCallback The function to call when the heading
 * data is available
 * @param {Function} errorCallback The function to call when there is an error 
 * getting the heading data.
 * @param {PositionOptions} options The options for getting the heading data
 * such as timeout.
 */
Compass.prototype.getCurrentHeading = function(successCallback, errorCallback, options) {
	if (this.lastHeading == null) {
		this.start(options);
	}
	else 
	if (typeof successCallback == "function") {
		successCallback(this.lastHeading);
	}
};

/**
 * Asynchronously aquires the heading repeatedly at a given interval.
 * @param {Function} successCallback The function to call each time the heading
 * data is available
 * @param {Function} errorCallback The function to call when there is an error 
 * getting the heading data.
 * @param {HeadingOptions} options The options for getting the heading data
 * such as timeout and the frequency of the watch.
 */
Compass.prototype.watchHeading= function(successCallback, errorCallback, options) {
	// Invoke the appropriate callback with a new Position object every time the implementation 
	// determines that the position of the hosting device has changed. 
	
	this.getCurrentHeading(successCallback, errorCallback, options);
	var frequency = 100;
    if (typeof(options) == 'object' && options.frequency)
        frequency = options.frequency;

	var self = this;
	return setInterval(function() {
		self.getCurrentHeading(successCallback, errorCallback, options);
	}, frequency);
};


/**
 * Clears the specified heading watch.
 * @param {String} watchId The ID of the watch returned from #watchHeading.
 */
Compass.prototype.clearWatch = function(watchId) {
	clearInterval(watchId);
};


/**
 * Called by the geolocation framework when the current heading is found.
 * @param {HeadingOptions} position The current heading.
 */
Compass.prototype.setHeading = function(heading) {
    this.lastHeading = heading;
    for (var i = 0; i < this.callbacks.onHeadingChanged.length; i++) {
        var f = this.callbacks.onHeadingChanged.shift();
        f(heading);
    }
};

/**
 * Called by the geolocation framework when an error occurs while looking up the current position.
 * @param {String} message The text of the error message.
 */
Compass.prototype.setError = function(message) {
    this.lastError = message;
    for (var i = 0; i < this.callbacks.onError.length; i++) {
        var f = this.callbacks.onError.shift();
        f(message);
    }
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.compass == "undefined") navigator.compass = new Compass();
});
var Contact = function(){
  this.name = new ContactName();
  this.emails = [];
  this.phones = [];
}

var ContactName = function()
{
  this.formatted = "";
  this.familyName = "";
  this.givenName = "";
  this.additionalNames = [];
  this.prefixes = [];
  this.suffixes = [];
}


var ContactEmail = function()
{
  this.types = [];
  this.address = "";
}

var ContactPhoneNumber = function()
{
  this.types = [];
  this.number = "";
}


var Contacts = function()
{
  this.records = [];  
}

Contacts.prototype.find = function(obj, win, fail)
{
  if(obj.name != null)
  {
	// Build up the search term that we'll use in SQL, based on the structure/contents of the contact object passed into find.
	   var searchTerm = '';
	   if (obj.name.givenName && obj.name.givenName.length > 0) {
			searchTerm = obj.name.givenName.split(' ').join('%');
	   }
	   if (obj.name.familyName && obj.name.familyName.length > 0) {
			searchTerm += obj.name.familyName.split(' ').join('%');
	   }
	   if (!obj.name.familyName && !obj.name.givenName && obj.name.formatted) {
			searchTerm = obj.name.formatted;
	   }
	   ContactHook.search(searchTerm, "", ""); 
  }
  this.win = win;
  this.fail = fail;
}

Contacts.prototype.droidFoundContact = function(name, npa, email)
{
  var contact = new Contact();
  contact.name = new ContactName();
  contact.name.formatted = name;
  contact.name.givenName = name;
  var mail = new ContactEmail();
  mail.types.push("home");
  mail.address = email;
  contact.emails.push(mail);
  phone = new ContactPhoneNumber();
  phone.types.push("home");
  phone.number = npa;
  contact.phones.push(phone);
  this.records.push(contact);
}

Contacts.prototype.droidDone = function()
{
  this.win(this.records);
}

PhoneGap.addConstructor(function() {
  if(typeof navigator.contacts == "undefined") navigator.contacts = new Contacts();
});
/**
 * this represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function Device() {
    this.available = PhoneGap.available;
    this.platform = null;
    this.version  = null;
    this.name     = null;
    this.gap      = null;
    this.uuid     = null;
    try {
        if (window.DroidGap) {
            this.available = true;
            this.uuid = window.DroidGap.getUuid();
            this.version = window.DroidGap.getOSVersion();
            this.gapVersion = window.DroidGap.getVersion();
            this.platform = window.DroidGap.getPlatform();
            this.name = window.DroidGap.getProductName();  
        } 
    } catch(e) {
        this.available = false;
    }
}

PhoneGap.addConstructor(function() {
    navigator.device = window.device = new Device();
});
/**
 * This class provides generic read and write access to the mobile device file system.
 */
function File() {
	/**
	 * The data of a file.
	 */
	this.data = "";
	/**
	 * The name of the file.
	 */
	this.name = "";
}

/**
 * Reads a file from the mobile device. This function is asyncronous.
 * @param {String} fileName The name (including the path) to the file on the mobile device. 
 * The file name will likely be device dependent.
 * @param {Function} successCallback The function to call when the file is successfully read.
 * @param {Function} errorCallback The function to call when there is an error reading the file from the device.
 */
File.prototype.read = function(fileName, successCallback, errorCallback) {
	
}

/**
 * Writes a file to the mobile device.
 * @param {File} file The file to write to the device.
 */
File.prototype.write = function(file) {
	
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.file == "undefined") navigator.file = new File();
});

File.prototype.read = function(fileName, successCallback, errorCallback) {
  this.failCallback = errorCallback; 
  this.winCallback = successCallback;

  return FileUtil.read(fileName);
}

File.prototype.hasRead = function(data)
{
  if(data.substr("FAIL"))
    this.failCallback(data);
  else
    this.winCallback(data);
}

/**
 * Writes a file to the mobile device.
 * @param {File} file The file to write to the device.
 */
File.prototype.write = function(file, str, mode, successCallback, failCallback) {
  this.winCallback = successCallback;
  this.failCallback = failCallback;
  var call = FileUtil.write(file, str, mode);
}

File.prototype.testFileExists = function(file, successCallback, failCallback)
{
  var exists = FileUtil.testFileExists(file);
  if(exists)
    successCallback();
  else
    failCallback();
  return exists;
}

File.prototype.testDirectoryExists = function(file, successCallback, failCallback)
{
  var exists = FileUtil.testDirectoryExists(file);
  if(exists)
    successCallback();
  else
    failCallback();
  return exists;
}

File.prototype.createDirectory = function(dir, successCallback, failCallback)
{
  var good = FileUtils.createDirectory(dir);
  good ? successCallback() : failCallback();
}

File.prototype.deleteDirectory = function(dir, successCallback, failCallback)
{
  var good = FileUtils.deleteDirectory(dir);
  good ? successCallback() : failCallback();
}

File.prototype.deleteFile = function(dir, successCallback, failCallback)
{
  var good = FileUtils.deleteFile(dir);
  good ? successCallback() : failCallback();
}

File.prototype.getFreeDiskSpace = function(successCallback, failCallback)
{
  var diskSpace =  FileUtils.getFreeDiskSpace();
  if(diskSpace > 0)
    successCallback();
  else
    failCallback();
  return diskSpace;
}
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

Geolocation.prototype.getCurrentPosition = function(successCallback, errorCallback, options)
{
  var position = Geo.getCurrentLocation();
  this.global_success = successCallback;
  this.fail = errorCallback;
}

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
// Taken from Jesse's geo fix (similar problem) in PhoneGap iPhone. Go figure, same browser!
function __proxyObj(origObj, proxyObj, funkList) {
	for (var v in funkList) {
		origObj[funkList[v]] = proxyObj[funkList[v]];
	}
}
PhoneGap.addConstructor(function() {
	navigator._geo = new Geolocation();
	__proxyObj(navigator.geolocation, navigator._geo,
		["setLocation", "getCurrentPosition", "watchPosition",
		 "clearWatch", "setError", "start", "stop", "gotCurrentPosition"]
	);
});function KeyEvent() 
{
}

KeyEvent.prototype.menuTrigger = function()
{
  var e = document.createEvent('Events');
  e.initEvent('menuKeyDown');
  document.dispatchEvent(e);
}

KeyEvent.prototype.searchTrigger= function()
{
  var e = document.createEvent('Events');
  e.initEvent('searchKeyDown');
  document.dispatchEvent(e);
}

if (document.keyEvent == null || typeof document.keyEvent == 'undefined')
{
  window.keyEvent = document.keyEvent = new KeyEvent();
}
/**
 * This class provides access to the device media, interfaces to both sound and video
 * @constructor
 */
function Media(src, successCallback, errorCallback) {
	this.src = src;
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;												
}

Media.prototype.record = function() {
}

Media.prototype.play = function() {
}

Media.prototype.pause = function() {
}

Media.prototype.stop = function() {
}


/**
 * This class contains information about any Media errors.
 * @constructor
 */
function MediaError() {
	this.code = null,
	this.message = "";
}

MediaError.MEDIA_ERR_ABORTED 		= 1;
MediaError.MEDIA_ERR_NETWORK 		= 2;
MediaError.MEDIA_ERR_DECODE 		= 3;
MediaError.MEDIA_ERR_NONE_SUPPORTED = 4;


//if (typeof navigator.audio == "undefined") navigator.audio = new Media(src);

/**
 * This class provides access to the device media, interfaces to both sound and video
 * @constructor
 */

Media.prototype.play = function() {
  DroidGap.startPlayingAudio(this.src);  
}

Media.prototype.stop = function() {
  DroidGap.stopPlayingAudio();
}

Media.prototype.startRecord = function() {
  DroidGap.startRecordingAudio(this.src);
}

Media.prototype.stopRecordingAudio = function() {
  DroidGap.stopRecordingAudio();
}


/**
 * This class contains information about any NetworkStatus.
 * @constructor
 */
function NetworkStatus() {
	this.code = null;
	this.message = "";
}

NetworkStatus.NOT_REACHABLE = 0;
NetworkStatus.REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
NetworkStatus.REACHABLE_VIA_WIFI_NETWORK = 2;

/**
 * This class provides access to device Network data (reachability).
 * @constructor
 */
function Network() {
    /**
     * The last known Network status.
	 * { hostName: string, ipAddress: string, 
		remoteHostStatus: int(0/1/2), internetConnectionStatus: int(0/1/2), localWiFiConnectionStatus: int (0/2) }
     */
	this.lastReachability = null;
};

/**
 * 
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options  (isIpAddress:boolean)
 */
Network.prototype.isReachable = function(hostName, successCallback, options) {
}

/**
 * Called by the geolocation framework when the reachability status has changed.
 * @param {Reachibility} reachability The current reachability status.
 */
Network.prototype.updateReachability = function(reachability) {
    this.lastReachability = reachability;
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.network == "undefined") navigator.network = new Network();
});
Network.prototype.isReachable = function(uri, win, options)
{
  var status = new NetworkStatus();
  if(NetworkManager.isReachable(uri))
  {
    if (NetworkManager.isWifiActive)
      status.code = 2;
    else
      status.code = 1;
  }
  else
      status.code = 0;
  win(status);
}
/**
 * This class provides access to notifications on the device.
 */
function Notification() {
	
}

/**
 * Open a native alert dialog, with a customizable title and button text.
 * @param {String} message Message to print in the body of the alert
 * @param {String} [title="Alert"] Title of the alert dialog (default: Alert)
 * @param {String} [buttonLabel="OK"] Label of the close button (default: OK)
 */
Notification.prototype.alert = function(message, title, buttonLabel) {
    // Default is to use a browser alert; this will use "index.html" as the title though
    alert(message);
};

/**
 * Start spinning the activity indicator on the statusbar
 */
Notification.prototype.activityStart = function() {
};

/**
 * Stop spinning the activity indicator on the statusbar, if it's currently spinning
 */
Notification.prototype.activityStop = function() {
};

/**
 * Causes the device to blink a status LED.
 * @param {Integer} count The number of blinks.
 * @param {String} colour The colour of the light.
 */
Notification.prototype.blink = function(count, colour) {
	
};

/**
 * Causes the device to vibrate.
 * @param {Integer} mills The number of milliseconds to vibrate for.
 */
Notification.prototype.vibrate = function(mills) {
	
};

/**
 * Causes the device to beep.
 * @param {Integer} count The number of beeps.
 * @param {Integer} volume The volume of the beep.
 */
Notification.prototype.beep = function(count, volume) {
	
};

// TODO: of course on Blackberry and Android there notifications in the UI as well

PhoneGap.addConstructor(function() {
    if (typeof navigator.notification == "undefined") navigator.notification = new Notification();
});

Notification.prototype.vibrate = function(mills)
{
  DroidGap.vibrate(mills);
}

/*
 * On the Android, we don't beep, we notify you with your 
 * notification!  We shouldn't keep hammering on this, and should
 * review what we want beep to do.
 */

Notification.prototype.beep = function(count, volume)
{
  DroidGap.beep(count);
}
/**
 * This class contains position information.
 * @param {Object} lat
 * @param {Object} lng
 * @param {Object} acc
 * @param {Object} alt
 * @param {Object} altacc
 * @param {Object} head
 * @param {Object} vel
 * @constructor
 */
function Position(coords, timestamp) {
	this.coords = coords;
        this.timestamp = new Date().getTime();
}

function Coordinates(lat, lng, alt, acc, head, vel) {
	/**
	 * The latitude of the position.
	 */
	this.latitude = lat;
	/**
	 * The longitude of the position,
	 */
	this.longitude = lng;
	/**
	 * The accuracy of the position.
	 */
	this.accuracy = acc;
	/**
	 * The altitude of the position.
	 */
	this.altitude = alt;
	/**
	 * The direction the device is moving at the position.
	 */
	this.heading = head;
	/**
	 * The velocity with which the device is moving at the position.
	 */
	this.speed = vel;
}

/**
 * This class specifies the options for requesting position data.
 * @constructor
 */
function PositionOptions() {
	/**
	 * Specifies the desired position accuracy.
	 */
	this.enableHighAccuracy = true;
	/**
	 * The timeout after which if position data cannot be obtained the errorCallback
	 * is called.
	 */
	this.timeout = 10000;
}

/**
 * This class contains information about any GSP errors.
 * @constructor
 */
function PositionError() {
	this.code = null;
	this.message = "";
}

PositionError.UNKNOWN_ERROR = 0;
PositionError.PERMISSION_DENIED = 1;
PositionError.POSITION_UNAVAILABLE = 2;
PositionError.TIMEOUT = 3;
/*
 * This is purely for the Android 1.5/1.6 HTML 5 Storage
 * I was hoping that Android 2.0 would deprecate this, but given the fact that 
 * most manufacturers ship with Android 1.5 and do not do OTA Updates, this is required
 */

var DroidDB = function()
{
  this.txQueue = [];
}

DroidDB.prototype.addResult = function(rawdata, tx_id)
{
  eval("var data = " + rawdata);
  var tx = this.txQueue[tx_id];
  tx.resultSet.push(data);
}

DroidDB.prototype.completeQuery = function(tx_id)
{
  var tx = this.txQueue[tx_id];
  var r = new result();
  r.rows.resultSet = tx.resultSet;
  r.rows.length = tx.resultSet.length;
  tx.win(r);
}

DroidDB.prototype.fail = function(reason, tx_id)
{
  var tx = this.txQueue[tx_id];
  tx.fail(reason);
}

var DatabaseShell = function()
{
  
}

DatabaseShell.prototype.transaction = function(process)
{
  tx = new Tx();
  process(tx);
}

var Tx = function()
{
  droiddb.txQueue.push(this);
  this.id = droiddb.txQueue.length - 1;
  this.resultSet = [];
}

Tx.prototype.executeSql = function(query, params, win, fail)
{
  droidStorage.executeSql(query, params, this.id);
  tx.win = win;
  tx.fail = fail;
}

var result = function()
{
  this.rows = new Rows();
}

var Rows = function()
{
  this.resultSet = [];
  this.length = 0;
}

Rows.prototype.item = function(row_id)
{
  return this.resultSet[id];
}

var dbSetup = function(name, version, display_name, size)
{
    droidStorage.openDatabase(name, version, display_name, size)
    db_object = new DatabaseShell();
    return db_object;
}

PhoneGap.addConstructor(function() {
  if (typeof navigator.openDatabase == "undefined") 
  {
    navigator.openDatabase = window.openDatabase = dbSetup;
  window.droiddb = new DroidDB();
  }
});

