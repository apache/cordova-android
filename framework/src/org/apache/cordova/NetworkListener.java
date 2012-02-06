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

import org.apache.cordova.api.CordovaInterface;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;

public class NetworkListener implements LocationListener {
	
	private CordovaInterface mCtx;              // CordovaActivity object
	
	private LocationManager mLocMan;           // Location manager object
	private GeoListener owner;                 // Geolistener object (parent)
	private boolean hasData = false;           // Flag indicates if location data is available in cLoc
	private Location cLoc;                     // Last recieved location
	private boolean running = false;           // Flag indicates if listener is running

	/**
	 * Constructor.  
	 * Automatically starts listening.
	 * 
	 * @param ctx
	 * @param interval
	 * @param m
	 */
	public NetworkListener(CordovaInterface ctx, int interval, GeoListener m) {
		this.owner = m;
		this.mCtx = ctx;
		this.mLocMan = (LocationManager) this.mCtx.getSystemService(Context.LOCATION_SERVICE);
		this.running = false;
		this.start(interval);
	}
	
	/**
	 * Get last location.
	 * 
	 * @return 				Location object
	 */
	public Location getLocation() {
		this.cLoc = this.mLocMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (this.cLoc != null) {
			this.hasData = true;
		}
		return this.cLoc;
	}

	/**
	 * Called when the provider is disabled by the user.
	 * 
	 * @param provider
	 */
	public void onProviderDisabled(String provider) {
		System.out.println("NetworkListener: The provider " + provider + " is disabled");
	}

	/**
	 * Called when the provider is enabled by the user.
	 * 
	 * @param provider
	 */
	public void onProviderEnabled(String provider) {
		System.out.println("NetworkListener: The provider "+ provider + " is enabled");
	}

	/**
	 * Called when the provider status changes. This method is called when a 
	 * provider is unable to fetch a location or if the provider has recently 
	 * become available after a period of unavailability.
	 * 
	 * @param provider
	 * @param status
	 * @param extras
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		System.out.println("NetworkListener: The status of the provider " + provider + " has changed");
		if (status == 0) {
			System.out.println("NetworkListener: " + provider + " is OUT OF SERVICE");
		}
		else if (status == 1) {
			System.out.println("NetworkListener: " + provider + " is TEMPORARILY_UNAVAILABLE");
		}
		else {
			System.out.println("NetworkListener: " + provider + " is Available");
		}
	}

	/**
	 * Called when the location has changed.
	 * 
	 * @param location
	 */
	public void onLocationChanged(Location location) {
		System.out.println("NetworkListener: The location has been updated!");
		this.hasData = true;
		this.cLoc = location;
		
		// The GPS is the primary form of Geolocation in Cordova.  
		// Only fire the success variables if the GPS is down for some reason.
		if (!this.owner.mGps.hasLocation()) {
			this.owner.success(location);
		}
	}
	
	/**
	 * Start requesting location updates.
	 * 
	 * @param interval
	 */
	public void start(int interval)	{
		if (!this.running) {
			this.running = true;
			this.mLocMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, 0, this);
			this.getLocation();
			
			// If Network provider has data but GPS provider doesn't, then send ours
			if (this.hasData && !this.owner.mGps.hasLocation()) {
				this.owner.success(this.cLoc);
			}
		}
	}
	
	/**
	 * Stop receiving location updates.
	 */
	public void stop() {
		if (this.running) {
			this.mLocMan.removeUpdates(this);
		}
		this.running = false;
	}
	
}
