package com.phonegap;

import android.content.Intent;
import android.webkit.WebView;

/**
 * This class should be extended by a PhoneGap module to register an onActivityResult() callback
 * with DroidGap.  It allows modules to start activities with result callback without having to
 * modify DroidGap.java.
 */
public abstract class ActivityResultModule extends Module {
	
	public int requestCode;
	
	/**
	 * Constructor.
	 * 
	 * @param view
	 * @param gap
	 */
	public ActivityResultModule(WebView view, DroidGap gap) {
		super(view, gap);
		this.requestCode = gap.addActivityResult(this);
	}

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }
    
    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits, 
     * your onActivityResult() method will be called.
     *  
     * @param intent
     */
    public void startActivityForResult(Intent intent) {
    	this.gap.startActivityForResult(intent, this.requestCode);
    }


}
