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

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;

public class ActivityPlugin extends CordovaPlugin {

    static String TAG = "ActivityPlugin";
    public static final String BACKBUTTONMULTIPAGE_URL = "file:///android_asset/www/backbuttonmultipage/index.html";

    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            String className = args.isNull(0) ? MainTestActivity.class.getCanonicalName() : args.getString(0);
            String startUrl = args.getString(1);
            JSONObject extraPrefs = args.getJSONObject(2);
            this.startActivity(className, startUrl, extraPrefs);
            callbackContext.success();
            return true;
        }
        return false;
    }

    public void startActivity(String className, String startUrl, JSONObject extraPrefs) throws JSONException {
        try {
            if (!startUrl.contains(":")) {
                startUrl = "file:///android_asset/www/" + startUrl;
            }
            Intent intent = new Intent(this.cordova.getActivity(), Class.forName(className));
            intent.putExtra("testStartUrl", startUrl);
            Iterator<String> iter = extraPrefs.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                intent.putExtra(key, extraPrefs.getString(key));
            }
            LOG.d(TAG, "Starting activity %s", className);
            this.cordova.getActivity().startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOG.e(TAG, "Error starting activity %s", className);
        }
    }

}
