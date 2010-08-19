package com.phonegap;

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
	private DroidGap mCtx;
	private HashMap<String, GeoListener> geoListeners;
	private GeoListener global;
	
	public GeoBroker(WebView view, DroidGap ctx)
	{
		mCtx = ctx;
		mAppView = view;
		geoListeners = new HashMap<String, GeoListener>();
	}
	
	public void getCurrentLocation()
	{	
		//It's supposed to run async!
		if(global == null)
			global = new GeoListener("global", mCtx, 10000, mAppView);
		else
			global.start(10000);
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
