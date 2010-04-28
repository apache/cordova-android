package com.phonegap;

import java.util.HashMap;

import android.content.Context;
import android.webkit.WebView;

public class AccelBroker {
    private WebView mAppView;
	private Context mCtx;
	private HashMap<String, AccelListener> accelListeners;
	
	public AccelBroker(WebView view, Context ctx)
	{
		mCtx = ctx;
		mAppView = view;
		accelListeners = new HashMap<String, AccelListener>();
	}
	
	public String start(int freq, String key)
	{
		AccelListener listener = new AccelListener(key, freq, mCtx, mAppView);
		accelListeners.put(key, listener);
		return key;
	}
	
	public void stop(String key)
	{
		AccelListener acc = accelListeners.get(key);
		acc.stop();
	}

}
