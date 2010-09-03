package com.phonegap;

import android.webkit.WebView;

/**
 * This class represents a PhoneGap module and should be extended by all modules
 * that provide functionality to PhoneGap.  If the module invokes an activity and
 * expects a result back (see CameraLauncher), then it should extend ActivityResultModule
 * instead.
 */
public abstract class Module {
	
	protected DroidGap gap;		// DroidGap object
	protected WebView view;		// WebView object
	
	/**
	 * Constructor.
	 * 
	 * @param view
	 * @param gap
	 */
	public Module(WebView view, DroidGap gap) {
		this.gap = gap;
		this.view = view;
	}

    /**
     * Called when the system is about to start resuming a previous activity. 
     */
    public void onPause() {
    }

    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    }
    
    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
    }

    /**
     * Send JavaScript statement back to JavaScript.
     * 
     * @param message
     */
    public void sendJavascript(String statement) {
    	System.out.println("Module.sendResponse("+statement+")");
    	this.gap.callbackServer.sendJavascript(statement);
    }

}
