/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010-2011, IBM Corporation
 */
package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.phonegap.api.PhonegapActivity;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginManager;

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
 *         // Add your plugins here or in JavaScript
 *         super.addService("MyService", "com.phonegap.examples.MyService");
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
 * 		// Display a native loading dialog.  Format for value = "Title,Message".  
 * 		// (String - default=null)
 * 		super.setStringProperty("loadingDialog", "Wait,Loading Demo...");
 * 
 * 		// Hide loadingDialog when page loaded instead of when deviceready event
 * 		// occurs. (Boolean - default=false)
 * 		super.setBooleanProperty("hideLoadingDialogOnPage", true);
 * 
 * 		// Cause all links on web page to be loaded into existing web view, 
 * 		// instead of being loaded into new browser. (Boolean - default=false)
 * 		super.setBooleanProperty("loadInWebView", true);
 * 
 * 		// Load a splash screen image from the resource drawable directory.
 * 		// (Integer - default=0)
 * 		super.setIntegerProperty("splashscreen", R.drawable.splash);
 * 
 * 		// Time in msec to wait before triggering a timeout error when loading
 * 		// with super.loadUrl().  (Integer - default=20000)
 * 		super.setIntegerProperty("loadUrlTimeoutValue", 60000);
 * 
 * 		// URL to load if there's an error loading specified URL with loadUrl().  
 * 		// Should be a local URL starting with file://. (String - default=null)
 * 		super.setStringProperty("errorUrl", "file:///android_asset/www/error.html");
 * 
 * 		// Enable app to keep running in background. (Boolean - default=true)
 * 		super.setBooleanProperty("keepRunning", false);
 */
public class DroidGap extends PhonegapActivity {

	// The webview for our app
	protected WebView appView;
	protected WebViewClient webViewClient;

	protected LinearLayout root;
	public boolean bound = false;
	public CallbackServer callbackServer;
	protected PluginManager pluginManager;
	protected boolean cancelLoadUrl = false;
	protected boolean clearHistory = false;

	// The initial URL for our app
	private String url;
	
	// The base of the initial URL for our app
	private String baseUrl;

	// Plugin to call when activity result is received
	protected Plugin activityResultCallback = null;
	protected boolean activityResultKeepRunning;

	// Flag indicates that a loadUrl timeout occurred
	private int loadUrlTimeout = 0;
	
	/*
	 * The variables below are used to cache some of the activity properties.
	 */
	
	// Flag indicates that "app loading" dialog should be hidden once page is loaded.
	// The default is to hide it once PhoneGap JavaScript code has initialized.
	protected boolean hideLoadingDialogOnPageLoad = false;	

	// Flag indicates that a URL navigated to from PhoneGap app should be loaded into same webview
	// instead of being loaded into the web browser.  
	protected boolean loadInWebView = false;

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
    	root.setBackgroundColor(Color.BLACK);
    	root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
    			ViewGroup.LayoutParams.FILL_PARENT, 0.0F));

    	// If url was passed in to intent, then init webview, which will load the url
    	Bundle bundle = this.getIntent().getExtras();
    	if (bundle != null) {
    		String url = bundle.getString("url");
    		if (url != null) {
    			this.init();
    		}
    	}
    	// Setup the hardware volume controls to handle volume control
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    
    /**
     * Create and initialize web container.
     */
	public void init() {
		
		// Create web container
		this.appView = new WebView(DroidGap.this);
		this.appView.setId(100);
		
		this.appView.setLayoutParams(new LinearLayout.LayoutParams(
        		ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 
        		1.0F));

        WebViewReflect.checkCompatibility();

        if (android.os.Build.VERSION.RELEASE.startsWith("1.")) {
        	this.appView.setWebChromeClient(new GapClient(DroidGap.this));
        }
        else {
        	this.appView.setWebChromeClient(new EclairClient(DroidGap.this));        	
        }
           
        this.setWebViewClient(this.appView, new GapViewClient(this));

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

        // Add web view but make it invisible while loading URL
        this.appView.setVisibility(View.INVISIBLE);
        root.addView(this.appView);
        setContentView(root);
        
        // Clear cancel flag
        this.cancelLoadUrl = false;

        // If url specified, then load it
        String url = this.getStringProperty("url", null);
        if (url != null) {
        	System.out.println("Loading initial URL="+url);
        	this.loadUrl(url);        	
        }
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
     * Bind PhoneGap objects to JavaScript.
     * 
     * @param appView
     */
	private void bindBrowser(WebView appView) {
		this.callbackServer = new CallbackServer();
		this.pluginManager = new PluginManager(appView, this);

		this.addService("App", "com.phonegap.App");
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
		this.addService("FileTransfer", "com.phonegap.FileTransfer");
		this.addService("Capture", "com.phonegap.Capture");
	}
        
	/**
	 * Look at activity parameters and process them.
	 * This must be called from the main UI thread.
	 */
	private void handleActivityParameters() {

		// Init web view if not already done
		if (this.appView == null) {
			this.init();
		}

		// If spashscreen
		this.splashscreen = this.getIntegerProperty("splashscreen", 0);
		if (this.splashscreen != 0) {
			root.setBackgroundResource(this.splashscreen);
		}

		// If hideLoadingDialogOnPageLoad
		this.hideLoadingDialogOnPageLoad = this.getBooleanProperty("hideLoadingDialogOnPageLoad", false);

		// If loadInWebView
		this.loadInWebView = this.getBooleanProperty("loadInWebView", false);

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
	public void loadUrl(final String url) {
		System.out.println("loadUrl("+url+")");
		this.url = url;
		int i = url.lastIndexOf('/');
		if (i > 0) {
			this.baseUrl = url.substring(0, i);
		}
		else {
			this.baseUrl = this.url;
		}
		System.out.println("url="+url+" baseUrl="+baseUrl);

		// Load URL on UI thread
		final DroidGap me = this;
		this.runOnUiThread(new Runnable() {
			public void run() {

				// Handle activity parameters
				me.handleActivityParameters();

				// Initialize callback server
				me.callbackServer.init(url);

				// If loadingDialog, then show the App loading dialog
				String loading = me.getStringProperty("loadingDialog", null);
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
					JSONArray parm = new JSONArray();
					parm.put(title);
					parm.put(message);
					me.pluginManager.exec("Notification", "activityStart", null, parm.toString(), false);
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
	 * @param time				The number of ms to wait before loading webview
	 */
	public void loadUrl(final String url, final int time) {
		System.out.println("loadUrl("+url+","+time+")");
		final DroidGap me = this;

		// Handle activity parameters
		this.runOnUiThread(new Runnable() {
			public void run() {
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
					me.loadUrl(url);
				}
				else{
					me.cancelLoadUrl = false;
					System.out.println("Aborting loadUrl("+url+"): Another URL was loaded before timer expired.");
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
    	this.clearHistory = true;
    	if (this.appView != null) {
    		this.appView.clearHistory();
    	}
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
        if (this.appView == null) {
        	return;
        }
        
       	// Send pause event to JavaScript
       	this.appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};"); 

      	// Forward to plugins
      	this.pluginManager.onPause();

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
        if (this.appView == null) {
        	return;
        }

       	// Send resume event to JavaScript
       	this.appView.loadUrl("javascript:try{PhoneGap.onResume.fire();}catch(e){};");

      	// Forward to plugins
      	this.pluginManager.onResume();

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
    	
        	// Make sure pause event is sent if onPause hasn't been called before onDestroy
        	this.appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};");

        	// Send destroy event to JavaScript
        	this.appView.loadUrl("javascript:try{PhoneGap.onDestroy.fire();}catch(e){};");

        	// Load blank page so that JavaScript onunload is called
        	this.appView.loadUrl("about:blank");
    	    	
        	// Forward to plugins
        	this.pluginManager.onDestroy();

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
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     * 
     * @param message
     */
    public void sendJavascript(String statement) {
    	this.callbackServer.sendJavascript(statement);
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

        /**
         * Tell the client to display a prompt dialog to the user. 
         * If the client returns true, WebView will assume that the client will 
         * handle the prompt dialog and call the appropriate JsPromptResult method.
         * 
         * @param view
         * @param url
         * @param message
         * @param defaultValue
         * @param result
         */
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        	boolean reqOk = false;
        	if (((DroidGap)(this.ctx)).url.equals(url)) {
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
        	else if (reqOk && defaultValue.equals("gap_poll:")) {
        		String r = callbackServer.getJavascript();
        		result.confirm(r);
        	}
        	
        	// Calling into CallbackServer
        	else if (reqOk && defaultValue.equals("gap_callbackServer:")) {
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
        	
        	// Show dialog
        	else {
				final JsPromptResult res = result;
				AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
				dlg.setMessage(message);
				final EditText input = new EditText(this.ctx);
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
        
    }

    /**
     * WebChromeClient that extends GapClient with additional support for Android 2.X
     */
    public class EclairClient extends GapClient {

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
    	/**
    	 * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin. 
    	 * 
    	 * @param origin
    	 * @param callback
    	 */
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
        	else if (url.startsWith("geo:")) {
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
        			System.out.println("Error sending sms "+url+":"+ e.toString());
        		}
        		return true;
        	}

        	// All else
        	else {

        		int i = url.lastIndexOf('/');
        		String newBaseUrl = url;
        		if (i > 0) {
        			newBaseUrl = url.substring(0, i);
        		}

        		// If our app or file:, then load into our webview
        		// NOTE: This replaces our app with new URL.  When BACK is pressed,
        		//       our app is reloaded and restarted.  All state is lost.
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
        }
    	
        /**
         * Notify the host application that a page has finished loading.
         * 
         * @param view			The webview initiating the callback.
         * @param url			The url of the page.
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

        	// Make app view visible
        	appView.setVisibility(View.VISIBLE);

        	// Stop "app loading" spinner if showing
        	if (this.ctx.hideLoadingDialogOnPageLoad) {
        		this.ctx.hideLoadingDialogOnPageLoad = false;
        		this.ctx.pluginManager.exec("Notification", "activityStop", null, "[]", false);
        	}

    		// Clear history, so that previous screen isn't there when Back button is pressed
    		if (this.ctx.clearHistory) {
    			this.ctx.clearHistory = false;
    			this.ctx.appView.clearHistory();
    		}
    		
    		// Shutdown if blank loaded
    		if (url.equals("about:blank")) {
    			if (this.ctx.callbackServer != null) {
    				this.ctx.callbackServer.destroy();
    			}
    		}
        }
        
        /**
         * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable). 
         * The errorCode parameter corresponds to one of the ERROR_* constants.
         *
         * @param view 			The WebView that is initiating the callback.
         * @param errorCode 	The error code corresponding to an ERROR_* value.
         * @param description 	A String describing the error.
         * @param failingUrl 	The url that failed to load. 
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	System.out.println("onReceivedError: Error code="+errorCode+" Description="+description+" URL="+failingUrl);

        	// Clear timeout flag
        	this.ctx.loadUrlTimeout++;

       	 	// Stop "app loading" spinner if showing
       	 	this.ctx.pluginManager.exec("Notification", "activityStop", null, "[]", false);

        	// Handle error
        	this.ctx.onReceivedError(errorCode, description, failingUrl);
        }
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
    			this.appView.loadUrl("javascript:PhoneGap.fireEvent('backbutton');");
    		}

    		// If not bound
    		else {

    			// Go to previous page in webview if it is possible to go back
    			if (this.appView.canGoBack()) {
    				this.appView.goBack();
    			}

    			// If not, then invoke behavior of super class
    			else {
    				return super.onKeyDown(keyCode, event);
    			}
    		}
    	}

    	// If menu key
    	else if (keyCode == KeyEvent.KEYCODE_MENU) {
    		this.appView.loadUrl("javascript:PhoneGap.fireEvent('menubutton');");
    	}

    	// If search key
    	else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
    		this.appView.loadUrl("javascript:PhoneGap.fireEvent('searchbutton');");
    	}

    	return false;
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
   
     /**
      * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable). 
      * The errorCode parameter corresponds to one of the ERROR_* constants.
      *
      * @param errorCode 	The error code corresponding to an ERROR_* value.
      * @param description 	A String describing the error.
      * @param failingUrl 	The url that failed to load. 
      */
     public void onReceivedError(int errorCode, String description, String failingUrl) {
    	 final DroidGap me = this;

    	 // If errorUrl specified, then load it
    	 final String errorUrl = me.getStringProperty("errorUrl", null);
    	 if ((errorUrl != null) && errorUrl.startsWith("file://") && (!failingUrl.equals(errorUrl))) {

    		 // Load URL on UI thread
    		 me.runOnUiThread(new Runnable() {
    			 public void run() {
    				 me.appView.loadUrl(errorUrl);
    			 }
    		 });
    	 }

    	 // If not, then display error dialog
    	 else {
    		 me.appView.loadUrl("about:blank");
    		 me.displayError("Application Error", description + " ("+failingUrl+")", "OK", true);
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
    						 me.finish();
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

    	    private static final String LOG_TAG = "SoftKeyboardDetect";
    	    
    	    private int oldHeight = 0;	// Need to save the old height as not to send redundant events
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
    	        
    	    	Log.d(LOG_TAG, "We are in our onMeasure method");

    	    	// Get the current height of the visible part of the screen.
    	    	// This height will not included the status bar.
    	    	int height = MeasureSpec.getSize(heightMeasureSpec);
    	        int width = MeasureSpec.getSize(widthMeasureSpec);
    	        
    	        Log.d(LOG_TAG, "Old Height = " + oldHeight);
    	        Log.d(LOG_TAG, "Height = " + height);  	       
    	        Log.d(LOG_TAG, "Old Width = " + oldWidth);
    	        Log.d(LOG_TAG, "Width = " + width);
    	        
    	        
    	        // If the oldHeight = 0 then this is the first measure event as the app starts up.
    	        // If oldHeight == height then we got a measurement change that doesn't affect us.
    	        if (oldHeight == 0 || oldHeight == height) {
    	        	Log.d(LOG_TAG, "Ignore this event");
    	        }
    	        // Account for orientation change and ignore this event/Fire orientation change
    	        else if(screenHeight == width)
    	        {
    	        	int tmp_var = screenHeight;
    	        	screenHeight = screenWidth;
    	        	screenWidth = tmp_var;
    	        	Log.d(LOG_TAG, "Orientation Change");
    	        }
    	        // If the height as gotten bigger then we will assume the soft keyboard has 
    	        // gone away.
    	        else if (height > oldHeight) {
    	        	Log.d(LOG_TAG, "Throw hide keyboard event");
    	        	callbackServer.sendJavascript("PhoneGap.fireEvent('hidekeyboard');");
    	        } 
    	        // If the height as gotten smaller then we will assume the soft keyboard has 
    	        // been displayed.
    	        else if (height < oldHeight) {
    	        	Log.d(LOG_TAG, "Throw show keyboard event");
    	        	callbackServer.sendJavascript("PhoneGap.fireEvent('showkeyboard');");
    	        }

    	        // Update the old height for the next event
    	        oldHeight = height;
    	        oldWidth = width;
    	    }
    }
}
