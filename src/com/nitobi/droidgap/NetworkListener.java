package com.nitobi.droidgap;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;

public class NetworkListener implements LocationListener {

	private Context mCtx;
	private Location cLoc;
	private LocationManager mLocMan;
	private static final String LOG_TAG = "PhoneGap";
	
	public NetworkListener(Context ctx)
	{
		mCtx = ctx;
		mLocMan = (LocationManager) mCtx.getSystemService(Context.LOCATION_SERVICE);
		mLocMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);
		cLoc = mLocMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
	public Location getLocation()
	{
		return cLoc;
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "The provider " + provider + " is disabled");
	}


	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "The provider "+ provider + " is enabled");
	}


	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "The status of the provider " + provider + " has changed");
		if(status == 0)
		{
			Log.d(LOG_TAG, provider + " is OUT OF SERVICE");
		}
		else if(status == 1)
		{
			Log.d(LOG_TAG, provider + " is TEMPORARILY_UNAVAILABLE");
		}
		else
		{
			Log.d(LOG_TAG, provider + " is Available");
		}
	}


	public void onLocationChanged(Location location) {
		Log.d(LOG_TAG, "The location has been updated!");
		cLoc = location;
	}
}
