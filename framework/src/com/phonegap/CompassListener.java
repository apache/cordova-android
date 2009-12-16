package com.phonegap;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.webkit.WebView;

public class CompassListener implements SensorEventListener{
	WebView mAppView;
	Context mCtx;
	Sensor mSensor;	
	
	private SensorManager sensorManager;
	
	CompassListener(Context ctx, WebView appView)
	{
		mCtx = ctx;
		mAppView = appView;		
		sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
	}
	

	public void start()
	{
		List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
		if (list.size() > 0)
		{
			this.mSensor = list.get(0);
			this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
	}
	
	public void stop()
	{
		this.sensorManager.unregisterListener(this);
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}


	public void onSensorChanged(SensorEvent event) {
		// We only care about the orientation as far as it refers to Magnetic North
		float heading = event.values[0];
		mAppView.loadUrl("javascript:navigator.compass.setHeading(" + heading + ")");
	}
}
