
/**
 * The order of events during page load and PhoneGap startup is as follows:
 *
 * onDOMContentLoaded         Internal event that is received when the web page is loaded and parsed.
 * window.onload              Body onload event.
 * onNativeReady              Internal event that indicates the PhoneGap native side is ready.
 * onPhoneGapInit             Internal event that kicks off creation of all PhoneGap JavaScript objects (runs constructors).
 * onPhoneGapReady            Internal event fired when all PhoneGap JavaScript objects have been created
 * onPhoneGapInfoReady        Internal event fired when device properties are available
 * onDeviceReady              User event fired to indicate that PhoneGap is ready
 * onResume                   User event fired to indicate a start/resume lifecycle event
 *
 * The only PhoneGap events that user code should register for are:
 *      onDeviceReady
 *      onResume
 *
 * Listeners can be registered as:
 *      document.addEventListener("deviceready", myDeviceReadyListener, false);
 *      document.addEventListener("resume", myResumeListener, false);
 */

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
    PhoneGap.onPhoneGapInit.subscribeOnce(function() {
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
 * onPhoneGapInit channel is fired when the web page is fully loaded and
 * PhoneGap native code has been initialized.
 */
PhoneGap.onPhoneGapInit = new PhoneGap.Channel('onPhoneGapInit');

/**
 * onPhoneGapReady channel is fired when the JS PhoneGap objects have been created.
 */
PhoneGap.onPhoneGapReady = new PhoneGap.Channel('onPhoneGapReady');

/**
 * onPhoneGapInfoReady channel is fired when the PhoneGap device properties
 * has been set.
 */
PhoneGap.onPhoneGapInfoReady = new PhoneGap.Channel('onPhoneGapInfoReady');

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
 * onDeviceReady is fired only after all PhoneGap objects are created and
 * the device properties are set.
 */
PhoneGap.onDeviceReady = new PhoneGap.Channel('onDeviceReady');


/**
 * Create all PhoneGap objects once page has fully loaded and native side is ready.
 */
PhoneGap.Channel.join(function() {

    // Start listening for XHR callbacks
    PhoneGap.JSCallback();

    // Run PhoneGap constructors
    PhoneGap.onPhoneGapInit.fire();

    // Fire event to notify that all objects are created
    PhoneGap.onPhoneGapReady.fire();

}, [ PhoneGap.onDOMContentLoaded, PhoneGap.onNativeReady ]);

/**
 * Fire onDeviceReady event once all constructors have run and PhoneGap info has been
 * received from native side.
 */
PhoneGap.Channel.join(function() {
    PhoneGap.onDeviceReady.fire();

    // Fire the onresume event, since first one happens before JavaScript is loaded
    PhoneGap.onResume.fire();
}, [ PhoneGap.onPhoneGapReady, PhoneGap.onPhoneGapInfoReady]);

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

/**
 * If JSON not included, use our own stringify. (Android 1.6)
 * The restriction on ours is that it must be an array of simple types.
 *
 * @param args
 * @return
 */
PhoneGap.stringify = function(args) {
    if (typeof JSON == "undefined") {
        var s = "[";
        for (var i=0; i<args.length; i++) {
            if (i > 0) {
                s = s + ",";
            }
            var type = typeof args[i];
            if ((type == "number") || (type == "boolean")) {
                s = s + args[i];
            }
            else if (args[i] instanceof Array) {
            	s = s + "[" + args[i] + "]";
            }
            else if (args[i] instanceof Object) {
            	var start = true;
            	s = s + '{';
            	for (var name in args[i]) {
            		if (!start) {
            			s = s + ',';
            		}
            		s = s + '"' + name + '":';
            		var nameType = typeof args[i][name];
            		if ((nameType == "number") || (nameType == "boolean")) {
            			s = s + args[i][name];
            		}
            		else {
                        s = s + '"' + args[i][name] + '"';            			
            		}
                    start=false;
            	} 
            	s = s + '}';
            }
            else {
                s = s + '"' + args[i] + '"';
            }
        }
        s = s + "]";
        return s;
    }
    else {
        return JSON.stringify(args);
    }
};

/**
 * Does a deep clone of the object.
 *
 * @param obj
 * @return
 */
PhoneGap.clone = function(obj) {
	if(!obj) { 
		return obj;
	}
	
	if(obj instanceof Array){
		var retVal = new Array();
		for(var i = 0; i < obj.length; ++i){
			retVal.push(PhoneGap.clone(obj[i]));
		}
		return retVal;
	}
	
	if (obj instanceof Function) {
		return obj;
	}
	
	if(!(obj instanceof Object)){
		return obj;
	}

	retVal = new Object();
	for(i in obj){
		if(!(i in retVal) || retVal[i] != obj[i]) {
			retVal[i] = PhoneGap.clone(obj[i]);
		}
	}
	return retVal;
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
// TODO: Not used anymore, should be removed.
PhoneGap.exec = function(clazz, action, args) {
    try {
        var callbackId = 0;
        var r = PluginManager.exec(clazz, action, callbackId, this.stringify(args), false);
        eval("var v="+r+";");
        
        // If status is OK, then return value back to caller
        if (v.status == 0) {
            return v.message;
        }

        // If error, then display error
        else {
            console.log("Error: Status="+r.status+" Message="+v.message);
            return null;
        }
    } catch (e) {
        console.log("Error: "+e);
    }
};

/**
 * Execute a PhoneGap command.  It is up to the native side whether this action is synch or async.  
 * The native side can return:
 *      Synchronous: PluginResult object as a JSON string
 *      Asynchrounous: Empty string ""
 * If async, the native side will PhoneGap.callbackSuccess or PhoneGap.callbackError,
 * depending upon the result of the action.
 *
 * @param {Function} success    The success callback
 * @param {Function} fail       The fail callback
 * @param {String} service      The name of the service to use
 * @param {String} action       Action to be run in PhoneGap
 * @param {String[]} [args]     Zero or more arguments to pass to the method
 */
PhoneGap.execAsync = function(success, fail, service, action, args) {
    try {
        var callbackId = service + PhoneGap.callbackId++;
        if (success || fail) {
            PhoneGap.callbacks[callbackId] = {success:success, fail:fail};
        }
        
        // Note: Device returns string, but for some reason emulator returns object - so convert to string.
        var r = ""+PluginManager.exec(service, action, callbackId, this.stringify(args), true);
        
        // If a result was returned
        if (r.length > 0) {
            eval("var v="+r+";");
        
            // If status is OK, then return value back to caller
            if (v.status == 0) {

                // If there is a success callback, then call it now with returned value
                if (success) {
                    success(v.message);
                    delete PhoneGap.callbacks[callbackId];
                }
                return v.message;
            }

            // If error, then display error
            else {
                console.log("Error: Status="+r.status+" Message="+v.message);

                // If there is a fail callback, then call it now with returned value
                if (fail) {
                    fail(v.message);
                    delete PhoneGap.callbacks[callbackId];
                }
                return null;
            }
        }
    } catch (e) {
        console.log("Error: "+e);
    }
};

/**
 * Called by native code when returning successful result from an action.
 *
 * @param callbackId
 * @param args
 */
PhoneGap.callbackSuccess = function(callbackId, args) {
    if (PhoneGap.callbacks[callbackId]) {
        try {
            if (PhoneGap.callbacks[callbackId].success) {
                PhoneGap.callbacks[callbackId].success(args.message);
            }
        }
        catch (e) {
            console.log("Error in success callback: "+callbackId+" = "+e);
        }
        delete PhoneGap.callbacks[callbackId];
    }
};

/**
 * Called by native code when returning error result from an action.
 *
 * @param callbackId
 * @param args
 */
PhoneGap.callbackError = function(callbackId, args) {
    if (PhoneGap.callbacks[callbackId]) {
        try {
            if (PhoneGap.callbacks[callbackId].fail) {
                PhoneGap.callbacks[callbackId].fail(args.message);
            }
        }
        catch (e) {
            console.log("Error in error callback: "+callbackId+" = "+e);
        }
        delete PhoneGap.callbacks[callbackId];
    }
};


/**
 * Internal function used to dispatch the request to PhoneGap.  It processes the
 * command queue and executes the next command on the list.  If one of the
 * arguments is a JavaScript object, it will be passed on the QueryString of the
 * url, which will be turned into a dictionary on the other end.
 * @private
 */
// TODO: Is this used?
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
 * This is only for Android.
 *
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
};

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
};

Accelerometer.ERROR_MSG = ["Not running", "Starting", "", "Failed to start"];

/**
 * Asynchronously aquires the current acceleration.
 *
 * @param {Function} successCallback    The function to call when the acceleration data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the acceleration data. (OPTIONAL)
 * @param {AccelerationOptions} options The options for getting the accelerometer data such as timeout. (OPTIONAL)
 */
Accelerometer.prototype.getCurrentAcceleration = function(successCallback, errorCallback, options) {
    console.log("Accelerometer.getCurrentAcceleration()");

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

    // Get acceleration
    PhoneGap.execAsync(successCallback, errorCallback, "Accelerometer", "getAcceleration", []);
};

/**
 * Asynchronously aquires the acceleration repeatedly at a given interval.
 *
 * @param {Function} successCallback    The function to call each time the acceleration data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the acceleration data. (OPTIONAL)
 * @param {AccelerationOptions} options The options for getting the accelerometer data such as timeout. (OPTIONAL)
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
    PhoneGap.execAsync(
        function(timeout) {
            if (timeout < (frequency + 10000)) {
                PhoneGap.execAsync(null, null, "Accelerometer", "setTimeout", [frequency + 10000]);
            }
        },
        function(e) { }, "Accelerometer", "getTimeout", []);

    // Start watch timer
    var id = PhoneGap.createUUID();
    navigator.accelerometer.timers[id] = setInterval(function() {
        PhoneGap.execAsync(successCallback, errorCallback, "Accelerometer", "getAcceleration", []);
    }, (frequency ? frequency : 1));

    return id;
};

/**
 * Clears the specified accelerometer watch.
 *
 * @param {String} id       The id of the watch returned from #watchAcceleration.
 */
Accelerometer.prototype.clearWatch = function(id) {

    // Stop javascript timer & remove from timer list
    if (id && navigator.accelerometer.timers[id] != undefined) {
        clearInterval(navigator.accelerometer.timers[id]);
        delete navigator.accelerometer.timers[id];
    }
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
});

/**
 * This class provides access to the device camera.
 *
 * @constructor
 */
Camera = function() {
    this.successCallback = null;
    this.errorCallback = null;
    this.options = null;
};

/**
 * Format of image that returned from getPicture.
 *
 * Example: navigator.camera.getPicture(success, fail,
 *              { quality: 80,
 *                destinationType: Camera.DestinationType.DATA_URL,
 *                sourceType: Camera.PictureSourceType.PHOTOLIBRARY})
 */
Camera.DestinationType = {
    DATA_URL: 0,                // Return base64 encoded string
    FILE_URI: 1                 // Return file uri (content://media/external/images/media/2 for Android)
};
Camera.prototype.DestinationType = Camera.DestinationType;

/**
 * Source to getPicture from.
 *
 * Example: navigator.camera.getPicture(success, fail,
 *              { quality: 80,
 *                destinationType: Camera.DestinationType.DATA_URL,
 *                sourceType: Camera.PictureSourceType.PHOTOLIBRARY})
 */
Camera.PictureSourceType = {
    PHOTOLIBRARY : 0,           // Choose image from picture library (same as SAVEDPHOTOALBUM for Android)
    CAMERA : 1,                 // Take picture from camera
    SAVEDPHOTOALBUM : 2         // Choose image from picture library (same as PHOTOLIBRARY for Android)
};
Camera.prototype.PictureSourceType = Camera.PictureSourceType;

/**
 * Gets a picture from source defined by "options.sourceType", and returns the
 * image as defined by the "options.destinationType" option.

 * The defaults are sourceType=CAMERA and destinationType=DATA_URL.
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
    var quality = 80;
    if (options.quality) {
        quality = this.options.quality;
    }
    var destinationType = Camera.DestinationType.DATA_URL;
    if (this.options.destinationType) {
        destinationType = this.options.destinationType;
    }
    var sourceType = Camera.PictureSourceType.CAMERA;
    if (typeof this.options.sourceType == "number") {
        sourceType = this.options.sourceType;
    }
    PhoneGap.execAsync(null, null, "Camera", "takePicture", [quality, destinationType, sourceType]);
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

Compass.ERROR_MSG = ["Not running", "Starting", "", "Failed to start"];

/**
 * Asynchronously aquires the current heading.
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 * @param {PositionOptions} options The options for getting the heading data such as timeout. (OPTIONAL)
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

    // Get heading
    PhoneGap.execAsync(successCallback, errorCallback, "Compass", "getHeading", []);
};

/**
 * Asynchronously aquires the heading repeatedly at a given interval.
 *
 * @param {Function} successCallback    The function to call each time the heading data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the heading data. (OPTIONAL)
 * @param {HeadingOptions} options      The options for getting the heading data such as timeout and the frequency of the watch. (OPTIONAL)
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
    PhoneGap.execAsync(
        function(timeout) {
            if (timeout < (frequency + 10000)) {
                PhoneGap.execAsync(null, null, "Compass", "setTimeout", [frequency + 10000]);
            }
        },
        function(e) { }, "Compass", "getTimeout", []);

    // Start watch timer to get headings
    var id = PhoneGap.createUUID();
    navigator.compass.timers[id] = setInterval(
        function() {
            PhoneGap.execAsync(successCallback, errorCallback, "Compass", "getHeading", []);
        }, (frequency ? frequency : 1));

    return id;
};


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
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.compass == "undefined") navigator.compass = new Compass();
});

var Contact = function(id, displayName, name, nickname, phoneNumbers, emails, addresses,
    ims, organizations, published, updated, birthday, anniversary, gender, note,
    preferredUsername, photos, tags, relationships, urls, accounts, utcOffset, connected) {
    this.id = id || null;
    this.displayName = displayName || null;
    this.name = name || null; // ContactName
    this.nickname = nickname || null;
    this.phoneNumbers = phoneNumbers || null; // ContactField[]
    this.emails = emails || null; // ContactField[]
    this.addresses = addresses || null; // ContactAddress[]
    this.ims = ims || null; // ContactField[]
    this.organizations = organizations || null; // ContactOrganization[]
    this.published = published || null;
    this.updated = updated || null;
    this.birthday = birthday || null;
    this.anniversary = anniversary || null;
    this.gender = gender || null;
    this.note = note || null;
    this.preferredUsername = preferredUsername || null;
    this.photos = photos || null; // ContactField[]
    this.tags = tags || null; // ContactField[]
    this.relationships = relationships || null; // ContactField[]
    this.urls = urls || null; // ContactField[]
    this.accounts = accounts || null; // ContactAccount[]
    this.utcOffset = utcOffset || null;
    this.connected = connected || null;
};


Contact.prototype.remove = function(successCB, errorCB) {
	if (this.id == null) {
		var errorObj = new ContactError();
		errorObj.code = ContactError.NOT_FOUND_ERROR;
		errorCB(errorObj);
	}
	
    PhoneGap.execAsync(successCB, errorCB, "Contacts", "remove", [this.id]);	
};

Contact.prototype.clone = function() {
	var clonedContact = PhoneGap.clone(this);
	clonedContact.id = null;
    return clonedContact;
};

Contact.prototype.save = function(win, fail) {
};


var ContactName = function(formatted, familyName, givenName, middle, prefix, suffix) {
    this.formatted = formatted || null;
    this.familyName = familyName || null;
    this.givenName = givenName || null;
    this.middleName = middle || null;
    this.honorificPrefix = prefix || null;
    this.honorificSuffix = suffix || null;
};

var ContactField = function(type, value, primary) {
    this.type = type || null;
    this.value = value || null;
    this.primary = primary || null;
};

var ContactAddress = function(formatted, streetAddress, locality, region, postalCode, country) {
    this.formatted = formatted || null;
    this.streetAddress = streetAddress || null;
    this.locality = locality || null;
    this.region = region || null;
    this.postalCode = postalCode || null;
    this.country = country || null;
};

var ContactOrganization = function(name, dept, title, startDate, endDate, location, desc) {
    this.name = name || null;
    this.department = dept || null;
    this.title = title || null;
    this.startDate = startDate || null;
    this.endDate = endDate || null;
    this.location = location || null;
    this.description = desc || null;
};

var ContactAccount = function(domain, username, userid) {
    this.domain = domain || null;
    this.username = username || null;
    this.userid = userid || null;
}

var Contacts = function() {
    this.inProgress = false;
    this.records = new Array();
}

Contacts.prototype.find = function(fields, win, fail, options) {
    PhoneGap.execAsync(win, fail, "Contacts", "search", [fields, options]);
};

//This function does not create a new contact in the db.  
//Must call contact.save() for it to be persisted in the db.
Contacts.prototype.create = function(properties) {
	var contact = new Contact();
	for (i in properties) {
		if (contact[i]!='undefined') {
			contact[i]=properties[i];
		}
	}
	return contact;
};

Contacts.prototype.droidDone = function(contacts) {
    this.win(eval('(' + contacts + ')'));
};

Contacts.prototype.m_foundContacts = function(win, contacts) {
    this.inProgress = false;
    win(contacts);
};

var ContactFindOptions = function(filter, multiple, limit, updatedSince) {
    this.filter = filter || '';
    this.multiple = multiple || true;
    this.limit = limit || Number.MAX_VALUE;
    this.updatedSince = updatedSince || '';
};

var ContactError = function() {
    this.code=null;
};

ContactError.UNKNOWN_ERROR = 0;
ContactError.INVALID_ARGUMENT_ERROR = 1;
ContactError.NOT_FOUND_ERROR = 2;
ContactError.TIMEOUT_ERROR = 3;
ContactError.PENDING_OPERATION_ERROR = 4;
ContactError.IO_ERROR = 5;
ContactError.NOT_SUPPORTED_ERROR = 6;
ContactError.PERMISSION_DENIED_ERROR = 20;

PhoneGap.addConstructor(function() {
    if(typeof navigator.service == "undefined") navigator.service = new Object();
    if(typeof navigator.service.contacts == "undefined") navigator.service.contacts = new Contacts();
});

var Crypto = function() {
};

Crypto.prototype.encrypt = function(seed, string, callback) {
    this.encryptWin = callback;
    PhoneGap.execAsync(null, null, "Crypto", "encrypt", [seed, string]);
};

Crypto.prototype.decrypt = function(seed, string, callback) {
    this.decryptWin = callback;
    PhoneGap.execAsync(null, null, "Crypto", "decrypt", [seed, string]);
};

Crypto.prototype.gotCryptedString = function(string) {
    this.encryptWin(string);
};

Crypto.prototype.getPlainString = function(string) {
    this.decryptWin(string);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.Crypto == "undefined") navigator.Crypto = new Crypto();
});

/**
 * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function Device() {
    this.available = PhoneGap.available;
    this.platform = null;
    this.version = null;
    this.name = null;
    this.uuid = null;
    this.phonegap = null;

    var me = this;
    this.getInfo(
        function(info) {
            me.available = true;
            me.platform = info.platform;
            me.version = info.version;
            me.uuid = info.uuid;
            me.phonegap = info.phonegap;
            PhoneGap.onPhoneGapInfoReady.fire();
        },
        function(e) {
            me.available = false;
            console.log("Error initializing PhoneGap: " + e);
            alert("Error initializing PhoneGap: "+e);
        });
}

/**
 * Get device info
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
Device.prototype.getInfo = function(successCallback, errorCallback) {

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Device Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Device Error: errorCallback is not a function");
        return;
    }

    // Get info
    PhoneGap.execAsync(successCallback, errorCallback, "Device", "getDeviceInfo", []);
};

/*
 * This is only for Android.
 *
 * You must explicitly override the back button.
 */
Device.prototype.overrideBackButton = function() {
    BackButton.override();
}

/*
 * This is only for Android.
 *
 * This resets the back button to the default behaviour
 */
Device.prototype.resetBackButton = function() {
    BackButton.reset();
}

/*
 * This is only for Android.
 *
 * This terminates the activity!
 */
Device.prototype.exitApp = function() {
    BackButton.exitApp();
}

PhoneGap.addConstructor(function() {
    navigator.device = window.device = new Device();
});
/**
 * This class provides generic read and write access to the mobile device file system.
 * They are not used to read files from a server.
 */

/**
 * List of files
 */
function FileList() {
    this.files = {};
};

/**
 * Describes a single file in a FileList
 */
function File() {
    this.name = null;
    this.type = null;
    this.urn = null;
};

/**
 * Create an event object since we can't set target on DOM event.
 *
 * @param type
 * @param target
 *
 */
File._createEvent = function(type, target) {
    // Can't create event object, since we can't set target (its readonly)
    //var evt = document.createEvent('Events');
    //evt.initEvent("onload", false, false);
    var evt = {"type": type};
    evt.target = target;
    return evt;
};

function FileError() {
   // File error codes
   // Found in DOMException
   this.NOT_FOUND_ERR = 8;
   this.SECURITY_ERR = 18;
   this.ABORT_ERR = 20;

   // Added by this specification
   this.NOT_READABLE_ERR = 24;
   this.ENCODING_ERR = 26;

   this.code = null;
};

//-----------------------------------------------------------------------------
// File manager
//-----------------------------------------------------------------------------

function FileMgr() {
};

FileMgr.prototype.getFileBasePaths = function() {
};

FileMgr.prototype.testSaveLocationExists = function(successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "testSaveLocationExists", []);
};

FileMgr.prototype.testFileExists = function(fileName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "testFileExists", [fileName]);
};

FileMgr.prototype.testDirectoryExists = function(dirName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "testDirectoryExists", [dirName]);
};

FileMgr.prototype.createDirectory = function(dirName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "createDirectory", [dirName]);
};

FileMgr.prototype.deleteDirectory = function(dirName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "deleteDirectory", [dirName]);
};

FileMgr.prototype.deleteFile = function(fileName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "deleteFile", [fileName]);
};

FileMgr.prototype.getFreeDiskSpace = function(successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "getFreeDiskSpace", []);
};

FileMgr.prototype.writeAsText = function(fileName, data, append, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "writeAsText", [fileName, data, append]);
};

FileMgr.prototype.readAsText = function(fileName, encoding, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "readAsText", [fileName, encoding]);
};

FileMgr.prototype.readAsDataURL = function(fileName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "readAsDataURL", [fileName]);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.fileMgr == "undefined") navigator.fileMgr = new FileMgr();
});

//-----------------------------------------------------------------------------
// File Reader
//-----------------------------------------------------------------------------
// TODO: All other FileMgr function operate on the SD card as root.  However,
//       for FileReader & FileWriter the root is not SD card.  Should this be changed?

/**
 * This class reads the mobile device file system.
 *
 * For Android:
 *      The root directory is the root of the file system.
 *      To read from the SD card, the file name is "sdcard/my_file.txt"
 */
function FileReader() {
    this.fileName = "";

    this.readyState = 0;

    // File data
    this.result = null;

    // Error
    this.error = null;

    // Event handlers
    this.onloadstart = null;    // When the read starts.
    this.onprogress = null;     // While reading (and decoding) file or fileBlob data, and reporting partial file data (progess.loaded/progress.total)
    this.onload = null;         // When the read has successfully completed.
    this.onerror = null;        // When the read has failed (see errors).
    this.onloadend = null;      // When the request has completed (either in success or failure).
    this.onabort = null;        // When the read has been aborted. For instance, by invoking the abort() method.
};

// States
FileReader.EMPTY = 0;
FileReader.LOADING = 1;
FileReader.DONE = 2;

/**
 * Abort reading file.
 */
FileReader.prototype.abort = function() {
    this.readyState = FileReader.DONE;

    // If abort callback
    if (typeof this.onabort == "function") {
        var evt = File._createEvent("abort", this);
        this.onabort(evt);
    }

    // TODO: Anything else to do?  Maybe sent to native?
};

/**
 * Read text file.
 *
 * @param file          The name of the file
 * @param encoding      [Optional] (see http://www.iana.org/assignments/character-sets)
 */
FileReader.prototype.readAsText = function(file, encoding) {
    this.fileName = file;

    // LOADING state
    this.readyState = FileReader.LOADING;

    // If loadstart callback
    if (typeof this.onloadstart == "function") {
        var evt = File._createEvent("loadstart", this);
        this.onloadstart(evt);
    }

    // Default encoding is UTF-8
    var enc = encoding ? encoding : "UTF-8";

    var me = this;

    // Read file
    navigator.fileMgr.readAsText(file, enc,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // DONE state
            me.readyState = FileReader.DONE;

            // If onload callback
            if (typeof me.onload == "function") {
                var evt = File._createEvent("load", me);
                me.onload(evt);
            }

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // DONE state
            me.readyState = FileReader.DONE;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        }
        );
};


/**
 * Read file and return data as a base64 encoded data url.
 * A data url is of the form:
 *      data:[<mediatype>][;base64],<data>
 *
 * @param file          The name of the file
 */
FileReader.prototype.readAsDataURL = function(file) {
    this.fileName = file;

    // LOADING state
    this.readyState = FileReader.LOADING;

    // If loadstart callback
    if (typeof this.onloadstart == "function") {
        var evt = File._createEvent("loadstart", this);
        this.onloadstart(evt);
    }

    var me = this;

    // Read file
    navigator.fileMgr.readAsDataURL(file,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // DONE state
            me.readyState = FileReader.DONE;

            // If onload callback
            if (typeof me.onload == "function") {
                var evt = File._createEvent("load", me);
                me.onload(evt);
            }

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // DONE state
            me.readyState = FileReader.DONE;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        }
        );
};

/**
 * Read file and return data as a binary data.
 *
 * @param file          The name of the file
 */
FileReader.prototype.readAsBinaryString = function(file) {
    // TODO - Can't return binary data to browser.
    this.fileName = file;
};

//-----------------------------------------------------------------------------
// File Writer
//-----------------------------------------------------------------------------

/**
 * This class writes to the mobile device file system.
 *
 * For Android:
 *      The root directory is the root of the file system.
 *      To write to the SD card, the file name is "sdcard/my_file.txt"
 */
function FileWriter() {
    this.fileName = "";
    this.result = null;
    this.readyState = 0; // EMPTY
    this.result = null;
    this.onerror = null;
    this.oncomplete = null;
};

// States
FileWriter.EMPTY = 0;
FileWriter.LOADING = 1;
FileWriter.DONE = 2;

FileWriter.prototype.writeAsText = function(file, text, bAppend) {
    if (bAppend != true) {
        bAppend = false; // for null values
    }

    this.fileName = file;

    // LOADING state
    this.readyState = FileWriter.LOADING;

    var me = this;

    // Read file
    navigator.fileMgr.writeAsText(file, text, bAppend,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // DONE state
            me.readyState = FileWriter.DONE;

            // If oncomplete callback
            if (typeof me.oncomplete == "function") {
                var evt = File._createEvent("complete", me);
                me.oncomplete(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }
        }
        );

};

/**
 * This class provides access to device GPS data.
 * @constructor
 */
function Geolocation() {

    // The last known GPS position.
    this.lastPosition = null;

    // Geolocation listeners
    this.listeners = {};
};

/**
 * Position error object
 *
 * @param code
 * @param message
 */
function PositionError(code, message) {
    this.code = code;
    this.message = message;
};

PositionError.PERMISSION_DENIED = 1;
PositionError.POSITION_UNAVAILABLE = 2;
PositionError.TIMEOUT = 3;

/**
 * Asynchronously aquires the current position.
 *
 * @param {Function} successCallback    The function to call when the position data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the heading position. (OPTIONAL)
 * @param {PositionOptions} options     The options for getting the position data. (OPTIONAL)
 */
Geolocation.prototype.getCurrentPosition = function(successCallback, errorCallback, options) {
    if (navigator._geo.listeners["global"]) {
        console.log("Geolocation Error: Still waiting for previous getCurrentPosition() request.");
        try {
            errorCallback(new PositionError(PositionError.TIMEOUT, "Geolocation Error: Still waiting for previous getCurrentPosition() request."));
        } catch (e) {
        }
        return;
    }
    var maximumAge = 10000;
    var enableHighAccuracy = false;
    var timeout = 10000;
    if (typeof options != "undefined") {
        if (typeof options.maximumAge != "undefined") {
            maximumAge = options.maximumAge;
        }
        if (typeof options.enableHighAccuracy != "undefined") {
            enableHighAccuracy = options.enableHighAccuracy;
        }
        if (typeof options.timeout != "undefined") {
            timeout = options.timeout;
        }
    }
    navigator._geo.listeners["global"] = {"success" : successCallback, "fail" : errorCallback };
    PhoneGap.execAsync(null, null, "Geolocation", "getCurrentLocation", [enableHighAccuracy, timeout, maximumAge]);
}

/**
 * Asynchronously watches the geolocation for changes to geolocation.  When a change occurs,
 * the successCallback is called with the new location.
 *
 * @param {Function} successCallback    The function to call each time the location data is available
 * @param {Function} errorCallback      The function to call when there is an error getting the location data. (OPTIONAL)
 * @param {PositionOptions} options     The options for getting the location data such as frequency. (OPTIONAL)
 * @return String                       The watch id that must be passed to #clearWatch to stop watching.
 */
Geolocation.prototype.watchPosition = function(successCallback, errorCallback, options) {
    var maximumAge = 10000;
    var enableHighAccuracy = false;
    var timeout = 10000;
    if (typeof options != "undefined") {
        if (typeof options.frequency  != "undefined") {
            maximumAge = options.frequency;
        }
        if (typeof options.maximumAge != "undefined") {
            maximumAge = options.maximumAge;
        }
        if (typeof options.enableHighAccuracy != "undefined") {
            enableHighAccuracy = options.enableHighAccuracy;
        }
        if (typeof options.timeout != "undefined") {
            timeout = options.timeout;
        }
    }
    var id = PhoneGap.createUUID();
    navigator._geo.listeners[id] = {"success" : successCallback, "fail" : errorCallback };
    PhoneGap.execAsync(null, null, "Geolocation", "start", [id, enableHighAccuracy, timeout, maximumAge]);
    return id;
};

/*
 * Native callback when watch position has a new position.
 * PRIVATE METHOD
 *
 * @param {String} id
 * @param {Number} lat
 * @param {Number} lng
 * @param {Number} alt
 * @param {Number} altacc
 * @param {Number} head
 * @param {Number} vel
 * @param {Number} stamp
 */
Geolocation.prototype.success = function(id, lat, lng, alt, altacc, head, vel, stamp) {
    var coords = new Coordinates(lat, lng, alt, altacc, head, vel);
    var loc = new Position(coords, stamp);
    try {
        if (lat == "undefined" || lng == "undefined") {
            navigator._geo.listeners[id].fail(new PositionError(PositionError.POSITION_UNAVAILABLE, "Lat/Lng are undefined."));
        }
        else {
            navigator._geo.lastPosition = loc;
            navigator._geo.listeners[id].success(loc);
        }
    }
    catch (e) {
        console.log("Geolocation Error: Error calling success callback function.");
    }

    if (id == "global") {
        delete navigator._geo.listeners["global"];
    }
};

/**
 * Native callback when watch position has an error.
 * PRIVATE METHOD
 *
 * @param {String} id       The ID of the watch
 * @param {Number} code     The error code
 * @param {String} msg      The error message
 */
Geolocation.prototype.fail = function(id, code, msg) {
    try {
        navigator._geo.listeners[id].fail(new PositionError(code, msg));
    }
    catch (e) {
        console.log("Geolocation Error: Error calling error callback function.");
    }
};

/**
 * Clears the specified heading watch.
 *
 * @param {String} id       The ID of the watch returned from #watchPosition
 */
Geolocation.prototype.clearWatch = function(id) {
    PhoneGap.execAsync(null, null, "Geolocation", "stop", [id]);
    delete navigator._geo.listeners[id];
};

/**
 * Force the PhoneGap geolocation to be used instead of built-in.
 */
Geolocation.usingPhoneGap = false;
Geolocation.usePhoneGap = function() {
    if (Geolocation.usingPhoneGap) {
        return;
    }
    Geolocation.usingPhoneGap = true;

    // Set built-in geolocation methods to our own implementations
    // (Cannot replace entire geolocation, but can replace individual methods)
    navigator.geolocation.setLocation = navigator._geo.setLocation;
    navigator.geolocation.getCurrentPosition = navigator._geo.getCurrentPosition;
    navigator.geolocation.watchPosition = navigator._geo.watchPosition;
    navigator.geolocation.clearWatch = navigator._geo.clearWatch;
    navigator.geolocation.start = navigator._geo.start;
    navigator.geolocation.stop = navigator._geo.stop;
};

PhoneGap.addConstructor(function() {
    navigator._geo = new Geolocation();

    // No native geolocation object for Android 1.x, so use PhoneGap geolocation
    if (typeof navigator.geolocation == 'undefined') {
        navigator.geolocation = navigator._geo;
        Geolocation.usingPhoneGap = true;
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
 * PRIVATE
 */
PhoneGap.mediaObjects = {};

/**
 * Object that receives native callbacks.
 * PRIVATE
 */
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
 *                                  successCallback() - OPTIONAL
 * @param errorCallback         The callback to be called if there is an error.
 *                                  errorCallback(int errorCode) - OPTIONAL
 * @param statusCallback        The callback to be called when media status has changed.
 *                                  statusCallback(int statusCode) - OPTIONAL
 * @param positionCallback      The callback to be called when media position has changed.
 *                                  positionCallback(long position) - OPTIONAL
 */
Media = function(src, successCallback, errorCallback, statusCallback, positionCallback) {

    // successCallback optional
    if (successCallback && (typeof successCallback != "function")) {
        console.log("Media Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Media Error: errorCallback is not a function");
        return;
    }

    // statusCallback optional
    if (statusCallback && (typeof statusCallback != "function")) {
        console.log("Media Error: statusCallback is not a function");
        return;
    }

    // statusCallback optional
    if (positionCallback && (typeof positionCallback != "function")) {
        console.log("Media Error: positionCallback is not a function");
        return;
    }

    this.id = PhoneGap.createUUID();
    PhoneGap.mediaObjects[this.id] = this;
    this.src = src;
    this.successCallback = successCallback;
    this.errorCallback = errorCallback;
    this.statusCallback = statusCallback;
    this.positionCallback = positionCallback;
    this._duration = -1;
    this._position = -1;
};

// Media messages
Media.MEDIA_STATE = 1;
Media.MEDIA_DURATION = 2;
Media.MEDIA_ERROR = 9;

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
    PhoneGap.execAsync(null, null, "Media", "startPlayingAudio", [this.id, this.src]);
};

/**
 * Stop playing audio file.
 */
Media.prototype.stop = function() {
    return PhoneGap.execAsync(null, null, "Media", "stopPlayingAudio", [this.id]);
};

/**
 * Pause playing audio file.
 */
Media.prototype.pause = function() {
    PhoneGap.execAsync(null, null, "Media", "pausePlayingAudio", [this.id]);
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
Media.prototype.getCurrentPosition = function(success, fail) {
    PhoneGap.execAsync(success, fail, "Media", "getCurrentPositionAudio", [this.id]);
};

/**
 * Start recording audio file.
 */
Media.prototype.startRecord = function() {
    PhoneGap.execAsync(null, null, "Media", "startRecordingAudio", [this.id, this.src]);
};

/**
 * Stop recording audio file.
 */
Media.prototype.stopRecord = function() {
    PhoneGap.execAsync(null, null, "Media", "stopRecordingAudio", [this.id]);
};


/**
 * This class contains information about any NetworkStatus.
 * @constructor
 */
function NetworkStatus() {
    //this.code = null;
    //this.message = "";
};

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
// TODO: Callback from native code not implemented for Android
Network.prototype.updateReachability = function(reachability) {
    this.lastReachability = reachability;
};

/**
 * Determine if a URI is reachable over the network.

 * @param {Object} uri
 * @param {Function} callback
 * @param {Object} options  (isIpAddress:boolean)
 */
Network.prototype.isReachable = function(uri, callback, options) {
    var isIpAddress = false;
    if (options && options.isIpAddress) {
        isIpAddress = options.isIpAddress;
    }
    PhoneGap.execAsync(callback, null, "Network Status", "isReachable", [uri, isIpAddress]);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.network == "undefined") navigator.network = new Network();
});

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
	var _title = (title || "Alert");
	var _buttonLabel = (buttonLabel || "OK");
    PhoneGap.execAsync(null, null, "Notification", "alert", [message,_title,_buttonLabel]);
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
    PhoneGap.execAsync(null, null, "Notification", "vibrate", [mills]);
};

/**
 * Causes the device to beep.
 * On Android, the default notification ringtone is played.
 *
 * @param {Integer} count The number of beeps.
 */
Notification.prototype.beep = function(count) {
    PhoneGap.execAsync(null, null, "Notification", "beep", [count]);
};

// TODO: of course on Blackberry and Android there notifications in the UI as well

PhoneGap.addConstructor(function() {
    if (typeof navigator.notification == "undefined") navigator.notification = new Notification();
});

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
});
/*
 * This is purely for the Android 1.5/1.6 HTML 5 Storage
 * I was hoping that Android 2.0 would deprecate this, but given the fact that
 * most manufacturers ship with Android 1.5 and do not do OTA Updates, this is required
 */

var DroidDB = function() {
    this.txQueue = [];
};

DroidDB.prototype.addResult = function(rawdata, tx_id) {
    eval("var data = " + rawdata);
    var tx = this.txQueue[tx_id];
    tx.resultSet.push(data);
};

DroidDB.prototype.completeQuery = function(tx_id) {
    var tx = this.txQueue[tx_id];
    var r = new result();
    r.rows.resultSet = tx.resultSet;
    r.rows.length = tx.resultSet.length;
    tx.win(r);
};

DroidDB.prototype.fail = function(reason, tx_id) {
    var tx = this.txQueue[tx_id];
    tx.fail(reason);
};

var DatabaseShell = function() {
};

DatabaseShell.prototype.transaction = function(process) {
    tx = new Tx();
    process(tx);
};

var Tx = function() {
    droiddb.txQueue.push(this);
    this.id = droiddb.txQueue.length - 1;
    this.resultSet = [];
};

Tx.prototype.executeSql = function(query, params, win, fail) {
    PhoneGap.execAsync(null, null, "Storage", "executeSql", [query, params, this.id]);
    tx.win = win;
    tx.fail = fail;
};

var result = function() {
    this.rows = new Rows();
};

var Rows = function() {
    this.resultSet = [];
    this.length = 0;
};

Rows.prototype.item = function(row_id) {
    return this.resultSet[id];
};

var dbSetup = function(name, version, display_name, size) {
    PhoneGap.execAsync(null, null, "Storage", "openDatabase", [name, version, display_name, size]);
    db_object = new DatabaseShell();
    return db_object;
};

PhoneGap.addConstructor(function() {
    if (typeof window.openDatabase == "undefined") {
        navigator.openDatabase = window.openDatabase = dbSetup;
        window.droiddb = new DroidDB();
    }
});
