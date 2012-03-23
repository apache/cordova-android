package org.apache.cordova.test;

import org.apache.cordova.DroidGap;

import android.app.Activity;
import android.os.Bundle;

public class JailActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!super.areAssetsInJail())
        {
            super.moveAssetsToJail();
        }
        super.loadJailedFile("www/index.html");
    }
}
