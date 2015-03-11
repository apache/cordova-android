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

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.cordova.Config;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

import android.app.Activity;
import android.os.Bundle;

/**
 * Tests creating the views via inflating a layout, and also tests *not* using CordovaActivity.
 */
public class CordovaWebViewTestActivity extends Activity {
    private CordovaWebView cordovaWebView;
    public final ArrayBlockingQueue<String> onPageFinishedUrl = new ArrayBlockingQueue<String>(5);
    public static final String START_URL = "file:///android_asset/www/index.html";

    protected CordovaInterfaceImpl cordovaInterface = new CordovaInterfaceImpl(this) {
        @Override
        public Object onMessage(String id, Object data) {
            if ("onPageFinished".equals(id)) {
                onPageFinishedUrl.add((String) data);
            }
            return super.onMessage(id, data);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        //Set up the webview
        ConfigXmlParser parser = new ConfigXmlParser();

        SystemWebView webView = (SystemWebView) findViewById(R.id.cordovaWebView);
        cordovaWebView = new CordovaWebViewImpl(this, new SystemWebViewEngine(webView));
        cordovaWebView.init(cordovaInterface, parser.getPluginEntries(), parser.getPreferences());

        cordovaWebView.loadUrl(START_URL);
    }

    public CordovaWebView getCordovaWebView() {
        return cordovaWebView;
    }
}
