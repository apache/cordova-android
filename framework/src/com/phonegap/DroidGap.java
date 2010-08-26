	package com.phonegap;
/* License (MIT)
 * Copyright (c) 2008 Nitobi
 * website: http://phonegap.com
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * Software), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


import java.io.File;

import com.phonegap.api.Command;
import com.phonegap.api.CommandManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.os.Build.*;
import android.provider.MediaStore;

public class DroidGap extends Activity {
		
	private static final String LOG_TAG = "DroidGap";
	protected WebView appView;
	protected ImageView splashScreen;
	private LinearLayout root;	
	
	private Device gap;
	private GeoBroker geo;
    private AccelListener accel;
	private CameraLauncher launcher;
	private ContactManager mContacts;
	private FileUtils fs;
	private NetworkManager netMan;
	private CompassListener mCompass;
	private Storage	cupcakeStorage;
	private CryptoHandler crypto;
	private BrowserKey mKey;
	private AudioHandler audio;
    private CallbackServer callbackServer;
	private CommandManager commandManager;
	
	private Uri imageUri;
    private String url;							// The initial URL for our app
    private String baseUrl;						// The base of the initial URL for our app
    private boolean resumeState = false;		// Track if onResume() has been called
	
    /** Called when the activity is first created. */
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
        
        root.addView(appView);

        setContentView(root);        
	}
	
	private void initWebView() {
		appView = new WebView(DroidGap.this);
		
        appView.setLayoutParams(new LinearLayout.LayoutParams(
        		ViewGroup.LayoutParams.FILL_PARENT,
        		ViewGroup.LayoutParams.FILL_PARENT, 
        		1.0F));

        WebViewReflect.checkCompatibility();

        if (android.os.Build.VERSION.RELEASE.startsWith("2.")) {
        	appView.setWebChromeClient(new EclairClient(DroidGap.this));        	
        } else {
        	appView.setWebChromeClient(new GapClient(DroidGap.this));
        }
           
        appView.setWebViewClient(new GapViewClient(this));

        appView.setInitialScale(100);
        appView.setVerticalScrollBarEnabled(false);
        appView.requestFocusFromTouch();

        WebSettings settings = appView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

    	Package pack = this.getClass().getPackage();
    	String appPackage = pack.getName();
    	
        WebViewReflect.setStorage(settings, true, "/data/data/" + appPackage + "/app_database/");
        
        // Turn on DOM storage!
        WebViewReflect.setDomStorage(settings);
        // Turn off native geolocation object in browser - we use our own :)
        WebViewReflect.setGeolocationEnabled(settings, true);
        // Bind the appView object to the gap class methods
        bindBrowser(appView);
        if(cupcakeStorage != null)
        	cupcakeStorage.setStorage(appPackage);
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
    protected void onPause(){
        super.onPause();

        // Send pause event to JavaScript
        if (this.resumeState) {
        	appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};");
        	this.resumeState = false;
        }
        
        // Pause JavaScript timers (including setInterval)
        appView.pauseTimers();
    }

    @Override
    /**
     * Called when the activity will start interacting with the user. 
     */
    protected void onResume(){
        super.onResume();

        // Send resume event to JavaScript
        if (!this.resumeState) {
        	appView.loadUrl("javascript:try{PhoneGap.onResume.fire();}catch(e){};");
        	this.resumeState = true;
        }
        
        // Resume JavaScript timers (including setInterval)
        appView.resumeTimers();
    }
    
    @Override
    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Make sure pause event is sent if onPause hasn't been called before onDestroy
    	if (this.resumeState) {
    		appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};");
    		this.resumeState = false;
    	}
    	
    	// Load blank page so that JavaScript onunload is called
    	appView.loadUrl("about:blank");
    	    	
    	// Clean up objects
    	if (accel != null) {
    		accel.destroy();
    	}
    	if (launcher != null) {
    		
    	}
    	if (mContacts != null) {
    		
    	}
    	if (fs != null) {
    		
    	}
    	if (netMan != null) {
    		
    	}
    	if (mCompass != null) {
    		mCompass.destroy();
    	}
    	if (crypto != null) {
    		
    	}
    	if (mKey != null) {
    		
    	}
    	if (audio != null) {
    		
    	}
    	if (callbackServer != null) {
    		callbackServer.destroy();
    	}
    }

    private void bindBrowser(WebView appView)
    {
        callbackServer = new CallbackServer();
    	commandManager = new CommandManager(appView, this);
    	gap = new Device(appView, this);
        accel = new AccelListener(appView, this);
    	launcher = new CameraLauncher(appView, this);
    	mContacts = new ContactManager(appView, this);
    	fs = new FileUtils(appView);
    	netMan = new NetworkManager(appView, this);
    	mCompass = new CompassListener(appView, this);  
    	crypto = new CryptoHandler(appView);
    	mKey = new BrowserKey(appView, this);
    	audio = new AudioHandler(appView, this);
    	
    	// This creates the new javascript interfaces for PhoneGap
    	appView.addJavascriptInterface(commandManager, "CommandManager");
    	appView.addJavascriptInterface(gap, "DroidGap");
    	appView.addJavascriptInterface(accel, "Accel");
    	appView.addJavascriptInterface(launcher, "GapCam");
    	appView.addJavascriptInterface(mContacts, "ContactHook");
    	appView.addJavascriptInterface(fs, "FileUtil");
    	appView.addJavascriptInterface(netMan, "NetworkManager");
    	appView.addJavascriptInterface(mCompass, "CompassHook");
    	appView.addJavascriptInterface(crypto, "GapCrypto");
    	appView.addJavascriptInterface(mKey, "BackButton");
    	appView.addJavascriptInterface(audio, "GapAudio");
        appView.addJavascriptInterface(callbackServer, "CallbackServer");
    	appView.addJavascriptInterface(new SplashScreen(this), "SplashScreen");
    	
    	if (android.os.Build.VERSION.RELEASE.startsWith("1."))
    	{
            cupcakeStorage = new Storage(appView, this);
        	geo = new GeoBroker(appView, this);
    		appView.addJavascriptInterface(cupcakeStorage, "droidStorage");
        	appView.addJavascriptInterface(geo, "Geo");
    	}
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
		
		Context mCtx;
		public GapClient(Context ctx)
		{
			mCtx = ctx;
		}
		
		@Override
	    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
	        Log.d(LOG_TAG, message);
	        // This shows the dialog box.  This can be commented out for dev
	        AlertDialog.Builder alertBldr = new AlertDialog.Builder(mCtx);
	        GapOKDialog okHook = new GapOKDialog();
	        GapCancelDialog cancelHook = new GapCancelDialog();
	        alertBldr.setMessage(message);
	        alertBldr.setTitle("Alert");
	        alertBldr.setCancelable(true);
	        alertBldr.setPositiveButton("OK", okHook);
	        alertBldr.setNegativeButton("Cancel", cancelHook);
	        alertBldr.show();
	        result.confirm();
	        return true;
	    }
		
		/*
		 * This is the Code for the OK Button
		 */
		
		public class GapOKDialog implements DialogInterface.OnClickListener {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}			
		
		}
		
		public class GapCancelDialog implements DialogInterface.OnClickListener {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}			
		
		}
	  

	}
	
	public final class EclairClient extends GapClient
	{		
		private String TAG = "PhoneGapLog";
		private long MAX_QUOTA = 100 * 1024 * 1024;
		
		public EclairClient(Context ctx) {
			super(ctx);
			// TODO Auto-generated constructor stub
		}
		
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
	
    public class GapViewClient extends WebViewClient {

    	// TODO: hide splash screen here

    	DroidGap mCtx;

        /**
         * Constructor.
         * 
         * @param ctx
         */
        public GapViewClient(DroidGap ctx) {
            mCtx = ctx;
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
        	
        	// Make sure pause event is sent if loading a new url
        	if (mCtx.resumeState) {
        		appView.loadUrl("javascript:try{PhoneGap.onPause.fire();}catch(e){};");
        		mCtx.resumeState = false;
        	}        	

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
        		if (url.startsWith("file://") || mCtx.baseUrl.equals(newBaseUrl)) {
        			appView.loadUrl(url);
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
    	root.addView(appView);
    }
    
    // This is required to start the camera activity!  It has to come from the previous activity
    public void startCamera()
    {
    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, 0);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	   super.onActivityResult(requestCode, resultCode, intent);
    	   
    	   if (resultCode == Activity.RESULT_OK) {
    		   Uri selectedImage = imageUri;
    	       getContentResolver().notifyChange(selectedImage, null);
    	       ContentResolver cr = getContentResolver();
    	       Bitmap bitmap;
    	       try {
    	            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
    	            launcher.processPicture(bitmap);
    	       } catch (Exception e) {
    	    	   launcher.failPicture("Did not complete!");
    	       }
    	    }
    	   else
    	   {
    		   launcher.failPicture("Did not complete!");
    	   }
    }      
}
