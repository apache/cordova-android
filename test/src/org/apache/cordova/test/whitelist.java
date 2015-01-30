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

import org.apache.cordova.*;

public class whitelist extends MainTestActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        ((AndroidWebView)appView).setWebViewClient(new TestViewClient(cordovaInterface, ((AndroidWebView)appView)));
        super.loadUrl("file:///android_asset/www/whitelist/index.html");
    }

    /**
     * This class can be used to override the GapViewClient and receive notification of webview events.
     */
    public class TestViewClient extends AndroidWebViewClient {

        public TestViewClient(CordovaInterface ctx, AndroidWebView app) {
            super(ctx, app);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LOG.d("whitelist", "shouldOverrideUrlLoading(" + url + ")");
            LOG.d("whitelist", "originalUrl=" + view.getOriginalUrl());
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
