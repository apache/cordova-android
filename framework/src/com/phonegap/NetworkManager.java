package com.phonegap;

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
		boolean conn = false;
		if(info != null)
			conn = info.isConnected();
		return conn;
	}
	
	public boolean isWifiActive()
	{
		NetworkInfo info = sockMan.getActiveNetworkInfo();
		String type = info.getTypeName();
		return type.equals("WIFI");
	}
	
	public boolean isReachable(String uri)
	{
		if (uri.indexOf("http://") == -1)
			uri = "http://" + uri;
		boolean reached = isAvailable();
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(uri);
			httpclient.execute(httpget);			
		} catch (Exception e) { 
			reached = false;
		}
		return reached;
	}
	
	
}
