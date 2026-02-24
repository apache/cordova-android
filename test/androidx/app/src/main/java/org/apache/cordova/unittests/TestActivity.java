/**
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

package org.apache.cordova.unittests;

import android.os.Bundle;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RunnableFuture;

/**
 * The purpose of this activity is to allow the test framework to manipulate the start url, which
 * is normally locked down by CordovaActivity to standard users who aren't editing their Java code.
 */

public class TestActivity extends CordovaActivity {

    public final ArrayBlockingQueue<String> onPageFinishedUrl = new ArrayBlockingQueue<String>(500);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("cdvStartInBackground", false)) {
                moveTaskToBack(true);
            }
            launchUrl = extras.getString("startUrl", "index.html");
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("onPageFinished".equals(id)) {
            onPageFinishedUrl.add((String) data);
        }
        return super.onMessage(id, data);
    }

    public CordovaWebView getWebInterface() { return this.appView; }

}
