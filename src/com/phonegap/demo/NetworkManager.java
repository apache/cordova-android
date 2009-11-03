package com.phonegap.demo;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.*;
import android.webkit.WebView;

public class NetworkManager {

	Context mCtx;
	WebView mView;
	ConnectivityManager sockMan;
	
	NetworkManager(Context ctx, WebView view)
	{
		mCtx = ctx;
		mView = view;
		sockMan = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public boolean isAvailable()
	{
		NetworkInfo info = sockMan.getActiveNetworkInfo();
		return info.isConnected();
	}
	
	public boolean isReachable(String uri)
	{		
		boolean reached = true;
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(uri);
			httpclient.execute(httpget);			
		} catch (Exception e) { reached = false;}
		return reached;
	}
	
	
}
