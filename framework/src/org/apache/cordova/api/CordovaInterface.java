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

//import java.util.HashMap;

import android.app.Activity;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.ContentResolver;
//import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.res.AssetManager;
//import android.content.res.Resources;
//import android.database.Cursor;
//import android.hardware.SensorManager;
//import android.net.Uri;
//import android.view.Menu;
//import android.view.MenuItem;

/**
 * The Cordova activity abstract class that is extended by DroidGap.
 * It is used to isolate plugin development, and remove dependency on entire Cordova library.
 */
public interface CordovaInterface {

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits, 
     * your onActivityResult() method will be called.
     *  
     * @param command     The command object
     * @param intent      The intent to start
     * @param requestCode   The request code that is passed to callback to identify the activity
     */
    abstract public void startActivityForResult(IPlugin command, Intent intent, int requestCode);

    /**
     * Set the plugin to be called when a sub-activity exits.
     * 
     * @param plugin      The plugin on which onActivityResult is to be called
     */
    abstract public void setActivityResultCallback(IPlugin plugin);

    /**
     * Causes the Activity to override the back button behavior.
     * 
     * @param override
     */
    public abstract void bindBackButton(boolean override);

    /**
     * A hook required to check if the Back Button is bound.
     * 
     * @return
     */
    public abstract boolean isBackButtonBound();

    /* 
     * Hook in DroidGap for menu plugins
     * (This is in the Android SDK, do we need this on the Interface?)
     */

    //public abstract boolean onCreateOptionsMenu(Menu menu);

    //public abstract boolean onPrepareOptionsMenu(Menu menu);

    //public abstract boolean onOptionsItemSelected(MenuItem item);

    /**
     * Get the Android activity.
     * 
     * @return
     */
    public abstract Activity getActivity();

    /**
     * @deprecated
     * Add services to res/xml/plugins.xml instead.
     * 
     * Add a class that implements a service.
     * 
     * @param serviceType
     * @param className
     */
//    @Deprecated
//    abstract public void addService(String serviceType, String className);

    /**
     * @deprecated
     * Send JavaScript statement back to JavaScript.
     * 
     * @param message
     */
//    @Deprecated
//    abstract public void sendJavascript(String statement);

    /**
     * @deprecated
     * Launch an activity for which you would not like a result when it finished. 
     *  
     * @param intent            The intent to start
     */
//    @Deprecated
//    abstract public void startActivity(Intent intent);

    /**
     * @deprecated
     * Load the specified URL in the Cordova webview.
     * 
     * @param url				The URL to load.
     */
//    @Deprecated
//    abstract public void loadUrl(String url);

    /**
     * @deprecated
     * Send a message to all plugins. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
//    @Deprecated
//    abstract public void postMessage(String id, Object data);

//    @Deprecated
//    public abstract Resources getResources();

//    @Deprecated
//    public abstract String getPackageName();

//    @Deprecated
//    public abstract Object getSystemService(String service);

//    @Deprecated
//    public abstract Context getContext();

//    @Deprecated
//    public abstract Context getBaseContext();

//    @Deprecated
//    public abstract Intent registerReceiver(BroadcastReceiver receiver,
//            IntentFilter intentFilter);

//    @Deprecated
//    public abstract ContentResolver getContentResolver();

//    @Deprecated
//    public abstract void unregisterReceiver(BroadcastReceiver receiver);

//    @Deprecated
//    public abstract Cursor managedQuery(Uri uri, String[] projection, String selection,
//            String[] selectionArgs, String sortOrder);

//    @Deprecated
//    public abstract void runOnUiThread(Runnable runnable);

//    @Deprecated
//    public abstract AssetManager getAssets();

//    @Deprecated
//    public abstract void clearCache();

//    @Deprecated
//    public abstract void clearHistory();

//    @Deprecated
//    public abstract boolean backHistory();

    //public abstract void addWhiteListEntry(String origin, boolean subdomains);

    @Deprecated
    public abstract void cancelLoadUrl();

//    @Deprecated
//    public abstract void showWebPage(String url, boolean openExternal,
//            boolean clearHistory, HashMap<String, Object> params);

//    @Deprecated
//    public abstract Context getApplicationContext();

//    @Deprecated
//    public abstract boolean isUrlWhiteListed(String source);

    /**
     * Called when a message is sent to plugin. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
    public void onMessage(String id, Object data);

    /**
     * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable). 
     * The errorCode parameter corresponds to one of the ERROR_* constants.
     *
     * @param errorCode    The error code corresponding to an ERROR_* value.
     * @param description  A String describing the error.
     * @param failingUrl   The url that failed to load. 
     */
    //public void onReceivedError(final int errorCode, final String description, final String failingUrl);

}
