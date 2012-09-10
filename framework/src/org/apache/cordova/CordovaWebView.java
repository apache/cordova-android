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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.LOG;
import org.apache.cordova.api.PluginManager;
import org.apache.cordova.api.PluginResult;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;

public class CordovaWebView extends WebView {

    public static final String TAG = "CordovaWebView";

    /** The whitelist **/
    private ArrayList<Pattern> whiteList = new ArrayList<Pattern>();
    private HashMap<String, Boolean> whiteListCache = new HashMap<String, Boolean>();
    private ArrayList<Integer> keyDownCodes = new ArrayList<Integer>();
    private ArrayList<Integer> keyUpCodes = new ArrayList<Integer>();

    public PluginManager pluginManager;
    public CallbackServer callbackServer;
    private boolean paused;


    /** Activities and other important classes **/
    private CordovaInterface cordova;
    CordovaWebViewClient viewClient;
    @SuppressWarnings("unused")
    private CordovaChromeClient chromeClient;

    //This is for the polyfil history
    private String url;
    String baseUrl;
    private Stack<String> urls = new Stack<String>();

    boolean useBrowserHistory = false;

    // Flag to track that a loadUrl timeout occurred
    int loadUrlTimeout = 0;

    private boolean bound;

    private boolean volumedownBound;

    private boolean volumeupBound;

    private boolean handleButton = false;

	NativeToJsMessageQueue jsMessageQueue;

    /**
     * Constructor.
     *
     * @param context
     */
    public CordovaWebView(Context context) {
        super(context);
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
        this.loadConfiguration();
        this.setup();
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     */
    public CordovaWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
        this.setWebChromeClient(new CordovaChromeClient(this.cordova, this));
        this.initWebViewClient(this.cordova);
        this.loadConfiguration();
        this.setup();
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     * @param defStyle
     *
     */
    public CordovaWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
        this.setWebChromeClient(new CordovaChromeClient(this.cordova, this));
        this.initWebViewClient(this.cordova);
        this.loadConfiguration();
        this.setup();
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     * @param defStyle
     * @param privateBrowsing
     */
	@TargetApi(11)
	public CordovaWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
        this.setWebChromeClient(new CordovaChromeClient(this.cordova));
        this.initWebViewClient(this.cordova);
        this.loadConfiguration();
        this.setup();
    }


    private void initWebViewClient(CordovaInterface cordova) {
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            this.setWebViewClient(new CordovaWebViewClient(this.cordova, this));
        }
        else
        {
            this.setWebViewClient(new IceCreamCordovaWebViewClient(this.cordova, this));
        }
    }

    /**
     * Initialize webview.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setup() {
    	jsMessageQueue = new NativeToJsMessageQueue(this, cordova);
    	
        this.setInitialScale(0);
        this.setVerticalScrollBarEnabled(false);
        this.requestFocusFromTouch();

        // Enable JavaScript
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

        //Set the nav dump for HTC 2.x devices (disabling for ICS/Jellybean)
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
            settings.setNavDump(true);
        
        // Jellybean rightfully tried to lock this down. Too bad they didn't give us a whitelist
        // while we do this
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        	Level16Apis.enableUniversalAccess(settings);
        // Enable database
        settings.setDatabaseEnabled(true);
        String databasePath = this.cordova.getActivity().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);

        //Start up the plugin manager
        try {
            this.pluginManager = new PluginManager(this, this.cordova);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        exposeJsInterface();
    }

    private void exposeJsInterface() {
        // addJavascriptInterface crashes on the 2.3 emulator.
        if (Build.VERSION.RELEASE.startsWith("2.3") && Build.MANUFACTURER.equals("unknown")) {
            Log.i(TAG, "Disabled addJavascriptInterface() bridge callback due to a bug on the 2.3 emulator");
            return;
        }
        this.addJavascriptInterface(new Object() {
            @SuppressWarnings("unused")
            public String exec(String service, String action, String callbackId, String arguments) throws JSONException {
                PluginResult r = pluginManager.exec(service, action, callbackId, arguments, true /* async */);
                return r == null ? "" : r.getJSONString();
            }
        }, "_cordovaExec");
    }

    /**
     * Set the WebViewClient.
     *
     * @param client
     */
    public void setWebViewClient(CordovaWebViewClient client) {
        this.viewClient = client;
        super.setWebViewClient(client);
    }

    /**
     * Set the WebChromeClient.
     *
     * @param client
     */
    public void setWebChromeClient(CordovaChromeClient client) {
        this.chromeClient = client;
        super.setWebChromeClient(client);
    }

    /**
     * Add entry to approved list of URLs (whitelist)
     *
     * @param origin        URL regular expression to allow
     * @param subdomains    T=include all subdomains under origin
     */
    public void addWhiteListEntry(String origin, boolean subdomains) {
        try {
            // Unlimited access to network resources
            if (origin.compareTo("*") == 0) {
                LOG.d(TAG, "Unlimited access to network resources");
                this.whiteList.add(Pattern.compile(".*"));
            } else { // specific access
                // check if subdomains should be included
                // TODO: we should not add more domains if * has already been added
                if (subdomains) {
                    // XXX making it stupid friendly for people who forget to include protocol/SSL
                    if (origin.startsWith("http")) {
                        this.whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://(.*\\.)?")));
                    } else {
                        this.whiteList.add(Pattern.compile("^https?://(.*\\.)?" + origin));
                    }
                    LOG.d(TAG, "Origin to allow with subdomains: %s", origin);
                } else {
                    // XXX making it stupid friendly for people who forget to include protocol/SSL
                    if (origin.startsWith("http")) {
                        this.whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://")));
                    } else {
                        this.whiteList.add(Pattern.compile("^https?://" + origin));
                    }
                    LOG.d(TAG, "Origin to allow: %s", origin);
                }
            }
        } catch (Exception e) {
            LOG.d(TAG, "Failed to add origin %s", origin);
        }
    }

    /**
     * Determine if URL is in approved list of URLs to load.
     *
     * @param url
     * @return
     */
    public boolean isUrlWhiteListed(String url) {

        // Check to see if we have matched url previously
        if (this.whiteListCache.get(url) != null) {
            return true;
        }

        // Look for match in white list
        Iterator<Pattern> pit = this.whiteList.iterator();
        while (pit.hasNext()) {
            Pattern p = pit.next();
            Matcher m = p.matcher(url);

            // If match found, then cache it to speed up subsequent comparisons
            if (m.find()) {
                this.whiteListCache.put(url, true);
                return true;
            }
        }
        return false;
    }

    /**
     * Load the url into the webview.
     *
     * @param url
     */
    @Override
    public void loadUrl(String url) {
        if (url.equals("about:blank") || url.startsWith("javascript:")) {
            this.loadUrlNow(url);
        }
        else {

            String initUrl = this.getProperty("url", null);

            // If first page of app, then set URL to load to be the one passed in
            if (initUrl == null || (this.urls.size() > 0)) {
                this.loadUrlIntoView(url);
            }
            // Otherwise use the URL specified in the activity's extras bundle
            else {
                this.loadUrlIntoView(initUrl);
            }
        }
    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     *
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    public void loadUrl(final String url, int time) {
        String initUrl = this.getProperty("url", null);

        // If first page of app, then set URL to load to be the one passed in
        if (initUrl == null || (this.urls.size() > 0)) {
            this.loadUrlIntoView(url, time);
        }
        // Otherwise use the URL specified in the activity's extras bundle
        else {
            this.loadUrlIntoView(initUrl);
        }
    }

    /**
     * Load the url into the webview.
     *
     * @param url
     */
    public void loadUrlIntoView(final String url) {
        LOG.d(TAG, ">>> loadUrl(" + url + ")");

        this.url = url;
        if (this.baseUrl == null) {
            int i = url.lastIndexOf('/');
            if (i > 0) {
                this.baseUrl = url.substring(0, i + 1);
            }
            else {
                this.baseUrl = this.url + "/";
            }

            this.pluginManager.init();

            if (!this.useBrowserHistory) {
                this.urls.push(url);
            }
        }

        // Create a timeout timer for loadUrl
        final CordovaWebView me = this;
        final int currentLoadUrlTimeout = me.loadUrlTimeout;
        final int loadUrlTimeoutValue = Integer.parseInt(this.getProperty("loadUrlTimeoutValue", "20000"));

        // Timeout error method
        final Runnable loadError = new Runnable() {
            public void run() {
                me.stopLoading();
                LOG.e(TAG, "CordovaWebView: TIMEOUT ERROR!");
                if (viewClient != null) {
                    viewClient.onReceivedError(me, -6, "The connection to the server was unsuccessful.", url);
                }
            }
        };

        // Timeout timer method
        final Runnable timeoutCheck = new Runnable() {
            public void run() {
                try {
                    synchronized (this) {
                        wait(loadUrlTimeoutValue);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // If timeout, then stop loading and handle error
                if (me.loadUrlTimeout == currentLoadUrlTimeout) {
                    me.cordova.getActivity().runOnUiThread(loadError);
                }
            }
        };

        // Load url
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Thread thread = new Thread(timeoutCheck);
                thread.start();
                me.loadUrlNow(url);
            }
        });
    }

    /**
     * Load URL in webview.
     *
     * @param url
     */
    void loadUrlNow(String url) {
        LOG.d(TAG, ">>> loadUrlNow()");
        super.loadUrl(url);
    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     *
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    public void loadUrlIntoView(final String url, final int time) {

        // If not first page of app, then load immediately
        // Add support for browser history if we use it.
        if ((url.startsWith("javascript:")) || this.urls.size() > 0 || this.canGoBack()) {
        }

        // If first page, then show splashscreen
        else {

            LOG.d(TAG, "DroidGap.loadUrl(%s, %d)", url, time);

            // Send message to show splashscreen now if desired
            this.postMessage("splashscreen", "show");
        }

        // Load url
        this.loadUrlIntoView(url);
    }
    
    /**
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     *
     * @param message
     */
    public void sendJavascript(String statement) {
        this.jsMessageQueue.add(statement);
    }

    /**
     * Send a message to all plugins.
     *
     * @param id            The message id
     * @param data          The message data
     */
    public void postMessage(String id, Object data) {
        if (this.pluginManager != null) {
            this.pluginManager.postMessage(id, data);
        }
    }

    /**
     * Returns the top url on the stack without removing it from
     * the stack.
     */
    public String peekAtUrlStack() {
        if (this.urls.size() > 0) {
            return this.urls.peek();
        }
        return "";
    }

    /**
     * Add a url to the stack
     *
     * @param url
     */
    public void pushUrl(String url) {
        this.urls.push(url);
    }

    /**
     * Go to previous page in history.  (We manage our own history)
     *
     * @return true if we went back, false if we are already at top
     */
    public boolean backHistory() {

        // Check webview first to see if there is a history
        // This is needed to support curPage#diffLink, since they are added to appView's history, but not our history url array (JQMobile behavior)
        if (super.canGoBack()) {
            super.goBack();
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

    /**
     * Return true if there is a history item.
     *
     * @return
     */
    public boolean canGoBack() {
        if (super.canGoBack()) {
            return true;
        }
        if (this.urls.size() > 1) {
            return true;
        }
        return false;
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
                LOG.w(TAG, "showWebPage: Cannot load URL into webview since it is not in white list.  Loading into browser instead. (URL=" + url + ")");
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    cordova.getActivity().startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(TAG, "Error loading url " + url, e);
                }
            }
        }

        // Load in default view intent
        else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                cordova.getActivity().startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                LOG.e(TAG, "Error loading url " + url, e);
            }
        }
    }

    /**
     * Load Cordova configuration from res/xml/cordova.xml.
     * Approved list of URLs that can be loaded into DroidGap
     *      <access origin="http://server regexp" subdomains="true" />
     * Log level: ERROR, WARN, INFO, DEBUG, VERBOSE (default=ERROR)
     *      <log level="DEBUG" />
     */
    private void loadConfiguration() {
        int id = getResources().getIdentifier("config", "xml", this.cordova.getActivity().getPackageName());
        if(id == 0)
        {
            id = getResources().getIdentifier("cordova", "xml", this.cordova.getActivity().getPackageName());   
            Log.i("CordovaLog", "config.xml missing, reverting to cordova.xml");
        }
        if (id == 0) {
            LOG.i("CordovaLog", "cordova.xml missing. Ignoring...");
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
                    LOG.i("CordovaLog", "Found log level %s", level);
                    if (level != null) {
                        LOG.setLogLevel(level);
                    }
                }
                else if (strNode.equals("preference")) {
                    String name = xml.getAttributeValue(null, "name");
                    String value = xml.getAttributeValue(null, "value");

                    LOG.i("CordovaLog", "Found preference for %s=%s", name, value);
                    Log.d("CordovaLog", "Found preference for " + name + "=" + value);

                    // Save preferences in Intent
                    this.cordova.getActivity().getIntent().putExtra(name, value);
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

        // Init preferences
        if ("true".equals(this.getProperty("useBrowserHistory", "false"))) {
            this.useBrowserHistory = true;
        }
        else {
            this.useBrowserHistory = false;
        }

        if ("true".equals(this.getProperty("fullscreen", "false"))) {
            this.cordova.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            this.cordova.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Get string property for activity.
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public String getProperty(String name, String defaultValue) {
        Bundle bundle = this.cordova.getActivity().getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        Object p = bundle.get(name);
        if (p == null) {
            return defaultValue;
        }
        return p.toString();
    }

    /*
     * onKeyDown
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyDownCodes.contains(keyCode))
        {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    // only override default behavior is event bound
                    LOG.d(TAG, "Down Key Hit");
                    this.loadUrl("javascript:cordova.fireDocumentEvent('volumedownbutton');");
                    return true;
            }
            // If volumeup key
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    LOG.d(TAG, "Up Key Hit");
                    this.loadUrl("javascript:cordova.fireDocumentEvent('volumeupbutton');");
                    return true;
            }
            else
            {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        // If back key
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // If back key is bound, then send event to JavaScript
            if (this.bound) {
                this.loadUrl("javascript:cordova.fireDocumentEvent('backbutton');");
                return true;
            } else {
                // If not bound
                // Go to previous page in webview if it is possible to go back
                if (this.backHistory()) {
                    return true;
                }
                // If not, then invoke default behavior
                else {
                    //this.activityState = ACTIVITY_EXITING;
                    return false;
                }
            }
        }
        // Legacy
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            this.loadUrl("javascript:cordova.fireDocumentEvent('menubutton');");
            return super.onKeyUp(keyCode, event);
        }
        // If search key
        else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            this.loadUrl("javascript:cordova.fireDocumentEvent('searchbutton');");
            return true;
        }
        else if(keyUpCodes.contains(keyCode))
        {
            //What the hell should this do?
            return super.onKeyUp(keyCode, event);
        }

        //Does webkit change this behavior?
        return super.onKeyUp(keyCode, event);
    }

    
    public void bindButton(boolean override)
    {
        this.bound = override;
    }

    public void bindButton(String button, boolean override) {
        // TODO Auto-generated method stub
        if (button.compareTo("volumeup")==0) {
          keyDownCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
        }
        else if (button.compareTo("volumedown")==0) {
          keyDownCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        }
      }

    public void bindButton(int keyCode, boolean keyDown, boolean override) {
       if(keyDown)
       {
           keyDownCodes.add(keyCode);
       }
       else
       {
           keyUpCodes.add(keyCode);
       }
    }

    public boolean isBackButtonBound()
    {
        return this.bound;
    }
    
    public void handlePause(boolean keepRunning)
    {
        LOG.d(TAG, "Handle the pause");
        // Send pause event to JavaScript
        this.loadUrl("javascript:try{cordova.fireDocumentEvent('pause');}catch(e){console.log('exception firing pause event from native');};");

        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onPause(keepRunning);
        }

        // If app doesn't want to run in background
        if (!keepRunning) {
            // Pause JavaScript timers (including setInterval)
            this.pauseTimers();
        }
        paused = true;
   
    }
    
    public void handleResume(boolean keepRunning, boolean activityResultKeepRunning)
    {

        // Send resume event to JavaScript
        this.loadUrl("javascript:try{cordova.fireDocumentEvent('resume');}catch(e){console.log('exception firing resume event from native');};");

        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onResume(keepRunning);
        }

        // Resume JavaScript timers (including setInterval)
        this.resumeTimers();
        paused = false;
    }
    
    public void handleDestroy()
    {
        // Send destroy event to JavaScript
        this.loadUrl("javascript:try{cordova.require('cordova/channel').onDestroy.fire();}catch(e){console.log('exception firing destroy event from native');};");

        // Load blank page so that JavaScript onunload is called
        this.loadUrl("about:blank");

        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onDestroy();
        }
    }
    
    public void onNewIntent(Intent intent)
    {
        //Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onNewIntent(intent);
        }
    }
    
    public boolean isPaused()
    {
        return paused;
    }

    public boolean hadKeyEvent() {
        return handleButton;
    }

    // Wrapping these functions in their own class prevents warnings in adb like:
    // VFY: unable to resolve virtual method 285: Landroid/webkit/WebSettings;.setAllowUniversalAccessFromFileURLs
    @TargetApi(16)
    private static class Level16Apis {
        static void enableUniversalAccess(WebSettings settings) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
    }
}
