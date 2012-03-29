package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class errorurl extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        this.setStringProperty("errorUrl", "file:///android_asset/www/htmlnotfound/error.html");
        super.loadUrl("file:///android_asset/www/htmlnotfound/index.html");
    }
}
