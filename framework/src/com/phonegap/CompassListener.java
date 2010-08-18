package com.phonegap;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.webkit.WebView;

/**
 * This class listens to the compass sensor and calls navigator.compass.setHeading(heading)
 * method in Javascript every sensor change event it receives.
 */
public class CompassListener implements SensorEventListener{

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
	
    WebView mAppView;					// WebView object
    DroidGap mCtx;						// Activity (DroidGap) object

    int status;							// status of listener
	
    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;						// Compass sensor returned by sensor manager
	
	/**
	 * Constructor.
	 * 
	 * @param appView
	 * @param ctx			The Activity (DroidGap) object
	 */
	CompassListener(WebView appView, Context ctx)
	{
		this.mCtx = (DroidGap)ctx;
		this.mAppView = appView;
		this.sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
	}
	
    /**
     * Start listening for compass sensor.
     * 
     * @return 			status of listener
     */
	public int start() {

		// Get accelerometer from sensor manager
		List<Sensor> list = this.sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

        // If found, then register as listener
		if (list.size() > 0) {
			this.mSensor = list.get(0);
			this.sensorManager.registerListener(this, this.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
            this.status = CompassListener.STARTING;
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

		this.status = CompassListener.RUNNING;
		
		// TODO This is very expensive to process every event. Should this use polling from JS instead?
        mCtx.sendJavascript("navigator.compass.setHeading(" + heading + ");");
	}
}
