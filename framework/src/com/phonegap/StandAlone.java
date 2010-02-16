package com.phonegap;

import java.lang.reflect.Field;

import android.app.Activity;
import android.os.Bundle;

public class StandAlone extends DroidGap {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        super.loadUrl("file:///android_asset/www/index.html");                        
    }		
	
}
