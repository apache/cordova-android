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

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.webkit.WebView;

public class GeoListener {
	public static int PERMISSION_DENIED = 1;
	public static int POSITION_UNAVAILABLE = 2;
	public static int TIMEOUT = 3;

	String id;							// Listener ID
	String successCallback;				// 
	String failCallback;
    GpsListener mGps;					// GPS listener
    NetworkListener mNetwork;			// Network listener
    LocationManager mLocMan;			// Location manager
    
    private GeoBroker broker;			// GeoBroker object
	
	int interval;
	
	/**
	 * Constructor.
	 * 
	 * @param id			Listener id
	 * @param ctx
	 * @param time			Sampling period in msec
	 * @param appView
	 */
	GeoListener(GeoBroker broker, String id, int time) {
		this.id = id;
		this.interval = time;
		this.broker = broker;
		this.mGps = null;
		this.mNetwork = null;
		this.mLocMan = (LocationManager) broker.ctx.getSystemService(Context.LOCATION_SERVICE);

		// If GPS provider, then create and start GPS listener
		if (this.mLocMan.getProvider(LocationManager.GPS_PROVIDER) != null) {
			this.mGps = new GpsListener(broker.ctx, time, this);
		}
		
		// If network provider, then create and start network listener
		if (this.mLocMan.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			this.mNetwork = new NetworkListener(broker.ctx, time, this);
		}
	}
	
	/**
	 * Destroy listener.
	 */
	public void destroy() {
		this.stop();
	}
	
	/**
	 * Location found.  Send location back to JavaScript.
	 * 
	 * @param loc
	 */
	void success(Location loc) {
		
		String params = loc.getLatitude() + "," + loc.getLongitude() + ", " + loc.getAltitude() + 
				"," + loc.getAccuracy() + "," + loc.getBearing() +
		 		"," + loc.getSpeed() + "," + loc.getTime();
		
		if (id == "global") {
			this.stop();
		}
		this.broker.sendJavascript("navigator._geo.success('" + id + "'," +  params + ");");
	}
	
	/**
	 * Location failed.  Send error back to JavaScript.
	 * 
	 * @param code			The error code
	 * @param msg			The error message
	 */
	void fail(int code, String msg) {
		this.broker.sendJavascript("navigator._geo.fail('" + this.id + "', '" + code + "', '" + msg + "');");
		this.stop();
	}
	
	/**
	 * Start retrieving location.
	 * 
	 * @param interval
	 */
	void start(int interval) {
		if (this.mGps != null) {
			this.mGps.start(interval);
		}
		if (this.mNetwork != null) {
			this.mNetwork.start(interval);
		}
		if (this.mNetwork == null && this.mGps == null) {
			this.fail(POSITION_UNAVAILABLE, "No location providers available.");
		}
	}
	
	/**
	 * Stop listening for location.
	 */
	void stop() {
		if (this.mGps != null) {
			this.mGps.stop();
		}
		if (this.mNetwork != null) {
			this.mNetwork.stop();
		}
	}

}
