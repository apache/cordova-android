/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import android.content.Context;

/**
 * This class listens to the accelerometer sensor and stores the latest 
 * acceleration values x,y,z.
 */
public class AccelListener extends Plugin implements SensorEventListener {

    public static int STOPPED = 0;
    public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    
    private float x,y,z;						        // most recent acceleration values
    private long timestamp;					        // time of most recent value
    private int status;							        // status of listener
    private int accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;

    private SensorManager sensorManager;    // Sensor manager
    private Sensor mSensor;						      // Acceleration sensor returned by sensor manager
    
    private HashMap<String, String> watches = new HashMap<String, String>();
    private List<String> callbacks = new ArrayList<String>();

    /**
     * Create an accelerometer listener.
     */
    public AccelListener() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.timestamp = 0;
        this.setStatus(AccelListener.STOPPED);
     }
    
    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     * 
     * @param ctx The context of the main Activity.
     */
    public void setContext(CordovaInterface ctx) {
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
    	PluginResult.Status status = PluginResult.Status.NO_RESULT;
    	String message = "";
    	PluginResult result = new PluginResult(status, message);
    	result.setKeepCallback(true);	
    	try {
    		if (action.equals("getAcceleration")) {
    			if (this.status != AccelListener.RUNNING) {
    				// If not running, then this is an async call, so don't worry about waiting
    				// We drop the callback onto our stack, call start, and let start and the sensor callback fire off the callback down the road
    				this.callbacks.add(callbackId);
    				this.start();
    			} else {
    				return new PluginResult(PluginResult.Status.OK, this.getAccelerationJSON());
    			}
    		}
    		else if (action.equals("addWatch")) {
    			String watchId = args.getString(0);
    			this.watches.put(watchId, callbackId);
    			if (this.status != AccelListener.RUNNING) {
    				this.start();
    			}
    		}
    		else if (action.equals("clearWatch")) {
    			String watchId = args.getString(0);
    			if (this.watches.containsKey(watchId)) {
    				this.watches.remove(watchId);
    				if (this.size() == 0) {
    					this.stop();
    				}
    			}
    			return new PluginResult(PluginResult.Status.OK);
    		} else {
    			// Unsupported action
    			return new PluginResult(PluginResult.Status.INVALID_ACTION);
    		}
    	} catch (JSONException e) {
    		return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
    	}
    	return result;
    }

    /**
     * Identifies if action to be executed returns a value and should be run synchronously.
     * 
     * @param action	The action to execute
     * @return			T=returns value
     */
    public boolean isSynch(String action) {
    	if (action.equals("getAcceleration") && this.status == AccelListener.RUNNING) {
    		return true;
    	} else if (action.equals("addWatch") && this.status == AccelListener.RUNNING) {
        	return true;
        } else if (action.equals("clearWatch")) {
            return true;
        }
        return false;
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

    private int size() {
    	return this.watches.size() + this.callbacks.size();
    }
    /**
     * Start listening for acceleration sensor.
     * 
     * @return 			status of listener
     */
    private int start() {
    	// If already starting or running, then just return
    	if ((this.status == AccelListener.RUNNING) || (this.status == AccelListener.STARTING)) {
    		return this.status;
    	}
    	
    	this.setStatus(AccelListener.STARTING);
    	
    	// Get accelerometer from sensor manager
    	List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

    	// If found, then register as listener
    	if ((list != null) && (list.size() > 0)) {
    		this.mSensor = list.get(0);
    		this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_UI);
    		this.setStatus(AccelListener.STARTING);
    	} else {
    		this.setStatus(AccelListener.ERROR_FAILED_TO_START);
    		this.fail(AccelListener.ERROR_FAILED_TO_START, "No sensors found to register accelerometer listening to.");
    		return this.status;
    	}
    	
    	// Wait until running
    	long timeout = 2000;
    	while ((this.status == STARTING) && (timeout > 0)) {
    		timeout = timeout - 100;
    		try {
    			Thread.sleep(100);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    	if (timeout == 0) {
    		this.setStatus(AccelListener.ERROR_FAILED_TO_START);
    		this.fail(AccelListener.ERROR_FAILED_TO_START, "Accelerometer could not be started.");
    	}
    	return this.status;
    }

    /**
     * Stop listening to acceleration sensor.
     */
    private void stop() {
        if (this.status != AccelListener.STOPPED) {
            this.sensorManager.unregisterListener(this);
        }
        this.setStatus(AccelListener.STOPPED);
        this.accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
    }

    /**
     * Called when the accuracy of the sensor has changed.
     * 
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	// Only look at accelerometer events
        if (sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        
        // If not running, then just return
        if (this.status == AccelListener.STOPPED) {
            return;
        }
        Log.d("ACCEL", "accuracy is now " + accuracy);
        this.accuracy = accuracy;
    }

    /**
     * Sensor listener event.
     * 
     * @param SensorEvent event
     */
    public void onSensorChanged(SensorEvent event) {
        // Only look at accelerometer events
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        
        // If not running, then just return
        if (this.status == AccelListener.STOPPED) {
            return;
        }
        
        this.setStatus(AccelListener.RUNNING);
        
        if (this.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {

        	// Save time that event was received
        	this.timestamp = System.currentTimeMillis();
        	this.x = event.values[0];
        	this.y = event.values[1];
        	this.z = event.values[2];

        	this.win();

        	if (this.size() == 0) {
        		this.stop();
        	}
        }
    }

    private void fail(int code, String message) {
    	// Error object
    	JSONObject errorObj = new JSONObject();
    	try {
			errorObj.put("code", code);
	    	errorObj.put("message", message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	PluginResult err = new PluginResult(PluginResult.Status.ERROR, errorObj);
    	
        for (String callbackId: this.callbacks)
        {
        	this.error(err, callbackId);
        }
        this.callbacks.clear();
        
        err.setKeepCallback(true);
        
        Iterator it = this.watches.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            this.error(err, (String)pairs.getValue());
        }
    }
    
    private void win() {
    	// Success return object
    	PluginResult result = new PluginResult(PluginResult.Status.OK, this.getAccelerationJSON());
    	
    	for (String callbackId: this.callbacks)
        {
        	this.success(result, callbackId);
        }
        this.callbacks.clear();
        
        result.setKeepCallback(true);
        
        Iterator it = this.watches.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            this.success(result, (String)pairs.getValue());
        }
    }

	private void setStatus(int status) {
        this.status = status;
    }
	
	private JSONObject getAccelerationJSON() {
		JSONObject r = new JSONObject();
		try {
			r.put("x", this.x);
			r.put("y", this.y);
			r.put("z", this.z);
			r.put("timestamp", this.timestamp);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}
}
