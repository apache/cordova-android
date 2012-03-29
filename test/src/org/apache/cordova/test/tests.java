package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class tests extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        super.pluginManager.addService("Activity", "org.apache.cordova.test.ActivityPlugin");
        super.loadUrl("file:///android_asset/www/index.html");
    }
}
