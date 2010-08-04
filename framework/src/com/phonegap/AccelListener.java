package com.phonegap;


import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.webkit.WebView;

public class AccelListener implements SensorEventListener{

	WebView mAppView;
	Context mCtx;
	String mKey;
	Sensor mSensor;
	int mTime = 10000;
	boolean started = false;
	
	private SensorManager sensorManager;
	
	private long lastUpdate = -1;
	
	public AccelListener(String key, int freq, Context ctx, WebView appView)
	{
		mCtx = ctx;
		mAppView = appView;		
		mKey = key;
		mTime = freq;
		sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
		
	}
	
	public void start(int time)
	{
		mTime = time;
		List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (list.size() > 0)
		{
			this.mSensor = list.get(0);
			this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_FASTEST);
		}
		else
		{
			mAppView.loadUrl("javascript:navigator.accelerometer.epicFail(" + mKey + ", 'Failed to start')");
		}
	}
	
	public void stop()
	{
		if(started)
			sensorManager.unregisterListener(this);
	}
	
	

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {		
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		long curTime = System.currentTimeMillis();
		if (lastUpdate == -1 || (curTime - lastUpdate) > mTime) {		
			lastUpdate = curTime;
				
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];			
			//mAppView.loadUrl("javascript:gotAccel(" + x +  ", " + y + "," + z + " )");
			mAppView.loadUrl("javascript:navigator.accelerometer.gotCurrentAcceleration(" +  mKey + "," + x + "," + y + "," + z + ")");
		}		
	}
	
	
}
