package com.phonegap;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.webkit.WebView;

/**
 * This class listens to the compass sensor and stores the latest heading value.
 */
public class CompassListener implements SensorEventListener{

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    
    public float TIMEOUT = 30000;		// Timeout in msec to shut off listener
	
    WebView mAppView;					// WebView object
    DroidGap mCtx;						// Activity (DroidGap) object

    int status;							// status of listener
    float heading;						// most recent heading value
    long timeStamp;						// time of most recent value
    long lastAccessTime;				// time the value was last retrieved
	
    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;						// Compass sensor returned by sensor manager
	
	/**
	 * Constructor.
	 * 
	 * @param appView
	 * @param ctx			The Activity (DroidGap) object
	 */
	CompassListener(WebView appView, DroidGap ctx)
	{
		this.mCtx = ctx;
		this.mAppView = appView;
		this.sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
        this.timeStamp = 0;
        this.status = CompassListener.STOPPED;
	}
	
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
            this.status = CompassListener.STARTING;
            this.lastAccessTime = System.currentTimeMillis();
		}

		// If error, then set status to error
        else {
            this.status = CompassListener.ERROR_FAILED_TO_START;
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
        this.status = CompassListener.STOPPED;
	}
	
    /**
     * Called when listener is to be shut down and object is being destroyed.
     */
	public void destroy() {
		this.stop();
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
		this.status = CompassListener.RUNNING;

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
	public void setTimeout(float timeout) {
		this.TIMEOUT = timeout;
	}
	
	/**
	 * Get the timeout to turn off compass sensor if getHeading() hasn't been called.
	 * 
	 * @return timeout in msec
	 */
	public float getTimeout() {
		return this.TIMEOUT;
	}
}
