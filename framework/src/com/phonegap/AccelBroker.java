package com.phonegap;

import android.content.Context;
import android.webkit.WebView;

/**
 * This class manages access to the accelerometer from JavaScript.
 * One, free running accelerometer listener is created.  
 * It's state is controlled by start() and stop().
 * JavaScript is responsible for starting, stopping, and retrieving status and values.
 * 
 * Since there may be some delay between starting and the first available value, when
 * retrieving values from JavaScript, the thread sleeps until the first value is 
 * received or until 1 sec elapses.
 * 
 * @author bcurtis
 *
 */
public class AccelBroker {

	public static int STOPPED = 0;
	public static int STARTING = 1;
    public static int RUNNING = 2;
    public static int ERROR_FAILED_TO_START = 3;
    public static int ERROR_NOT_FOUND = 4;
	
    private WebView mAppView;								// WebView object
    private Context mCtx;									// Activity (DroidGap) object
    private AccelListener listener;							// Accelerator listener

    /**
     * Constructor
     * 
     * @param view
     * @param ctx
     */
    public AccelBroker(WebView view, Context ctx)
    {
        mCtx = ctx;
        mAppView = view;

        // Create listener
       	listener = new AccelListener(mCtx, mAppView);
    }

    /**
     * Start listening to acceleration sensor.
     * 
     * @return		true if started, false if not
     */
    public boolean start()
    {        
        // Start listener if necessary
        if ((listener.status != AccelBroker.RUNNING) && (listener.status != AccelBroker.STARTING)) {
        	listener.start();
        }
        
        return ((listener.status == AccelBroker.RUNNING) || (listener.status == AccelBroker.STARTING));
    }

    /**
     * Stop listening for acceleration sensor.
     * 
     * @return 		true if stopped, false if not
     */
    public boolean stop()
    {
        listener.stop();
        return (listener.status == AccelBroker.STOPPED);
    }
    
    /**
     * Wait until sensor is done starting up.
     * If a request for values is made while sensor is still starting, then delay thread until first reading is made.
     */
    void waitToStart() {
       	if (listener.status == AccelBroker.STARTING) {
       		System.out.println("AccelBroker.waitToStart...");
    		long timeout = 1000; // wait at most 1 sec
    		while ((listener.status == AccelBroker.STARTING) && (timeout > 0)) {
    			try {
    				Thread.sleep(10);
    				timeout = timeout - 10;
    			}
    			catch (InterruptedException e) {
    			}
    		}
    	}
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
    	
    	// Wait for startup
       	this.waitToStart();
        	
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
     * If sensor is still starting, wait until 1st value is acquired before returning.
     * 
     * @return			x value
     */
    public float getX() {
       	this.waitToStart();
   		return listener.x;
    }
    
    /**
     * Get Y value of last accelerometer value.
     * If sensor is still starting, wait until 1st value is acquired before returning.
     * 
     * @return			y value
     */
    public float getY() {
       	this.waitToStart();
   		return listener.y;
	}

    /**
     * Get Z value of last accelerometer value.
     * If sensor is still starting, wait until 1st value is acquired before returning.
     * 
     * @return			z value
     */
    public float getZ() {
       	this.waitToStart();
   		return listener.x;
	}

}
