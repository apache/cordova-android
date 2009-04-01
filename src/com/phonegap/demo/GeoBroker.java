package com.phonegap.demo;

import java.util.HashMap;

import android.content.Context;
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
	
	public void getCurrentLocation()
	{
		GeoListener listener = new GeoListener("global", mCtx, 10000, mAppView);
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
