package com.nitobi.phonegap;
/* License (MIT)
 * Copyright (c) 2008 Nitobi
 * website: http://phonegap.com
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import java.io.IOException;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;

public class PhoneGap{
	
	private static final String LOG_TAG = "PhoneGap";
	/*
	 * UUID, version and availability	
	 */
	public boolean droid = true;
	private String version = "0.1";	
    private Context mCtx;    
    private Handler mHandler;
    private WebView mAppView;    
    private GpsListener mGps;
    private NetworkListener mNetwork;
    
	public PhoneGap(Context ctx, Handler handler, WebView appView) {
        this.mCtx = ctx;
        this.mHandler = handler;
        this.mAppView = appView;
        mGps = new GpsListener(ctx);
        mNetwork = new NetworkListener(ctx);
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
    			GeoTuple geoloc = new GeoTuple();
    			Location loc = mGps.hasLocation() ?  mGps.getLocation() : mNetwork.getLocation();
    			if (loc != null)
        		{
        			geoloc.lat = loc.getLatitude();
        			geoloc.lng = loc.getLongitude();
        			geoloc.ele = loc.getAltitude();
        		}
        		else
        		{
        			geoloc.lat = 0;
        			geoloc.lng = 0;
        			geoloc.ele = 0;
        		}
        		mAppView.loadUrl("javascript:gotLocation(" + geoloc.lat + ", " + geoloc.lng + ")");
            }
        });
	}
	
	public void playSound(final String filename)
	{
		MediaPlayer mp = new MediaPlayer();
		
		try {
			mp.setDataSource("file:///android_asset/" + filename);
			mp.prepare();
			mp.start();
		} catch (IllegalArgumentException e) {
			//TO-DO: Load a Javascript Exception thrower
		} catch (IllegalStateException e) {
			//TO-DO: Load a Javascript Exception thrower
		} catch (IOException e) {
			//TO-DO: Load a Javascript Exception thrower
		}
		
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
