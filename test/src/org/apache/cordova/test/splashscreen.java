package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class splashscreen extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();

        // Show splashscreen
        this.setIntegerProperty("splashscreen", R.drawable.sandy);

        super.loadUrl("file:///android_asset/www/splashscreen/index.html", 2000);
    }
}
