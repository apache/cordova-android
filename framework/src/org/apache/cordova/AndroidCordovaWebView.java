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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/*
 * This class is our web view.
 *
 * @see <a href="http://developer.android.com/guide/webapps/webview.html">WebView guide</a>
 * @see <a href="http://developer.android.com/reference/android/webkit/WebView.html">WebView</a>
 */
public class AndroidCordovaWebView extends CordovaWebView {

    public static final String TAG = "CordovaWebView";
    public static final String CORDOVA_VERSION = "3.6.0-dev";

    private ArrayList<Integer> keyDownCodes = new ArrayList<Integer>();
    private ArrayList<Integer> keyUpCodes = new ArrayList<Integer>();

    private boolean paused;

    private BroadcastReceiver receiver;

    private AndroidWebView webview;

    /** Activities and other important classes **/
    private CordovaInterface cordova;
    private CordovaWebViewClient viewClient;
    private CordovaChromeClient chromeClient;

    private String url;

    // Flag to track that a loadUrl timeout occurred
    int loadUrlTimeout = 0;

    private boolean bound;

    private boolean handleButton = false;
    
    ExposedJsApi exposedJsApi;

    /** custom view created by the browser (a video player for example) */
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private ActivityResult mResult = null;

    private CordovaResourceApi resourceApi;

    class ActivityResult {
        
        int request;
        int result;
        Intent incoming;
        
        public ActivityResult(int req, int res, Intent intent) {
            request = req;
            result = res;
            incoming = intent;
        }

        
    }
    
    static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
            new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER);
    
    
    /**
     * Constructor.
     *
     * @param context
     */
    public AndroidCordovaWebView(Context context) {
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
        webview = new AndroidWebView(context);
        webview.setCordovaWebView(this);
        this.loadConfiguration();
        this.setup();
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     */
    public AndroidCordovaWebView(Context context, AttributeSet attrs) {
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
    	webview = new AndroidWebView(context, attrs);
        webview.setCordovaWebView(this);
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
    public AndroidCordovaWebView(Context context, AttributeSet attrs, int defStyle) {
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
    	webview = new AndroidWebView(context, attrs, defStyle);
        webview.setCordovaWebView(this);
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
    public AndroidCordovaWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        if (CordovaInterface.class.isInstance(context))
        {
            this.cordova = (CordovaInterface) context;
        }
        else
        {
            Log.d(TAG, "Your activity must implement CordovaInterface to work");
        }
    	webview = new AndroidWebView(context, attrs, defStyle, privateBrowsing);
        webview.setCordovaWebView(this);
        this.loadConfiguration();
        this.setup();
    }

    /**
     * Create a default WebViewClient object for this webview. This can be overridden by the
     * main application's CordovaActivity subclass.
     *
     * There are two possible client objects that can be returned:
     *   AndroidWebViewClient for android < 3.0
     *   IceCreamCordovaWebViewClient for Android >= 3.0 (Supports shouldInterceptRequest)
     */
    @Override
    public CordovaWebViewClient makeWebViewClient() {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            return (CordovaWebViewClient) new AndroidWebViewClient(this.cordova, this);
        }
        else
        {
            return (CordovaWebViewClient) new IceCreamCordovaWebViewClient(this.cordova, this);
        }
    }

    /**
     * Create a default WebViewClient object for this webview. This can be overridden by the
     * main application's CordovaActivity subclass.
     */
    @Override
    public CordovaChromeClient makeWebChromeClient() {
        return (CordovaChromeClient) new AndroidChromeClient(this.cordova);
    }

    /**
     * Initialize webview.
     */
    private void setup() {
        pluginManager = new PluginManager(this, this.cordova);
        jsMessageQueue = new NativeToJsMessageQueue(this, cordova);
        exposedJsApi = new AndroidExposedJsApi(pluginManager, jsMessageQueue);
        resourceApi = new CordovaResourceApi(this.getContext(), pluginManager);
        exposeJsInterface();
    }

    private void exposeJsInterface() {
        int SDK_INT = Build.VERSION.SDK_INT;
        if ((SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            Log.i(TAG, "Disabled addJavascriptInterface() bridge since Android version is old.");
            // Bug being that Java Strings do not get converted to JS strings automatically.
            // This isn't hard to work-around on the JS side, but it's easier to just
            // use the prompt bridge instead.
            return;            
        } 
        webview.addJavascriptInterface(exposedJsApi, "_cordovaNative");
    }

    /**
     * Set the WebViewClient.
     *
     * @param client
     */
    @Override
    public void setWebViewClient(CordovaWebViewClient client) {
        this.viewClient = client;
        webview.setWebViewClient((WebViewClient) client);
    }
    
//    @Override
    public CordovaWebViewClient getWebViewClient() {
        return this.viewClient;
    }

    /**
     * Set the WebChromeClient.
     *
     * @param client
     */
    @Override
    public void setWebChromeClient(CordovaChromeClient client) {
        this.chromeClient = client;
        webview.setWebChromeClient((WebChromeClient) client);
    }
    
    @Override
    public CordovaChromeClient getWebChromeClient() {
        return this.chromeClient;
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

            String initUrl = getProperty("url", null);

            // If first page of app, then set URL to load to be the one passed in
            if (initUrl == null) {
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
    @Override
    public void loadUrl(final String url, int time) {
        String initUrl = getProperty("url", null);

        // If first page of app, then set URL to load to be the one passed in
        if (initUrl == null) {
            this.loadUrlIntoView(url, time);
        }
        // Otherwise use the URL specified in the activity's extras bundle
        else {
            this.loadUrlIntoView(initUrl);
        }
    }

    @Override
    public void loadUrlIntoView(final String url) {
        loadUrlIntoView(url, true);
    }

    /**
     * Load the url into the webview.
     *
     * @param url
     */
    @Override
    public void loadUrlIntoView(final String url, boolean recreatePlugins) {
        if (recreatePlugins) {
            this.url = url;
            this.pluginManager.init();
        }
        webview.loadUrlIntoView(url);
    }

    /**
     * Load URL in webview.
     *
     * @param url
     */
    @Override
    public void loadUrlNow(String url) {
    	webview.loadUrlNow(url);
    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     *
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    @Override
    public void loadUrlIntoView(final String url, final int time) {

        // If not first page of app, then load immediately
        // Add support for browser history if we use it.
        if ((url.startsWith("javascript:")) || this.canGoBack()) {
        }

        // If first page, then show splashscreen
        else {

            LOG.d(TAG, "loadUrlIntoView(%s, %d)", url, time);

            // Send message to show splashscreen now if desired
            postMessage("splashscreen", "show");
        }

        // Load url
        this.loadUrlIntoView(url);    	
    }
    
    @Override
    public void stopLoading() {
        //viewClient.isCurrentlyLoading = false;
        webview.stopLoading();
    }
    
    /**
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     *
     * @param statement
     */
    @Override
    public void sendJavascript(String statement) {
        this.jsMessageQueue.addJavaScript(statement);
    }

    /**
     * Send a plugin result back to JavaScript.
     * (This is a convenience method)
     *
     * @param result
     * @param callbackId
     */
    @Override
    public void sendPluginResult(PluginResult result, String callbackId) {
        this.jsMessageQueue.addPluginResult(result, callbackId);
    }

    /**
     * Send a message to all plugins.
     *
     * @param id            The message id
     * @param data          The message data
     */
    @Override
    public void postMessage(String id, Object data) {
        if (this.pluginManager != null) {
            this.pluginManager.postMessage(id, data);
        }
    }


    /**
     * Go to previous page in history.  (We manage our own history)
     *
     * @return true if we went back, false if we are already at top
     */
    @Override
    public boolean backHistory() {
    	return webview.backHistory();
    }


    /**
     * Load the specified URL in the Cordova webview or a new browser instance.
     *
     * NOTE: If openExternal is false, only URLs listed in whitelist can be loaded.
     *
     * @param url           The url to load.
     * @param openExternal  Load url in browser instead of Cordova webview.
     * @param clearHistory  Clear the history stack, so new page becomes top of history
     * @param params        Parameters for new app
     */
    @Override
    public void showWebPage(String url, boolean openExternal, boolean clearHistory, HashMap<String, Object> params) {
        LOG.d(TAG, "showWebPage(%s, %b, %b, HashMap", url, openExternal, clearHistory);

        // If clearing history
        if (clearHistory) {
            this.clearHistory();
        }

        // If loading into our webview
        if (!openExternal) {

            // Make sure url is in whitelist
            if (url.startsWith("file://") || Config.isUrlWhiteListed(url)) {
                // TODO: What about params?
                // Load new URL
                this.loadUrl(url);
                return;
            }
            // Load in default viewer if not
            LOG.w(TAG, "showWebPage: Cannot load URL into webview since it is not in white list.  Loading into browser instead. (URL=" + url + ")");
        }
        try {
            // Omitting the MIME type for file: URLs causes "No Activity found to handle Intent".
            // Adding the MIME type to http: URLs causes them to not be handled by the downloader.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            if ("file".equals(uri.getScheme())) {
                intent.setDataAndType(uri, resourceApi.getMimeType(uri));
            } else {
                intent.setData(uri);
            }
            cordova.getActivity().startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            LOG.e(TAG, "Error loading url " + url, e);
        }
    }

    /**
     * Check configuration parameters from Config.
     * Approved list of URLs that can be loaded into Cordova
     *      <access origin="http://server regexp" subdomains="true" />
     * Log level: ERROR, WARN, INFO, DEBUG, VERBOSE (default=ERROR)
     *      <log level="DEBUG" />
     */
    private void loadConfiguration() {
 
        if ("true".equals(this.getProperty("Fullscreen", "false"))) {
            this.cordova.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            this.cordova.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * Get string property for activity.
     *
     * @param name
     * @param defaultValue
     * @return the String value for the named property
     */
    public String getProperty(String name, String defaultValue) {
        Bundle bundle = this.cordova.getActivity().getIntent().getExtras();
        if (bundle == null) {
            return defaultValue;
        }
        name = name.toLowerCase(Locale.getDefault());
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	return webview.onKeyDown(keyCode, event);
    }
    

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	return webview.onKeyUp(keyCode, event);
    }

    @Override
    public void bindButton(boolean override)
    {
        this.bound = override;
    }

    @Override
    public void bindButton(String button, boolean override) {
        if (button.compareTo("volumeup")==0) {
          keyDownCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
        }
        else if (button.compareTo("volumedown")==0) {
          keyDownCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        }
      }

    private void bindButton(int keyCode, boolean keyDown, boolean override) {
       if(keyDown)
       {
           keyDownCodes.add(keyCode);
       }
       else
       {
           keyUpCodes.add(keyCode);
       }
    }

    @Override
    public boolean isBackButtonBound()
    {
        return this.bound;
    }
    
    @Override
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
            webview.pauseTimers();
        }
        paused = true;
   
    }

    @Override
    public void handleResume(boolean keepRunning, boolean activityResultKeepRunning)
    {
        this.loadUrl("javascript:try{cordova.fireDocumentEvent('resume');}catch(e){console.log('exception firing resume event from native');};");
        
        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onResume(keepRunning);
        }

        // Resume JavaScript timers (including setInterval)
        webview.resumeTimers();
        paused = false;
    }

    @Override
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
        
        // unregister the receiver
        if (this.receiver != null) {
            try {
                this.cordova.getActivity().unregisterReceiver(this.receiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering configuration receiver: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        //Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onNewIntent(intent);
        }
    }

    @Override
    public boolean isPaused()
    {
        return paused;
    }

    /* CB-1146 */
    public boolean hadKeyEvent() {
        return handleButton;
    }
    
    @Override
    public void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // This code is adapted from the original Android Browser code, licensed under the Apache License, Version 2.0
        Log.d(TAG, "showing Custom View");
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
        
        // Store the view and its callback for later (to kill it properly)
        mCustomView = view;
        mCustomViewCallback = callback;
        
        // Add the custom view to its container.
        ViewGroup parent = (ViewGroup) this.getParent();
        parent.addView(view, COVER_SCREEN_GRAVITY_CENTER);
        
        // Hide the content view.
        this.setVisibility(View.GONE);
        
        // Finally show the custom view container.
        parent.setVisibility(View.VISIBLE);
        parent.bringToFront();
    }

    @Override
    public void hideCustomView() {
        // This code is adapted from the original Android Browser code, licensed under the Apache License, Version 2.0
        Log.d(TAG, "Hiding Custom View");
        if (mCustomView == null) return;

        // Hide the custom view.
        mCustomView.setVisibility(View.GONE);
        
        // Remove the custom view from its container.
        ViewGroup parent = (ViewGroup) this.getParent();
        parent.removeView(mCustomView);
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        
        // Show the content view.
        this.setVisibility(View.VISIBLE);
    }
    
    /**
     * if the video overlay is showing then we need to know 
     * as it effects back button handling
     * 
     * @return true if custom view is showing
     */
    @Override
    public boolean isCustomViewShowing() {
        return mCustomView != null;
    }

    public void storeResult(int requestCode, int resultCode, Intent intent) {
        mResult = new ActivityResult(requestCode, resultCode, intent);
    }
    
    /* git:5ca233779d11177ec2bef97afa2910d383d6d4a2 */
    public CordovaResourceApi getResourceApi() {
        return resourceApi;
    }

    @Override
    public void setLayoutParams(
            android.widget.LinearLayout.LayoutParams layoutParams) {
        webview.setLayoutParams(layoutParams);
    }

    @SuppressLint("NewApi")
	@Override
    public void setOverScrollMode(int mode) {
        webview.setOverScrollMode(mode);
    }

    @Override
    public void addJavascript(String statement) {
        this.jsMessageQueue.addJavaScript(statement);
    }

    @Override
    public CordovaPlugin getPlugin(String initCallbackClass) {
        return this.pluginManager.getPlugin(initCallbackClass);
    }

    @Override
    public String exec(String service, String action, String callbackId,
            String message) throws JSONException {
        return this.exposedJsApi.exec(service, action, callbackId, message);
    }

    @Override
    public void setNativeToJsBridgeMode(int parseInt) {
        this.exposedJsApi.setNativeToJsBridgeMode(parseInt);
    }

    @Override
    public String retrieveJsMessages(boolean equals) {
        return this.exposedJsApi.retrieveJsMessages(equals);
    }

    @Override
    public boolean onOverrideUrlLoading(String url) {
        return this.pluginManager.onOverrideUrlLoading(url);
    }

    @Override
    public void resetJsMessageQueue() {
        this.jsMessageQueue.reset();
    }

    @Override
    public void onReset() {
        this.pluginManager.onReset();
    }

    @Override
    public void incUrlTimeout() {
        this.loadUrlTimeout++;
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public void setLayoutParams(
            android.widget.FrameLayout.LayoutParams layoutParams) {
        webview.setLayoutParams(layoutParams);
    }

    @Override
    public View getView() {
        return webview;
    }

	@Override
	public void setId(int i) {
		webview.setId(i);
	}

	@Override
	public int getVisibility() {
		return webview.getVisibility();
	}

	@Override
	public void setVisibility(int invisible) {
		webview.setVisibility(invisible);
	}

	@Override
	public Object getParent() {
		return webview.getParent();
	}

	@Override
	public boolean canGoBack() {
		return webview.canGoBack();
	}

	@Override
	public void clearCache(boolean b) {
		webview.clearCache(b);
	}

	@Override
	public void clearHistory() {
		webview.clearHistory();
	}

	@Override
	public Object getFocusedChild() {
		return webview.getFocusedChild();
	}

	@Override
	public Context getContext() {
		return webview.getContext();
	}

	@Override
	public void setNetworkAvailable(boolean online) {
		webview.setNetworkAvailable(online);
	}

	@Override
	public String getUrl() {
		return webview.getUrl();
	}


}
