package com.phonegap.api;

import org.json.JSONArray;

import com.phonegap.DroidGap;

import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;

/**
 * Plugin interface must be implemented by any plugin classes.
 *
 * The execute method is called by the PluginManager.
 */
public interface Plugin {
	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 	The action to execute.
	 * @param args 		JSONArry of arguments for the plugin.
	 * @return 			A PluginResult object with a status and message.
	 */
	PluginResult execute(String action, JSONArray args);

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action);

	/**
	 * Sets the context of the Plugin. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	void setContext(DroidGap ctx);

	/**
	 * Sets the main View of the application, this is the WebView within which 
	 * a PhoneGap app runs.
	 * 
	 * @param webView The PhoneGap WebView
	 */
	void setView(WebView webView);
		
    /**
     * Called when the system is about to start resuming a previous activity. 
     */
    void onPause();

    /**
     * Called when the activity will start interacting with the user. 
     */
    void onResume();
    
    /**
     * The final call you receive before your activity is destroyed. 
     */
    void onDestroy();
	
    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    void onActivityResult(int requestCode, int resultCode, Intent intent);

}
