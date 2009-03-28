package com.nitobi.phonegap;

import java.util.HashMap;

import android.content.Context;
import android.location.Location;
import android.webkit.WebView;

/*
 * This class is the interface to the Geolocation.  It's bound to the geo object.
 * 
 * This class only starts and stops various GeoListeners, which consist of a GPS and a Network Listener
 */

public class GeoBroker {
    private WebView mAppView;
	private Context mCtx;
	private HashMap<String, GeoListener> geoListeners;
	
	GeoBroker(WebView view, Context ctx)
	{
		mCtx = ctx;
		mAppView = view;
	}
	
	public GeoTuple getCurrentLocation()
	{
		GeoListener listener = new GeoListener("0", mCtx, 60000, mAppView);
		Location loc = listener.getCurrentLocation();
		GeoTuple geo = new GeoTuple();
		if (loc == null)
		{
			geo.lat = 0;
			geo.lng = 0;
			geo.ele = 0;
		}
		else
		{
			geo.lng = loc.getLongitude();
			geo.lat = loc.getLatitude();
			geo.ele = 0;
		}
		listener.stop();
		return geo;
	}
	
	public String start(int freq, String key)
	{
		GeoListener listener = new GeoListener(key, mCtx, freq, mAppView);
		geoListeners.put(key, listener);
		return key;
	}
	
	public void stop(String key)
	{
		GeoListener geo = geoListeners.get(key);
	}
}
