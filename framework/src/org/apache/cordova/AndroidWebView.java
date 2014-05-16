package org.apache.cordova;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;

public class AndroidWebView extends WebView {

    public static final String TAG = "CordovaWebView";
    public static final String CORDOVA_VERSION = "3.6.0-dev";

    private ArrayList<Integer> keyDownCodes = new ArrayList<Integer>();
    private ArrayList<Integer> keyUpCodes = new ArrayList<Integer>();

    private BroadcastReceiver receiver;

    private AndroidCordovaWebView cordovaWebView;

    /** Activities and other important classes **/
    private CordovaInterface cordova;

    // Flag to track that a loadUrl timeout occurred
    int loadUrlTimeout = 0;

    private boolean bound;
    
    private long lastMenuEventTime = 0;

    /**
     * Constructor.
     *
     * @param context
     */
    public AndroidWebView(Context context) {
        super(context);
        this.setup((CordovaInterface)context);
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     */
    public AndroidWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setup((CordovaInterface)context);
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     * @param defStyle
     *
     */
    public AndroidWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setup((CordovaInterface)context);
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     * @param defStyle
     * @param privateBrowsing
     */
    @SuppressWarnings("deprecation")
	@TargetApi(11)
    public AndroidWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
        this.setup((CordovaInterface)context);
    }

    /**
     * Initialize webview.
     */
    @SuppressWarnings("deprecation")
    @SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
    private void setup(CordovaInterface cordova) {
    	this.cordova = cordova;
        this.setInitialScale(0);
        this.setVerticalScrollBarEnabled(false);
        if (shouldRequestFocusOnInit()) {
			this.requestFocusFromTouch();
		}
		// Enable JavaScript
        WebSettings settings = this.getSettings();
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
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            Level16Apis.enableUniversalAccess(settings);
        // Enable database
        // We keep this disabled because we use or shim to get around DOM_EXCEPTION_ERROR_16
        String databasePath = cordova.getActivity().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);
        
        
        //Determine whether we're in debug or release mode, and turn on Debugging!
        try {
            final String packageName = cordova.getActivity().getPackageName();
            final PackageManager pm = cordova.getActivity().getPackageManager();
            ApplicationInfo appInfo;
            
            appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            
            if((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0 &&  
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            {
                setWebContentsDebuggingEnabled(true);
            }
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "You have one job! To turn on Remote Web Debugging! YOU HAVE FAILED! ");
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            Log.d(TAG, "This should never happen: Your application's package can't be found.");
            e.printStackTrace();
        }  
        
        settings.setGeolocationDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);
        
        // Enable AppCache
        // Fix for CB-2282
        settings.setAppCacheMaxSize(5 * 1048576);
        String pathToCache = cordova.getActivity().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(pathToCache);
        settings.setAppCacheEnabled(true);
        
        // Fix for CB-1405
        // Google issue 4641
        updateUserAgentString();
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateUserAgentString();
                }
            };
            cordova.getActivity().registerReceiver(this.receiver, intentFilter);
        }
        // end CB-1405

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

	private void updateUserAgentString() {
        this.getSettings().getUserAgentString();
    }

	public AndroidCordovaWebView getCordovaWebView() {
		return cordovaWebView;
	}

	public void setCordovaWebView(AndroidCordovaWebView cordovaWebView) {
		this.cordovaWebView = cordovaWebView;
	}

    /**
     * Load the url into the webview.
     *
     * @param url
     */
    @Override
    public void loadUrl(String url) {
    	this.getCordovaWebView().loadUrl(url);
    }

    /**
     * Load the url into the webview.
     *
     * @param url
     */
    public void loadUrlIntoView(final String url) {
        LOG.d(TAG, ">>> loadUrl(" + url + ")");

        // Create a timeout timer for loadUrl
        final AndroidWebView me = this;
        final int currentLoadUrlTimeout = me.loadUrlTimeout;
        final int loadUrlTimeoutValue = Integer.parseInt(getCordovaWebView().getProperty("LoadUrlTimeoutValue", "20000"));

        // Timeout error method
        final Runnable loadError = new Runnable() {
            public void run() {
                me.stopLoading();
                LOG.e(TAG, "CordovaWebView: TIMEOUT ERROR!");
                CordovaWebViewClient viewClient = me.getCordovaWebView().getWebViewClient();
                if (viewClient != null) {
                    viewClient.onReceivedError(me.getCordovaWebView(), -6, "The connection to the server was unsuccessful.", url);
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
     *
     * @param url
     */
    public void loadUrlNow(String url) {
        if (LOG.isLoggable(LOG.DEBUG) && !url.startsWith("javascript:")) {
            LOG.d(TAG, ">>> loadUrlNow()");
        }
        if (url.startsWith("file://") || url.startsWith("javascript:") || Config.isUrlWhiteListed(url)) {
            super.loadUrl(url);
        }
    }

    @Override
    public void stopLoading() {
        //viewClient.isCurrentlyLoading = false;
        super.stopLoading();
    }
    
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
        //We should post a message that the scroll changed
        ScrollEvent myEvent = new ScrollEvent(l, t, oldl, oldt, this);
        getCordovaWebView().postMessage("onScrollChanged", myEvent);
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
        else if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            return !(this.startOfHistory()) || this.bound;
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
            if(getCordovaWebView().isCustomViewShowing()) {
                getCordovaWebView().hideCustomView();
            } else {
                // The webview is currently displayed
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
                    	//return false;
                    	// If they hit back button when app is initializing, app should exit instead of hang until initialization (CB2-458)
                    	this.cordova.getActivity().finish();
                    }
                }
            }
        }
        // Legacy
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (this.lastMenuEventTime < event.getEventTime()) {
                this.loadUrl("javascript:cordova.fireDocumentEvent('menubutton');");
            }
            this.lastMenuEventTime = event.getEventTime();
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

    // Wrapping these functions in their own class prevents warnings in adb like:
    // VFY: unable to resolve virtual method 285: Landroid/webkit/WebSettings;.setAllowUniversalAccessFromFileURLs
    @TargetApi(16)
    private static class Level16Apis {
        static void enableUniversalAccess(WebSettings settings) {
            settings.setAllowUniversalAccessFromFileURLs(true);
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

    @Override
    public WebBackForwardList restoreState(Bundle savedInstanceState)
    {
        WebBackForwardList myList = super.restoreState(savedInstanceState);
        Log.d(TAG, "WebView restoration crew now restoring!");
        //Initialize the plugin manager once more
        getCordovaWebView().pluginManager.init();
        return myList;
    }

}
