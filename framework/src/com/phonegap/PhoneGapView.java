package com.phonegap;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;


public class PhoneGapView extends WebView {

	private Activity mCtx;
	private PhoneGap gap;
	private GeoBroker geo;
	private AccelListener accel;	
	private ContactManager mContacts;
	private FileUtils fs;
	private NetworkManager netMan;
	private CameraLauncher launcher;
	
	public PhoneGapView(Activity action){
		super((Context) action);
		mCtx = action;
	}
	
	public void loadUrl(String url)
	{
		super.loadUrl(url);
		bindBrowser();
	}
	
    private void bindBrowser()
    {
    	gap = new PhoneGap(mCtx, this);
    	geo = new GeoBroker(this, mCtx);
    	accel = new AccelListener(mCtx, this);
    	mContacts = new ContactManager(mCtx, this);
    	fs = new FileUtils(this);
    	netMan = new NetworkManager(mCtx, this);
    	
    	// This creates the new javascript interfaces for PhoneGap
    	this.addJavascriptInterface(gap, "DroidGap");
    	this.addJavascriptInterface(geo, "Geo");
    	this.addJavascriptInterface(accel, "Accel");
    	this.addJavascriptInterface(mContacts, "ContactHook");
    	this.addJavascriptInterface(fs, "FileUtil");
    	this.addJavascriptInterface(netMan, "NetworkManager");
    }    

    // This is required to start the camera activity!  It has to come from the previous activity
    public void startCamera(int quality)
    {
    	Intent i = new Intent(mCtx, CameraPreview.class);
    	i.setAction("android.intent.action.PICK");
    	i.putExtra("quality", quality);
    	mCtx.startActivityForResult(i, 0);
    }
    
    protected void processResult(int requestCode, int resultCode, Intent intent)
    {
    	String data;
    	if (resultCode == mCtx.RESULT_OK)
    	{
    		data = intent.getStringExtra("picture");    	     
    		// Send the graphic back to the class that needs it
    		launcher.processPicture(data);
    	}
    	else
    	{
    		launcher.failPicture("Did not complete!");
    	}
    }
    
}
