package com.android.droidgap;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.webkit.WebView;

public class PhoneGap {
	
	/*
	 * UUID, version and availability	
	 */
	public boolean droid = true;
	private String version = "0.1";	
    private Context mCtx;    
    private Handler mHandler;
    private WebView mAppView;    

	public PhoneGap(Context ctx, Handler handler, WebView appView) {
        this.mCtx = ctx;
        this.mHandler = handler;
        this.mAppView = appView;
    }
	

	public void updateAccel(){
		mHandler.post(new Runnable() {
			public void run() {
				int accelX = SensorManager.DATA_X;
				int accelY = SensorManager.DATA_Y;
				int accelZ = SensorManager.DATA_Z;
        		mAppView.loadUrl("javascript:gotAcceleration(" + accelX + ", " + accelY + "," + accelZ + ")");
			}			
		});
				
	}
	
	public void takePhoto(){
		// TO-DO: Figure out what this should do
	}
	
	public void playSound(){
		// TO-DO: Figure out what this should do
	}
	
	public void vibrate(long pattern){
        // Start the vibration
        Vibrator vibrator = (Vibrator) mCtx.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern);
	}
	
	/*
	 * Android requires a provider, since it can fall back on triangulation and other means as well as GPS	
	 */
	
	public void getLocation(final String provider){
		mHandler.post(new Runnable() {
            public void run() {
        		LocationManager locMan = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);		
        		Location myLoc = (Location) locMan.getLastKnownLocation(provider);
        		GeoTuple geoloc = new GeoTuple();
        		geoloc.lat = myLoc.getLatitude();
        		geoloc.lng = myLoc.getLongitude();
        		geoloc.ele = myLoc.getAltitude();
        		mAppView.loadUrl("javascript:gotLocation(" + geoloc.lat + ", " + geoloc.lng + ")");
            }
        });
	}
	
	public String outputText(){
		String test = "<p>Test</p>";
		return test;
	}
	

	public String getUuid()
	{

		TelephonyManager operator = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
		String uuid = operator.getDeviceId();
		return uuid;
	}
	
	public String getVersion()
	{
		return version;
	}	
	
	public boolean exists()
	{
		return true;	
	}
	
}
