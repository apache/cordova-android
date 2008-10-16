package com.android.droidgap;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;

public class PhoneGap {
	
	public static GeoTuple location;
	public static AccelTuple accel;
	public String uuid = getDeviceId();
	public static String version = "0.1";
	
    private Context mCtx;    

	public PhoneGap(Context ctx) {
        this.mCtx = ctx;
    }
	
	public void updateAccel() {
		accel.accelX = SensorManager.DATA_X;
		accel.accelY = SensorManager.DATA_Y;
		accel.accelZ = SensorManager.DATA_Z;		
	}
	
	public void takePhoto(){
		
	}
	
	public void playSound(){
	
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
	
	private String getDeviceId(){
		TelephonyManager operator = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
		String uniqueId = operator.getDeviceId();
		return uniqueId;
	}	
}
