package org.apache.cordova.test;

import org.apache.cordova.DroidGap;

import android.app.Activity;
import android.os.Bundle;

public class PhoneGapActivity extends DroidGap {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/index.html");
    }
}
