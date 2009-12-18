package com.phonegap.plugins;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.webkit.WebView;

public class TtsManager {

	TextToSpeech voice;	
	Context mCtx;
	WebView appView;
	
	TtsManager(WebView view, Context ctx)
	{
		mCtx = ctx;
		appView = view;
		voice = new TextToSpeech(mCtx, ttsInitListener);
	}

	public void say(String text)
	{
		voice.speak(text, 1, null);
	}
	
	//I have no idea why you would use this?  Do you have predefined speech?
	
	private TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
		    public void onInit(int status) {		     
		      
		    }
	 };

	
}
