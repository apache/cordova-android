package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class loading extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setStringProperty("loadingDialog", "Testing,Loading...");
        super.loadUrl("http://www.google.com");
    }
}
