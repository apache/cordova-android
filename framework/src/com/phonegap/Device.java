package com.phonegap;
/* License (MIT)
 * Copyright (c) 2008 Nitobi
 * website: http://phonegap.com
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * Software), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.webkit.WebView;
import android.media.Ringtone;
import android.media.RingtoneManager;

public class Device implements Plugin {
	
	public static String phonegapVersion = "pre-0.92 EDGE";		// PhoneGap version
	public static String platform = "Android";					// Device OS
	public static String uuid;									// Device UUID
	private DroidGap ctx;										// DroidGap object
    @SuppressWarnings("unused")
	private WebView webView;									// Webview object
    
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
		this.ctx = ctx;
        Device.uuid = getUuid();
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
			else if (action.equals("beep")) {
				this.beep(args.getLong(0));
			}
			else if (action.equals("vibrate")) {
				this.vibrate(args.getLong(0));
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
     * Called when the activity is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {   	
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
	 * Beep plays the default notification ringtone.
	 * 
	 * @param count			Number of times to play notification
	 */
	public void beep(long count) {
		Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone notification = RingtoneManager.getRingtone(this.ctx, ringtone);
		
		// If phone is not set to silent mode
		if (notification != null) {
			for (long i = 0; i < count; ++i) {
				notification.play();
				long timeout = 5000;
				while (notification.isPlaying() && (timeout > 0)) {
					timeout = timeout - 100;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	/**
	 * Vibrates the device for the specified amount of time.
	 * 
	 * @param time			Time to vibrate in ms.
	 */
	public void vibrate(long time){
        // Start the vibration, 0 defaults to half a second.
		if (time == 0) {
			time = 500;
		}
        Vibrator vibrator = (Vibrator) this.ctx.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
	}
	
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

