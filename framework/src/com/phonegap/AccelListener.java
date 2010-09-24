package com.phonegap;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;

/**
 * This class listens to the accelerometer sensor and stores the latest 
 * acceleration values x,y,z.
 */
public class AccelListener implements SensorEventListener, Plugin{

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    
    public float TIMEOUT = 30000;		// Timeout in msec to shut off listener

    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object
    
    float x,y,z;						// most recent acceleration values
    long timestamp;						// time of most recent value
    int status;							// status of listener
    long lastAccessTime;				// time the value was last retrieved

    private SensorManager sensorManager;// Sensor manager
    Sensor mSensor;						// Acceleration sensor returned by sensor manager

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
	public void setContext(DroidGap ctx) {
		this.ctx = ctx;
        this.sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * Sets the main View of the application, this is the WebView within which 
	 * a PhoneGap app runs.
	 * 
	 * @param webView The PhoneGap WebView
	 */
	public void setView(WebView webView) {
		this.webView = webView;
	}

	/**
	 * Executes the request and returns CommandResult.
	 * 
	 * @param action The command to execute.
	 * @param args JSONArry of arguments for the command.
	 * @return A CommandResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("getStatus")) {
				int i = this.getStatus();
				return new PluginResult(status, i);
			}
			else if (action.equals("start")) {
				int i = this.start();
				return new PluginResult(status, i);
			}
			else if (action.equals("stop")) {
				this.stop();
				return new PluginResult(status, 0);
			}
			else if (action.equals("getAcceleration")) {
				// If not running, then this is an async call, so don't worry about waiting
				if (this.status != AccelListener.RUNNING) {
					int r = this.start();
					if (r == AccelListener.ERROR_FAILED_TO_START) {
						return new PluginResult(PluginResult.Status.IO_EXCEPTION, AccelListener.ERROR_FAILED_TO_START);
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
				JSONObject r = new JSONObject();
				r.put("x", this.x);
				r.put("y", this.y);
				r.put("z", this.z);
				// TODO: Should timestamp be sent?
				r.put("timestamp", this.timestamp);
				return new PluginResult(status, r);
			}
			else if (action.equals("setTimeout")) {
				try {
					float timeout = Float.parseFloat(args.getString(0));
					this.setTimeout(timeout);
					return new PluginResult(status, 0);
				} catch (NumberFormatException e) {
					status = PluginResult.Status.INVALID_ACTION;
					e.printStackTrace();
				} catch (JSONException e) {
					status = PluginResult.Status.JSON_EXCEPTION;
					e.printStackTrace();
				}
			}
			else if (action.equals("getTimeout")) {
				float f = this.getTimeout();
				return new PluginResult(status, f);
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
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
		else if (action.equals("getAcceleration")) {
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
     * Called when the system is about to start resuming a previous activity. 
     */
    public void onPause() {
    }

    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {
    	this.stop();    	
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

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
            this.setStatus(AccelListener.STARTING);
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        // If error, then set status to error
        else {
            this.setStatus(AccelListener.ERROR_FAILED_TO_START);
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
        this.setStatus(AccelListener.STOPPED);
    }

    /**
     * Called when the accuracy of the sensor has changed.
     * 
     * @param sensor
     * @param accuracy
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
        
        // Save time that event was received
        this.timestamp = System.currentTimeMillis();
        this.x = event.values[0];
        this.y = event.values[1];
        this.z = event.values[2];            

        this.setStatus(AccelListener.RUNNING);

        // If values haven't been read for TIMEOUT time, then turn off accelerometer sensor to save power
		if ((this.timestamp - this.lastAccessTime) > this.TIMEOUT) {
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
	
	/**
	 * Set the status and send it to JavaScript.
	 * @param status
	 */
	private void setStatus(int status) {
		this.status = status;
	}
    
}
