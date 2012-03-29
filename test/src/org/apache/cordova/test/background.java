package org.apache.cordova.test;

import android.os.Bundle;
import android.webkit.WebView;

import org.apache.cordova.*;

public class background extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(new FixWebView(this), new CordovaWebViewClient(this), new CordovaChromeClient(this));
        super.setBooleanProperty("keepRunning", false);
        super.loadUrl("file:///android_asset/www/background/index.html");
    }
}
