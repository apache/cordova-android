/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 */
package com.phonegap;

import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;

/*
 * This class literally exists to protect DroidGap from Javascript directly.
 * 
 * 
 */

public class BrowserKey {

	DroidGap mAction;
	boolean bound;
	WebView mView;
	
	BrowserKey(WebView view, DroidGap action)
	{
		bound = false;
		mAction = action;
	}
	
	public void override()
	{
		Log.d("PhoneGap", "WARNING: Back Button Default Behaviour will be overridden.  The backKeyDown event will be fired!");
		bound = true;
	}
	
	public boolean isBound()
	{
		return bound;
	}
	
	public void reset()
	{
		bound = false;
	}
	
	public void exitApp()
	{
		mAction.finish();
	}
}
