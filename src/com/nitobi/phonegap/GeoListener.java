package com.nitobi.phonegap;

import android.content.Context;
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
	
	GeoListener(String key, Context ctx, int time, String succ, String fail)
	{
		id = key;
		interval = time;
		mCtx = ctx;
        mGps = new GpsListener(mCtx, interval, this);
        mNetwork = new NetworkListener(mCtx, interval, this);
	}
	
	void success()
	{
		/*
		 * We only need to figure out what we do when we succeed!
		 */
		mAppView.loadUrl("javascript:geoLocation.success(" + ")");
	}
	
	void fail()
	{
	}
}
