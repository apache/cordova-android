package com.phonegap.demo;
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
import java.io.IOException;
import java.util.TimeZone;

import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.webkit.WebView;
import android.media.Ringtone;
import android.media.RingtoneManager;

public class PhoneGap{
	
	private static final String LOG_TAG = "PhoneGap";
	/*
	 * UUID, version and availability	
	 */
	public boolean droid = true;
	public static String version = "0.2";
	public static String platform = "Android";
	public static String uuid;
	private Context mCtx;
    private WebView mAppView;
    SmsListener mSmsListener;
    DirectoryManager fileManager;
    AudioHandler audio; 
    
	public PhoneGap(Context ctx, WebView appView) {
        this.mCtx = ctx;
        this.mAppView = appView;

        mSmsListener = new SmsListener(ctx,mAppView);
        fileManager = new DirectoryManager();
        audio = new AudioHandler("/sdcard/tmprecording.mp3", ctx);
        uuid = getUuid();
    }
	
	public void beep(long pattern)
	{
		RingtoneManager beeper = new RingtoneManager(mCtx);
		Uri ringtone = beeper.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone notification = beeper.getRingtone(mCtx, ringtone);
		for (long i = 0; i < pattern; ++i)
		{
			notification.play();
		}
	}
	
	public void vibrate(long pattern){
        // Start the vibration, 0 defaults to half a second.
		if (pattern == 0)
			pattern = 500;
        Vibrator vibrator = (Vibrator) mCtx.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern);
	}
	
	public String getPlatform()
	{
		return this.platform;
	}
	
	public String getUuid()
	{

		TelephonyManager operator = (TelephonyManager) mCtx.getSystemService(Context.TELEPHONY_SERVICE);
		String uuid = operator.getDeviceId();
		return uuid;
	}
	
	public void init()
	{
		mAppView.loadUrl("javascript:Device.setData('Android','" + version + "','" + this.getUuid() + "')");
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
	public String getOSVersion()
	{
		String osversion = android.os.Build.VERSION.RELEASE;
		return osversion;
	}
	public String getSDKVersion()
	{
		String sdkversion = android.os.Build.VERSION.SDK;
		return sdkversion;
	}
	
	public String getVersion()
	{
		return version;
	}	
	
	// Old SMS code, figure out what to do with this!
	// BTW: This is awesome!
	
	public void notificationWatchPosition(String filter)
	/**
	 * Starts the listener for incoming notifications of type filter
	 * TODO: JavaScript Call backs for success and error handling. More filter types. 
	 */
	{
		if (filter.contains("SMS"))
		{
    		IntentFilter mFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    		mCtx.registerReceiver(mSmsListener,mFilter);
		}
	}
	
    public void notificationClearWatch(String filter) 
	/**
	 * Stops the listener for incoming notifications of type filter
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    {
    	if (filter.contains("SMS")) 
    	{
    		mCtx.unregisterReceiver(mSmsListener);
    	}	
    }
    
    public void httpGet(String url, String file)
    /**
     * grabs a file from specified url and saves it to a name and location
     * the base directory /sdcard is abstracted so that paths may be the same from one mobile OS to another
     * TODO: JavaScript call backs and error handling
     */
    {
    	HttpHandler http = new HttpHandler();
    	http.get(url, file);
    }
    
    

    	
    public int testSaveLocationExists(){
        if (fileManager.testSaveLocationExists())
            return 0;
        else
            return 1;
    }
    
    public long getFreeDiskSpace(){
        long freeDiskSpace=fileManager.getFreeDiskSpace();
        return freeDiskSpace;
    }

    public int testFileExists(String file){
        if (fileManager.testFileExists(file))
            return 0;
        else
            return 1;
    }
    
    public int testDirectoryExists(String file){
        if (fileManager.testFileExists(file))
            return 0;
        else
            return 1;
    } 

    /**
	 * Delete a specific directory. 
	 * Everyting in side the directory would be gone.
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int deleteDirectory (String dir){
        if (fileManager.deleteDirectory(dir))
            return 0;
        else
            return 1;
    }
    

    /**
	 * Delete a specific file. 
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int deleteFile (String file){
        if (fileManager.deleteFile(file))
            return 0;
        else
            return 1;
    }
    

    /**
	 * Create a new directory. 
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int createDirectory(String dir){
    	if (fileManager.createDirectory(dir))
            return 0;
        else
            return 1;
    } 
    
    
    /**
     * AUDIO
     * TODO: Basic functions done but needs more work on error handling and call backs, remove record hack
     */
    
    public void startRecordingAudio(String file)
    {
    	/* for this to work the recording needs to be specified in the constructor,
    	 * a hack to get around this, I'm moving the recording after it's complete 
    	 */
    	audio.startRecording(file);
    }
    
    public void stopRecordingAudio()
    {
    	audio.stopRecording();
    }
    
    public void startPlayingAudio(String file)
    {
    	audio.startPlaying(file);
    }
    
    public void stopPlayingAudio()
    {
    	audio.stopPlaying();
    }
    
    public long getCurrentPositionAudio()
    {
    	System.out.println(audio.getCurrentPosition());
    	return(audio.getCurrentPosition());
    }
    
    public long getDurationAudio(String file)
    {
    	System.out.println(audio.getDuration(file));
    	return(audio.getDuration(file));
    }  
    
    public void setAudioOutputDevice(int output){
    	audio.setAudioOutputDevice(output);
    }
    
    public int getAudioOutputDevice(){
    	return audio.getAudioOutputDevice();
    }
    
    public String getLine1Number() {
        TelephonyManager tm =
            (TelephonyManager)mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        return(tm.getLine1Number());
    }
    
    public String getVoiceMailNumber() {
    	TelephonyManager tm =
    		(TelephonyManager)mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        return(tm.getVoiceMailNumber());
    }
    
    public String getNetworkOperatorName(){
    	TelephonyManager tm =
    		(TelephonyManager)mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        return(tm.getNetworkOperatorName());
    }
    
    public String getSimCountryIso(){
    	TelephonyManager tm =
    		(TelephonyManager)mCtx.getSystemService(Context.TELEPHONY_SERVICE);
        return(tm.getSimCountryIso());
    }
    
    public String getTimeZoneID() {
       TimeZone tz = TimeZone.getDefault();
        return(tz.getID());
    } 
    
}

