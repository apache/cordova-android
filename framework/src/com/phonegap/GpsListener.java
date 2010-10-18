/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * This class handles requests for GPS location services.
 *
 */
public class GpsListener implements LocationListener {
	
	private DroidGap mCtx;						// DroidGap object
	
	private LocationManager mLocMan;			// Location manager object
	private GeoListener owner;					// Geolistener object (parent)
	private boolean hasData = false;			// Flag indicates if location data is available in cLoc
	private Location cLoc;						// Last recieved location
	private boolean running = false;			// Flag indicates if listener is running
	
	/**
	 * Constructor.  
	 * Automatically starts listening.
	 * 
	 * @param ctx
	 * @param interval
	 * @param m
	 */
	public GpsListener(DroidGap ctx, int interval, GeoListener m) {
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
		this.cLoc = this.mLocMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
		this.owner.fail(GeoListener.POSITION_UNAVAILABLE, "GPS provider disabled.");
	}

	/**
	 * Called when the provider is enabled by the user.
	 * 
	 * @param provider
	 */
	public void onProviderEnabled(String provider) {
		System.out.println("GpsListener: The provider "+ provider + " is enabled");
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
		System.out.println("GpsListener: The status of the provider " + provider + " has changed");
		if (status == 0) {
			System.out.println("GpsListener: " + provider + " is OUT OF SERVICE");
			this.owner.fail(GeoListener.POSITION_UNAVAILABLE, "GPS out of service.");
		}
		else if (status == 1) {
			System.out.println("GpsListener: " + provider + " is TEMPORARILY_UNAVAILABLE");
		}
		else {
			System.out.println("GpsListener: " + provider + " is Available");
		}
	}

	/**
	 * Called when the location has changed.
	 * 
	 * @param location
	 */
	public void onLocationChanged(Location location) {
		System.out.println("GpsListener: The location has been updated!");
		this.hasData = true;
		this.cLoc = location;
		this.owner.success(location);
	}

	/**
	 * Determine if location data is available.
	 * 
	 * @return
	 */
	public boolean hasLocation() {
		return this.hasData;
	}
	
	/**
	 * Start requesting location updates.
	 * 
	 * @param interval
	 */
	public void start(int interval) {
		if (!this.running) {
			this.running = true;
			this.mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, this);
			this.getLocation();

			// If GPS provider has data, then send now
			if (this.hasData) {
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
