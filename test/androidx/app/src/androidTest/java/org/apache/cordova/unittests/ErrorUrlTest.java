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


package org.apache.cordova.unittests;

import android.content.Intent;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.apache.cordova.CordovaWebView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ErrorUrlTest {
    private static final String START_URL = "file:///android_asset/www/htmlnotfound/index.html";
    private static final String ERROR_URL = "file:///android_asset/www/htmlnotfound/error.html";
    private static final String INVALID_URL = "file:///android_asset/www/invalid.html";

    //I have no idea why we picked 100, but we did.
    private static final int WEBVIEW_ID = 100;
    private TestActivity mActivity;

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            TestActivity.class);

    @Before
    public void launchApplicationWithIntent() {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        intent.putExtra("errorurl", INVALID_URL);
        intent.putExtra("url", INVALID_URL);
        mActivity = (TestActivity) mActivityRule.launchActivity(intent);
    }

    @Test
    public void errorUrlTest() throws Throwable {
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());
        assertEquals(ERROR_URL, mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertEquals(ERROR_URL, mActivity.getWebInterface().getUrl());
            }
        });
    }
}
