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
package org.apache.cordova;

//import java.io.IOException;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.Stack;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

//import org.apache.cordova.PreferenceNode;
//import org.apache.cordova.PreferenceSet;
import org.apache.cordova.api.IPlugin;
import org.apache.cordova.api.LOG;
import org.apache.cordova.api.CordovaInterface;
import org.json.JSONException;
import org.json.JSONObject;
//import org.apache.cordova.api.PluginManager;
//import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
//import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.media.AudioManager;
//import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebSettings.LayoutAlgorithm;
//import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

/**
 * This class is the main Android activity that represents the Cordova
 * application.  It should be extended by the user to load the specific
 * html file that contains the application.
 * 
 * As an example:
 * 
 *     package org.apache.cordova.examples;
 *     import android.app.Activity;
 *     import android.os.Bundle;
 *     import org.apache.cordova.*;
 *     
 *     public class Examples extends DroidGap {
 *       @Override
 *       public void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *                  
 *         // Set properties for activity
 *         super.setStringProperty("loadingDialog", "Title,Message"); // show loading dialog
 *         super.setStringProperty("errorUrl", "file:///android_asset/www/error.html"); // if error loading file in super.loadUrl().
 *
 *         // Initialize activity
 *         super.init();
 *         
 *         // Clear cache if you want
 *         super.appView.clearCache(true);
 *         
 *         // Load your application
 *         super.setIntegerProperty("splashscreen", R.drawable.splash); // load splash.jpg image from the resource drawable directory
 *         super.loadUrl("file:///android_asset/www/index.html", 3000); // show splash screen 3 sec before loading app
 *       }
 *     }
 *
 * Properties: The application can be configured using the following properties:
 * 
 *      // Display a native loading dialog when loading app.  Format for value = "Title,Message".  
 *      // (String - default=null)
 *      super.setStringProperty("loadingDialog", "Wait,Loading Demo...");
 * 
 *      // Display a native loading dialog when loading sub-pages.  Format for value = "Title,Message".  
 *      // (String - default=null)
 *      super.setStringProperty("loadingPageDialog", "Loading page...");
 *  
 *      // Load a splash screen image from the resource drawable directory.
 *      // (Integer - default=0)
 *      super.setIntegerProperty("splashscreen", R.drawable.splash);
 *
 *      // Set the background color.
 *      // (Integer - default=0 or BLACK)
 *      super.setIntegerProperty("backgroundColor", Color.WHITE);
 * 
 *      // Time in msec to wait before triggering a timeout error when loading
 *      // with super.loadUrl().  (Integer - default=20000)
 *      super.setIntegerProperty("loadUrlTimeoutValue", 60000);
 * 
 *      // URL to load if there's an error loading specified URL with loadUrl().  
 *      // Should be a local URL starting with file://. (String - default=null)
 *      super.setStringProperty("errorUrl", "file:///android_asset/www/error.html");
 * 
 *      // Enable app to keep running in background. (Boolean - default=true)
 *      super.setBooleanProperty("keepRunning", false);
 *      
 * Cordova.xml configuration:
 *      Cordova uses a configuration file at res/xml/cordova.xml to specify the following settings.
 *      
 *      Approved list of URLs that can be loaded into DroidGap
 *          <access origin="http://server regexp" subdomains="true" />
 *      Log level: ERROR, WARN, INFO, DEBUG, VERBOSE (default=ERROR)
 *          <log level="DEBUG" />
 *
 * Cordova plugins:
 *      Cordova uses a file at res/xml/plugins.xml to list all plugins that are installed.
 *      Before using a new plugin, a new element must be added to the file.
 *          name attribute is the service name passed to Cordova.exec() in JavaScript
 *          value attribute is the Java class name to call.
 *      
 *      <plugins>
 *          <plugin name="App" value="org.apache.cordova.App"/>
 *          ...
 *      </plugins>
 */
public class DroidGap extends Activity implements CordovaInterface {
    public static String TAG = "DroidGap";

    // The webview for our app
    protected CordovaWebView appView;
    protected CordovaWebViewClient webViewClient;

    protected LinearLayout root;
    public boolean bound = false;
    protected boolean cancelLoadUrl = false;
    protected ProgressDialog spinnerDialog = null;

    // The initial URL for our app
    // ie http://server/path/index.html#abc?query
    private String url = null;
    //private Stack<String> urls = new Stack<String>();

    // Url was specified from extras (activity was started programmatically)
    //private String initUrl = null;

    private static int ACTIVITY_STARTING = 0;
    private static int ACTIVITY_RUNNING = 1;
    private static int ACTIVITY_EXITING = 2;
    private int activityState = 0;  // 0=starting, 1=running (after 1st resume), 2=shutting down

    // The base of the initial URL for our app.
    // Does not include file name.  Ends with /
    // ie http://server/path/
    String baseUrl = null;

    // Plugin to call when activity result is received
    protected IPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;

    // Flag indicates that a loadUrl timeout occurred
    //int loadUrlTimeout = 0;

    // Default background color for activity 
    // (this is not the color for the webview, which is set in HTML)
    private int backgroundColor = Color.BLACK;

    /*
     * The variables below are used to cache some of the activity properties.
     */

    // Draw a splash screen using an image located in the drawable resource directory.
    // This is not the same as calling super.loadSplashscreen(url)
    protected int splashscreen = 0;
    protected int splashscreenTime = 0;

    // LoadUrl timeout value in msec (default of 20 sec)
    //protected int loadUrlTimeoutValue = 20000;

    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    // Store the useBrowserHistory preference until we actually need it.
    //private boolean useBrowserHistory = false;

    // preferences read from cordova.xml
    //protected PreferenceSet preferences;

    /**
    * Sets the authentication token.
    * 
    * @param authenticationToken
    * @param host
    * @param realm
    */
    public void setAuthenticationToken(AuthenticationToken authenticationToken, String host, String realm) {
        if (this.appView != null && this.appView.viewClient != null) {
            this.appView.viewClient.setAuthenticationToken(authenticationToken, host, realm);
        }
    }

    /**
     * Removes the authentication token.
     * 
     * @param host
     * @param realm
     * 
     * @return the authentication token or null if did not exist
     */
    public AuthenticationToken removeAuthenticationToken(String host, String realm) {
        if (this.appView != null && this.appView.viewClient != null) {
            return this.appView.viewClient.removeAuthenticationToken(host, realm);
        }
        return null;
    }

    /**
     * Gets the authentication token.
     * 
     * In order it tries:
     * 1- host + realm
     * 2- host
     * 3- realm
     * 4- no host, no realm
     * 
     * @param host
     * @param realm
     * 
     * @return the authentication token
     */
    public AuthenticationToken getAuthenticationToken(String host, String realm) {
        if (this.appView != null && this.appView.viewClient != null) {
            return this.appView.viewClient.getAuthenticationToken(host, realm);
        }
        return null;
    }

    /**
     * Clear all authentication tokens.
     */
    public void clearAuthenticationTokens() {
        if (this.appView != null && this.appView.viewClient != null) {
            this.appView.viewClient.clearAuthenticationTokens();
        }
    }

    /** 
     * Called when the activity is first created. 
     * 
     * @param savedInstanceState
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //preferences = new PreferenceSet();

        LOG.d(TAG, "DroidGap.onCreate()");
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        // This builds the view.  We could probably get away with NOT having a LinearLayout, but I like having a bucket!
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        root = new LinearLayoutSoftKeyboardDetect(this, width, height);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(this.backgroundColor);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));

        // If url was passed in to intent, then init webview, which will load the url
//        Bundle bundle = this.getIntent().getExtras();
//        if (bundle != null) {
//            String url = bundle.getString("url");
//            if (url != null) {
//                this.initUrl = url;
//            }
//        }
        // Setup the hardware volume controls to handle volume control
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    /**
     * Get the Android activity.
     * 
     * @return
     */
    public Activity getActivity() {
        return this;
    }

    /**
     * Create and initialize web container with default web view objects.
     */
    public void init() {
        CordovaWebView webView = new CordovaWebView(DroidGap.this);
        this.init(webView, new CordovaWebViewClient(this, webView), new CordovaChromeClient(this, webView));
    }

    /**
     * Initialize web container with web view objects.
     * 
     * @param webView
     * @param webViewClient
     * @param webChromeClient
     */
    public void init(CordovaWebView webView, CordovaWebViewClient webViewClient, CordovaChromeClient webChromeClient) {
        LOG.d(TAG, "DroidGap.init()");

        // Set up web container
        this.appView = webView;
        this.appView.setId(100);

        this.appView.setWebViewClient(webViewClient);
        this.appView.setWebChromeClient(webChromeClient);
        webViewClient.setWebView(this.appView);
        webChromeClient.setWebView(this.appView);

        // Load Cordova configuration:
        //      white list of allowed URLs
        //      debug setting
        //this.loadConfiguration();
        //Now we can check the preference
        //this.appView.useBrowserHistory = preferences.prefMatches("useBrowserHistory", "true");

        //

        this.appView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0F));

        // Add web view but make it invisible while loading URL
        this.appView.setVisibility(View.INVISIBLE);
        this.root.addView(this.appView);
        setContentView(this.root);

        // Clear cancel flag
        this.cancelLoadUrl = false;
    }

    /**
     * Look at activity parameters and process them.
     * This must be called from the main UI thread.
     */
    //private void handleActivityParameters() {

    // If backgroundColor
    //this.backgroundColor = this.getIntegerProperty("backgroundColor", Color.BLACK);
    //LOG.e(TAG, "Setting background color=" + this.backgroundColor);
    //this.root.setBackgroundColor(this.backgroundColor);

    // If spashscreen
    //this.splashscreen = this.getIntegerProperty("splashscreen", 0);

    // If loadUrlTimeoutValue
    //int timeout = this.getIntegerProperty("loadUrlTimeoutValue", 0);
    //if (timeout > 0) {
    //    this.loadUrlTimeoutValue = timeout;
    //}

    // If keepRunning
    //this.keepRunning = this.getBooleanProperty("keepRunning", true);
    //}

    /**
     * Load the url into the webview.
     * 
     * @param url
     */
    public void loadUrl(String url) {

        // Init web view if not already done
        if (this.appView == null) {
            this.init();
        }

        // Handle activity parameters 
        //this.handleActivityParameters();

        // If backgroundColor
        this.backgroundColor = this.getIntegerProperty("backgroundColor", Color.BLACK);
        LOG.e(TAG, "Setting background color=" + this.backgroundColor);
        this.root.setBackgroundColor(this.backgroundColor);

        // If keepRunning
        this.keepRunning = this.getBooleanProperty("keepRunning", true);

        // Then load the spinner
        this.loadSpinner();

        this.appView.loadUrl(url);

//        // If first page of app, then set URL to load to be the one passed in
//        if (this.initUrl == null || (this.urls.size() > 0)) {
//            this.loadUrlIntoView(url);
//        }
//        // Otherwise use the URL specified in the activity's extras bundle
//        else {
//            this.loadUrlIntoView(this.initUrl);
//        }
    }

    /*
     * Load the spinner
     */
    void loadSpinner() {

        // If loadingDialog property, then show the App loading dialog for first page of app
        String loading = null;
        if ((this.appView == null) || !this.appView.canGoBack()) {
            loading = this.getStringProperty("loadingDialog", null);
        }
        else {
            loading = this.getStringProperty("loadingPageDialog", null);
        }
        if (loading != null) {

            String title = "";
            String message = "Loading Application...";

            if (loading.length() > 0) {
                int comma = loading.indexOf(',');
                if (comma > 0) {
                    title = loading.substring(0, comma);
                    message = loading.substring(comma + 1);
                }
                else {
                    title = "";
                    message = loading;
                }
            }
            this.spinnerStart(title, message);
        }
    }

    /**
     * Load the url into the webview.
     * 
     * @param url
     */
//    private void loadUrlIntoView(final String url) {
//        if (!url.startsWith("javascript:")) {
//            LOG.d(TAG, "DroidGap.loadUrl(%s)", url);
//        }
//
//        if (!url.startsWith("javascript:")) {
//            LOG.d(TAG, "DroidGap: url=%s baseUrl=%s", url, baseUrl);
//        }
//
//        // Load URL on UI thread
//        final DroidGap me = this;
//
////        final Runnable loadError = new Runnable() {
////            public void run() {
////                me.appView.stopLoading();
////                LOG.e(TAG, "DroidGap: TIMEOUT ERROR! - calling webViewClient");
////                appView.viewClient.onReceivedError(me.appView, -6, "The connection to the server was unsuccessful.", url);
////            }
////        };
//
//        this.runOnUiThread(new Runnable() {
//            public void run() {
//
//                // Init web view if not already done
//                if (me.appView == null) {
//                    me.init();
//                }
//
//                // Handle activity parameters (TODO: Somehow abstract this)
//                me.handleActivityParameters();
//
//                // Then load the spinner
//                me.loadSpinner();
//
////                // Create a timeout timer for loadUrl
////                final int currentLoadUrlTimeout = me.loadUrlTimeout;
////                Runnable runnable = new Runnable() {
////                    public void run() {
////                        try {
////                            synchronized (this) {
////                                wait(me.loadUrlTimeoutValue);
////                            }
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                        }
////
////                        // If timeout, then stop loading and handle error
////                        if (me.loadUrlTimeout == currentLoadUrlTimeout) {
////                            me.runOnUiThread(loadError);
////
////                        }
////                    }
////                };
////                Thread thread = new Thread(runnable);
////                thread.start();
//                me.appView.loadUrl(url);
//            }
//        });
//    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     * 
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    public void loadUrl(final String url, int time) {

        // Init web view if not already done
        if (this.appView == null) {
            this.init();
        }

        this.splashscreenTime = time;
        this.appView.loadUrl(url, time);

//        // If first page of app, then set URL to load to be the one passed in
//        if (this.initUrl == null || (this.urls.size() > 0)) {
//            this.loadUrlIntoView(url, time);
//        }
//        // Otherwise use the URL specified in the activity's extras bundle
//        else {
//            this.loadUrlIntoView(this.initUrl);
//        }
    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     * 
     * @param url
     * @param time              The number of ms to wait before showing webview
     */
//    private void loadUrlIntoView(final String url, final int time) {
//
//        // Clear cancel flag
//        this.cancelLoadUrl = false;
//
//        // If not first page of app, then load immediately
//        if (this.urls.size() > 0) {
//            this.loadUrlIntoView(url);
//        }
//
//        if (!url.startsWith("javascript:")) {
//            LOG.d(TAG, "DroidGap.loadUrl(%s, %d)", url, time);
//        }
//
//        this.handleActivityParameters();
//        if (this.splashscreen != 0) {
//            this.showSplashScreen(time);
//        }
//        this.loadUrlIntoView(url);
//    }

    /**
     * Cancel loadUrl before it has been loaded.
     */
    // TODO NO-OP
    public void cancelLoadUrl() {
        this.cancelLoadUrl = true;
    }

    /**
     * Clear the resource cache.
     */
    public void clearCache() {
        if (this.appView == null) {
            this.init();
        }
        this.appView.clearCache(true);
    }

    /**
     * Clear web history in this web view.
     */
    public void clearHistory() {
        this.appView.clearHistory();
//        this.urls.clear();
//        this.appView.clearHistory();
//
//        // Leave current url on history stack
//        if (this.url != null) {
//            this.urls.push(this.url);
//        }
    }

    /**
     * Go to previous page in history.  (We manage our own history)
     * 
     * @return true if we went back, false if we are already at top
     */
    public boolean backHistory() {
        if (this.appView != null) {
            return appView.backHistory();
        }
        return false;
    }

    @Override
    /**
     * Called by the system when the device configuration changes while your activity is running. 
     * 
     * @param Configuration newConfig
     */
    public void onConfigurationChanged(Configuration newConfig) {
        //don't reload the current page when the orientation is changed
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Get boolean property for activity.
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        Boolean p = (Boolean) bundle.get(name);
        if (p == null) {
            return defaultValue;
        }
        return p.booleanValue();
    }

    /**
     * Get int property for activity.
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    public int getIntegerProperty(String name, int defaultValue) {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        Integer p = (Integer) bundle.get(name);
        if (p == null) {
            return defaultValue;
        }
        return p.intValue();
    }

    /**
     * Get string property for activity.
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    public String getStringProperty(String name, String defaultValue) {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        String p = bundle.getString(name);
        if (p == null) {
            return defaultValue;
        }
        return p;
    }

    /**
     * Get double property for activity.
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    public double getDoubleProperty(String name, double defaultValue) {
        Bundle bundle = this.getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        Double p = (Double) bundle.get(name);
        if (p == null) {
            return defaultValue;
        }
        return p.doubleValue();
    }

    /**
     * Set boolean property on activity.
     * 
     * @param name
     * @param value
     */
    public void setBooleanProperty(String name, boolean value) {
        this.getIntent().putExtra(name, value);
    }

    /**
     * Set int property on activity.
     * 
     * @param name
     * @param value
     */
    public void setIntegerProperty(String name, int value) {
        this.getIntent().putExtra(name, value);
    }

    /**
     * Set string property on activity.
     * 
     * @param name
     * @param value
     */
    public void setStringProperty(String name, String value) {
        this.getIntent().putExtra(name, value);
    }

    /**
     * Set double property on activity.
     * 
     * @param name
     * @param value
     */
    public void setDoubleProperty(String name, double value) {
        this.getIntent().putExtra(name, value);
    }

    @Override
    /**
     * Called when the system is about to start resuming a previous activity. 
     */
    protected void onPause() {
        super.onPause();

        // Don't process pause if shutting down, since onDestroy() will be called
        if (this.activityState == ACTIVITY_EXITING) {
            return;
        }

        if (this.appView == null) {
            return;
        }

        // Send pause event to JavaScript
        this.appView.loadUrl("javascript:try{cordova.require('cordova/channel').onPause.fire();}catch(e){console.log('exception firing pause event from native');};");

        // Forward to plugins
        if (this.appView.pluginManager != null) {
            this.appView.pluginManager.onPause(this.keepRunning);
        }

        // If app doesn't want to run in background
        if (!this.keepRunning) {

            // Pause JavaScript timers (including setInterval)
            this.appView.pauseTimers();
        }
    }

    @Override
    /**
     * Called when the activity receives a new intent
     **/
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //Forward to plugins
        if ((this.appView != null) && (this.appView.pluginManager != null)) {
            this.appView.pluginManager.onNewIntent(intent);
        }
    }

    @Override
    /**
     * Called when the activity will start interacting with the user. 
     */
    protected void onResume() {
        super.onResume();

        if (this.activityState == ACTIVITY_STARTING) {
            this.activityState = ACTIVITY_RUNNING;
            return;
        }

        if (this.appView == null) {
            return;
        }

        // Send resume event to JavaScript
        this.appView.loadUrl("javascript:try{cordova.require('cordova/channel').onResume.fire();}catch(e){console.log('exception firing resume event from native');};");

        // Forward to plugins
        if (this.appView.pluginManager != null) {
            this.appView.pluginManager.onResume(this.keepRunning || this.activityResultKeepRunning);
        }

        // If app doesn't want to run in background
        if (!this.keepRunning || this.activityResultKeepRunning) {

            // Restore multitasking state
            if (this.activityResultKeepRunning) {
                this.keepRunning = this.activityResultKeepRunning;
                this.activityResultKeepRunning = false;
            }

            // Resume JavaScript timers (including setInterval)
            if (this.appView.pluginManager != null) {
                this.appView.resumeTimers();
            }
        }
    }

    @Override
    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
        super.onDestroy();

        if (this.appView != null) {

            // Send destroy event to JavaScript
            this.appView.loadUrl("javascript:try{cordova.require('cordova/channel').onDestroy.fire();}catch(e){console.log('exception firing destroy event from native');};");

            // Load blank page so that JavaScript onunload is called
            this.appView.loadUrl("about:blank");

            // Forward to plugins
            if (this.appView.pluginManager != null) {
                this.appView.pluginManager.onDestroy();
            }
        }
        else {
            this.endActivity();
        }
    }

    /**
     * Send a message to all plugins. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
    public void postMessage(String id, Object data) {
        if (this.appView != null) {
            this.appView.postMessage(id, data);
        }
    }

    /**
     * @deprecated
     * Add services to res/xml/plugins.xml instead.
     * 
     * Add a class that implements a service.
     * 
     * @param serviceType
     * @param className
     */
    public void addService(String serviceType, String className) {
        if (this.appView != null && this.appView.pluginManager != null) {
            this.appView.pluginManager.addService(serviceType, className);
        }
    }

    /**
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     * 
     * @param message
     */
    public void sendJavascript(String statement) {
        //We need to check for the null case on the Kindle Fire because it changes the width and height on load
        if (this.appView != null && this.appView.callbackServer != null) {
            this.appView.callbackServer.sendJavascript(statement);
        }
    }

    /**
     * Show the spinner.  Must be called from the UI thread.
     * 
     * @param title         Title of the dialog
     * @param message       The message of the dialog
     */
    public void spinnerStart(final String title, final String message) {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
        final DroidGap me = this;
        this.spinnerDialog = ProgressDialog.show(DroidGap.this, title, message, true, true,
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        me.spinnerDialog = null;
                    }
                });
    }

    /**
     * Stop spinner.
     */
    public void spinnerStop() {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
    }

    /**
     * End this activity by calling finish for activity
     */
    public void endActivity() {
        //this.activityState = ACTIVITY_EXITING;
        this.finish();
    }

    /**
     * Finish for Activity has been called.
     */
    public void finish() {
        this.activityState = ACTIVITY_EXITING;
        super.finish();
    }

    /**
     * Called when a key is released. (Key UP)
     * 
     * @param keyCode
     * @param event
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.appView == null) {
            return super.onKeyUp(keyCode, event);
        }

        // If back key
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // If back key is bound, then send event to JavaScript
            if (this.bound) {
                this.appView.loadUrl("javascript:cordova.fireDocumentEvent('backbutton');");
                return true;
            } else {
                // If not bound
                // Go to previous page in webview if it is possible to go back
                if (this.backHistory()) {
                    return true;
                }
                // If not, then invoke behavior of super class
                else {
                    this.activityState = ACTIVITY_EXITING;
                    return super.onKeyUp(keyCode, event);
                }
            }
        }

        // If menu key
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.appView.loadUrl("javascript:cordova.fireDocumentEvent('menubutton');");
            return super.onKeyUp(keyCode, event);
        }

        // If search key
        else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            this.appView.loadUrl("javascript:cordova.fireDocumentEvent('searchbutton');");
            return true;
        }

        return false;
    }

    /**
     * Any calls to Activity.startActivityForResult must use method below, so 
     * the result can be routed to them correctly.  
     * 
     * This is done to eliminate the need to modify DroidGap.java to receive activity results.
     * 
     * @param intent            The intent to start
     * @param requestCode       Identifies who to send the result to
     * 
     * @throws RuntimeException
     */
    //@Override
    //public void startActivityForResult(Intent intent, int requestCode) throws RuntimeException {
    //    LOG.d(TAG, "DroidGap.startActivityForResult(intent,%d)", requestCode);
    //    super.startActivityForResult(intent, requestCode);
    //}

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits, 
     * your onActivityResult() method will be called.
     *  
     * @param command           The command object
     * @param intent            The intent to start
     * @param requestCode       The request code that is passed to callback to identify the activity
     */
    public void startActivityForResult(IPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode       The request code originally supplied to startActivityForResult(), 
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public void setActivityResultCallback(IPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    /**
     * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable). 
     * The errorCode parameter corresponds to one of the ERROR_* constants.
     *
     * @param errorCode    The error code corresponding to an ERROR_* value.
     * @param description  A String describing the error.
     * @param failingUrl   The url that failed to load. 
     */
    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
        final DroidGap me = this;

        // Stop "app loading" spinner if showing
        this.spinnerStop();

        // If errorUrl specified, then load it
        final String errorUrl = me.getStringProperty("errorUrl", null);
        if ((errorUrl != null) && (errorUrl.startsWith("file://") || errorUrl.indexOf(me.baseUrl) == 0 || this.appView.isUrlWhiteListed(errorUrl)) && (!failingUrl.equals(errorUrl))) {

            // Load URL on UI thread
            me.runOnUiThread(new Runnable() {
                public void run() {
                    me.appView.showWebPage(errorUrl, false, true, null);
                }
            });
        }
        // If not, then display error dialog
        else {
            final boolean exit = !(errorCode == WebViewClient.ERROR_HOST_LOOKUP);
            me.runOnUiThread(new Runnable() {
                public void run() {
                    if (exit)
                    {
                        me.appView.setVisibility(View.GONE);
                        me.displayError("Application Error", description + " (" + failingUrl + ")", "OK", exit);
                    }
                }
            });
        }
    }

    /**
     * Display an error dialog and optionally exit application.
     * 
     * @param title
     * @param message
     * @param button
     * @param exit
     */
    public void displayError(final String title, final String message, final String button, final boolean exit) {
        final DroidGap me = this;
        me.runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder dlg = new AlertDialog.Builder(me);
                dlg.setMessage(message);
                dlg.setTitle(title);
                dlg.setCancelable(false);
                dlg.setPositiveButton(button,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (exit) {
                                    me.endActivity();
                                }
                            }
                        });
                dlg.create();
                dlg.show();
            }
        });
    }

    /**
     * Load Cordova configuration from res/xml/cordova.xml.
     * Approved list of URLs that can be loaded into DroidGap
     *      <access origin="http://server regexp" subdomains="true" />
     * Log level: ERROR, WARN, INFO, DEBUG, VERBOSE (default=ERROR)
     *      <log level="DEBUG" />
     */
//    private void loadConfiguration() {
//        int id = getResources().getIdentifier("cordova", "xml", getPackageName());
//        if (id == 0) {
//            LOG.i("CordovaLog", "cordova.xml missing. Ignoring...");
//            return;
//        }
//        XmlResourceParser xml = getResources().getXml(id);
//        int eventType = -1;
//        while (eventType != XmlResourceParser.END_DOCUMENT) {
//            if (eventType == XmlResourceParser.START_TAG) {
//                String strNode = xml.getName();
//                if (strNode.equals("access")) {
//                    String origin = xml.getAttributeValue(null, "origin");
//                    String subdomains = xml.getAttributeValue(null, "subdomains");
//                    if (origin != null) {
//                        appView.addWhiteListEntry(origin, (subdomains != null) && (subdomains.compareToIgnoreCase("true") == 0));
//                    }
//                }
//                else if (strNode.equals("log")) {
//                    String level = xml.getAttributeValue(null, "level");
//                    LOG.i("CordovaLog", "Found log level %s", level);
//                    if (level != null) {
//                        LOG.setLogLevel(level);
//                    }
//                }
//                else if (strNode.equals("preference")) {
//                    String name = xml.getAttributeValue(null, "name");
//                    String value = xml.getAttributeValue(null, "value");
//                    String readonlyString = xml.getAttributeValue(null, "readonly");
//
//                    boolean readonly = (readonlyString != null &&
//                            readonlyString.equals("true"));
//
//                    LOG.i("CordovaLog", "Found preference for %s", name);
//
//                    preferences.add(new PreferenceNode(name, value, readonly));
//                }
//            }
//            try {
//                eventType = xml.next();
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * Determine if URL is in approved list of URLs to load.
     * 
     * @param url
     * @return
     */
    public boolean isUrlWhiteListed(String url) {
        // Check to see if we have matched url previously
        if (this.appView != null) {
            return this.appView.isUrlWhiteListed(url);
        }
        return false;
    }

    /* 
     * Hook in DroidGap for menu plugins
     * 
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.postMessage("onCreateOptionsMenu", menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.postMessage("onPrepareOptionsMenu", menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.postMessage("onOptionsItemSelected", item);
        return true;
    }

    /**
     * Get Activity context.
     * 
     * @return
     */
    public Context getContext() {
        return this.getContext();
    }

    /**
     * Override the backbutton.
     * 
     * @param override
     */
    public void bindBackButton(boolean override) {
        this.bound = override;
    }

    /**
     * Determine of backbutton is overridden.
     * 
     * @return
     */
    public boolean isBackButtonBound() {
        return this.bound;
    }

    /**
     * Load the specified URL in the Cordova webview or a new browser instance.
     * 
     * NOTE: If openExternal is false, only URLs listed in whitelist can be loaded.
     *
     * @param url           The url to load.
     * @param openExternal  Load url in browser instead of Cordova webview.
     * @param clearHistory  Clear the history stack, so new page becomes top of history
     * @param params        DroidGap parameters for new app
     */
    public void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params) {
        if (this.appView != null) {
            appView.showWebPage(url, openExternal, clearHistory, params);
        }
    }

    protected Dialog splashDialog;

    /**
     * Removes the Dialog that displays the splash screen
     */
    public void removeSplashScreen() {
        if (splashDialog != null) {
            splashDialog.dismiss();
            splashDialog = null;
        }
    }

    /**
     * Shows the splash screen over the full Activity
     */
    @SuppressWarnings("deprecation")
    protected void showSplashScreen(int time) {

        // Get reference to display
        Display display = getWindowManager().getDefaultDisplay();

        // Create the layout for the dialog
        LinearLayout root = new LinearLayout(this);
        root.setMinimumHeight(display.getHeight());
        root.setMinimumWidth(display.getWidth());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(this.getIntegerProperty("backgroundColor", Color.BLACK));
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT, 0.0F));
        root.setBackgroundResource(this.splashscreen);

        // Create and show the dialog
        splashDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        splashDialog.setContentView(root);
        splashDialog.setCancelable(false);
        splashDialog.show();

        // Set Runnable to remove splash screen just in case
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                removeSplashScreen();
            }
        }, time);
    }

    /**
     * Called when a message is sent to plugin. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
    public void onMessage(String id, Object data) {
        LOG.d(TAG, "onMessage(" + id + "," + data + ")");
        if ("splashscreen".equals(id)) {
            if ("hide".equals(data.toString())) {
                this.removeSplashScreen();
            }
            else {
                this.splashscreen = this.getIntegerProperty("splashscreen", 0);
                this.showSplashScreen(this.splashscreenTime);
            }
        }
        else if ("spinner".equals(id)) {
            if ("stop".equals(data.toString())) {
                this.spinnerStop();
                this.appView.setVisibility(View.VISIBLE);
            }
        }
        else if ("onReceivedError".equals(id)) {
            JSONObject d = (JSONObject) data;
            try {
                this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
