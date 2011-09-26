/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010-2011, IBM Corporation
 */
package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import java.util.HashMap;

/**
 * This class exposes methods in DroidGap that can be called from JavaScript.
 */
public class App extends Plugin {

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        try {
        	if (action.equals("clearCache")) {
        		this.clearCache();
        	}
        	else if (action.equals("loadUrl")) {
            	this.loadUrl(args.getString(0), args.optJSONObject(1));
            }
        	else if (action.equals("cancelLoadUrl")) {
            	this.cancelLoadUrl();
            }
        	else if (action.equals("clearHistory")) {
            	this.clearHistory();
            }
        	else if (action.equals("overrideBackbutton")) {
            	this.overrideBackbutton(args.getBoolean(0));
            }
        	else if (action.equals("isBackbuttonOverridden")) {
            	boolean b = this.isBackbuttonOverridden();
            	return new PluginResult(status, b);
            }
        	else if (action.equals("exitApp")) {
            	this.exitApp();
            }
        	else if (action.equals("addWhiteListEntry")) {
        		this.addWhiteListEntry(args.getString(0), args.optBoolean(1));
        	}
            return new PluginResult(status, result);
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        }
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	/**
	 * Clear the resource cache.
	 */
	public void clearCache() {
		((DroidGap)this.ctx).clearCache();
	}
	
    /**
     * Load the url into the webview.
     * 
     * @param url
     * @param props			Properties that can be passed in to the DroidGap activity (i.e. loadingDialog, wait, ...)
     * @throws JSONException 
     */
	public void loadUrl(String url, JSONObject props) throws JSONException {
		System.out.println("App.loadUrl("+url+","+props+")");
		int wait = 0;
		boolean usePhoneGap = true;
		boolean clearPrev = false;

		// If there are properties, then set them on the Activity
		HashMap<String, Object> params = new HashMap<String, Object>();
		if (props != null) {
			JSONArray keys = props.names();
			for (int i=0; i<keys.length(); i++) {
				String key = keys.getString(i); 
				if (key.equals("wait")) {
					wait = props.getInt(key);
				}
				else if (key.equalsIgnoreCase("usephonegap")) {
					usePhoneGap = props.getBoolean(key);
				}
				else if (key.equalsIgnoreCase("clearprev")) {
					clearPrev = props.getBoolean(key);
				}
				else {
					Object value = props.get(key);
					if (value == null) {

					}
					else if (value.getClass().equals(String.class)) {
						params.put(key, (String)value);
					}
					else if (value.getClass().equals(Boolean.class)) {
						params.put(key, (Boolean)value);
					}
					else if (value.getClass().equals(Integer.class)) {
						params.put(key, (Integer)value);
					}
				}
			}
		}

		// If wait property, then delay loading

		if (wait > 0) {
			try {
				synchronized(this) {
					this.wait(wait);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		((DroidGap)this.ctx).showWebPage(url, usePhoneGap, clearPrev, params);
	}

	/**
	 * Cancel loadUrl before it has been loaded.
	 */
	public void cancelLoadUrl() {
		((DroidGap)this.ctx).cancelLoadUrl();
	}
	
    /**
     * Clear web history in this web view.
     */
    public void clearHistory() {
    	((DroidGap)this.ctx).clearHistory();
    }

    /**
     * Override the default behavior of the Android back button.
     * If overridden, when the back button is pressed, the "backKeyDown" JavaScript event will be fired.
     * 
     * @param override		T=override, F=cancel override
     */
    public void overrideBackbutton(boolean override) {
    	System.out.println("WARNING: Back Button Default Behaviour will be overridden.  The backbutton event will be fired!");
    	((DroidGap)this.ctx).bound = override;
    }

    /**
     * Return whether the Android back button is overridden by the user.
     * 
     * @return boolean
     */
    public boolean isBackbuttonOverridden() {
    	return ((DroidGap)this.ctx).bound;
    }

    /**
     * Exit the Android application.
     */
    public void exitApp() {
    	((DroidGap)this.ctx).finish();
    }

    /**
     * Add entry to approved list of URLs (whitelist)
     * 
     * @param origin		URL regular expression to allow
     * @param subdomains	T=include all subdomains under origin
     */
    public void addWhiteListEntry(String origin, boolean subdomains) {
    	((DroidGap)this.ctx).addWhiteListEntry(origin, subdomains);
    }
}
