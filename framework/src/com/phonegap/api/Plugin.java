/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap.api;

import org.json.JSONArray;

import android.content.Intent;
import android.webkit.WebView;

/**
 * Plugin interface must be implemented by any plugin classes.
 *
 * The execute method is called by the PluginManager.
 */
public abstract class Plugin implements IPlugin {

    public WebView webView;					// WebView object
    public PhonegapActivity ctx;			// PhonegapActivity object

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public abstract PluginResult execute(String action, JSONArray args, String callbackId);

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		return false;
	}

	/**
	 * Sets the context of the Plugin. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(PhonegapActivity ctx) {
		this.ctx = ctx;
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
     * The final call you receive before your activity is destroyed. 
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

    /**
     * Send generic JavaScript statement back to JavaScript.
     * success(...) and error(...) should be used instead where possible.
     * 
     * @param statement
     */
    public void sendJavascript(String statement) {
    	this.ctx.sendJavascript(statement);
    }

    /**
     * Call the JavaScript success callback for this plugin.
     * 
     * This can be used if the execute code for the plugin is asynchronous meaning
     * that execute should return null and the callback from the async operation can
     * call success(...) or error(...)
     * 
     * @param pluginResult		The result to return.
	 * @param callbackId		The callback id used when calling back into JavaScript.
     */
    public void success(PluginResult pluginResult, String callbackId) {
    	this.ctx.sendJavascript(pluginResult.toSuccessCallbackString(callbackId));
    }

    /**
     * Call the JavaScript error callback for this plugin.
     * 
     * @param pluginResult		The result to return.
	 * @param callbackId		The callback id used when calling back into JavaScript.
     */
    public void error(PluginResult pluginResult, String callbackId) {
    	this.ctx.sendJavascript(pluginResult.toErrorCallbackString(callbackId));
    }
}
