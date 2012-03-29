package org.apache.cordova.test;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.GeolocationPermissions.Callback;

import org.apache.cordova.*;
import org.apache.cordova.api.LOG;

public class userwebview extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(new WebView(this), new TestViewClient(this), new TestChromeClient(this));
        super.loadUrl("file:///android_asset/www/userwebview/index.html");
    }

    public class TestChromeClient extends CordovaChromeClient {
        public TestChromeClient(DroidGap arg0) {
            super(arg0);
            LOG.d("userwebview", "TestChromeClient()");
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            LOG.d("userwebview", "onGeolocationPermissionsShowPrompt(" + origin + ")");
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }
    }

    /**
     * This class can be used to override the GapViewClient and receive notification of webview events.
     */
    public class TestViewClient extends CordovaWebViewClient {
        public TestViewClient(DroidGap arg0) {
            super(arg0);
            LOG.d("userwebview", "TestViewClient()");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LOG.d("userwebview", "shouldOverrideUrlLoading(" + url + ")");
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LOG.d("userwebview", "onReceivedError: Error code=" + errorCode + " Description=" + description + " URL=" + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

}
