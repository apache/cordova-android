package org.apache.cordova.unittests;

import android.os.Bundle;

import org.apache.cordova.CordovaActivity;

public class TestActivity extends CordovaActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("cdvStartInBackground", false)) {
                moveTaskToBack(true);
            }
            launchUrl = extras.getString("startUrl", "index.html");
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }
}
