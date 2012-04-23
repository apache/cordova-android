package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.os.Bundle;

public class PhoneGapViewTestActivity extends Activity {
    
    CordovaWebView phoneGap;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        phoneGap = (CordovaWebView) findViewById(R.id.phoneGapView);
        
        phoneGap.loadUrl("file:///android_asset/www/index.html");
        
    }
    
    public void onDestroy()
    {
        super.onDestroy();
        //phoneGap.onDestroy();
    }
}