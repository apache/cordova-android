/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010-2011, IBM Corporation
 */
package com.phonegap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.phonegap.api.PhonegapActivity;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.*;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkManager extends Plugin {
	
	public static int NOT_REACHABLE = 0;
	public static int REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
	public static int REACHABLE_VIA_WIFI_NETWORK = 2;

	public static final String WIFI = "wifi";
	public static final String WIMAX = "wimax";
	// mobile
	public static final String MOBILE = "mobile";
	// 2G network types
	public static final String GSM = "gsm";
	public static final String GPRS = "gprs";
	public static final String EDGE = "edge";
	// 3G network types
	public static final String CDMA = "cdma";
	public static final String UMTS = "umts";
	// 4G network types
	public static final String LTE = "lte";
	public static final String UMB = "umb";
	// return types
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_ETHERNET = 1;
	public static final int TYPE_WIFI = 2;
	public static final int TYPE_2G = 3;
	public static final int TYPE_3G = 4;
	public static final int TYPE_4G = 5;
	public static final int TYPE_NONE = 20;
	
	private static final String LOG_TAG = "NetworkManager";
	private String connectionCallbackId;

    ConnectivityManager sockMan;
    TelephonyManager telephonyManager;
	
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
	public void setContext(PhonegapActivity ctx) {
		super.setContext(ctx);
		this.sockMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);		
		this.telephonyManager = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE));
		
		// We need to listen to connectivity events to update navigator.connection
		IntentFilter intentFilter = new IntentFilter() ;
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ctx.registerReceiver(new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {				
	            updateConnectionInfo((NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO));				
		    }
		}, intentFilter);
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
			else if (action.equals("getConnectionInfo")) {
				this.connectionCallbackId = callbackId;
				NetworkInfo info = sockMan.getActiveNetworkInfo();
				PluginResult pluginResult = new PluginResult(status, this.getConnectionInfo(info));
				pluginResult.setKeepCallback(true);
				return pluginResult;
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
	 * Updates the JavaScript side whenever the connection changes
	 * 
	 * @param info the current active network info
	 * @return
	 */
	private void updateConnectionInfo(NetworkInfo info) {
		JSONObject connection = this.getConnectionInfo(info); 

		// send update to javascript "navigator.connection"
		sendUpdate(connection);
	}

	/** 
	 * Get the latest network connection information
	 * 
	 * @param info the current active network info
	 * @return a JSONObject that represents the network info
	 */
	private JSONObject getConnectionInfo(NetworkInfo info) {
		JSONObject connection = new JSONObject();
		
		try {
			if (info != null) {
				// If we are not connected to any network set type to none
				if (!info.isConnected()) {
					connection.put("type", TYPE_NONE);
					connection.put("homeNW", null);
					connection.put("currentNW", null);				
				}			
				else {
					// If we are connected check which type
					// First off is wifi
					if (info.getTypeName().toLowerCase().equals(WIFI)) {
						connection.put("type", TYPE_WIFI);
						connection.put("homeNW", null);
						connection.put("currentNW", null);				
					}
					// Otherwise it must be one of the mobile network protocols
					else {
						// Determine the correct type, 2G, 3G, 4G
						connection.put("type", getType(info));
						connection.put("homeNW", telephonyManager.getSimOperatorName());
						connection.put("currentNW",  telephonyManager.getNetworkOperatorName());
					}
				}
			}
		}
		catch (JSONException e) {
			// this should never happen
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return connection;
	}
	
	/**
	 * Create a new plugin result and send it back to JavaScript
	 * 
	 * @param connection the network info to set as navigator.connection
	 */
	private void sendUpdate(JSONObject connection) {
		PluginResult result = new PluginResult(PluginResult.Status.OK, connection);
		result.setKeepCallback(true);
		this.success(result, this.connectionCallbackId);
	}
	
	/**
	 * Determine the type of connection
	 * 
	 * @param info the network info so we can determine connection type.
	 * @return the type of mobile network we are on
	 */
	private int getType(NetworkInfo info) {
		if (info != null) {
			String type = info.getTypeName(); 

			if (type.toLowerCase().equals(MOBILE)) {
				type = info.getSubtypeName();
				if (type.toLowerCase().equals(GSM) || 
						type.toLowerCase().equals(GPRS) ||
						type.toLowerCase().equals(EDGE)) {
					return TYPE_2G;
				}
				else if (type.toLowerCase().equals(CDMA) || 
						type.toLowerCase().equals(UMTS)) {
					return TYPE_3G;
				}
				else if (type.toLowerCase().equals(LTE) || 
						type.toLowerCase().equals(UMB)) {
					return TYPE_4G;
				}
			}
		} 
		else {
			return TYPE_NONE;
		}
		return TYPE_UNKNOWN;
	}
	
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