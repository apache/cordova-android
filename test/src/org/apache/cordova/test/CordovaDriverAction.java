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

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.IPlugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class CordovaDriverAction extends Activity implements CordovaInterface {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void bindBackButton(boolean arg0) {
        // TODO Auto-generated method stub

    }

    public void cancelLoadUrl() {
        // TODO Auto-generated method stub

    }

    public Activity getActivity() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isBackButtonBound() {
        // TODO Auto-generated method stub
        return false;
    }

    public Object onMessage(String arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setActivityResultCallback(IPlugin arg0) {
        // TODO Auto-generated method stub

    }

    public void startActivityForResult(IPlugin arg0, Intent arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    public Context getContext() {
        return this;
    }
}
