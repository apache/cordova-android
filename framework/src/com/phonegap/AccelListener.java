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
public class AccelListener implements SensorEventListener{

    WebView mAppView;					// WebView object
    DroidGap mCtx;						// DroidGap object
    
    float x,y,z;						// most recent acceleration values
    long timeStamp;						// time of most recent value
    int status;							// status of listener

    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;						// Acceleration sensor returned by sensor manager

    /**
     * Create an accelerometer listener.
     * 
     * @param ctx		The Activity (DroidGap) object
     * @param appView
     */
    public AccelListener(DroidGap ctx, WebView appView) {
        this.mCtx = ctx;
        this.mAppView = appView;
        this.sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.timeStamp = 0;
        this.status = AccelBroker.STOPPED;
    }

    /**
     * Start listening for acceleration sensor.
     * 
     * @return 			status of listener
     */
    public int start() {
        
        // Get accelerometer from sensor manager
        List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        //list = null; // @test failure AccelBroker.ERROR_FAILED_TO_START
        
        // If found, then register as listener
        if ((list != null) && (list.size() > 0)) {
            this.mSensor = list.get(0);
            this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            this.status = AccelBroker.STARTING;
        }
        
        // If error, then set status to error
        else {
            this.status = AccelBroker.ERROR_FAILED_TO_START;
        }
        
        return this.status;
    }

    /**
     * Stop listening to acceleration sensor.
     */
    public void stop() {
        if (this.status == AccelBroker.RUNNING) {
        	this.sensorManager.unregisterListener(this);
        }
        this.status = AccelBroker.STOPPED;
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void destroy() {
    	this.sensorManager.unregisterListener(this);    	
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

        this.status = AccelBroker.RUNNING;
    }

}
