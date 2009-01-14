// Utils
/* License (MIT)
 * Copyright (c) 2008 Nitobi
 * website: http://phonegap.com
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


try {
    $ // Test if it is alread used
} catch(e) {
    $ = function(id){
        return document.getElementById(id)
    };
}

// Acceleration Handling

var accelX = 0;
var accelY = 0;
var accelZ = 0;

function gotAcceleration(x,y,z){
	x = eval(x);
	y = eval(y);
	z = eval(z);
	if ((!isNaN(x)) && (!isNaN(y)) && (!isNaN(z))) {
		accelX = x;
		accelY = y;
		accelZ = z;
	}
	return x + " " + y + " " + z;
}

// A little more abstract

var DEBUG = true;
if (!window.console || !DEBUG) {
    console = {
        log: function(){
        },
        error: function(){
        }
    }
}

var Device = {

    available: false,
    model: "",
    version: "",
	uuid: "",
    isIPhone: null,
    isIPod: null,
    isAndroid: null,
    isSymbian: null,
    isBlackberry: null,
    whatPlatform: "",
    osversion: "",
    sdkfwversion: "",
    
    FNModel: "",
    FNVersion: "",
    FNOSVersion: "",
    FNSDKFWVersion: "",
    FNUUID: "",
    FNGapVersion: "",
    

    
    init: function(model, version) {
       
        	// We check against native appName in window.appName.exists()
        	// call to determine what platform as native AndroidName is
        	// DroidGap an diPhone is IPhoneGap and etc
        	// Than we can set the var for the if statements
	        // At this time we have no clear way
	        // to avoid having the library/framework user
	        // avoid the work of renaming window.AppTitle
	        // to their AppTitle
	        // yes the hack sucks..but its working at this point so..
	        
	
	
	
        	 if (window.DroidGap)
        	 {
        		Device.whatPlatform = "Android";
        		
        		Device.model = window.DroidGap.getModel();
        		
        		Device.version = window.DroidGap.getProductName();
        		
        		Device.osversion = window.DroidGap.getOSVersion();
        		
        		Device.sdkfwversion = window.DroidGap.getSDKVersion();
        		
        		Device.available = true;
        		
        	    Device.uuid = window.DroidGap.getUuid();
        	    
        	    Device.gapVersion = window.DroidGap.getVersion();
        	    
        	}
        	if (window.IPhoneGap)
        	{
        		Device.whatPlatform = "IPhone";
        		
        		Device.model = window.IPhoneGap.getModel();
        		
        		Device.version = window.IPhoneGap.getProductName();
        		
        		Device.osversion = window.IPhoneGap.getOSVersion();
        		
        		Device.sdkfwversion = window.IPhonedGap.getSDKVersion();
        		
        		Device.available = true;
        		
        	    Device.uuid = window.IPhoneGap.getUuid();
        	    
        	    Device.gapVersion = window.IPhoneGap.getVersion();
        	    
        	}
        	if (window.IPodGap)
        	{
        		Device.whatPlatform = "IPod";
        		
        		Device.model = window.IPodGap.getModel();
        		
        		Device.version = window.IPodGap.getProductName();
        		
        		Device.osversion = window.IPodGap.getOSVersion();
        		
        		Device.sdkfwversion = window.IPodGap.getSDKVersion();
        		
        		Device.available = true;
        		
        	    Device.uuid = window.IPodGap.getUuid();
        	    
        	    Device.gapVersion = window.IPoddGap.getVersion();
        	    
        	}
        	if (!window.DroidGap || !window.IPhoneGap || !window.IPodGap )
        	{
        		 Device.available = "__gap";
                 Device.model = "__gap_device_model";
                 Device.version = "__gap_device_version";
                 Device.osversion = "_gap_device_os";
                 Device.sdkfwversion = "_gap_device_sdkversion";
                 
                 Device.gapVersion = "__gap_version";
     			Device.uuid = "__gap_device_uniqueid";
     			alert("GAP is not supported!");
        	}
       
    },
    
    exec: function(command) {
    	// Different platforms have different ways the js browser bridge is called
    	// to get a native object so we handle each difference
    	
    	
        if (Device.available || Device.whatPlatform == "IPhone") {
            try {
                document.location = "gap:" + command;
            } catch(e) {
                console.log("Command '" + command + "' has not been executed, because of exception: " + e);
                alert("Error executing command '" + command + "'.")
            }
        }
        if (Device.available || Device.whatPlatform == "IPod") {
        	try {
                document.location = "gap:" + command;
            } catch(e) {
                console.log("Command '" + command + "' has not been executed, because of exception: " + e);
                alert("Error executing command '" + command + "'.")
            }
        }
        if (Device.available || Device.whatPlatform == "Android") {
        	try {
        		document.location = "javascript:" + "window.DroidGap." + command + "()";
        	} catch(e) {
        		console.log("Command '" + command + "' has not been executed, because of exception: " + e);
                alert("Error executing command '" + command + "'.")
            }
        }
        		
    },

    Location: {
        // available: true,
        
        lon: null,
        lat: null,
        callback: null,
        
        init: function() {
            Device.exec("getloc");
        },
        
        set: function(lat, lon) {
            Device.Location.lat = lat;
            Device.Location.lon = lon;
            if(Device.Location.callback != null) {
                Device.Location.callback(lat, lon)
                Device.Location.callback = null;
            }
        },

        wait: function(func) {
            Device.Location.callback = func
            Device.exec("getloc");
        }
        
    },

    Image: {

        //available: true,

		callback: null,
		
        getFromPhotoLibrary: function() {
            return Device.exec("getphoto" + ":" + Device.Image.callback)
        },
        
        getFromCamera: function() {
            return Device.exec("getphoto" + ":" + Device.Image.callback)
        },
        
        getFromSavedPhotosAlbum: function() {
            return Device.exec("getphoto" + ":" + Device.Image.callback)
        }

    },

    vibrate: function() {
        return Device.exec("vibrate")
    },

		playSound: function(clip) {
    	    xsound = "sound:";
    	   if (Device.whatPlatform == "Android") {
    		   xsound = "playSound";
    		   return Device.exec(xsound + clip);
    	   }
    	
			return Device.exec(xsound + clip);
		}

}

function gotLocation(lat, lon) {
    return Device.Location.set(lat, lon)
}

