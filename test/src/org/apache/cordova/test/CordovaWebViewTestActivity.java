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

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.IPlugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CordovaWebViewTestActivity extends Activity implements CordovaInterface {

    CordovaWebView phoneGap;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        phoneGap = (CordovaWebView) findViewById(R.id.phoneGapView);

        phoneGap.loadUrl("file:///android_asset/www/index.html");

    }

    public void onDestroy()
    {
        super.onDestroy();
        //phoneGap.onDestroy();
    }

    @Override
    public void startActivityForResult(IPlugin command, Intent intent, int requestCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setActivityResultCallback(IPlugin plugin) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bindBackButton(boolean override) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isBackButtonBound() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Activity getActivity() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public void cancelLoadUrl() {
        // TODO Auto-generated method stub

    }

    @Override
    public Object onMessage(String id, Object data) {
        // TODO Auto-generated method stub
        return null;
    }
}