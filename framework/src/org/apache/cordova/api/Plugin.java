/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.api;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.webkit.WebView;

/**
 * Plugin interface must be implemented by any plugin classes.
 *
 * The execute method is called by the PluginManager.
 */
public abstract class Plugin implements IPlugin {

    public String id;
    public WebView webView;					// WebView object
    public CordovaInterface ctx;			// CordovaActivity object

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
    public void setContext(CordovaInterface ctx) {
        this.ctx = ctx;
    }

    /**
     * Sets the main View of the application, this is the WebView within which
     * a Cordova app runs.
     *
     * @param webView The Cordova WebView
     */
    public void setView(WebView webView) {
        this.webView = webView;
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
    }

    /**
     * Called when the activity receives a new intent.
     */
    public void onNewIntent(Intent intent) {
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
    }

    /**
     * Called when a message is sent to plugin.
     *
     * @param id            The message id
     * @param data          The message data
     */
    public void onMessage(String id, Object data) {
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
     * By specifying a <url-filter> in plugins.xml you can map a URL (using startsWith atm) to this method.
     *
     * @param url				The URL that is trying to be loaded in the Cordova webview.
     * @return					Return true to prevent the URL from loading. Default is false.
     */
    public boolean onOverrideUrlLoading(String url) {
        return false;
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
     * Helper for success callbacks that just returns the Status.OK by default
     *
     * @param message			The message to add to the success result.
     * @param callbackId		The callback id used when calling back into JavaScript.
     */
    public void success(JSONObject message, String callbackId) {
        this.ctx.sendJavascript(new PluginResult(PluginResult.Status.OK, message).toSuccessCallbackString(callbackId));
    }

    /**
     * Helper for success callbacks that just returns the Status.OK by default
     *
     * @param message			The message to add to the success result.
     * @param callbackId		The callback id used when calling back into JavaScript.
     */
    public void success(String message, String callbackId) {
        this.ctx.sendJavascript(new PluginResult(PluginResult.Status.OK, message).toSuccessCallbackString(callbackId));
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

    /**
     * Helper for error callbacks that just returns the Status.ERROR by default
     *
     * @param message			The message to add to the error result.
     * @param callbackId		The callback id used when calling back into JavaScript.
     */
    public void error(JSONObject message, String callbackId) {
        this.ctx.sendJavascript(new PluginResult(PluginResult.Status.ERROR, message).toErrorCallbackString(callbackId));
    }

    /**
     * Helper for error callbacks that just returns the Status.ERROR by default
     *
     * @param message			The message to add to the error result.
     * @param callbackId		The callback id used when calling back into JavaScript.
     */
    public void error(String message, String callbackId) {
        this.ctx.sendJavascript(new PluginResult(PluginResult.Status.ERROR, message).toErrorCallbackString(callbackId));
    }
}
