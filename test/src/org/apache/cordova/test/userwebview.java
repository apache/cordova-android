/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.test;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.GeolocationPermissions.Callback;

import org.apache.cordova.*;
import org.apache.cordova.engine.SystemWebChromeClient;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;

public class userwebview extends MainTestActivity {
    
    public TestViewClient testViewClient;
    public TestChromeClient testChromeClient;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemWebViewEngine engine = (SystemWebViewEngine)appView.getEngine();
        testViewClient = new TestViewClient(engine);
        testChromeClient = new TestChromeClient(engine);
        super.init();
        WebView webView = (WebView)engine.getView();
        webView.setWebViewClient(testViewClient);
        webView.setWebChromeClient(testChromeClient);
        super.loadUrl("file:///android_asset/www/userwebview/index.html");
    }

    public class TestChromeClient extends SystemWebChromeClient {
        public TestChromeClient(SystemWebViewEngine parentEngine) {
            super(parentEngine);
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
    public class TestViewClient extends SystemWebViewClient {
        public TestViewClient(SystemWebViewEngine parentEngine) {
            super(parentEngine);
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
