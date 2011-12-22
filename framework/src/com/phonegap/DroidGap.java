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
package com.phonegap;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Iterator;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.phonegap.api.LOG;
import com.phonegap.api.PhonegapActivity;
import com.phonegap.api.IPlugin;
import com.phonegap.api.PluginManager;
import 	org.xmlpull.v1.XmlPullParserException;

/**
 * This class is the main Android activity that represents the PhoneGap
 * application.  It should be extended by the user to load the specific
 * html file that contains the application.
 * 
 * As an example:
 * 
 *     package com.phonegap.examples;
 *     import android.app.Activity;
 *     import android.os.Bundle;
 *     import com.phonegap.*;
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
 * Phonegap.xml configuration:
 *      PhoneGap uses a configuration file at res/xml/phonegap.xml to specify the following settings.
 *      
 *      Approved list of URLs that can be loaded into DroidGap
 *          <access origin="http://server regexp" subdomains="true" />
 *      Log level: ERROR, WARN, INFO, DEBUG, VERBOSE (default=ERROR)
 *          <log level="DEBUG" />
 *
 * Phonegap plugins:
 *      PhoneGap uses a file at res/xml/plugins.xml to list all plugins that are installed.
 *      Before using a new plugin, a new element must be added to the file.
 *          name attribute is the service name passed to PhoneGap.exec() in JavaScript
 *          value attribute is the Java class name to call.
 *      
 *      <plugins>
 *          <plugin name="App" value="com.phonegap.App"/>
 *          ...
 *      </plugins>
 */
public class DroidGap extends PhonegapActivity {
    public static String TAG = "DroidGap";
    
    // The webview for our app
    protected WebView appView;
    protected WebViewClient webViewClient;
    private ArrayList<Pattern> whiteList = new ArrayList<Pattern>();
    private HashMap<String, Boolean> whiteListCache = new HashMap<String,Boolean>();

    protected LinearLayout root;
    public boolean bound = false;
    public CallbackServer callbackServer;
    protected PluginManager pluginManager;
    protected boolean cancelLoadUrl = false;
    protected ProgressDialog spinnerDialog = null;

    // The initial URL for our app
    // ie http://server/path/index.html#abc?query
    private String url = null;
    private Stack<String> urls = new Stack<String>();
    
    // Url was specified from extras (activity was started programmatically)
    private String initUrl = null;
    
    private static int ACTIVITY_STARTING = 0;
    private static int ACTIVITY_RUNNING = 1;
    private static int ACTIVITY_EXITING = 2;
    private int activityState = 0;  // 0=starting, 1=running (after 1st resume), 2=shutting down
    
    // The base of the initial URL for our app.
    // Does not include file name.  Ends with /
    // ie http://server/path/
    private String baseUrl = null;

    // Plugin to call when activity result is received
    protected IPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;

    // Flag indicates that a loadUrl timeout occurred
    private int loadUrlTimeout = 0;
    
    // Default background color for activity 
    // (this is not the color for the webview, which is set in HTML)
    private int backgroundColor = Color.BLACK;
    
    /*
     * The variables below are used to cache some of the activity properties.
     */

    // Draw a splash screen using an image located in the drawable resource directory.
    // This is not the same as calling super.loadSplashscreen(url)
    protected int splashscreen = 0;

    // LoadUrl timeout value in msec (default of 20 sec)
    protected int loadUrlTimeoutValue = 20000;
    
    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    /** 
     * Called when the activity is first created. 
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
                ViewGroup.LayoutParams.FILL_PARENT, 0.0F));
        
        // Load PhoneGap configuration:
        //      white list of allowed URLs
        //      debug setting
        this.loadConfiguration();

        // If url was passed in to intent, then init webview, which will load the url
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString("url");
            if (url != null) {
                this.initUrl = url;
            }
        }
        // Setup the hardware volume controls to handle volume control
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    
    /**
     * Create and initialize web container.
     */
    public void init() {
        LOG.d(TAG, "DroidGap.init()");
        
        // Create web container
        this.appView = new WebView(DroidGap.this);
        this.appView.setId(100);
        
        this.appView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT, 
                1.0F));

        this.appView.setWebChromeClient(new GapClient(DroidGap.this));
        this.setWebViewClient(this.appView, new GapViewClient(this));

        this.appView.setInitialScale(0);
        this.appView.setVerticalScrollBarEnabled(false);
        this.appView.requestFocusFromTouch();

        // Enable JavaScript
        WebSettings settings = this.appView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        
        //Set the nav dump for HTC
        settings.setNavDump(true);

        // Enable database
        settings.setDatabaseEnabled(true);
        String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath(); 
        settings.setDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);
        
        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);

        // Add web view but make it invisible while loading URL
        this.appView.setVisibility(View.INVISIBLE);
        root.addView(this.appView);
        setContentView(root);
        
        // Clear cancel flag
        this.cancelLoadUrl = false;
    }
    
    /**
     * Set the WebViewClient.
     * 
     * @param appView
     * @param client
     */
    protected void setWebViewClient(WebView appView, WebViewClient client) {
        this.webViewClient = client;
        appView.setWebViewClient(client);
    }

    /**
     * Look at activity parameters and process them.
     * This must be called from the main UI thread.
     */
    private void handleActivityParameters() {

        // If backgroundColor
        this.backgroundColor = this.getIntegerProperty("backgroundColor", Color.BLACK);
        this.root.setBackgroundColor(this.backgroundColor);

        // If spashscreen
        this.splashscreen = this.getIntegerProperty("splashscreen", 0);
        if ((this.urls.size() == 0) && (this.splashscreen != 0)) {
            root.setBackgroundResource(this.splashscreen);
        }

        // If loadUrlTimeoutValue
        int timeout = this.getIntegerProperty("loadUrlTimeoutValue", 0);
        if (timeout > 0) {
            this.loadUrlTimeoutValue = timeout;
        }
        
        // If keepRunning
        this.keepRunning = this.getBooleanProperty("keepRunning", true);
    }
    
    /**
     * Load the url into the webview.
     * 
     * @param url
     */
    public void loadUrl(String url) {
    	
    	// If first page of app, then set URL to load to be the one passed in
    	if (this.initUrl == null || (this.urls.size() > 0)) {
    		this.loadUrlIntoView(url);
    	}
    	// Otherwise use the URL specified in the activity's extras bundle
    	else {
    		this.loadUrlIntoView(this.initUrl);
    	}
    }
    
    /**
     * Load the url into the webview.
     * 
     * @param url
     */
    private void loadUrlIntoView(final String url) {
        if (!url.startsWith("javascript:")) {
            LOG.d(TAG, "DroidGap.loadUrl(%s)", url);
        }

        this.url = url;
        if (this.baseUrl == null) {
            int i = url.lastIndexOf('/');
            if (i > 0) {
                this.baseUrl = url.substring(0, i+1);
            }
            else {
                this.baseUrl = this.url + "/";
            }
        }
        if (!url.startsWith("javascript:")) {
            LOG.d(TAG, "DroidGap: url=%s baseUrl=%s", url, baseUrl);
        }
        
        // Load URL on UI thread
        final DroidGap me = this;
        this.runOnUiThread(new Runnable() {
            public void run() {

                // Init web view if not already done
                if (me.appView == null) {
                    me.init();
                }

                // Handle activity parameters
                me.handleActivityParameters();

                // Track URLs loaded instead of using appView history
                me.urls.push(url);
                me.appView.clearHistory();
            
                // Create callback server and plugin manager
                if (me.callbackServer == null) {
                    me.callbackServer = new CallbackServer();
                    me.callbackServer.init(url);
                }
                else {
                    me.callbackServer.reinit(url);
                }
                if (me.pluginManager == null) {
                    me.pluginManager = new PluginManager(me.appView, me);        
                }
                else {
                    me.pluginManager.reinit();
                }
                
                // If loadingDialog property, then show the App loading dialog for first page of app
                String loading = null;
                if (me.urls.size() == 1) {
                	loading = me.getStringProperty("loadingDialog", null);
                }
                else {
                	loading = me.getStringProperty("loadingPageDialog", null);                	
                }
                if (loading != null) {

                    String title = "";
                    String message = "Loading Application...";

                    if (loading.length() > 0) {
                        int comma = loading.indexOf(',');
                        if (comma > 0) {
                            title = loading.substring(0, comma);
                            message = loading.substring(comma+1);
                        }
                        else {
                            title = "";
                            message = loading;
                        }
                    }
                    me.spinnerStart(title, message);
                }

                // Create a timeout timer for loadUrl
                final int currentLoadUrlTimeout = me.loadUrlTimeout;
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            synchronized(this) {
                                wait(me.loadUrlTimeoutValue);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // If timeout, then stop loading and handle error
                        if (me.loadUrlTimeout == currentLoadUrlTimeout) {
                            me.appView.stopLoading();
                            LOG.e(TAG, "DroidGap: TIMEOUT ERROR! - calling webViewClient");
                            me.webViewClient.onReceivedError(me.appView, -6, "The connection to the server was unsuccessful.", url);
                        }
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
                me.appView.loadUrl(url);
            }
        });
    }
    
    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     * 
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    public void loadUrl(final String url, int time) {
    	
    	// If first page of app, then set URL to load to be the one passed in
    	if (this.initUrl == null || (this.urls.size() > 0)) {
    		this.loadUrlIntoView(url, time);
    	}
    	// Otherwise use the URL specified in the activity's extras bundle
    	else {
    		this.loadUrlIntoView(this.initUrl);
    	}
    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     * 
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    private void loadUrlIntoView(final String url, final int time) {

        // Clear cancel flag
        this.cancelLoadUrl = false;
    	
    	// If not first page of app, then load immediately
        if (this.urls.size() > 0) {
    		this.loadUrlIntoView(url);
    	}
        
    	if (!url.startsWith("javascript:")) {
    	    LOG.d(TAG, "DroidGap.loadUrl(%s, %d)", url, time);
        }
        final DroidGap me = this;

        // Handle activity parameters
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (me.appView == null) {
                    me.init();
                }
                me.handleActivityParameters();
            }
        });

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    synchronized(this) {
                        this.wait(time);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!me.cancelLoadUrl) {
                    me.loadUrlIntoView(url);
                }
                else{
                    me.cancelLoadUrl = false;
                    LOG.d(TAG, "Aborting loadUrl(%s): Another URL was loaded before timer expired.", url);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
    
    /**
     * Cancel loadUrl before it has been loaded.
     */
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
        this.urls.clear();
        this.appView.clearHistory();
        
        // Leave current url on history stack
        if (this.url != null) {
            this.urls.push(this.url);
        }
    }
    
    /**
     * Go to previous page in history.  (We manage our own history)
     * 
     * @return true if we went back, false if we are already at top
     */
    public boolean backHistory() {

        // Check webview first to see if there is a history
        // This is needed to support curPage#diffLink, since they are added to appView's history, but not our history url array (JQMobile behavior)
        if (this.appView.canGoBack()) {
            this.appView.goBack();  
            return true;
        }

        // If our managed history has prev url
        if (this.urls.size() > 1) {
            this.urls.pop();                // Pop current url
            String url = this.urls.pop();   // Pop prev url that we want to load, since it will be added back by loadUrl()
            this.loadUrl(url);
            return true;
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
        Boolean p = (Boolean)bundle.get(name);
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
        Integer p = (Integer)bundle.get(name);
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
        Double p = (Double)bundle.get(name);
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
        this.appView.loadUrl("javascript:try{PhoneGap.fireDocumentEvent('pause');}catch(e){};");

        // Forward to plugins
        this.pluginManager.onPause(this.keepRunning);

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
        this.pluginManager.onNewIntent(intent);
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
        this.appView.loadUrl("javascript:try{PhoneGap.fireDocumentEvent('resume');}catch(e){};");

        // Forward to plugins
        this.pluginManager.onResume(this.keepRunning || this.activityResultKeepRunning);

        // If app doesn't want to run in background
        if (!this.keepRunning || this.activityResultKeepRunning) {

            // Restore multitasking state
            if (this.activityResultKeepRunning) {
                this.keepRunning = this.activityResultKeepRunning;
                this.activityResultKeepRunning = false;
            }

            // Resume JavaScript timers (including setInterval)
            this.appView.resumeTimers();
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
            this.appView.loadUrl("javascript:try{PhoneGap.onDestroy.fire();}catch(e){};");

            // Load blank page so that JavaScript onunload is called
            this.appView.loadUrl("about:blank");

            // Forward to plugins
            this.pluginManager.onDestroy();
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
        
        // Forward to plugins
        this.pluginManager.postMessage(id, data);
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
    @Deprecated
    public void addService(String serviceType, String className) {
        this.pluginManager.addService(serviceType, className);
    }
    
    /**
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     * 
     * @param message
     */
    public void sendJavascript(String statement) {
        this.callbackServer.sendJavascript(statement);
    }

    /**
     * Load the specified URL in the PhoneGap webview or a new browser instance.
     * 
     * NOTE: If openExternal is false, only URLs listed in whitelist can be loaded.
     *
     * @param url           The url to load.
     * @param openExternal  Load url in browser instead of PhoneGap webview.
     * @param clearHistory  Clear the history stack, so new page becomes top of history
     * @param params        DroidGap parameters for new app
     */
    public void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params) { //throws android.content.ActivityNotFoundException {
        LOG.d(TAG, "showWebPage(%s, %b, %b, HashMap", url, openExternal, clearHistory);
        
        // If clearing history
        if (clearHistory) {
            this.clearHistory();
        }
        
        // If loading into our webview
        if (!openExternal) {
            
            // Make sure url is in whitelist
            if (url.startsWith("file://") || url.indexOf(this.baseUrl) == 0 || isUrlWhiteListed(url)) {
                // TODO: What about params?
                
                // Clear out current url from history, since it will be replacing it
                if (clearHistory) {
                    this.urls.clear();
                }
                
                // Load new URL
                this.loadUrl(url);
            }
            // Load in default viewer if not
            else {
                LOG.w(TAG, "showWebPage: Cannot load URL into webview since it is not in white list.  Loading into browser instead. (URL="+url+")");
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    this.startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(TAG, "Error loading url "+url, e);
                }
            }
        }
        
        // Load in default view intent
        else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                this.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                LOG.e(TAG, "Error loading url "+url, e);
            }
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
        this.spinnerDialog = ProgressDialog.show(DroidGap.this, title , message, true, true, 
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
     * Set the chrome handler.
     */
    public class GapClient extends WebChromeClient {

        private String TAG = "PhoneGapLog";
        private long MAX_QUOTA = 100 * 1024 * 1024;
        private DroidGap ctx;
        
        /**
         * Constructor.
         * 
         * @param ctx
         */
        public GapClient(Context ctx) {
            this.ctx = (DroidGap)ctx;
        }

        /**
         * Tell the client to display a javascript alert dialog.
         * 
         * @param view
         * @param url
         * @param message
         * @param result
         */
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
            dlg.setMessage(message);
            dlg.setTitle("Alert");
            //Don't let alerts break the back button
            dlg.setCancelable(true);
            dlg.setPositiveButton(android.R.string.ok,
                new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
            dlg.setOnCancelListener(
               new DialogInterface.OnCancelListener() {
                   public void onCancel(DialogInterface dialog) {
                       result.confirm();
                       }
                   });
            dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
                //DO NOTHING
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        result.confirm();
                        return false;
                    }
                    else
                        return true;
                    }
                });
            dlg.create();
            dlg.show();
            return true;
        }       

        /**
         * Tell the client to display a confirm dialog to the user.
         * 
         * @param view
         * @param url
         * @param message
         * @param result
         */
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
            dlg.setMessage(message);
            dlg.setTitle("Confirm");
            dlg.setCancelable(true);
            dlg.setPositiveButton(android.R.string.ok, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
            dlg.setNegativeButton(android.R.string.cancel, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
            dlg.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        result.cancel();
                        }
                    });
            dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
                //DO NOTHING
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        result.cancel();
                        return false;
                    }
                    else
                        return true;
                    }
                });
            dlg.create();
            dlg.show();
            return true;
        }

        /**
         * Tell the client to display a prompt dialog to the user. 
         * If the client returns true, WebView will assume that the client will 
         * handle the prompt dialog and call the appropriate JsPromptResult method.
         * 
         * Since we are hacking prompts for our own purposes, we should not be using them for 
         * this purpose, perhaps we should hack console.log to do this instead!
         * 
         * @param view
         * @param url
         * @param message
         * @param defaultValue
         * @param result
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            
            // Security check to make sure any requests are coming from the page initially
            // loaded in webview and not another loaded in an iframe.
            boolean reqOk = false;
            if (url.startsWith("file://") || url.indexOf(this.ctx.baseUrl) == 0 || isUrlWhiteListed(url)) {
                reqOk = true;
            }
            
            // Calling PluginManager.exec() to call a native service using 
            // prompt(this.stringify(args), "gap:"+this.stringify([service, action, callbackId, true]));
            if (reqOk && defaultValue != null && defaultValue.length() > 3 && defaultValue.substring(0, 4).equals("gap:")) {
                JSONArray array;
                try {
                    array = new JSONArray(defaultValue.substring(4));
                    String service = array.getString(0);
                    String action = array.getString(1);
                    String callbackId = array.getString(2);
                    boolean async = array.getBoolean(3);
                    String r = pluginManager.exec(service, action, callbackId, message, async);
                    result.confirm(r);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            
            // Polling for JavaScript messages 
            else if (reqOk && defaultValue != null && defaultValue.equals("gap_poll:")) {
                String r = callbackServer.getJavascript();
                result.confirm(r);
            }
            
            // Calling into CallbackServer
            else if (reqOk && defaultValue != null && defaultValue.equals("gap_callbackServer:")) {
                String r = "";
                if (message.equals("usePolling")) {
                    r = ""+callbackServer.usePolling();
                }
                else if (message.equals("restartServer")) {
                    callbackServer.restartServer();
                }
                else if (message.equals("getPort")) {
                    r = Integer.toString(callbackServer.getPort());
                }
                else if (message.equals("getToken")) {
                    r = callbackServer.getToken();
                }
                result.confirm(r);
            }
            
            // PhoneGap JS has initialized, so show webview
            // (This solves white flash seen when rendering HTML)
            else if (reqOk && defaultValue != null && defaultValue.equals("gap_init:")) {
                appView.setVisibility(View.VISIBLE);
                ctx.spinnerStop();
                result.confirm("OK");
            }

            // Show dialog
            else {
                final JsPromptResult res = result;
                AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
                dlg.setMessage(message);
                final EditText input = new EditText(this.ctx);
                if (defaultValue != null) {
                    input.setText(defaultValue);
                }
                dlg.setView(input);
                dlg.setCancelable(false);
                dlg.setPositiveButton(android.R.string.ok, 
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        String usertext = input.getText().toString();
                        res.confirm(usertext);
                    }
                });
                dlg.setNegativeButton(android.R.string.cancel, 
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        res.cancel();
                    }
                });
                dlg.create();
                dlg.show();
            }
            return true;
        }
        
        /**
         * Handle database quota exceeded notification.
         *
         * @param url
         * @param databaseIdentifier
         * @param currentQuota
         * @param estimatedSize
         * @param totalUsedQuota
         * @param quotaUpdater
         */
        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
                long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
        {
            LOG.d(TAG, "DroidGap:  onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", estimatedSize, currentQuota, totalUsedQuota);

            if( estimatedSize < MAX_QUOTA)
            {
                //increase for 1Mb
                long newQuota = estimatedSize;
                LOG.d(TAG, "calling quotaUpdater.updateQuota newQuota: %d", newQuota);
                quotaUpdater.updateQuota(newQuota);
            }
            else
            {
                // Set the quota to whatever it is and force an error
                // TODO: get docs on how to handle this properly
                quotaUpdater.updateQuota(currentQuota);
            }
        }

        // console.log in api level 7: http://developer.android.com/guide/developing/debug-tasks.html
        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID)
        {       
            LOG.d(TAG, "%s: Line %d : %s", sourceID, lineNumber, message);
            super.onConsoleMessage(message, lineNumber, sourceID);
        }
        
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage)
        {       
            LOG.d(TAG, consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        /**
         * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin. 
         * 
         * @param origin
         * @param callback
         */
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }

    }

    /**
     * The webview client receives notifications about appView
     */
    public class GapViewClient extends WebViewClient {

        DroidGap ctx;

        /**
         * Constructor.
         * 
         * @param ctx
         */
        public GapViewClient(DroidGap ctx) {
            this.ctx = ctx;
        }
        
        /**
         * Give the host application a chance to take over the control when a new url 
         * is about to be loaded in the current WebView.
         * 
         * @param view          The WebView that is initiating the callback.
         * @param url           The url to be loaded.
         * @return              true to override, false for default behavior
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            
            // First give any plugins the chance to handle the url themselves
            if (this.ctx.pluginManager.onOverrideUrlLoading(url)) {
            }
            
            // If dialing phone (tel:5551212)
            else if (url.startsWith(WebView.SCHEME_TEL)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(TAG, "Error dialing "+url+": "+ e.toString());
                }
            }

            // If displaying map (geo:0,0?q=address)
            else if (url.startsWith("geo:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(TAG, "Error showing map "+url+": "+ e.toString());
                }
            }

            // If sending email (mailto:abc@corp.com)
            else if (url.startsWith(WebView.SCHEME_MAILTO)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(TAG, "Error sending email "+url+": "+ e.toString());
                }
            }

            // If sms:5551212?body=This is the message
            else if (url.startsWith("sms:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    // Get address
                    String address = null;
                    int parmIndex = url.indexOf('?');
                    if (parmIndex == -1) {
                        address = url.substring(4);
                    }
                    else {
                        address = url.substring(4, parmIndex);

                        // If body, then set sms body
                        Uri uri = Uri.parse(url);
                        String query = uri.getQuery();
                        if (query != null) {
                            if (query.startsWith("body=")) {
                                intent.putExtra("sms_body", query.substring(5));
                            }
                        }
                    }
                    intent.setData(Uri.parse("sms:"+address));
                    intent.putExtra("address", address);
                    intent.setType("vnd.android-dir/mms-sms");
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(TAG, "Error sending sms "+url+":"+ e.toString());
                }
            }

            // All else
            else {

                // If our app or file:, then load into a new phonegap webview container by starting a new instance of our activity.
                // Our app continues to run.  When BACK is pressed, our app is redisplayed.
                if (url.startsWith("file://") || url.indexOf(this.ctx.baseUrl) == 0 || isUrlWhiteListed(url)) {
                    this.ctx.loadUrl(url);
                }

                // If not our application, let default viewer handle
                else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        LOG.e(TAG, "Error loading url "+url, e);
                    }
                }
            }
            return true;
        }
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // Clear history so history.back() doesn't do anything.  
            // So we can reinit() native side CallbackServer & PluginManager.
            view.clearHistory(); 
        }
        
        /**
         * Notify the host application that a page has finished loading.
         * 
         * @param view          The webview initiating the callback.
         * @param url           The url of the page.
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            // Clear timeout flag
            this.ctx.loadUrlTimeout++;

            // Try firing the onNativeReady event in JS. If it fails because the JS is
            // not loaded yet then just set a flag so that the onNativeReady can be fired
            // from the JS side when the JS gets to that code.
            if (!url.equals("about:blank")) {
                appView.loadUrl("javascript:try{ PhoneGap.onNativeReady.fire();}catch(e){_nativeReady = true;}");
            }

            // Make app visible after 2 sec in case there was a JS error and PhoneGap JS never initialized correctly
            if (appView.getVisibility() == View.INVISIBLE) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            ctx.runOnUiThread(new Runnable() {
                                public void run() {
                                    appView.setVisibility(View.VISIBLE);
                                    ctx.spinnerStop();
                                }
                            });
                        } catch (InterruptedException e) {
                        }
                    }
                });
                t.start();
            }


            // Shutdown if blank loaded
            if (url.equals("about:blank")) {
                if (this.ctx.callbackServer != null) {
                    this.ctx.callbackServer.destroy();
                }
                this.ctx.endActivity();
            }
        }
        
        /**
         * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable). 
         * The errorCode parameter corresponds to one of the ERROR_* constants.
         *
         * @param view          The WebView that is initiating the callback.
         * @param errorCode     The error code corresponding to an ERROR_* value.
         * @param description   A String describing the error.
         * @param failingUrl    The url that failed to load. 
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LOG.d(TAG, "DroidGap: GapViewClient.onReceivedError: Error code=%s Description=%s URL=%s", errorCode, description, failingUrl);

            // Clear timeout flag
            this.ctx.loadUrlTimeout++;

            // Stop "app loading" spinner if showing
            this.ctx.spinnerStop();

            // Handle error
            this.ctx.onReceivedError(errorCode, description, failingUrl);
        }
        
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            
            final String packageName = this.ctx.getPackageName();
            final PackageManager pm = this.ctx.getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    // debug = true
                    handler.proceed();
                    return;
                } else {
                    // debug = false
                    super.onReceivedSslError(view, handler, error);    
                }
            } catch (NameNotFoundException e) {
                // When it doubt, lock it out!
                super.onReceivedSslError(view, handler, error);
            }
        }
    }
    
    /**
     * End this activity by calling finish for activity
     */
    public void endActivity() {
        this.activityState = ACTIVITY_EXITING;
        this.finish();
    }
    
    /**
     * Called when a key is pressed.
     * 
     * @param keyCode
     * @param event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.appView == null) {
            return super.onKeyDown(keyCode, event);
        }

        // If back key
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // If back key is bound, then send event to JavaScript
            if (this.bound) {
                this.appView.loadUrl("javascript:PhoneGap.fireDocumentEvent('backbutton');");
                return true;
            }

            // If not bound
            else {

                // Go to previous page in webview if it is possible to go back
                if (this.backHistory()) {
                    return true;
                }

                // If not, then invoke behavior of super class
                else {
                    this.activityState = ACTIVITY_EXITING;
                    return super.onKeyDown(keyCode, event);
                }
            }
        }

        // If menu key
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.appView.loadUrl("javascript:PhoneGap.fireDocumentEvent('menubutton');");
            return super.onKeyDown(keyCode, event);
        }

        // If search key
        else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            this.appView.loadUrl("javascript:PhoneGap.fireDocumentEvent('searchbutton');");
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
    @Override
    public void startActivityForResult(Intent intent, int requestCode) throws RuntimeException {
        LOG.d(TAG, "DroidGap.startActivityForResult(intent,%d)", requestCode);
        super.startActivityForResult(intent, requestCode);
    }

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

     @Override
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

         // If errorUrl specified, then load it
         final String errorUrl = me.getStringProperty("errorUrl", null);
         if ((errorUrl != null) && (errorUrl.startsWith("file://") || errorUrl.indexOf(me.baseUrl) == 0 || isUrlWhiteListed(errorUrl)) && (!failingUrl.equals(errorUrl))) {

             // Load URL on UI thread
             me.runOnUiThread(new Runnable() {
                 public void run() {
                     me.showWebPage(errorUrl, false, true, null); 
                 }
             });
         }

         // If not, then display error dialog
         else {
             me.runOnUiThread(new Runnable() {
                 public void run() {
                     me.appView.setVisibility(View.GONE);
                     me.displayError("Application Error", description + " ("+failingUrl+")", "OK", true);
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
      * We are providing this class to detect when the soft keyboard is shown 
      * and hidden in the web view.
      */
     class LinearLayoutSoftKeyboardDetect extends LinearLayout {

            private static final String TAG = "SoftKeyboardDetect";
            
            private int oldHeight = 0;  // Need to save the old height as not to send redundant events
            private int oldWidth = 0; // Need to save old width for orientation change          
            private int screenWidth = 0;
            private int screenHeight = 0;
                        
            public LinearLayoutSoftKeyboardDetect(Context context, int width, int height) {
                super(context);     
                screenWidth = width;
                screenHeight = height;                  
            }

            @Override
            /**
             * Start listening to new measurement events.  Fire events when the height 
             * gets smaller fire a show keyboard event and when height gets bigger fire 
             * a hide keyboard event.
             * 
             * Note: We are using callbackServer.sendJavascript() instead of 
             * this.appView.loadUrl() as changing the URL of the app would cause the 
             * soft keyboard to go away.
             * 
             * @param widthMeasureSpec
             * @param heightMeasureSpec
             */
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);       
                
                LOG.v(TAG, "We are in our onMeasure method");

                // Get the current height of the visible part of the screen.
                // This height will not included the status bar.
                int height = MeasureSpec.getSize(heightMeasureSpec);
                int width = MeasureSpec.getSize(widthMeasureSpec);

                LOG.v(TAG, "Old Height = %d", oldHeight);
                LOG.v(TAG, "Height = %d", height);             
                LOG.v(TAG, "Old Width = %d", oldWidth);
                LOG.v(TAG, "Width = %d", width);

                // If the oldHeight = 0 then this is the first measure event as the app starts up.
                // If oldHeight == height then we got a measurement change that doesn't affect us.
                if (oldHeight == 0 || oldHeight == height) {
                    LOG.d(TAG, "Ignore this event");
                }
                // Account for orientation change and ignore this event/Fire orientation change
                else if(screenHeight == width)
                {
                    int tmp_var = screenHeight;
                    screenHeight = screenWidth;
                    screenWidth = tmp_var;
                    LOG.v(TAG, "Orientation Change");
                }
                // If the height as gotten bigger then we will assume the soft keyboard has 
                // gone away.
                else if (height > oldHeight) {
                    if (callbackServer != null) {
                        LOG.v(TAG, "Throw hide keyboard event");
                        callbackServer.sendJavascript("PhoneGap.fireDocumentEvent('hidekeyboard');");
                    }
                } 
                // If the height as gotten smaller then we will assume the soft keyboard has 
                // been displayed.
                else if (height < oldHeight) {
                    if (callbackServer != null) {
                        LOG.v(TAG, "Throw show keyboard event");
                        callbackServer.sendJavascript("PhoneGap.fireDocumentEvent('showkeyboard');");
                    }
                }

                // Update the old height for the next event
                oldHeight = height;
                oldWidth = width;
            }
    }
     
    /**
     * Load PhoneGap configuration from res/xml/phonegap.xml.
     * Approved list of URLs that can be loaded into DroidGap
     * 		<access origin="http://server regexp" subdomains="true" />
     * Log level: ERROR, WARN, INFO, DEBUG, VERBOSE (default=ERROR)
     *      <log level="DEBUG" />
     */
    private void loadConfiguration() {
        int id = getResources().getIdentifier("phonegap", "xml", getPackageName());
        if (id == 0) {
            LOG.i("PhoneGapLog", "phonegap.xml missing. Ignoring...");
            return;
        }
        XmlResourceParser xml = getResources().getXml(id);
        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals("access")) {
                    String origin = xml.getAttributeValue(null, "origin");
                    String subdomains = xml.getAttributeValue(null, "subdomains");
                    if (origin != null) {
                        this.addWhiteListEntry(origin, (subdomains != null) && (subdomains.compareToIgnoreCase("true") == 0));
                    }
                }
                else if (strNode.equals("log")) {
                    String level = xml.getAttributeValue(null, "level");
                    LOG.i("PhoneGapLog", "Found log level %s", level);
                    if (level != null) {
                        LOG.setLogLevel(level);
                    }
                }
            }
            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add entry to approved list of URLs (whitelist)
     * 
     * @param origin        URL regular expression to allow
     * @param subdomains    T=include all subdomains under origin
     */
    private void addWhiteListEntry(String origin, boolean subdomains) {
      try {
        // Unlimited access to network resources
        if(origin.compareTo("*") == 0) {
            LOG.d(TAG, "Unlimited access to network resources");
            whiteList.add(Pattern.compile("*"));
        } else { // specific access
          // check if subdomains should be included
          // TODO: we should not add more domains if * has already been added
          if (subdomains) {
              // XXX making it stupid friendly for people who forget to include protocol/SSL
              if(origin.startsWith("http")) {
                whiteList.add(Pattern.compile(origin.replaceFirst("https{0,1}://", "^https{0,1}://.*")));
              } else {
                whiteList.add(Pattern.compile("^https{0,1}://.*"+origin));
              }
              LOG.d(TAG, "Origin to allow with subdomains: %s", origin);
          } else {
              // XXX making it stupid friendly for people who forget to include protocol/SSL
              if(origin.startsWith("http")) {
                whiteList.add(Pattern.compile(origin.replaceFirst("https{0,1}://", "^https{0,1}://")));
              } else {
                whiteList.add(Pattern.compile("^https{0,1}://"+origin));
              }
              LOG.d(TAG, "Origin to allow: %s", origin);
          }    
        }
      } catch(Exception e) {
        LOG.d(TAG, "Failed to add origin %s", origin);
      }
    }

    /**
     * Determine if URL is in approved list of URLs to load.
     * 
     * @param url
     * @return
     */
    private boolean isUrlWhiteListed(String url) {

        // Check to see if we have matched url previously
        if (whiteListCache.get(url) != null) {
            return true;
        }

        // Look for match in white list
        Iterator<Pattern> pit = whiteList.iterator();
        while (pit.hasNext()) {
            Pattern p = pit.next();
            Matcher m = p.matcher(url);

            // If match found, then cache it to speed up subsequent comparisons
            if (m.find()) {
                whiteListCache.put(url, true);
                return true;
            }
        }
        return false;
    }

    /* 
     * Hook in DroidGap for menu plugins
     * 
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        this.postMessage("onCreateOptionsMenu", menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        this.postMessage("onPrepareOptionsMenu", menu);
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        this.postMessage("onOptionsItemSelected", item);
        return true;
    }
}
