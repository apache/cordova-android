package com.phonegap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.webkit.WebView;

public class GeoListener {
	String id;
	String successCallback;
	String failCallback;
    GpsListener mGps; 
    NetworkListener mNetwork;
    LocationManager mLocMan;
    private DroidGap mCtx;
    private WebView mAppView;
	
	int interval;
	
	GeoListener(String i, DroidGap ctx, int time, WebView appView) {
		id = i;
		interval = time;
		mCtx = ctx;
		mGps = null;
		mNetwork = null;
		mLocMan = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);

		if (mLocMan.getProvider(LocationManager.GPS_PROVIDER) != null) {
			mGps = new GpsListener(mCtx, interval, this);
		}
		if (mLocMan.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			mNetwork = new NetworkListener(mCtx, interval, this);
		}
		mAppView = appView;
	}
	
	void success(Location loc) {
		/*
		 * We only need to figure out what we do when we succeed!
		 */
		
		String params; 
		/*
		 * Build the giant string to send back to Javascript!
		 */
		params = loc.getLatitude() + "," + loc.getLongitude() + ", " + loc.getAltitude() + "," + loc.getAccuracy() + "," + loc.getBearing();
		params += "," + loc.getSpeed() + "," + loc.getTime();
		if (id != "global") {
			mCtx.sendJavascript("navigator._geo.success(" + id + "," +  params + ");");
		}
		else {
			mCtx.sendJavascript("navigator.geolocation.gotCurrentPosition(" + params + ");");
			this.stop();
		}
	}
	
	void fail() {
		// Do we need to know why? How would we handle this?
		if (id != "global") {
			mCtx.sendJavascript("navigator._geo.fail(" + id + ");");
		} else {
			mCtx.sendJavascript("navigator._geo.fail();");
		}
	}
	
	void start(int interval) {
		if (mGps != null) {
			mGps.start(interval);
		}
		if (mNetwork != null) {
			mNetwork.start(interval);
		}
		if (mNetwork == null && mGps == null) {
			// Really, how were you going to get the location???
			mCtx.sendJavascript("navigator._geo.fail();");
		}
	}
	
	// This stops the listener
	void stop() {
		if (mGps != null) {
			mGps.stop();
		}
		if (mNetwork != null) {
			mNetwork.stop();
		}
	}

}
