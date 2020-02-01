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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


import static org.apache.cordova.unittests.R.id.cordovaWebView;


@RunWith(AndroidJUnit4.class)
public class BackButtonMultipageTest {

    private static final String START_URL = "file:///android_asset/www/backbuttonmultipage/index.html";
    //I have no idea why we picked 100, but we did.
    private static final int WEBVIEW_ID = 100;
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
    public void testViaHref() throws Throwable {
        final CordovaWebView webInterface = mActivity.getWebInterface();
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());

        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                webInterface.sendJavascript("window.location = 'sample2.html';");
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                webInterface.sendJavascript("window.location = 'sample3.html';");
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertFalse(webInterface.backHistory());
            }
        });
    }

    @Test
    public void testViaLoadUrl() throws Throwable {
        final CordovaWebView webInterface = mActivity.getWebInterface();
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());

        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                webInterface.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                webInterface.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                assertFalse(webInterface.backHistory());
            }
        });
    }

    @Test
    public void testViaBackButtonOnView() throws Throwable {
        final CordovaWebView webInterface = mActivity.getWebInterface();
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());

        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                webInterface.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", mActivity.onPageFinishedUrl.take());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                webInterface.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
            }
        });
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", mActivity.onPageFinishedUrl.take());
        onView(withId(WEBVIEW_ID)).perform(pressBack());
        assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", mActivity.onPageFinishedUrl.take());
        onView(withId(WEBVIEW_ID)).perform(pressBack());
        assertEquals(START_URL, mActivity.onPageFinishedUrl.take());
    }
}
