package com.phonegap;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.content.Context;
import android.content.Intent;
import android.net.*;
import android.webkit.WebView;

public class NetworkManager implements Plugin {

    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object
	
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
		this.ctx = ctx;
		this.sockMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Sets the main View of the application, this is the WebView within which 
	 * a PhoneGap app runs.
	 * 
	 * @param webView The PhoneGap WebView
	 */
	public void setView(WebView webView) {
		this.webView = webView;
	}

	/**
	 * Executes the request and returns CommandResult.
	 * 
	 * @param action The command to execute.
	 * @param args JSONArry of arguments for the command.
	 * @return A CommandResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args) {
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
				boolean b = this.isReachable(args.getString(0));
				return new PluginResult(status, b);
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

	/**
     * Called when the system is about to start resuming a previous activity. 
     */
    public void onPause() {
    }

    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {    	
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	public boolean isAvailable() {
		NetworkInfo info = sockMan.getActiveNetworkInfo();
		boolean conn = false;
		if (info != null) {
			conn = info.isConnected();
		}
		return conn;
	}
	
	public boolean isWifiActive() {
		NetworkInfo info = sockMan.getActiveNetworkInfo();
		if (info != null) {
			String type = info.getTypeName();
			return type.equals("WIFI");
		}
		return false;
	}
	
	public boolean isReachable(String uri) {
		if (uri.indexOf("http://") == -1) {
			uri = "http://" + uri;
		}
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
