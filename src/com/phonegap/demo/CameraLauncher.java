package com.phonegap.demo;

import android.webkit.WebView;


public class CameraLauncher {
		
	private WebView mAppView;
	private DroidGap mGap;
	int quality;	
	
	CameraLauncher(WebView view, DroidGap gap)
	{
		mAppView = view;
		mGap = gap;
	}
	
	public void takePicture(int quality)
	{
		mGap.startCamera(quality);
	}
	
	/* Return Base64 Encoded String to Javascript */
	public void processPicture( String js_out )
	{		
		mAppView.loadUrl("javascript:navigator.camera.win('" + js_out + "');");			
	}
	
	public void failPicture(String err)
	{
		mAppView.loadUrl("javascript:navigator.camera.fail('" + err + "');");
	}
	
}
