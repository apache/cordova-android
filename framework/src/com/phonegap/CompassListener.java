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
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

/**
 * This class listens to the compass sensor and stores the latest heading value.
 */
public class CompassListener extends Plugin implements SensorEventListener {

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    
    public long TIMEOUT = 30000;		// Timeout in msec to shut off listener
	
    int status;							// status of listener
    float heading;						// most recent heading value
    long timeStamp;						// time of most recent value
    long lastAccessTime;				// time the value was last retrieved
	
    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;						// Compass sensor returned by sensor manager
	
	/**
	 * Constructor.
	 */
	public CompassListener() {
        this.timeStamp = 0;
        this.setStatus(CompassListener.STOPPED);
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
		
		try {
			if (action.equals("start")) {
				this.start();
			}
			else if (action.equals("stop")) {
				this.stop();
			}
			else if (action.equals("getStatus")) {
				int i = this.getStatus();
				return new PluginResult(status, i);
			}
			else if (action.equals("getHeading")) {
				// If not running, then this is an async call, so don't worry about waiting
				if (this.status != RUNNING) {
					int r = this.start();
					if (r == ERROR_FAILED_TO_START) {
						return new PluginResult(PluginResult.Status.IO_EXCEPTION, ERROR_FAILED_TO_START);
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
						return new PluginResult(PluginResult.Status.IO_EXCEPTION, AccelListener.ERROR_FAILED_TO_START);						
					}
				}
				float f = this.getHeading();
				return new PluginResult(status, f);
			}
			else if (action.equals("setTimeout")) {
				this.setTimeout(args.getLong(0));
			}
			else if (action.equals("getTimeout")) {
				long l = this.getTimeout();
				return new PluginResult(status, l);
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		if (action.equals("getStatus")) {
			return true;
		}
		else if (action.equals("getHeading")) {
			// Can only return value if RUNNING
			if (this.status == RUNNING) {
				return true;
			}
		}
		else if (action.equals("getTimeout")) {
			return true;
		}
		return false;
	}
    
    /**
     * Called when listener is to be shut down and object is being destroyed.
     */
	public void onDestroy() {
		this.stop();
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Start listening for compass sensor.
     * 
     * @return 			status of listener
     */
	public int start() {
		
		// If already starting or running, then just return
        if ((this.status == CompassListener.RUNNING) || (this.status == CompassListener.STARTING)) {
        	return this.status;
        }

		// Get accelerometer from sensor manager
		List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

        // If found, then register as listener
		if (list.size() > 0) {
			this.mSensor = list.get(0);
			this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            this.lastAccessTime = System.currentTimeMillis();
            this.setStatus(CompassListener.STARTING);
		}

		// If error, then set status to error
        else {
            this.setStatus(CompassListener.ERROR_FAILED_TO_START);
        }
        
        return this.status;
	}
	
    /**
     * Stop listening to compass sensor.
     */
	public void stop() {
        if (this.status != CompassListener.STOPPED) {
        	this.sensorManager.unregisterListener(this);
        }
        this.setStatus(CompassListener.STOPPED);
	}
	
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub	
	}

    /**
     * Sensor listener event.
     * 
     * @param SensorEvent event
     */
	public void onSensorChanged(SensorEvent event) {

		// We only care about the orientation as far as it refers to Magnetic North
		float heading = event.values[0];

		// Save heading
        this.timeStamp = System.currentTimeMillis();
		this.heading = heading;
		this.setStatus(CompassListener.RUNNING);

		// If heading hasn't been read for TIMEOUT time, then turn off compass sensor to save power
		if ((this.timeStamp - this.lastAccessTime) > this.TIMEOUT) {
			this.stop();
		}
	}
	
    /**
     * Get status of compass sensor.
     * 
     * @return			status
     */
	public int getStatus() {
		return this.status;
	}
	
	/**
	 * Get the most recent compass heading.
	 * 
	 * @return			heading
	 */
	public float getHeading() {
        this.lastAccessTime = System.currentTimeMillis();
		return this.heading;
	}
	
	/**
	 * Set the timeout to turn off compass sensor if getHeading() hasn't been called.
	 * 
	 * @param timeout		Timeout in msec.
	 */
	public void setTimeout(long timeout) {
		this.TIMEOUT = timeout;
	}
	
	/**
	 * Get the timeout to turn off compass sensor if getHeading() hasn't been called.
	 * 
	 * @return timeout in msec
	 */
	public long getTimeout() {
		return this.TIMEOUT;
	}

	/**
	 * Set the status and send it to JavaScript.
	 * @param status
	 */
	private void setStatus(int status) {
		this.status = status;
	}

}
