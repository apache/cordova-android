package com.android.droidgap;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;

public class PhoneGap {
	
	public GeoTuple location;
	public AccelTuple accel;
	
    private Context mCtx;    

	public PhoneGap(Context ctx) {
        this.mCtx = ctx;
    }
	
	public void updateAccel(AccelTuple accel){
		accel.accelX = SensorManager.DATA_X;
		accel.accelY = SensorManager.DATA_Y;
		accel.accelZ = SensorManager.DATA_Z;		
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
	
	public void getLocation(String provider){
		LocationManager locMan = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);		
		Location myLoc = (Location) locMan.getLastKnownLocation(provider);
		location.lat = myLoc.getLatitude();
		location.lng = myLoc.getLongitude();
		location.ele = myLoc.getAltitude();
	}
	
	public String outputText(){
		String test = "<p>Test</p>";
		return test;
	}
}
