/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 */
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
