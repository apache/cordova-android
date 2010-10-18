/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class Device extends Plugin {
	
	public static String phonegapVersion = "0.9.2";				// PhoneGap version
	public static String platform = "Android";					// Device OS
	public static String uuid;									// Device UUID
    
    /**
     * Constructor.
     */
	public Device() {
    }
	
	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(DroidGap ctx) {
		super.setContext(ctx);
        Device.uuid = getUuid();
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
			if (action.equals("getDeviceInfo")) {
				JSONObject r = new JSONObject();
				r.put("uuid", Device.uuid);
				r.put("version", this.getOSVersion());
				r.put("platform", Device.platform);
				r.put("name", this.getProductName());
				r.put("phonegap", Device.phonegapVersion);
				//JSONObject pg = new JSONObject();
				//pg.put("version", Device.phonegapVersion);
				//r.put("phonegap", pg);
				return new PluginResult(status, r);
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
		if (action.equals("getDeviceInfo")) {
			return true;
		}
		return false;
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------
		
	/**
	 * Get the OS name.
	 * 
	 * @return
	 */
	public String getPlatform()	{
		return Device.platform;
	}
	
	/**
	 * Get the device's Universally Unique Identifier (UUID).
	 * 
	 * @return
	 */
	public String getUuid()	{		
		String uuid = Settings.Secure.getString(this.ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		return uuid;
	}

	/**
	 * Get the PhoneGap version.
	 * 
	 * @return
	 */
	public String getPhonegapVersion() {
		return Device.phonegapVersion;
	}	

	public String getLine1Number(){
	  TelephonyManager operator = (TelephonyManager)this.ctx.getSystemService(Context.TELEPHONY_SERVICE);
	  return operator.getLine1Number();
	}
	
	public String getDeviceId(){
	  TelephonyManager operator = (TelephonyManager)this.ctx.getSystemService(Context.TELEPHONY_SERVICE);
	  return operator.getDeviceId();
	}
	
	public String getSimSerialNumber(){
	  TelephonyManager operator = (TelephonyManager)this.ctx.getSystemService(Context.TELEPHONY_SERVICE);
	  return operator.getSimSerialNumber();
  }
  
	public String getSubscriberId(){
	  TelephonyManager operator = (TelephonyManager)this.ctx.getSystemService(Context.TELEPHONY_SERVICE);
	  return operator.getSubscriberId();
	}
	
	public String getModel()
	{
		String model = android.os.Build.MODEL;
		return model;
	}
	public String getProductName()
	{
		String productname = android.os.Build.PRODUCT;
		return productname;
	}
	
	/**
	 * Get the OS version.
	 * 
	 * @return
	 */
	public String getOSVersion() {
		String osversion = android.os.Build.VERSION.RELEASE;
		return osversion;
	}
	
	public String getSDKVersion()
	{
		String sdkversion = android.os.Build.VERSION.SDK;
		return sdkversion;
	}
	
    
    public String getTimeZoneID() {
       TimeZone tz = TimeZone.getDefault();
        return(tz.getID());
    } 
    
}

