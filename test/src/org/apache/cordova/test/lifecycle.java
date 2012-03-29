package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class lifecycle extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/lifecycle/index.html");
    }
}
