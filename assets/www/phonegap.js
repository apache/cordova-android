    
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
            } else {          
                this.platform = DeviceInfo.platform;
                this.version  = DeviceInfo.version;
                this.name     = DeviceInfo.name;
                this.gap      = DeviceInfo.gap;
                this.uuid     = DeviceInfo.uuid;
            }
        } catch(e) {
            this.available = false;
        }
    }
    
    PhoneGap.addConstructor(function() {
        navigator.device = window.device = new Device();
    });
    
    
    
    
    
    
    /**
     * This class contains acceleration information
     * @constructor
     * @param {Number} x The force applied by the device in the x-axis.
     * @param {Number} y The force applied by the device in the y-axis.
     * @param {Number} z The force applied by the device in the z-axis.
     */
    function Acceleration(x, y, z) {
    	/**
    	 * The force applied by the device in the x-axis.
    	 */
    	this.x = x;
    	/**
    	 * The force applied by the device in the y-axis.
    	 */
    	this.y = y;
    	/**
    	 * The force applied by the device in the z-axis.
    	 */
    	this.z = z;
    	/**
    	 * The time that the acceleration was obtained.
    	 */
    	this.timestamp = new Date().getTime();
    }
    
    /**
     * This class specifies the options for requesting acceleration data.
     * @constructor
     */
    function AccelerationOptions() {
    	/**
    	 * The timeout after which if acceleration data cannot be obtained the errorCallback
    	 * is called.
    	 */
    	this.timeout = 10000;
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
    	this.getCurrentAcceleration(successCallback, errorCallback, options);
    	// TODO: add the interval id to a list so we can clear all watches
     	var frequency = (options != undefined)? options.frequency : 10000;
    	return setInterval(function() {
    		navigator.accelerometer.getCurrentAcceleration(successCallback, errorCallback, options);
    	}, frequency);
    }
    
    /**
     * Clears the specified accelerometer watch.
     * @param {String} watchId The ID of the watch returned from #watchAcceleration.
     */
    Accelerometer.prototype.clearWatch = function(watchId) {
    	clearInterval(watchId);
    }
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.accelerometer == "undefined") navigator.accelerometer = new Accelerometer();
    });
    
    
    
    // Need to define these for android
    _accel = {}
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
     * This class provides access to the device media, interfaces to both sound and video
     * @constructor
     */
    function Media(src) {
    	this.src = src;
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
    var MEDIA_AUDIO = "mp3";
    
    function Media(src) {
    	this.src = src;
    }
    
    Media.prototype.record = function() {
    	var fileparse = this.src.split('.');
    	var filetype = fileparse[fileparse.length - 1];
    	if ( filetype == MEDIA_AUDIO )
    	{
    		// Queue up Audio API		
    	}
    	else
    	{
    		// Start Camera Preview for Video Recording
    	}
    }
    
    Media.prototype.play = function() {
    	var fileparse = this.src.split('.');
    	var filetype = fileparse[fileparse.length - 1];
    	if ( filetype = MEDIA_AUDIO)
    	{
    		// Play Audio
    	}
    	else
    	{
    		// Bring up Video Overlay
    		// (Note: On HTML 5 Browsers (iPhone), this should
    		// create a DIV with a VIDEO tag in it)
    	}
    }
    
    Media.prototype.pause = function() {
    	// Do some pausing
    }
    
    Media.prototype.stop = function() {
    	// Stop playback
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
    	
    }
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.camera == "undefined") navigator.camera = new Camera();
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
     * This class provides access to the device contacts.
     * @constructor
     */
    
    function Contact(jsonObject) {
    	this.firstName = "";
    	this.lastName = "";
        this.name = "";
        this.phones = {};
        this.emails = {};
    	this.address = "";
    }
    
    Contact.prototype.displayName = function()
    {
        // TODO: can be tuned according to prefs
    	return this.name;
    }
    
    function ContactManager() {
    	// Dummy object to hold array of contacts
    	this.contacts = [];
    	this.timestamp = new Date().getTime();
    }
    
    ContactManager.prototype.getAllContacts = function(successCallback, errorCallback, options) {
    	// Interface
    }
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.ContactManager == "undefined") navigator.ContactManager = new ContactManager();
    });
    
    
    
    /**
     * This class exposes mobile phone interface controls to JavaScript, such as
     * native tab and tool bars, etc.
     * @constructor
     */
    function UIControls() {
        this.tabBarTag = 0;
        this.tabBarCallbacks = {};
    }
    
    /**
     * Create a native tab bar that can have tab buttons added to it which can respond to events.
     */
    UIControls.prototype.createTabBar = function() {};
    
    /**
     * Show a tab bar.  The tab bar has to be created first.
     * @param {Object} [options] Options indicating how the tab bar should be shown:
     * - \c height integer indicating the height of the tab bar (default: \c 49)
     * - \c position specifies whether the tab bar will be placed at the \c top or \c bottom of the screen (default: \c bottom)
     */
    UIControls.prototype.showTabBar = function(options) {};
    
    /**
     * Hide a tab bar.  The tab bar has to be created first.
     */
    UIControls.prototype.hideTabBar = function(animate) {};
    
    /**
     * Create a new tab bar item for use on a previously created tab bar.  Use ::showTabBarItems to show the new item on the tab bar.
     *
     * If the supplied image name is one of the labels listed below, then this method will construct a tab button
     * using the standard system buttons.  Note that if you use one of the system images, that the \c title you supply will be ignored.
     *
     * <b>Tab Buttons</b>
     *   - tabButton:More
     *   - tabButton:Favorites
     *   - tabButton:Featured
     *   - tabButton:TopRated
     *   - tabButton:Recents
     *   - tabButton:Contacts
     *   - tabButton:History
     *   - tabButton:Bookmarks
     *   - tabButton:Search
     *   - tabButton:Downloads
     *   - tabButton:MostRecent
     *   - tabButton:MostViewed
     * @param {String} name internal name to refer to this tab by
     * @param {String} [title] title text to show on the tab, or null if no text should be shown
     * @param {String} [image] image filename or internal identifier to show, or null if now image should be shown
     * @param {Object} [options] Options for customizing the individual tab item
     *  - \c badge value to display in the optional circular badge on the item; if null or unspecified, the badge will be hidden
     */
    UIControls.prototype.createTabBarItem = function(name, label, image, options) {};
    
    /**
     * Update an existing tab bar item to change its badge value.
     * @param {String} name internal name used to represent this item when it was created
     * @param {Object} options Options for customizing the individual tab item
     *  - \c badge value to display in the optional circular badge on the item; if null or unspecified, the badge will be hidden
     */
    UIControls.prototype.updateTabBarItem = function(name, options) {};
    
    /**
     * Show previously created items on the tab bar
     * @param {String} arguments... the item names to be shown
     * @param {Object} [options] dictionary of options, notable options including:
     *  - \c animate indicates that the items should animate onto the tab bar
     * @see createTabBarItem
     * @see createTabBar
     */
    UIControls.prototype.showTabBarItems = function(tabs, options) {};
    
    /**
     * Manually select an individual tab bar item, or nil for deselecting a currently selected tab bar item.
     * @param {String} tabName the name of the tab to select, or null if all tabs should be deselected
     * @see createTabBarItem
     * @see showTabBarItems
     */
    UIControls.prototype.selectTabBarItem = function(tab) {};
    
    /**
     * Function called when a tab bar item has been selected.
     * @param {Number} tag the tag number for the item that has been selected
     */
    UIControls.prototype.tabBarItemSelected = function(tag) {
        if (typeof(this.tabBarCallbacks[tag]) == 'function')
            this.tabBarCallbacks[tag]();
    };
    
    /**
     * Create a toolbar.
     */
    UIControls.prototype.createToolBar = function() {};
    
    /**
     * Function called when a tab bar item has been selected.
     * @param {String} title the title to set within the toolbar
     */
    UIControls.prototype.setToolBarTitle = function(title) {};
    
    PhoneGap.addConstructor(function() {
        window.uicontrols = new UIControls();
    });
    
    
    
    /**
     * This class provides access to the debugging console.
     * @constructor
     */
    function DebugConsole() {
    }
    
    /**
     * Utility function for rendering and indenting strings, or serializing
     * objects to a string capable of being printed to the console.
     * @param {Object|String} message The string or object to convert to an indented string
     * @private
     */
    DebugConsole.prototype.processMessage = function(message) {
        if (typeof(message) != 'object') {
            return message;
        } else {
            /**
             * @function
             * @ignore
             */
            function indent(str) {
                return str.replace(/^/mg, "    ");
            }
            /**
             * @function
             * @ignore
             */
            function makeStructured(obj) {
                var str = "";
                for (var i in obj) {
                    try {
                        if (typeof(obj[i]) == 'object') {
                            str += i + ":\n" + indent(makeStructured(obj[i])) + "\n";
                        } else {
                            str += i + " = " + indent(String(obj[i])).replace(/^    /, "") + "\n";
                        }
                    } catch(e) {
                        str += i + " = EXCEPTION: " + e.message + "\n";
                    }
                }
                return str;
            }
            return "Object:\n" + makeStructured(message);
        }
    };
    
    /**
     * Print a normal log message to the console
     * @param {Object|String} message Message or object to print to the console
     */
    DebugConsole.prototype.log = function(message) {
    };
    
    /**
     * Print a warning message to the console
     * @param {Object|String} message Message or object to print to the console
     */
    DebugConsole.prototype.warn = function(message) {
    };
    
    /**
     * Print an error message to the console
     * @param {Object|String} message Message or object to print to the console
     */
    DebugConsole.prototype.error = function(message) {
    };
    
    PhoneGap.addConstructor(function() {
        window.debug = new DebugConsole();
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
     * The file name will likely be device dependant.
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
    
    
    
    /**
     * This class provides access to native mapping applications on the device.
     */
    function Map() {
    	
    }
    
    /**
     * Shows a native map on the device with pins at the given positions.
     * @param {Array} positions
     */
    Map.prototype.show = function(positions) {
    	
    }
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.map == "undefined") navigator.map = new Map();
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
     * This class provides access to the device orientation.
     * @constructor
     */
    function Orientation() {
    	/**
    	 * The current orientation, or null if the orientation hasn't changed yet.
    	 */
    	this.currentOrientation = null;
    }
    
    /**
     * Set the current orientation of the phone.  This is called from the device automatically.
     * 
     * When the orientation is changed, the DOMEvent \c orientationChanged is dispatched against
     * the document element.  The event has the property \c orientation which can be used to retrieve
     * the device's current orientation, in addition to the \c Orientation.currentOrientation class property.
     *
     * @param {Number} orientation The orientation to be set
     */
    Orientation.prototype.setOrientation = function(orientation) {
        Orientation.currentOrientation = orientation;
        var e = document.createEvent('Events');
        e.initEvent('orientationChanged', 'false', 'false');
        e.orientation = orientation;
        document.dispatchEvent(e);
    };
    
    /**
     * Asynchronously aquires the current orientation.
     * @param {Function} successCallback The function to call when the orientation
     * is known.
     * @param {Function} errorCallback The function to call when there is an error 
     * getting the orientation.
     */
    Orientation.prototype.getCurrentOrientation = function(successCallback, errorCallback) {
    	// If the position is available then call success
    	// If the position is not available then call error
    };
    
    /**
     * Asynchronously aquires the orientation repeatedly at a given interval.
     * @param {Function} successCallback The function to call each time the orientation
     * data is available.
     * @param {Function} errorCallback The function to call when there is an error 
     * getting the orientation data.
     */
    Orientation.prototype.watchOrientation = function(successCallback, errorCallback) {
    	// Invoke the appropriate callback with a new Position object every time the implementation 
    	// determines that the position of the hosting device has changed. 
    	this.getCurrentPosition(successCallback, errorCallback);
    	return setInterval(function() {
    		navigator.orientation.getCurrentOrientation(successCallback, errorCallback);
    	}, 10000);
    };
    
    /**
     * Clears the specified orientation watch.
     * @param {String} watchId The ID of the watch returned from #watchOrientation.
     */
    Orientation.prototype.clearWatch = function(watchId) {
    	clearInterval(watchId);
    };
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.orientation == "undefined") navigator.orientation = new Orientation();
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
    
    
    
    function Position(coords, timestamp)
    {
    	this.coords = coords;
    	this.timestamp = timestamp;
    }
    
    
    
    /**
     * This class provides access to the device SMS functionality.
     * @constructor
     */
    function Sms() {
    
    }
    
    /**
     * Sends an SMS message.
     * @param {Integer} number The phone number to send the message to.
     * @param {String} message The contents of the SMS message to send.
     * @param {Function} successCallback The function to call when the SMS message is sent.
     * @param {Function} errorCallback The function to call when there is an error sending the SMS message.
     * @param {PositionOptions} options The options for accessing the GPS location such as timeout and accuracy.
     */
    Sms.prototype.send = function(number, message, successCallback, errorCallback, options) {
    	
    }
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.sms == "undefined") navigator.sms = new Sms();
    });
    
    
    
    /**
     * This class provides access to the telephony features of the device.
     * @constructor
     */
    function Telephony() {
    	
    }
    
    /**
     * Calls the specifed number.
     * @param {Integer} number The number to be called.
     */
    Telephony.prototype.call = function(number) {
    	
    }
    
    PhoneGap.addConstructor(function() {
        if (typeof navigator.telephony == "undefined") navigator.telephony = new Telephony();
    });
    
    
