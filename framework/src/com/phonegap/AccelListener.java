package com.phonegap;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.webkit.WebView;

/**
 * This class listens to the accelerometer sensor and stores the latest 
 * acceleration values x,y,z.
 */
public class AccelListener extends Module implements SensorEventListener{

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    
    public float TIMEOUT = 30000;		// Timeout in msec to shut off listener

    WebView mAppView;					// WebView object
    DroidGap mCtx;						// DroidGap object
    
    float x,y,z;						// most recent acceleration values
    long timeStamp;						// time of most recent value
    int status;							// status of listener
    long lastAccessTime;				// time the value was last retrieved

    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;						// Acceleration sensor returned by sensor manager

    /**
     * Create an accelerometer listener.
     * 
     * @param ctx		The Activity (DroidGap) object
     * @param appView
     */
    public AccelListener(WebView appView, DroidGap ctx) {
    	super(appView, ctx);
        this.mCtx = ctx;
        this.mAppView = appView;
        this.sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.timeStamp = 0;
        this.status = AccelListener.STOPPED;
    }

    /**
     * Start listening for acceleration sensor.
     * 
     * @return 			status of listener
     */
    public int start() {

		// If already starting or running, then just return
        if ((this.status == AccelListener.RUNNING) || (this.status == AccelListener.STARTING)) {
        	return this.status;
        }

        // Get accelerometer from sensor manager
        List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        
        // If found, then register as listener
        if ((list != null) && (list.size() > 0)) {
            this.mSensor = list.get(0);
            this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            this.status = AccelListener.STARTING;
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        // If error, then set status to error
        else {
            this.status = AccelListener.ERROR_FAILED_TO_START;
        }
        
        return this.status;
    }

    /**
     * Stop listening to acceleration sensor.
     */
    public void stop() {
        if (this.status != AccelListener.STOPPED) {
        	this.sensorManager.unregisterListener(this);
        }
        this.status = AccelListener.STOPPED;
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    @Override
    public void onDestroy() {
    	super.onDestroy();
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
    	
    	// Only look at accelerometer events
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        
        // Save time that event was received
        this.timeStamp = System.currentTimeMillis();
        this.x = event.values[0];
        this.y = event.values[1];
        this.z = event.values[2];            

        this.status = AccelListener.RUNNING;

        // If values haven't been read for TIMEOUT time, then turn off accelerometer sensor to save power
		if ((this.timeStamp - this.lastAccessTime) > this.TIMEOUT) {
			this.stop();
		}		
    }

    /**
     * Get status of accelerometer sensor.
     * 
     * @return			status
     */
	public int getStatus() {
		return this.status;
	}
	
    /**
     * Get X value of last accelerometer value.
     * 
     * @return			x value
     */
    public float getX() {
        this.lastAccessTime = System.currentTimeMillis();
   		return this.x;
    }
    
    /**
     * Get Y value of last accelerometer value.
     * 
     * @return			y value
     */
    public float getY() {
        this.lastAccessTime = System.currentTimeMillis();
   		return this.y;
	}

    /**
     * Get Z value of last accelerometer value.
     * 
     * @return			z value
     */
    public float getZ() {
        this.lastAccessTime = System.currentTimeMillis();
   		return this.x;
	}
	
	/**
	 * Set the timeout to turn off accelerometer sensor if getX() hasn't been called.
	 * 
	 * @param timeout		Timeout in msec.
	 */
	public void setTimeout(float timeout) {
		this.TIMEOUT = timeout;
	}
	
	/**
	 * Get the timeout to turn off accelerometer sensor if getX() hasn't been called.
	 * 
	 * @return timeout in msec
	 */
	public float getTimeout() {
		return this.TIMEOUT;
	}
    
}
