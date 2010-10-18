/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;



import com.phonegap.api.Plugin;
import com.phonegap.api.PluginManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
 *         super.loadUrl("file:///android_asset/www/index.html");
 *       }
 *     }
 */
public class DroidGap extends Activity {

    private static final String LOG_TAG = "DroidGap";

    protected WebView appView;					// The webview for our app
	protected ImageView splashScreen;
	protected Boolean loadInWebView = false;
    private LinearLayout root;

    private BrowserKey mKey;
    public CallbackServer callbackServer;
	protected PluginManager pluginManager;

    private String url;							// The initial URL for our app
    private String baseUrl;						// The base of the initial URL for our app

    private Plugin activityResultCallback = null;	// Plugin to call when activity result is received
         
    /** 
     * Called when the activity is first created. 
     * 
     * @param savedInstanceState
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 
        // This builds the view.  We could probably get away with NOT having a LinearLayout, but I like having a bucket!

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
        		ViewGroup.LayoutParams.FILL_PARENT, 0.0F));

        /*
        splashScreen = new ImageView(this);
        splashScreen.setLayoutParams(new LinearLayout.LayoutParams(
        		ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 
        		1.0F));
        splashScreen.setImageResource(R.drawable.splash);
      
        root.addView(splashScreen);
 		*/

        initWebView();
        root.addView(this.appView);
        setContentView(root);        
	}
	
    /**
     * Create and initialize web container.
     */
	private void initWebView() {
		
		// Create web container
		this.appView = new WebView(DroidGap.this);
		
		this.appView.setLayoutParams(new LinearLayout.LayoutParams(
        		ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 
        		1.0F));

        WebViewReflect.checkCompatibility();

        if (android.os.Build.VERSION.RELEASE.startsWith("2.")) {
        	this.appView.setWebChromeClient(new EclairClient(DroidGap.this));        	
        }
        else {
        	this.appView.setWebChromeClient(new GapClient(DroidGap.this));
        }
           
        this.appView.setWebViewClient(new GapViewClient(this));

        this.appView.setInitialScale(100);
        this.appView.setVerticalScrollBarEnabled(false);
        this.appView.requestFocusFromTouch();

        // Enable JavaScript
        WebSettings settings = this.appView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

        // Enable database
        Package pack = this.getClass().getPackage();
        String appPackage = pack.getName();
        WebViewReflect.setStorage(settings, true, "/data/data/" + appPackage + "/app_database/");

        // Enable DOM storage
        WebViewReflect.setDomStorage(settings);
        
        // Enable built-in geolocation
        WebViewReflect.setGeolocationEnabled(settings, true);

        // Bind PhoneGap objects to JavaScript
        this.bindBrowser(this.appView);
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

    @Override
    /**
     * Called when the system is about to start resuming a previous activity. 
     */
    protected void onPause() {
        super.onPause();

        // Forward to plugins
        this.pluginManager.onPause();
        
        // Send pause event to JavaScript
        this.appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};");
        
        // Pause JavaScript timers (including setInterval)
        this.appView.pauseTimers();
    }

    @Override
    /**
     * Called when the activity will start interacting with the user. 
     */
    protected void onResume() {
        super.onResume();

        // Forward to plugins
        this.pluginManager.onResume();
        
        // Send resume event to JavaScript
        this.appView.loadUrl("javascript:try{PhoneGap.onResume.fire();}catch(e){};");
        
        // Resume JavaScript timers (including setInterval)
        this.appView.resumeTimers();
    }
    
    @Override
    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Make sure pause event is sent if onPause hasn't been called before onDestroy
       	this.appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};");
    	
    	// Load blank page so that JavaScript onunload is called
       	this.appView.loadUrl("about:blank");
    	    	
    	// Clean up objects
    	if (this.mKey != null) {
    	}
    	
        // Forward to plugins
        this.pluginManager.onDestroy();

    	if (this.callbackServer != null) {
    		this.callbackServer.destroy();
    	}
    }

    /**
     * Add a class that implements a service.
     * 
     * @param serviceType
     * @param className
     */
    public void addService(String serviceType, String className) {
    	this.pluginManager.addService(serviceType, className);
    }

    /**
     * Bind PhoneGap objects to JavaScript.
     * 
     * @param appView
     */
    private void bindBrowser(WebView appView) {
        this.callbackServer = new CallbackServer();
    	this.pluginManager = new PluginManager(appView, this);
        this.mKey = new BrowserKey(appView, this);
    	
    	// This creates the new javascript interfaces for PhoneGap
    	appView.addJavascriptInterface(this.pluginManager, "PluginManager");
        
        appView.addJavascriptInterface(this.mKey, "BackButton");
        
        appView.addJavascriptInterface(this.callbackServer, "CallbackServer");
    	appView.addJavascriptInterface(new SplashScreen(this), "SplashScreen");

        
        this.addService("Geolocation", "com.phonegap.GeoBroker");
        this.addService("Device", "com.phonegap.Device");
        this.addService("Accelerometer", "com.phonegap.AccelListener");
        this.addService("Compass", "com.phonegap.CompassListener");
        this.addService("Media", "com.phonegap.AudioHandler");
        this.addService("Camera", "com.phonegap.CameraLauncher");
        this.addService("Contacts", "com.phonegap.ContactManager");
        this.addService("Crypto", "com.phonegap.CryptoHandler");
        this.addService("File", "com.phonegap.FileUtils");
        this.addService("Location", "com.phonegap.GeoBroker");	// Always add Location, even though it is built-in on 2.x devices. Let JavaScript decide which one to use.
        this.addService("Network Status", "com.phonegap.NetworkManager");
        this.addService("Notification", "com.phonegap.Notification");
        this.addService("Storage", "com.phonegap.Storage");
        this.addService("Temperature", "com.phonegap.TempListener");

    }
        
    /**
     * Load the url into the webview.
     * 
     * @param url
     */
    public void loadUrl(final String url) {
        this.url = url;
        int i = url.lastIndexOf('/');
        if (i > 0) {
        	this.baseUrl = url.substring(0, i);
        }
        else {
        	this.baseUrl = this.url;
        }
	    
	    this.runOnUiThread(new Runnable() {
			public void run() {
		        DroidGap.this.appView.loadUrl(url);
	        }
        });
	}
    
    /**
     * Send JavaScript statement back to JavaScript.
     * 
     * @param message
     */
    public void sendJavascript(String statement) {
    	this.callbackServer.sendJavascript(statement);
    }
    
    /**
     * Get the port that the callback server is listening on.
     * 
     * @return
     */
    public int getPort() {
    	return this.callbackServer.getPort();
    }
	
    /**
     * Provides a hook for calling "alert" from javascript. Useful for
     * debugging your javascript.
     */
    public class GapClient extends WebChromeClient {

        private Context ctx;
        
        /**
         * Constructor.
         * 
         * @param ctx
         */
        public GapClient(Context ctx) {
            this.ctx = ctx;
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
            Log.d(LOG_TAG, message);
            AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
            dlg.setMessage(message);
            dlg.setTitle("Alert");
            dlg.setCancelable(false);
            dlg.setPositiveButton(android.R.string.ok,
            	new AlertDialog.OnClickListener() {
                	public void onClick(DialogInterface dialog, int which) {
                		result.confirm();
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
            dlg.setCancelable(false);
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
            dlg.create();
            dlg.show();
            return true;
        }

    }

    /**
     * WebChromeClient that extends GapClient with additional support for Android 2.X
     */
    public final class EclairClient extends GapClient {
        
		private String TAG = "PhoneGapLog";
		private long MAX_QUOTA = 100 * 1024 * 1024;

		/**
		 * Constructor.
		 * 
		 * @param ctx
		 */
        public EclairClient(Context ctx) {
            super(ctx);
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
		  Log.d(TAG, "event raised onExceededDatabaseQuota estimatedSize: " + Long.toString(estimatedSize) + " currentQuota: " + Long.toString(currentQuota) + " totalUsedQuota: " + Long.toString(totalUsedQuota));  	
		  		  
			if( estimatedSize < MAX_QUOTA)
		    	{	                                        
		    	  //increase for 1Mb        		    	  		    	  
		    		long newQuota = estimatedSize;		    		
		    		Log.d(TAG, "calling quotaUpdater.updateQuota newQuota: " + Long.toString(newQuota) );  	
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
			// This is a kludgy hack!!!!
			Log.d(TAG, sourceID + ": Line " + Integer.toString(lineNumber) + " : " + message);              
		}
		
		@Override
		public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
			// TODO Auto-generated method stub
			super.onGeolocationPermissionsShowPrompt(origin, callback);
			callback.invoke(origin, true, false);
		}
		
	}
	
    /**
     * The webview client receives notifications about appView
     */
    public class GapViewClient extends WebViewClient {

    	// TODO: hide splash screen here

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
         * @param view			The WebView that is initiating the callback.
         * @param url			The url to be loaded.
         * @return				true to override, false for default behavior
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

        	// If dialing phone (tel:5551212)
        	if (url.startsWith(WebView.SCHEME_TEL)) {
        		try {
        			Intent intent = new Intent(Intent.ACTION_DIAL);
        			intent.setData(Uri.parse(url));
        			startActivity(intent);
        		} catch (android.content.ActivityNotFoundException e) {
        			System.out.println("Error dialing "+url+": "+ e.toString());
        		}
        		return true;
        	}
        	
        	// If displaying map (geo:0,0?q=address)
        	else if (url.startsWith(WebView.SCHEME_GEO)) {
           		try {
        			Intent intent = new Intent(Intent.ACTION_VIEW);
        			intent.setData(Uri.parse(url));
        			startActivity(intent);
        		} catch (android.content.ActivityNotFoundException e) {
        			System.out.println("Error showing map "+url+": "+ e.toString());
        		}
        		return true;        		
        	}
			
        	// If sending email (mailto:abc@corp.com)
        	else if (url.startsWith(WebView.SCHEME_MAILTO)) {
           		try {
        			Intent intent = new Intent(Intent.ACTION_VIEW);
        			intent.setData(Uri.parse(url));
        			startActivity(intent);
        		} catch (android.content.ActivityNotFoundException e) {
        			System.out.println("Error sending email "+url+": "+ e.toString());
        		}
        		return true;        		
        	}
        	
        	// If sms:5551212
            else if (url.startsWith("sms:")) {
            	try {
            		Intent intent = new Intent(Intent.ACTION_VIEW);
            		intent.setData(Uri.parse(url));
            		intent.putExtra("address", url.substring(4));
            		intent.setType("vnd.android-dir/mms-sms");
            		startActivity(intent);
            	} catch (android.content.ActivityNotFoundException e) {
            		System.out.println("Error sending sms "+url+":"+ e.toString());
            	}
            	return true;
            }  	

        	// If http, https or file
        	else if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {

        		int i = url.lastIndexOf('/');
        		String newBaseUrl = url;
        		if (i > 0) {
        			newBaseUrl = url.substring(0, i);
        		}

        		// If our app or file:, then load into our webview
        		if (this.ctx.loadInWebView || url.startsWith("file://") || this.ctx.baseUrl.equals(newBaseUrl)) {
        			this.ctx.appView.loadUrl(url);
        		}
  		
        		// If not our application, let default viewer handle
        		else {
        			try {
        				Intent intent = new Intent(Intent.ACTION_VIEW);
        				intent.setData(Uri.parse(url));
        				startActivity(intent);
                	} catch (android.content.ActivityNotFoundException e) {
                		System.out.println("Error loading url "+url+":"+ e.toString());
                	}
        		}
        		return true;
        	}
        	
        	return false;
        }
    	
        /**
         * Notify the host application that a page has finished loading.
         * 
         * @param view			The webview initiating the callback.
         * @param url			The url of the page.
         */
        @Override
        public void onPageFinished  (WebView view, String url) {
        	super.onPageFinished(view, url);
            // Try firing the onNativeReady event in JS. If it fails because the JS is
            // not loaded yet then just set a flag so that the onNativeReady can be fired
            // from the JS side when the JS gets to that code.
    		appView.loadUrl("javascript:try{ PhoneGap.onNativeReady.fire();}catch(e){_nativeReady = true;}");
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {	
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (mKey.isBound())
        	{
        		//We fire an event here!
        		appView.loadUrl("javascript:document.keyEvent.backTrigger()");
        	}
        	else
        	{
        		// only go back if the webview tells you that it is possible to go back
        		if(appView.canGoBack())
        		{
        			appView.goBack();
        		}
        		else // if you can't go back, invoke behavior of super class
        		{
        			return super.onKeyDown(keyCode, event);
        		}
        	}
        }
        
        if (keyCode == KeyEvent.KEYCODE_MENU) 
        {
        	// This is where we launch the menu
        	appView.loadUrl("javascript:keyEvent.menuTrigger()");
        }
        return false;
    }
	
    /**
     * Removes the splash screen from root view and adds the WebView
     */
    public void hideSplashScreen() {
    	root.removeView(splashScreen);
    	root.addView(this.appView);
    }
    
    /**
     * Any calls to Activity.startActivityForResult must use method below, so 
     * the result can be routed to them correctly.  
     * 
     * This is done to eliminate the need to modify DroidGap.java to receive activity results.
     * 
     * @param intent			The intent to start
     * @param requestCode		Identifies who to send the result to
     * 
     * @throws RuntimeException
     */
    @Override
    public void startActivityForResult(Intent intent, int requestCode) throws RuntimeException {
    	System.out.println("startActivityForResult(intent,"+requestCode+")");
    	if (requestCode == -1) {
    		super.startActivityForResult(intent, requestCode);
    	}
    	else {
    		throw new RuntimeException("PhoneGap Exception: Call startActivityForResult(Command, Intent) instead.");
    	}
    }

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits, 
     * your onActivityResult() method will be called.
     *  
     * @param command			The command object
     * @param intent			The intent to start
     * @param requestCode		The request code that is passed to callback to identify the activity
     */
    public void startActivityForResult(Plugin command, Intent intent, int requestCode) {
    	this.activityResultCallback = command;
    	super.startActivityForResult(intent, requestCode);
    }

     @Override
    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Plugin callback = this.activityResultCallback;
        if (callback != null) {
        	callback.onActivityResult(requestCode, resultCode, intent); 
        }        
    }
}
