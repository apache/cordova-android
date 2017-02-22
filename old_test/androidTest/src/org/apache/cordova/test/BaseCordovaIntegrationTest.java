package org.apache.cordova.test;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/


import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;

import org.apache.cordova.CordovaWebView;

public class BaseCordovaIntegrationTest extends ActivityInstrumentationTestCase2<MainTestActivity> {
    protected MainTestActivity testActivity;
    protected FrameLayout containerView;
    protected CordovaWebView cordovaWebView;


    public BaseCordovaIntegrationTest() {
    super(MainTestActivity.class);
  }

    protected void setUpWithStartUrl(String url, String... prefsAndValues) {
        Intent intent = new Intent(getInstrumentation().getContext(), MainTestActivity.class);
        intent.putExtra("testStartUrl", url);
        for (int i = 0; i < prefsAndValues.length; i += 2) {
            intent.putExtra(prefsAndValues[i], prefsAndValues[i + 1]);
        }
        setActivityIntent(intent);
        testActivity = getActivity();
        containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
        cordovaWebView = testActivity.getCordovaWebView();
    }
}

