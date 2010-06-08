package com.phonegap;

import android.content.Context;
import android.webkit.WebView;

public class AudioHandler {
	
	AudioPlayer audio;
	WebView mAppView;
	Context mCtx;
	
	AudioHandler(WebView view, Context ctx)
	{
		mAppView = view;
		mCtx = ctx;
		// YES, I know this is bad, but I can't do it the right way because Google didn't have the
		// foresight to add android.os.environment.getExternalDataDirectory until Android 2.2
        audio = new AudioPlayer("/sdcard/tmprecording.mp3", mCtx);
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
}
