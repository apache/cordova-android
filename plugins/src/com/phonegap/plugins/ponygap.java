package com.phonegap.plugins;

import android.os.Bundle;

import com.phonegap.*;

public class ponygap extends DroidGap
{
	/* Declare plugins here */
	TtsManager tts;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	tts = new TtsManager(super.appView, this);
    	
    	super.appView.addJavascriptInterface(tts, "ttsHook");
    }
}
