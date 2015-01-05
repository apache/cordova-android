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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;


/*
 * This class is our web view.
 *
 * @see <a href="http://developer.android.com/guide/webapps/webview.html">WebView guide</a>
 * @see <a href="http://developer.android.com/reference/android/webkit/WebView.html">WebView</a>
 */
public class AndroidWebView extends WebView implements CordovaWebView {

    public static final String TAG = "AndroidWebView";

    private HashSet<Integer> boundKeyCodes = new HashSet<Integer>();

    PluginManager pluginManager;

    private BroadcastReceiver receiver;


    /** Activities and other important classes **/
    private CordovaInterface cordova;
    AndroidWebViewClient viewClient;
    private AndroidChromeClient chromeClient;

    // Flag to track that a loadUrl timeout occurred
    int loadUrlTimeout = 0;

    private long lastMenuEventTime = 0;

    CordovaBridge bridge;

    /** custom view created by the browser (a video player for example) */
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private CordovaResourceApi resourceApi;
    private Whitelist internalWhitelist;
    private Whitelist externalWhitelist;
    private CordovaPreferences preferences;
    private CoreAndroid appPlugin;
    // The URL passed to loadUrl(), not necessarily the URL of the current page.
    String loadedUrl;
    
    static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
            new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER);
    
    /** Used when created via reflection. */
    public AndroidWebView(Context context) {
        this(context, null);
    }

    /** Required to allow view to be used within XML layouts. */
    public AndroidWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Use two-phase init so that the control will work with XML layouts.
    @Override
    public void init(CordovaInterface cordova, List<PluginEntry> pluginEntries,
            Whitelist internalWhitelist, Whitelist externalWhitelist,
            CordovaPreferences preferences) {
        if (this.cordova != null) {
            throw new IllegalStateException();
        }
        this.cordova = cordova;
        this.internalWhitelist = internalWhitelist;
        this.externalWhitelist = externalWhitelist;
        this.preferences = preferences;

        pluginManager = new PluginManager(this, this.cordova, pluginEntries);
        resourceApi = new CordovaResourceApi(this.getContext(), pluginManager);
        bridge = new CordovaBridge(pluginManager, new NativeToJsMessageQueue(this, cordova), this.cordova.getActivity().getPackageName());
        initWebViewSettings();
        pluginManager.addService(CoreAndroid.PLUGIN_NAME, "org.apache.cordova.CoreAndroid");
        pluginManager.init();
        
        if (this.viewClient == null) {
            setWebViewClient(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ?
                new AndroidWebViewClient(cordova, this) :
                new IceCreamCordovaWebViewClient(cordova, this));
        }
        if (this.chromeClient == null) {
            setWebChromeClient(new AndroidChromeClient(cordova, this));
        }

        exposeJsInterface();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    private void initWebViewSettings() {
        this.setInitialScale(0);
        this.setVerticalScrollBarEnabled(false);
        // TODO: The Activity is the one that should call requestFocus().
        if (shouldRequestFocusOnInit()) {
            this.requestFocusFromTouch();
        }
        this.setInitialScale(0);
        this.setVerticalScrollBarEnabled(false);
        if (shouldRequestFocusOnInit()) {
            this.requestFocusFromTouch();
        }
        // Enable JavaScript
        final WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        
        // Set the nav dump for HTC 2.x devices (disabling for ICS, deprecated entirely for Jellybean 4.2)
        try {
            Method gingerbread_getMethod =  WebSettings.class.getMethod("setNavDump", new Class[] { boolean.class });
            
            String manufacturer = android.os.Build.MANUFACTURER;
            Log.d(TAG, "CordovaWebView is running on device made by: " + manufacturer);
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB &&
                    android.os.Build.MANUFACTURER.contains("HTC"))
            {
                gingerbread_getMethod.invoke(settings, true);
            }
        } catch (NoSuchMethodException e) {
            Log.d(TAG, "We are on a modern version of Android, we will deprecate HTC 2.3 devices in 2.8");
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Doing the NavDump failed with bad arguments");
        } catch (IllegalAccessException e) {
            Log.d(TAG, "This should never happen: IllegalAccessException means this isn't Android anymore");
        } catch (InvocationTargetException e) {
            Log.d(TAG, "This should never happen: InvocationTargetException means this isn't Android anymore.");
        }

        //We don't save any form data in the application
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        
        // Jellybean rightfully tried to lock this down. Too bad they didn't give us a whitelist
        // while we do this
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Level16Apis.enableUniversalAccess(settings);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Level17Apis.setMediaPlaybackRequiresUserGesture(settings, false);
        }
        // Enable database
        // We keep this disabled because we use or shim to get around DOM_EXCEPTION_ERROR_16
        String databasePath = getContext().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);
        
        
        //Determine whether we're in debug or release mode, and turn on Debugging!
        ApplicationInfo appInfo = getContext().getApplicationContext().getApplicationInfo();
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0 &&
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            enableRemoteDebugging();
        }
        
        settings.setGeolocationDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);
        
        // Enable AppCache
        // Fix for CB-2282
        settings.setAppCacheMaxSize(5 * 1048576);
        settings.setAppCachePath(databasePath);
        settings.setAppCacheEnabled(true);
        
        // Fix for CB-1405
        // Google issue 4641
        settings.getUserAgentString();
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    settings.getUserAgentString();
                }
            };
            getContext().registerReceiver(this.receiver, intentFilter);
        }
        // end CB-1405
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void enableRemoteDebugging() {
        try {
            WebView.setWebContentsDebuggingEnabled(true);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "You have one job! To turn on Remote Web Debugging! YOU HAVE FAILED! ");
            e.printStackTrace();
        }
    }

	/**
	 * Override this method to decide whether or not you need to request the
	 * focus when your application start
	 * 
	 * @return true unless this method is overriden to return a different value
	 */
    protected boolean shouldRequestFocusOnInit() {
		return true;
	}

    private void exposeJsInterface() {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            Log.i(TAG, "Disabled addJavascriptInterface() bridge since Android version is old.");
            // Bug being that Java Strings do not get converted to JS strings automatically.
            // This isn't hard to work-around on the JS side, but it's easier to just
            // use the prompt bridge instead.
            return;            
        } 
        AndroidExposedJsApi exposedJsApi = new AndroidExposedJsApi(bridge);
        this.addJavascriptInterface(exposedJsApi, "_cordovaNative");
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        this.viewClient = (AndroidWebViewClient)client;
        super.setWebViewClient(client);
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        this.chromeClient = (AndroidChromeClient)client;
        super.setWebChromeClient(client);
    }

    /**
     * Load the url into the webview.
     */
    @Override
    public void loadUrl(String url) {
        this.loadUrlIntoView(url, true);
    }

    /**
     * Load the url into the webview.
     */
    @Override
    public void loadUrlIntoView(final String url, boolean recreatePlugins) {
        if (url.equals("about:blank") || url.startsWith("javascript:")) {
            this.loadUrlNow(url);
            return;
        }

        LOG.d(TAG, ">>> loadUrl(" + url + ")");
        recreatePlugins = recreatePlugins || (loadedUrl == null);

        if (recreatePlugins) {
            // Don't re-initialize on first load.
            if (loadedUrl != null) {
                this.pluginManager.init();
            }
            this.loadedUrl = url;
        }

        // Create a timeout timer for loadUrl
        final AndroidWebView me = this;
        final int currentLoadUrlTimeout = me.loadUrlTimeout;
        final int loadUrlTimeoutValue = preferences.getInteger("LoadUrlTimeoutValue", 20000);

        // Timeout error method
        final Runnable loadError = new Runnable() {
            public void run() {
                me.stopLoading();
                LOG.e(TAG, "CordovaWebView: TIMEOUT ERROR!");
                if (viewClient != null) {
                    viewClient.onReceivedError(AndroidWebView.this, -6, "The connection to the server was unsuccessful.", url);
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
                cordova.getThreadPool().execute(timeoutCheck);
                me.loadUrlNow(url);
            }
        });
    }

    /**
     * Load URL in webview.
     */
    private void loadUrlNow(String url) {
        if (LOG.isLoggable(LOG.DEBUG) && !url.startsWith("javascript:")) {
            LOG.d(TAG, ">>> loadUrlNow()");
        }
        if (url.startsWith("file://") || url.startsWith("javascript:") || internalWhitelist.isUrlWhiteListed(url)) {
            super.loadUrl(url);
        }
    }

    @Override
    public void stopLoading() {
        //viewClient.isCurrentlyLoading = false;
        super.stopLoading();
    }
    
    public void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
        //We should post a message that the scroll changed
        ScrollEvent myEvent = new ScrollEvent(l, t, oldl, oldt, this);
        pluginManager.postMessage("onScrollChanged", myEvent);
    }
    
    /**
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     */
    public void sendJavascript(String statement) {
        bridge.getMessageQueue().addJavaScript(statement);
    }

    /**
     * Send a plugin result back to JavaScript.
     */
    public void sendPluginResult(PluginResult result, String callbackId) {
        bridge.getMessageQueue().addPluginResult(result, callbackId);
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
            printBackForwardList();
            super.goBack();
            
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
     * @param params        Parameters for new app
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
            if (url.startsWith("file://") || internalWhitelist.isUrlWhiteListed(url)) {
                // TODO: What about params?
                // Load new URL
                loadUrlIntoView(url, true);
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

    /*
     * onKeyDown
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(boundKeyCodes.contains(keyCode))
        {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    sendJavascriptEvent("volumedownbutton");
                    return true;
            }
            else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    sendJavascriptEvent("volumeupbutton");
                    return true;
            }
            else
            {
                return super.onKeyDown(keyCode, event);
            }
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            return !(this.startOfHistory()) || isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK);

        }
        else if(keyCode == KeyEvent.KEYCODE_MENU)
        {
            //How did we get here?  Is there a childView?
            View childView = this.getFocusedChild();
            if(childView != null)
            {
                //Make sure we close the keyboard if it's present
                InputMethodManager imm = (InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(childView.getWindowToken(), 0);
                cordova.getActivity().openOptionsMenu();
                return true;
            } else {
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
            // A custom view is currently displayed  (e.g. playing a video)
            if(mCustomView != null) {
                this.hideCustomView();
                return true;
            } else {
                // The webview is currently displayed
                // If back key is bound, then send event to JavaScript
                if (isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK)) {
                    sendJavascriptEvent("backbutton");
                    return true;
                } else {
                    // If not bound
                    // Go to previous page in webview if it is possible to go back
                    if (this.backHistory()) {
                        return true;
                    }
                    // If not, then invoke default behavior
                }
            }
        }
        // Legacy
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (this.lastMenuEventTime < event.getEventTime()) {
                sendJavascriptEvent("menubutton");
            }
            this.lastMenuEventTime = event.getEventTime();
            return super.onKeyUp(keyCode, event);
        }
        // If search key
        else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            sendJavascriptEvent("searchbutton");
            return true;
        }

        //Does webkit change this behavior?
        return super.onKeyUp(keyCode, event);
    }

    private void sendJavascriptEvent(String event) {
        if (appPlugin == null) {
            appPlugin = (CoreAndroid)this.pluginManager.getPlugin(CoreAndroid.PLUGIN_NAME);
        }

        if (appPlugin == null) {
            LOG.w(TAG, "Unable to fire event without existing plugin");
            return;
        }
        appPlugin.fireJavascriptEvent(event);
    }

    @Override
    public void setButtonPlumbedToJs(int keyCode, boolean override) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_BACK:
                // TODO: Why are search and menu buttons handled separately?
                if (override) {
                    boundKeyCodes.add(keyCode);
                } else {
                    boundKeyCodes.remove(keyCode);
                }
                return;
            default:
                throw new IllegalArgumentException("Unsupported keycode: " + keyCode);
        }
    }

    @Override
    public boolean isButtonPlumbedToJs(int keyCode)
    {
        return boundKeyCodes.contains(keyCode);
    }

    public void handlePause(boolean keepRunning)
    {
        LOG.d(TAG, "Handle the pause");
        // Send pause event to JavaScript
        sendJavascriptEvent("pause");

        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onPause(keepRunning);
        }

        // If app doesn't want to run in background
        if (!keepRunning) {
            // Pause JavaScript timers (including setInterval)
            this.pauseTimers();
        }
    }
    
    public void handleResume(boolean keepRunning, boolean activityResultKeepRunning)
    {
        sendJavascriptEvent("resume");
        
        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onResume(keepRunning);
        }

        // Resume JavaScript timers (including setInterval)
        this.resumeTimers();
    }
    
    public void handleDestroy()
    {
        // Cancel pending timeout timer.
        loadUrlTimeout++;

        // Send destroy event to JavaScript
        this.loadUrl("javascript:try{cordova.require('cordova/channel').onDestroy.fire();}catch(e){console.log('exception firing destroy event from native');};");

        // Load blank page so that JavaScript onunload is called
        this.loadUrl("about:blank");
        
        //Remove last AlertDialog
        this.chromeClient.destroyLastDialog();

        // Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onDestroy();
        }
        
        // unregister the receiver
        if (this.receiver != null) {
            try {
                getContext().unregisterReceiver(this.receiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering configuration receiver: " + e.getMessage(), e);
            }
        }
    }
    
    public void onNewIntent(Intent intent)
    {
        //Forward to plugins
        if (this.pluginManager != null) {
            this.pluginManager.onNewIntent(intent);
        }
    }
    
    // Wrapping these functions in their own class prevents warnings in adb like:
    // VFY: unable to resolve virtual method 285: Landroid/webkit/WebSettings;.setAllowUniversalAccessFromFileURLs
    @TargetApi(16)
    private static class Level16Apis {
        static void enableUniversalAccess(WebSettings settings) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
    }

    @TargetApi(17)
    private static final class Level17Apis {
        static void setMediaPlaybackRequiresUserGesture(WebSettings settings, boolean value) {
            settings.setMediaPlaybackRequiresUserGesture(value);
        }
    }
    public void printBackForwardList() {
        WebBackForwardList currentList = this.copyBackForwardList();
        int currentSize = currentList.getSize();
        for(int i = 0; i < currentSize; ++i)
        {
            WebHistoryItem item = currentList.getItemAtIndex(i);
            String url = item.getUrl();
            LOG.d(TAG, "The URL at index: " + Integer.toString(i) + " is " + url );
        }
    }
    
    
    //Can Go Back is BROKEN!
    public boolean startOfHistory()
    {
        WebBackForwardList currentList = this.copyBackForwardList();
        WebHistoryItem item = currentList.getItemAtIndex(0);
        if( item!=null){	// Null-fence in case they haven't called loadUrl yet (CB-2458)
	        String url = item.getUrl();
	        String currentUrl = this.getUrl();
	        LOG.d(TAG, "The current URL is: " + currentUrl);
	        LOG.d(TAG, "The URL at item 0 is: " + url);
	        return currentUrl.equals(url);
        }
        return false;
    }

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
    public boolean isCustomViewShowing() {
        return mCustomView != null;
    }
    
    public WebBackForwardList restoreState(Bundle savedInstanceState)
    {
        WebBackForwardList myList = super.restoreState(savedInstanceState);
        Log.d(TAG, "WebView restoration crew now restoring!");
        //Initialize the plugin manager once more
        this.pluginManager.init();
        return myList;
    }

    public CordovaResourceApi getResourceApi() {
        return resourceApi;
    }

    void onPageReset() {
        boundKeyCodes.clear();
        pluginManager.onReset();
        bridge.reset(loadedUrl);
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public Whitelist getWhitelist() {
        return this.internalWhitelist;
    }

    @Override
    public Whitelist getExternalWhitelist() {
        return this.externalWhitelist;
    }

    @Override
    public CordovaPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onFilePickerResult(Uri uri) {
        if (null == chromeClient.mUploadMessage)
            return;
        chromeClient.mUploadMessage.onReceiveValue(uri);
        chromeClient.mUploadMessage = null;
    }
    
    @Override
    public Object postMessage(String id, Object data) {
        return pluginManager.postMessage(id, data);
    }
}
