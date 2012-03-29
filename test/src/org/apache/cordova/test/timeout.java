package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class timeout extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();

        // Short timeout to cause error
        this.setIntegerProperty("loadUrlTimeoutValue", 10);
        super.loadUrl("http://www.google.com");
    }
}
