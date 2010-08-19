package com.phonegap;

import java.util.HashMap;

import android.webkit.WebView;

/**
 * This class manages access to the accelerometer from JavaScript.
 * One, free running accelerometer listener is created.  
 * It's state is controlled by start(id) and stop(id).
 * JavaScript is responsible for starting, stopping, and retrieving status and values.
 * When all listener ids that were started are stopped, the accelerometer listener is stopped.
 */
public class AccelBroker {

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    public static int ERROR_NOT_FOUND = 4;
	
    private WebView mAppView;								// WebView object
    private DroidGap mCtx;									// DroidGap object
    private AccelListener listener;							// Accelerator listener
    private HashMap<String,Integer> listenerIds;			// List of listener ids

    /**
     * Constructor
     * 
     * @param view
     * @param ctx
     */
    public AccelBroker(WebView view, DroidGap ctx)
    {
        mCtx = ctx;
        mAppView = view;

        // Create listener
       	listener = new AccelListener(mCtx, mAppView);
       	
       	listenerIds = new HashMap<String,Integer>();
    }

    /**
     * Start listening to acceleration sensor.
     * 
     * @param String id		The id of the listener
     * @return				true if started, false if not
     */
    public boolean start(String id)
    {     
    	// Track start for listener
    	listenerIds.put(id, 1);
    	
        // Start listener if necessary
        if ((listener.status != AccelBroker.RUNNING) && (listener.status != AccelBroker.STARTING)) {
        	listener.start();
        }
        
        return ((listener.status == AccelBroker.RUNNING) || (listener.status == AccelBroker.STARTING));
    }

    /**
     * Stop listening for acceleration sensor.
     * 
     * @param String id		The id of the listener
     * @return 				true if stopped, false if not
     */
    public boolean stop(String id)
    {   	
    	// Stop tracking listener
    	if (listenerIds.containsKey(id)) {
    		listenerIds.remove(id);
    	}
    	
    	// If no more listeners, then stop accelerometer
    	if (listenerIds.isEmpty()) {
    		listener.stop();
    	}
    	
    	return (listener.status == AccelBroker.STOPPED);
    }
    
    /**
     * Destroy listener
     */
    public void destroy() {
    	listener.destroy();
    }
        
    /**
     * Get result of the last request or update if watching.
     * If sensor is still starting, wait until 1st value is acquired before returning.
     * 
     * NOTE: NOT USED - DO WE NEED THIS, SINCE JSON MUST BE PARSED ON JS SIDE?
     * 
     * @param key		listener id
     * @return 			String representation of JSON object 
     */
    public String getResult() {
    	        	
       	// If acceleration values
       	if (listener.status == AccelBroker.RUNNING) {
       		return "{status:" + listener.status + ",value:{x:" + listener.x + ", y:" + listener.y + ", z:" + listener.z + "}}";
       	}

       	// If error or not running
   		return "{status:" + listener.status + ",value:null}"; 
    }
    
    /**
     * Get status of accelerometer sensor.
     * 
     * @return			status
     */
    public int getStatus() {
   		return listener.status;
    }
        
    /**
     * Get X value of last accelerometer value.
     * 
     * @return			x value
     */
    public float getX() {
   		return listener.x;
    }
    
    /**
     * Get Y value of last accelerometer value.
     * 
     * @return			y value
     */
    public float getY() {
   		return listener.y;
	}

    /**
     * Get Z value of last accelerometer value.
     * 
     * @return			z value
     */
    public float getZ() {
   		return listener.x;
	}

}
