// Utils
/* License (MIT)
 * Copyright (c) 2008 Nitobi
 * website: http://phonegap.com
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * Software), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * At times we have to do polling in javascript, that is have function run at a specified interval 
 * repeated times.  However, certain function polling will reduce battery life thus
 * each of those features should have its own user defined interval
 * that our js reads in and uses with of course conservative value used
 * is consumer has not set one or the user of this library has not set one.
 * 
 * Timeout only runs function twice not repeated times and thus we use
 * setInterval. This should correct problems with 
 * both GPS and Accelerometer readings on Android
 */

try {
    $ // Test if it is already used
} catch(e) {
    $ = function(id){
        return document.getElementById(id)
    };
}

// Acceleration Handling

var accelX = 0;
alert('accelX initially =' + accelX);
var accelY = 0;
alert('accelY initially =' + accelY);
var accelZ = 0;
alert('accelZ initially =' + accelZ);

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
    
    whatPlatform: "",
    osversion: "",
    sdkfwversion: "",
    
    FNModel: "",
    FNVersion: "",
    FNOSVersion: "",
    FNSDKFWVersion: "",
    FNUUID: "",
    FNGapVersion: "",
    
    myLat: "",
    myLon: "",
    
    myGPSInterval: "30000",
    myAccelInterval: "30000",
    
    

    
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
	        
	        alert('window.DroidGap=' + window.DroidGap);
	        alert('window.IPhoneGap=' + window.IPhoneGap);
	        alert('window.IPodGap=' + window.IPodGap);
	
        	 if (window.DroidGap)
        	 {
        		Device.whatPlatform = "Android";
        		alert('Device.whatPlatform=' + Device.whatPlatform);
        		Device.model = window.DroidGap.getModel();
        		alert('Device.model=' + Device.model);
        		Device.version = window.DroidGap.getProductName();
        		alert('Device.version=' + Device.version);
        		Device.osversion = window.DroidGap.getOSVersion();
        		alert('Device.osversion=' + Device.osversion);
        		Device.sdkfwversion = window.DroidGap.getSDKVersion();
        		alert('Device.sdkfwversion=' + Device.sdkfwversion);
        		Device.available = true;
        		alert('Device.available=' + Device.available);
        	    Device.uuid = window.DroidGap.getUuid();
        	    alert('Device.uuid=' + Device.uuid);
        	    Device.gapVersion = window.DroidGap.getVersion();
        	    alert('Device.gapVersion=' + Device.gapVersion);
        	} else if (window.IPhoneGap)
        	{
        		Device.whatPlatform = "IPhone";
        		
        		Device.model = window.IPhoneGap.getModel();
        		
        		Device.version = window.IPhoneGap.getProductName();
        		
        		Device.osversion = window.IPhoneGap.getOSVersion();
        		
        		Device.sdkfwversion = window.IPhonedGap.getSDKVersion();
        		
        		Device.available = true;
        		
        	    Device.uuid = window.IPhoneGap.getUuid();
        	    
        	    Device.gapVersion = window.IPhoneGap.getVersion();
        	    
        	}else if (window.IPodGap)
        	{
        		Device.whatPlatform = "IPod";
        		
        		Device.model = window.IPodGap.getModel();
        		
        		Device.version = window.IPodGap.getProductName();
        		
        		Device.osversion = window.IPodGap.getOSVersion();
        		
        		Device.sdkfwversion = window.IPodGap.getSDKVersion();
        		
        		Device.available = true;
        		
        	    Device.uuid = window.IPodGap.getUuid();
        	    
        	    Device.gapVersion = window.IPodGap.getVersion();
        	    
        	} else
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
    	// 
    	
    	
    	
        if ( Device.whatPlatform == "IPhone") {
            try {
            	
                document.location = "gap:" + command;
            } catch(e) {
                console.log("Command '" + command + "' has not been executed, because of exception: " + e);
                alert("Error executing command '" + command + "'.")
            }
        } else
        if ( Device.whatPlatform == "IPod") {
        	try {
                document.location = "gap:" + command;
            } catch(e) {
                console.log("Command '" + command + "' has not been executed, because of exception: " + e);
                alert("Error executing command '" + command + "'.")
            }
        } else
        if (Device.whatPlatform == "Android" || command == "getloc") {
        	try {
        		alert('start here');
        		
        		document.location="javascript:window.DroidGap.getLocation()";
        		
        		alert('window.DroidGap.getLocation()=' + window.DroidGap.getLocation());
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
            alert(' getloc was called');
            
        },
        
        set: function(lat, lon) {
            Device.Location.lat = lat;
            Device.Location.lon = lon;
            alert('Device.Location let is' + lat);
            alert('Device.Location lon is' + lon);
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
	},
	
	notification: {
		watchPosition: function(filter) {
			window.DroidGap.notificationWatchPosition(filter);
		}, 
		clearWatch: function(filter) {
			window.DroidGap.notificationClearWatch(filter);
		} 
	},
	
	http: {
		get: function(url, file) {
			window.DroidGap.httpGet(url, file);
		}
	},

	storage: {
       result: "",
       testSDCard: function(){
           Device.storage.result = window.DroidGap.testSaveLocationExists();
           return Device.storage.result;
       },
       testExistence: function(file){
           Device.storage.result = window.DroidGap.testDirOrFileExists(file);
           return Device.storage.result;
       },
       delFile: function(file){
           Device.storage.result = window.DroidGap.deleteFile(file);
           return Device.storage.result;
       },
       delDir: function(file){
           Device.storage.result = window.DroidGap.deleteDirectory(file);
           return Device.storage.result;
       },
       createDir: function(file){
           Device.storage.result = window.DroidGap.createDirectory(file);
           return Device.storage.result;
             }
     }, 

	
	audio: {
		startRecording: function(file) {
			window.DroidGap.startRecordingAudio(file);
		},
		stopRecording: function() {
			window.DroidGap.stopRecordingAudio();
		},
		startPlaying: function(file) {
			window.DroidGap.startPlayingAudio(file);
		},
		stopPlaying: function() {
			window.DroidGap.stopPlayingAudio();
		},
		getCurrentPosition: function() {
			return window.DroidGap.getCurrentPositionAudio();
		},
		getDuration: function(file) {
			return window.DroidGap.getDurationAudio(file);
		},
		setAudioOutputDevice: function(output){
			window.DroidGap.setAudioOutputDevice(output);
		},
		getAudioOutputDevice: function (){
			return window.DroidGap.getAudioOutputDevice();
		}
	},
	information: {
       getLine1Number: function(){
           	return window.DroidGap.getLine1Number();
       },
       getVoiceMailNumber: function(){
          	return window.DroidGap.getVoiceMailNumber();
       },
       getNetworkOperatorName: function(){
       		return window.DroidGap.getNetworkOperatorName();
       },
       getSimCountryIso: function(){
       		return window.DroidGap.getSimCountryIso();
       },
       getTimeZoneID: function(){
        	return window.DroidGap.getTimeZoneID();
       }
   } 


}

function gotLocation(lat, lon) {
	alert('gotLocation lat=' + lat + " gotLocation lon=" + lon);
    return Device.Location.set(lat, lon)
}


