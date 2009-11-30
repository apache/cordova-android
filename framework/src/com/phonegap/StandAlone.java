package com.phonegap;

import java.lang.reflect.Field;

import android.app.Activity;
import android.os.Bundle;

public class StandAlone extends DroidGap {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* Load a URI from the strings.xml file */
        Class<R.string> c = R.string.class;
        Field f;
        String uri;
        
        int i = 0;
        
        try {
          f = c.getField("url");
          i = f.getInt(f);
          uri = this.getResources().getString(i);
        } catch (Exception e)
        {
          uri = "http://www.phonegap.com";
        }
        super.loadUrl(uri);                        
    }		
	
}
