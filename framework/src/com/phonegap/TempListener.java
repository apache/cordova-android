/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import java.util.List;

import org.json.JSONArray;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

public class TempListener extends Plugin implements SensorEventListener {
	
    Sensor mSensor;	
	private SensorManager sensorManager;
	
	/**
	 * Constructor.
	 */
	public TempListener() {
	}

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(DroidGap ctx) {
		super.setContext(ctx);
        this.sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		if (action.equals("start")) {
			this.start();
		}
		else if (action.equals("stop")) {
			this.stop();
		}
		return new PluginResult(status, result);
	}
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {
    	this.stop();    	
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	public void start()	{
		List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_TEMPERATURE);
		if (list.size() > 0) {
			this.mSensor = list.get(0);
			this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	public void stop() {
		this.sensorManager.unregisterListener(this);
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
		// We want to know what temp this is.
		float temp = event.values[0];
		this.sendJavascript("gotTemp(" + temp + ");");
	}

}