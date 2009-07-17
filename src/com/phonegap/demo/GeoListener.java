package com.phonegap.demo;

import android.content.Context;
import android.location.Location;
import android.webkit.WebView;

public class GeoListener {
	String id;
	String successCallback;
	String failCallback;
    GpsListener mGps; 
    NetworkListener mNetwork;
    Context mCtx;
    private WebView mAppView;
	
	int interval;
	
	GeoListener(String i, Context ctx, int time, WebView appView)
	{
		id = i;
		interval = time;
		mCtx = ctx;
        mGps = new GpsListener(mCtx, interval, this);
        mNetwork = new NetworkListener(mCtx, interval, this);
        mAppView = appView;
	}
	
	void success(Location loc)
	{
		/*
		 * We only need to figure out what we do when we succeed!
		 */
		
		String params; 
		/*
		 * Build the giant string to send back to Javascript!
		 */
		params = loc.getLatitude() + "," + loc.getLongitude() + ", " + loc.getAltitude() + "," + loc.getAccuracy() + "," + loc.getBearing();
		params += "," + loc.getSpeed() + "," + loc.getTime();
		if(id != "global")
		{
			mAppView.loadUrl("javascript:navigator.geolocation.success(" + id + "," +  params + ")");
		}
		else
		{
			mAppView.loadUrl("javascript:navigator.geolocation.gotCurrentPosition(" + params + ")");
			this.stop();
		}
	}
	
	void fail()
	{
		// Do we need to know why?  How would we handle this?
		if (id != "global") {
			mAppView.loadUrl("javascript:navigator.geolocation.fail(" + id + ")");
		}
		else
		{
			mAppView.loadUrl("javascript:navigator.geolocation.fail()");
		}
	}
	
	// This stops the listener
	void stop()
	{
		mGps.stop();
		mNetwork.stop();
	}

	public Location getCurrentLocation() {
		Location loc = mGps.getLocation();
		if (loc == null)
			loc = mNetwork.getLocation();
		return loc;
	}
}
