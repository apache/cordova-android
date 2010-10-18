/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.content.Context;
import android.net.*;

public class NetworkManager extends Plugin {
	
	public static int NOT_REACHABLE = 0;
	public static int REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
	public static int REACHABLE_VIA_WIFI_NETWORK = 2;
	
    ConnectivityManager sockMan;
	
	/**
	 * Constructor.
	 */
	public NetworkManager()	{
	}

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(DroidGap ctx) {
		super.setContext(ctx);
		this.sockMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		try {
			if (action.equals("isAvailable")) {
				boolean b = this.isAvailable();
				return new PluginResult(status, b);
			}
			else if (action.equals("isWifiActive")) {
				boolean b = this.isWifiActive();
				return new PluginResult(status, b);
			}
			else if (action.equals("isReachable")) {
				int i = this.isReachable(args.getString(0), args.getBoolean(1));
				return new PluginResult(status, i);
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		// All methods take a while, so always use async
		return false;
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Determine if a network connection exists.
     * 
     * @return
     */
	public boolean isAvailable() {
		NetworkInfo info = sockMan.getActiveNetworkInfo();
		boolean conn = false;
		if (info != null) {
			conn = info.isConnected();
		}
		return conn;
	}
	
	/**
	 * Determine if a WIFI connection exists.
	 * 
	 * @return
	 */
	public boolean isWifiActive() {
		NetworkInfo info = sockMan.getActiveNetworkInfo();
		if (info != null) {
			String type = info.getTypeName();
			return type.equals("WIFI");
		}
		return false;
	}
	
	/**
	 * Determine if a URI is reachable over the network.
	 * 
	 * @param uri
	 * @param isIpAddress
	 * @return
	 */
	public int isReachable(String uri, boolean isIpAddress) {
		int reachable = NOT_REACHABLE;
		
		if (uri.indexOf("http://") == -1) {
			uri = "http://" + uri;
		}

		if (this.isAvailable()) {
			try {
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(uri);
				httpclient.execute(httpget);			

				if (this.isWifiActive()) {
					reachable = REACHABLE_VIA_WIFI_NETWORK;
				}
				else {
					reachable = REACHABLE_VIA_CARRIER_DATA_NETWORK;
				}
			} catch (Exception e) { 
				reachable = NOT_REACHABLE;
			}
		}
				
		return reachable;
	}
}
