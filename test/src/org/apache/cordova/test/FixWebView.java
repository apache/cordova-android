package org.apache.cordova.test;

import android.content.Context;
import android.webkit.WebView;

public class FixWebView extends WebView {

    public FixWebView(Context context) {
        super(context);
    }

    @Override
    public void pauseTimers() {
        // Do nothing
    }

    /**
     * This method is with different signature in order to stop the timers while move application to background
     * @param realPause
     */
    public void pauseTimers(@SuppressWarnings("unused") boolean realPause) {
        super.pauseTimers();
    }

}
