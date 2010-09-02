if (typeof(DeviceInfo) != 'object')
    DeviceInfo = {};

/**
 * This represents the PhoneGap API itself, and provides a global namespace for accessing
 * information about the state of PhoneGap.
 * @class
 */
var PhoneGap = {
    queue: {
        ready: true,
        commands: [],
        timer: null
    }
};


/**
 * Custom pub-sub channel that can have functions subscribed to it
 */
PhoneGap.Channel = function(type)
{
    this.type = type;
    this.handlers = {};
    this.guid = 0;
    this.fired = false;
    this.enabled = true;
};

/**
 * Subscribes the given function to the channel. Any time that 
 * Channel.fire is called so too will the function.
 * Optionally specify an execution context for the function
 * and a guid that can be used to stop subscribing to the channel.
 * Returns the guid.
 */
PhoneGap.Channel.prototype.subscribe = function(f, c, g) {
    // need a function to call
    if (f == null) { return; }

    var func = f;
    if (typeof c == "object" && f instanceof Function) { func = PhoneGap.close(c, f); }

    g = g || func.observer_guid || f.observer_guid || this.guid++;
    func.observer_guid = g;
    f.observer_guid = g;
    this.handlers[g] = func;
    return g;
};

/**
 * Like subscribe but the function is only called once and then it
 * auto-unsubscribes itself.
 */
PhoneGap.Channel.prototype.subscribeOnce = function(f, c) {
    var g = null;
    var _this = this;
    var m = function() {
        f.apply(c || null, arguments);
        _this.unsubscribe(g);
    }
    if (this.fired) {
	    if (typeof c == "object" && f instanceof Function) { f = PhoneGap.close(c, f); }
        f.apply(this, this.fireArgs);
    } else {
        g = this.subscribe(m);
    }
    return g;
};

/** 
 * Unsubscribes the function with the given guid from the channel.
 */
PhoneGap.Channel.prototype.unsubscribe = function(g) {
    if (g instanceof Function) { g = g.observer_guid; }
    this.handlers[g] = null;
    delete this.handlers[g];
};

/** 
 * Calls all functions subscribed to this channel.
 */
PhoneGap.Channel.prototype.fire = function(e) {
    if (this.enabled) {
        var fail = false;
        for (var item in this.handlers) {
            var handler = this.handlers[item];
            if (handler instanceof Function) {
                var rv = (handler.apply(this, arguments)==false);
                fail = fail || rv;
            }
        }
        this.fired = true;
        this.fireArgs = arguments;
        return !fail;
    }
    return true;
};

/**
 * Calls the provided function only after all of the channels specified
 * have been fired.
 */
PhoneGap.Channel.join = function(h, c) {
    var i = c.length;
    var f = function() {
        if (!(--i)) h();
    }
    for (var j=0; j<i; j++) {
        (!c[j].fired?c[j].subscribeOnce(f):i--);
    }
    if (!i) h();
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
    PhoneGap.onDeviceReady.subscribeOnce(function() {
        try {
            func();
        } catch(e) {
            if (typeof(debug['log']) == 'function') {
                debug.log("Failed to run constructor: " + debug.processMessage(e));
            } else {
                alert("Failed to run constructor: " + e.message);
            }
        }
    });
};

/**
 * Adds a plugin object to window.plugins
 */
PhoneGap.addPlugin = function(name, obj) {
	if ( !window.plugins ) {
		window.plugins = {};
	}

	if ( !window.plugins[name] ) {
		window.plugins[name] = obj;
	}
}

/**
 * onDOMContentLoaded channel is fired when the DOM content 
 * of the page has been parsed.
 */
PhoneGap.onDOMContentLoaded = new PhoneGap.Channel('onDOMContentLoaded');

/**
 * onNativeReady channel is fired when the PhoneGap native code
 * has been initialized.
 */
PhoneGap.onNativeReady = new PhoneGap.Channel('onNativeReady');

/**
 * onResume channel is fired when the PhoneGap native code
 * resumes.
 */
PhoneGap.onResume = new PhoneGap.Channel('onResume');

/**
 * onPause channel is fired when the PhoneGap native code
 * pauses.
 */
PhoneGap.onPause = new PhoneGap.Channel('onPause');

// _nativeReady is global variable that the native side can set
// to signify that the native code is ready. It is a global since 
// it may be called before any PhoneGap JS is ready.
if (typeof _nativeReady !== 'undefined') { PhoneGap.onNativeReady.fire(); }

/**
 * onDeviceReady is fired only after both onDOMContentLoaded and 
 * onNativeReady have fired.
 */
PhoneGap.onDeviceReady = new PhoneGap.Channel('onDeviceReady');

PhoneGap.onDeviceReady.subscribeOnce(function() {
	PhoneGap.JSCallback();
});

PhoneGap.Channel.join(function() {
    PhoneGap.onDeviceReady.fire();

    // Fire the onresume event, since first one happens before JavaScript is loaded
    PhoneGap.onResume.fire();
}, [ PhoneGap.onDOMContentLoaded, PhoneGap.onNativeReady ]);


// Listen for DOMContentLoaded and notify our channel subscribers
document.addEventListener('DOMContentLoaded', function() {
    PhoneGap.onDOMContentLoaded.fire();
}, false);


// Intercept calls to document.addEventListener and watch for deviceready
PhoneGap.m_document_addEventListener = document.addEventListener;

document.addEventListener = function(evt, handler, capture) {
	var e = evt.toLowerCase();
    if (e == 'deviceready') {
        PhoneGap.onDeviceReady.subscribeOnce(handler);
    } else if (e == 'resume') {
        PhoneGap.onResume.subscribe(handler);
    } else if (e == 'pause') {
        PhoneGap.onPause.subscribe(handler);
    } else {
        PhoneGap.m_document_addEventListener.call(document, evt, handler);
    }
};

PhoneGap.callbackId = 0;
PhoneGap.callbacks = {};

/**
 * Execute a PhoneGap command in a queued fashion, to ensure commands do not
 * execute with any race conditions, and only run when PhoneGap is ready to
 * recieve them.
 * @param {String} command Command to be run in PhoneGap, e.g. "ClassName.method"
 * @param {String[]} [args] Zero or more arguments to pass to the method
 */
PhoneGap.exec = function(clazz, action, args) {
	return CommandManager.exec(clazz, action, callbackId, JSON.stringify(args), false);
};

PhoneGap.execAsync = function(success, fail, clazz, action, args) {
	var callbackId = clazz + PhoneGap.callbackId++;
	PhoneGap.callbacks[callbackId] = {success:success, fail:fail};
	return CommandManager.exec(clazz, action, callbackId, JSON.stringify(args), true);
};

PhoneGap.callbackSuccess = function(callbackId, args) {
	PhoneGap.callbacks[callbackId].success(args);
	delete PhoneGap.callbacks[callbackId];
};

PhoneGap.callbackError = function(callbackId, args) {
	PhoneGap.callbacks[callbackId].fail(args);
	delete PhoneGap.callbacks[callbackId];
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

/**
 * Internal function that uses XHR to call into PhoneGap Java code and retrieve 
 * any JavaScript code that needs to be run.  This is used for callbacks from
 * Java to JavaScript.
 */
PhoneGap.JSCallback = function() {
    var xmlhttp = new XMLHttpRequest();

    // Callback function when XMLHttpRequest is ready
    xmlhttp.onreadystatechange=function(){
        if(xmlhttp.readyState == 4){

            // If callback has JavaScript statement to execute
            if (xmlhttp.status == 200) {

                var msg = xmlhttp.responseText;
                setTimeout(function() {
                    try {
                        var t = eval(msg);
                    }
                    catch (e) {
                        console.log("JSCallback Error: "+e);
                    }
                }, 1);
                setTimeout(PhoneGap.JSCallback, 1);
            }

            // If callback ping (used to keep XHR request from timing out)
            else if (xmlhttp.status == 404) {
                setTimeout(PhoneGap.JSCallback, 10);
            }

            // If error, restart callback server
            else {
                console.log("JSCallback Error: Request failed.");
                CallbackServer.restartServer();
                setTimeout(PhoneGap.JSCallback, 100);
            }
        }
    }

    xmlhttp.open("GET", "http://127.0.0.1:"+CallbackServer.getPort()+"/" , true);
    xmlhttp.send();
};

/**
 * Create a UUID
 *
 * @return
 */
PhoneGap.createUUID = function() {
    return PhoneGap.UUIDcreatePart(4) + '-' +
        PhoneGap.UUIDcreatePart(2) + '-' +
        PhoneGap.UUIDcreatePart(2) + '-' +
        PhoneGap.UUIDcreatePart(2) + '-' +
        PhoneGap.UUIDcreatePart(6);
};

PhoneGap.UUIDcreatePart = function(length) {
    var uuidpart = "";
    for (var i=0; i<length; i++) {
        var uuidchar = parseInt((Math.random() * 256)).toString(16);
        if (uuidchar.length == 1) {
            uuidchar = "0" + uuidchar;
        }
        uuidpart += uuidchar;
    }
    return uuidpart;
};

PhoneGap.close = function(context, func, params) {
    if (typeof params === 'undefined') {
        return function() {
            return func.apply(context, arguments);
        }
    } else {
        return function() {
            return func.apply(context, params);
        }
    }
};

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
/**
 * This class provides access to the device camera.
 *
 * @constructor
 */
function Camera() {
    this.successCallback = null;
    this.errorCallback = null;
    this.options = null;
};

/**
 * Takes a photo and returns the image as a base64 encoded `String`.
 *
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
Camera.prototype.getPicture = function(successCallback, errorCallback, options) {

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Camera Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Camera Error: errorCallback is not a function");
        return;
    }

    this.successCallback = successCallback;
    this.errorCallback = errorCallback;
    this.options = options;
    if (options.quality) {
        GapCam.takePicture(options.quality);
    }
    else {
        GapCam.takePicture(80);
    }
};

/**
 * Callback function from native code that is called when image has been captured.
 *
 * @param picture           The base64 encoded string of the image
 */
Camera.prototype.success = function(picture) {
    if (this.successCallback) {
        this.successCallback(picture);
    }
};


/**
 * Callback function from native code that is called when there is an error
 * capturing an image, or the capture is cancelled.
 *
 * @param err               The error message
 */
Camera.prototype.error = function(err) {
    if (this.errorCallback) {
        this.errorCallback(err);
    }
};

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
var Crypto = function()
{
}

Crypto.prototype.encrypt = function(seed, string, callback)
{
	GapCrypto.encrypt(seed, string);
	this.encryptWin = callback;
}

Crypto.prototype.decrypt = function(seed, string, callback)
{
	GapCrypto.decrypt(seed, string);
	this.decryptWin = callback;
}

Crypto.prototype.gotCryptedString = function(string)
{
	this.encryptWin(string);
}

Crypto.prototype.getPlainString = function(string)
{
	this.decryptWin(string);
}

PhoneGap.addConstructor(function() {
  if (typeof navigator.Crypto == "undefined")
  {
    navigator.Crypto = new Crypto();
  }
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
            this.line1Number = window.DroidGap.getLine1Number();
            this.deviceId = window.DroidGap.getDeviceId();
            this.simSerialNumber = window.DroidGap.getSimSerialNumber();
            this.subscriberId = window.DroidGap.getSubscriberId();
        } 
    } catch(e) {
        this.available = false;
    }
}

/*
 * You must explicitly override the back button. 
 */

Device.prototype.overrideBackButton = function()
{
  BackButton.override();
}

/*
 * This resets the back button to the default behaviour
 */

Device.prototype.resetBackButton = function()
{
  BackButton.reset();
}

/*
 * This terminates the activity!
 */
Device.prototype.exitApp = function()
{
  BackButton.exitApp();
}

PhoneGap.addConstructor(function() {
    navigator.device = window.device = new Device();
});


PhoneGap.addConstructor(function() { if (typeof navigator.fileMgr == "undefined") navigator.fileMgr = new FileMgr();});


/**
 * This class provides iPhone read and write access to the mobile device file system.
 * Based loosely on http://www.w3.org/TR/2009/WD-FileAPI-20091117/#dfn-empty
 */
function FileMgr() 
{
	this.fileWriters = {}; // empty maps
	this.fileReaders = {};

	this.docsFolderPath = "../../Documents";
	this.tempFolderPath = "../../tmp";
	this.freeDiskSpace = -1;
	this.getFileBasePaths();
}

// private, called from Native Code
FileMgr.prototype._setPaths = function(docs,temp)
{
	this.docsFolderPath = docs;
	this.tempFolderPath = temp;
}

// private, called from Native Code
FileMgr.prototype._setFreeDiskSpace = function(val)
{
	this.freeDiskSpace = val;
}


// FileWriters add/remove
// called internally by writers
FileMgr.prototype.addFileWriter = function(filePath,fileWriter)
{
	this.fileWriters[filePath] = fileWriter;
}

FileMgr.prototype.removeFileWriter = function(filePath)
{
	this.fileWriters[filePath] = null;
}

// File readers add/remove
// called internally by readers
FileMgr.prototype.addFileReader = function(filePath,fileReader)
{
	this.fileReaders[filePath] = fileReader;
}

FileMgr.prototype.removeFileReader = function(filePath)
{
	this.fileReaders[filePath] = null;
}

/*******************************************
 *
 *	private reader callback delegation
 *	called from native code
 */
FileMgr.prototype.reader_onloadstart = function(filePath,result)
{
	this.fileReaders[filePath].onloadstart(result);
}

FileMgr.prototype.reader_onprogress = function(filePath,result)
{
	this.fileReaders[filePath].onprogress(result);
}

FileMgr.prototype.reader_onload = function(filePath,result)
{
	this.fileReaders[filePath].result = unescape(result);
	this.fileReaders[filePath].onload(this.fileReaders[filePath].result);
}

FileMgr.prototype.reader_onerror = function(filePath,err)
{
	this.fileReaders[filePath].result = err;
	this.fileReaders[filePath].onerror(err);
}

FileMgr.prototype.reader_onloadend = function(filePath,result)
{
	this.fileReaders[filePath].onloadend(result);
}

/*******************************************
 *
 *	private writer callback delegation
 *	called from native code
*/
FileMgr.prototype.writer_onerror = function(filePath,err)
{
	this.fileWriters[filePath].onerror(err);
}

FileMgr.prototype.writer_oncomplete = function(filePath,result)
{
	this.fileWriters[filePath].oncomplete(result); // result contains bytes written
}


FileMgr.prototype.getFileBasePaths = function()
{
	//PhoneGap.exec("File.getFileBasePaths");
}

FileMgr.prototype.testFileExists = function(fileName, successCallback, errorCallback)
{
	var test = FileUtil.testFileExists(fileName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.testDirectoryExists = function(dirName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	var test = FileUtil.testDirectoryExists(dirName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.createDirectory = function(dirName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	var test = FileUtil.createDirectory(dirName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.deleteDirectory = function(dirName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	var test = FileUtil.deleteDirectory(dirName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.deleteFile = function(fileName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	FileUtil.deleteFile(fileName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.getFreeDiskSpace = function(successCallback, errorCallback)
{
	if(this.freeDiskSpace > 0)
	{
		return this.freeDiskSpace;
	}
	else
	{
		this.successCallback = successCallback;
		this.errorCallback = errorCallback;
		this.freeDiskSpace = FileUtil.getFreeDiskSpace();
  		(this.freeDiskSpace > 0) ? successCallback() : errorCallback();
	}
}


// File Reader


function FileReader()
{
	this.fileName = "";
	this.result = null;
	this.onloadstart = null;
	this.onprogress = null;
	this.onload = null;
	this.onerror = null;
	this.onloadend = null;
}


FileReader.prototype.abort = function()
{
	// Not Implemented
}

FileReader.prototype.readAsText = function(file)
{
	if(this.fileName && this.fileName.length > 0)
	{
		navigator.fileMgr.removeFileReader(this.fileName,this);
	}
	this.fileName = file;
	navigator.fileMgr.addFileReader(this.fileName,this);

  	return FileUtil.read(this.fileName);
}

// File Writer

function FileWriter()
{
	this.fileName = "";
	this.result = null;
	this.readyState = 0; // EMPTY
	this.result = null;
	this.onerror = null;
	this.oncomplete = null;
}

FileWriter.prototype.writeAsText = function(file,text,bAppend)
{
	if(this.fileName && this.fileName.length > 0)
	{
		navigator.fileMgr.removeFileWriter(this.fileName,this);
	}
	this.fileName = file;
	if(bAppend != true)
	{
		bAppend = false; // for null values
	}
	navigator.fileMgr.addFileWriter(file,this);
	this.readyState = 0; // EMPTY
  	var call = FileUtil.write(file, text, bAppend);
	this.result = null;
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
    coords = new Coordinates(lat, lng, alt, acc, head, vel);
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
  var coords = new Coordinates(lat, lng, alt, acc, head, vel);
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
	// In the case of Android, we can use the Native Geolocation Object if it exists, so only load this on 1.x devices
  if (typeof navigator.geolocation == 'undefined') {
		navigator.geolocation = new Geolocation();
	}
});
function KeyEvent() 
{
}

KeyEvent.prototype.backTrigger = function()
{
  var e = document.createEvent('Events');
  e.initEvent('backKeyDown');
  document.dispatchEvent(e);
}

if (document.keyEvent == null || typeof document.keyEvent == 'undefined')
{
  window.keyEvent = document.keyEvent = new KeyEvent();
}
/**
 * List of media objects.
 */
PhoneGap.mediaObjects = {};

PhoneGap.Media = function() {};

/**
 * Get the media object.
 * PRIVATE
 *
 * @param id            The media object id (string)
 */
PhoneGap.Media.getMediaObject = function(id) {
    return PhoneGap.mediaObjects[id];
};

/**
 * Audio has status update.
 * PRIVATE
 *
 * @param id            The media object id (string)
 * @param status        The status code (int)
 * @param msg           The status message (string)
 */
PhoneGap.Media.onStatus = function(id, msg, value) {
    var media = PhoneGap.mediaObjects[id];

    // If state update
    if (msg == Media.MEDIA_STATE) {
        if (value == Media.MEDIA_STOPPED) {
            if (media.successCallback) {
                media.successCallback();
            }
        }
        if (media.statusCallback) {
            media.statusCallback(value);
        }
    }
    else if (msg == Media.MEDIA_DURATION) {
        media._duration = value;
    }
    else if (msg == Media.MEDIA_ERROR) {
        if (media.errorCallback) {
            media.errorCallback(value);
        }
    }
};

/**
 * This class provides access to the device media, interfaces to both sound and video
 *
 * @param src                   The file name or url to play
 * @param successCallback       The callback to be called when the file is done playing or recording.
 *                                  successCallback()
 * @param errorCallback         The callback to be called if there is an error.
 *                                  errorCallback(int errorCode)
 * @param statusCallback        The callback to be called when media status has changed.
 *                                  statusCallback(int statusCode)
 */
Media = function(src, successCallback, errorCallback, statusCallback) {
    this.id = PhoneGap.createUUID();
    PhoneGap.mediaObjects[this.id] = this;
    this.src = src;
    this.successCallback = successCallback;
    this.errorCallback = errorCallback;
    this.statusCallback = statusCallback;
    this._duration = -1;
};

// Media messages
Media.MEDIA_STATE = 1;
Media.MEDIA_DURATION = 2;
Media.MEDIA_ERROR = 3;

// Media states
Media.MEDIA_NONE = 0;
Media.MEDIA_STARTING = 1;
Media.MEDIA_RUNNING = 2;
Media.MEDIA_PAUSED = 3;
Media.MEDIA_STOPPED = 4;
Media.MEDIA_MSG = ["None", "Starting", "Running", "Paused", "Stopped"];

// TODO: Will MediaError be used?
/**
 * This class contains information about any Media errors.
 * @constructor
 */
function MediaError() {
    this.code = null,
    this.message = "";
};

MediaError.MEDIA_ERR_ABORTED        = 1;
MediaError.MEDIA_ERR_NETWORK        = 2;
MediaError.MEDIA_ERR_DECODE         = 3;
MediaError.MEDIA_ERR_NONE_SUPPORTED = 4;

/**
 * Start or resume playing audio file.
 */
Media.prototype.play = function() {
    GapAudio.startPlayingAudio(this.id, this.src);
};

/**
 * Stop playing audio file.
 */
Media.prototype.stop = function() {
    GapAudio.stopPlayingAudio(this.id);
};

/**
 * Pause playing audio file.
 */
Media.prototype.pause = function() {
    GapAudio.pausePlayingAudio(this.id);
};

/**
 * Get duration of an audio file.
 * The duration is only set for audio that is playing, paused or stopped.
 *
 * @return      duration or -1 if not known.
 */
Media.prototype.getDuration = function() {
    return this._duration;
};

/**
 * Get position of audio.
 *
 * @return
 */
Media.prototype.getCurrentPosition = function() {
    return GapAudio.getCurrentPositionAudio(this.id);
};

/**
 * Start recording audio file.
 */
Media.prototype.startRecord = function() {
    GapAudio.startRecordingAudio(this.id, this.src);
};

/**
 * Stop recording audio file.
 */
Media.prototype.stopRecord = function() {
    GapAudio.stopRecordingAudio(this.id);
};

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
 * Called by the geolocation framework when the reachability status has changed.
 * @param {Reachibility} reachability The current reachability status.
 */
Network.prototype.updateReachability = function(reachability) {
    this.lastReachability = reachability;
};
/**
 * 
 * @param {Object} uri
 * @param {Function} win
 * @param {Object} options  (isIpAddress:boolean)
 */
Network.prototype.isReachable = function(uri, win, options)
{
  var status = new NetworkStatus();
  if(NetworkManager.isReachable(uri))
  {
    if (NetworkManager.isWifiActive()) {
      status.code = NetworkStatus.REACHABLE_VIA_WIFI_NETWORK;
    } else {
      status.code = NetworkStatus.REACHABLE_VIA_CARRIER_DATA_NETWORK;
	}
  } else {
    status.code = NetworkStatus.NOT_REACHABLE;
  }
  win(status);
};
PhoneGap.addConstructor(function() {
    if (typeof navigator.network == "undefined") navigator.network = new Network();
});/**
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

function Coordinates(lat, lng, alt, acc, head, vel, altacc) {
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
	/**
	 * The altitude accuracy of the position.
	 */
	this.altitudeAccuracy = (altacc != 'undefined') ? altacc : null; 
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
PhoneGap.addConstructor(function() {
    if (typeof navigator.splashScreen == "undefined") {
    	navigator.splashScreen = SplashScreen;  // SplashScreen object come from native side through addJavaScriptInterface
    }
});/*
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
  if (typeof window.openDatabase == "undefined") 
  {
    navigator.openDatabase = window.openDatabase = dbSetup;
    window.droiddb = new DroidDB();
  }
});

