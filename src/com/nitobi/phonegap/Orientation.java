package com.nitobi.phonegap;


import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.SensorListener;
import android.webkit.WebView;

public class Orientation implements SensorListener{

	private WebView mAppView;
    private SensorManager sensorManager;
	private Context mCtx;
    
	Orientation(WebView kit, Context ctx) {
		mAppView = kit;
		mCtx = ctx;
        sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
        this.resumeAccel();
	}
	
	public void onSensorChanged(int sensor, final float[] values) {
		if (sensor != SensorManager.SENSOR_ACCELEROMETER || values.length < 3)
			return;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        mAppView.loadUrl("javascript:gotAcceleration(" + x + ", " + y + "," + z + ")");
	}

	public void onAccuracyChanged(int arg0, int arg1) {
		// This is a stub method.
		
	}

	public void pauseAccel()
	{
        sensorManager.unregisterListener(this);	
	}
	
	public void resumeAccel()
	{
		sensorManager.registerListener(this, 
				   SensorManager.SENSOR_ACCELEROMETER,
				   SensorManager.SENSOR_DELAY_GAME);
	}
	
}
