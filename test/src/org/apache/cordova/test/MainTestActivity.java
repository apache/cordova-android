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

public class MainTestActivity extends BaseTestCordovaActivity {
    public static final String START_URL = "file:///android_asset/www/index.html";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("testStartUrl");
        if (url == null) {
            url = START_URL;
        }
        super.loadUrl(url);
    }

    @Override protected void loadConfig() {
        super.loadConfig();
        // Need to set this explicitly in prefs since it's not settable via bundle extras (for security reasons).
        String errorUrl = getIntent().getStringExtra("testErrorUrl");
        if (errorUrl != null) {
            preferences.set("errorUrl", errorUrl);
        }
    }
}
