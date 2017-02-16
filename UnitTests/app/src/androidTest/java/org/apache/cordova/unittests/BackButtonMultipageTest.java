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
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.apache.cordova.CordovaWebView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.web.sugar.Web.onWebView;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


import static org.apache.cordova.unittests.R.id.cordovaWebView;


@RunWith(AndroidJUnit4.class)
public class BackButtonMultipageTest {

    private static final String START_URL = "file:///android_asset/www/backbuttonmultipage/index.html";
    private TestActivity mActivity;

    // Don't launch the activity, we're going to send it intents
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            TestActivity.class, true, false);

    @Before
    public void launchApplicationWithIntent() {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        mActivity = (TestActivity) mActivityRule.launchActivity(intent);
    }

    @Test
    public void testViaHref() {
        final CordovaWebView webInterface = mActivity.getWebInterface();
    }



}
