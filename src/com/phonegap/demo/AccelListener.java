package com.phonegap.demo;

import static android.hardware.SensorManager.DATA_X;
import static android.hardware.SensorManager.DATA_Y;
import static android.hardware.SensorManager.DATA_Z;
import android.hardware.SensorManager;
import android.content.Context;
import android.hardware.SensorListener;
import android.webkit.WebView;

public class AccelListener implements SensorListener{

	WebView mAppView;
	Context mCtx;
	String mKey;
	int mTime = 10000;
	boolean started = false;
	
	private SensorManager sensorManager;
	
	private long lastUpdate = -1;
	
	AccelListener(Context ctx, WebView appView)
	{
		mCtx = ctx;
		mAppView = appView;		
		sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
	}
	
	public void start(int time)
	{
		mTime = time;
		if (!started)
		{
			sensorManager.registerListener(this,
	            SensorManager.SENSOR_ACCELEROMETER,
	            SensorManager.SENSOR_DELAY_GAME);
		}
	}
	
	public void stop()
	{
		if(started)
			sensorManager.unregisterListener(this);
	}
	
	public void onAccuracyChanged(int sensor, int accuracy) {
		// This should call the FAIL method
	}
	
	public void onSensorChanged(int sensor, float[] values) {
		if (sensor != SensorManager.SENSOR_ACCELEROMETER || values.length < 3)
		      return;
		long curTime = System.currentTimeMillis();
		if (lastUpdate == -1 || (curTime - lastUpdate) > mTime) {
			
			lastUpdate = curTime;
			
			float x = values[DATA_X];
			float y = values[DATA_Y];
			float z = values[DATA_Z];
			mAppView.loadUrl("javascript:gotAccel(" + x +  ", " + y + "," + z + " )");
		}
	}
	
	
}
