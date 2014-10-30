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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.AndroidChromeClient;
import org.apache.cordova.AndroidWebViewClient;
import org.apache.cordova.Config;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.test.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CordovaWebViewTestActivity extends Activity implements CordovaInterface {
    public CordovaWebView cordovaWebView;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        //CB-7238: This has to be added now, because it got removed from somewhere else
        Config.init(this);
        
        cordovaWebView = (CordovaWebView) findViewById(R.id.cordovaWebView);
        cordovaWebView.init(this, Config.getPluginEntries(), Config.getPreferences());

        cordovaWebView.loadUrl("file:///android_asset/www/index.html");

    }

    public Context getContext() {
        return this;
    }

    public void startActivityForResult(CordovaPlugin command, Intent intent,
            int requestCode) {
        // TODO Auto-generated method stub
        
    }

    public void setActivityResultCallback(CordovaPlugin plugin) {
        // TODO Auto-generated method stub
        
    }

    //Note: This must always return an activity!
    public Activity getActivity() {
        return this;
    }

    @Deprecated
    public void cancelLoadUrl() {
        // TODO Auto-generated method stub
        
    }

    public Object onMessage(String id, Object data) {
        // TODO Auto-generated method stub
        return null;
    }

    public ExecutorService getThreadPool() {
        // TODO Auto-generated method stub
        return threadPool;
    }
    
    @Override
    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
        super.onDestroy();
        if (cordovaWebView != null) {
            // Send destroy event to JavaScript
            cordovaWebView.handleDestroy();
        }
    }
}
