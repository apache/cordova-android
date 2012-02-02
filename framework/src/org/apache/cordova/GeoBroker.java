/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;


/*
 * This class is the interface to the Geolocation.  It's bound to the geo object.
 * 
 * This class only starts and stops various GeoListeners, which consist of a GPS and a Network Listener
 */

public class GeoBroker extends Plugin {
    
    // List of gGeolocation listeners
    private HashMap<String, GeoListener> geoListeners;
	private GeoListener global;
	
	/**
	 * Constructor.
	 */
	public GeoBroker() {
		this.geoListeners = new HashMap<String, GeoListener>();
	}

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("getCurrentLocation")) {
				this.getCurrentLocation(args.getBoolean(0), args.getInt(1), args.getInt(2));
			}
			else if (action.equals("start")) {
				String s = this.start(args.getString(0), args.getBoolean(1), args.getInt(2), args.getInt(3));
				return new PluginResult(status, s);
			}
			else if (action.equals("stop")) {
				this.stop(args.getString(0));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		// Starting listeners is easier to run on main thread, so don't run async.
		return true;
	}
    
    /**
     * Called when the activity is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {
		java.util.Set<Entry<String,GeoListener>> s = this.geoListeners.entrySet();
        java.util.Iterator<Entry<String,GeoListener>> it = s.iterator();
        while (it.hasNext()) {
            Entry<String,GeoListener> entry = it.next();
            GeoListener listener = entry.getValue();
            listener.destroy();
		}
        this.geoListeners.clear();
        if (this.global != null) {
        	this.global.destroy();
        }
        this.global = null;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Get current location.
     * The result is returned to JavaScript via a callback.
     * 
	 * @param enableHighAccuracy
	 * @param timeout
	 * @param maximumAge
     */
	public void getCurrentLocation(boolean enableHighAccuracy, int timeout, int maximumAge) {
		
		// Create a geolocation listener just for getCurrentLocation and call it "global"
		if (this.global == null) {
			this.global = new GeoListener(this, "global", maximumAge);
		}
		else {
			this.global.start(maximumAge);
		}
	}
	
	/**
	 * Start geolocation listener and add to listener list.
	 * 
	 * @param key					The listener id
	 * @param enableHighAccuracy
	 * @param timeout
	 * @param maximumAge
	 * @return
	 */
	public String start(String key, boolean enableHighAccuracy, int timeout, int maximumAge) {
		
		// Make sure this listener doesn't already exist
		GeoListener listener = geoListeners.get(key);
		if (listener == null) {
			listener = new GeoListener(this, key, maximumAge);
			geoListeners.put(key, listener);
		}
		
		// Start it
		listener.start(maximumAge);
		return key;
	}
	
	/**
	 * Stop geolocation listener and remove from listener list.
	 * 
	 * @param key			The listener id
	 */
	public void stop(String key) {
		GeoListener listener = geoListeners.remove(key);
		if (listener != null) {
			listener.stop();
		}
	}
}
