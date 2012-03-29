package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class iframe extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/iframe/index.html");
    }
}
