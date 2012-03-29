package org.apache.cordova.test;

import android.os.Bundle;
import org.apache.cordova.*;

public class htmlnotfound extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        super.loadUrl("file:///android_asset/www/htmlnotfound/index.html");
    }
}
