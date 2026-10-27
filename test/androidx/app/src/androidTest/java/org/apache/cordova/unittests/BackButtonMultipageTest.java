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
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.cordova.CordovaWebView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


import static org.apache.cordova.unittests.WebViewTestUtils.awaitPageFinished;
import static org.apache.cordova.unittests.WebViewTestUtils.clickElement;
import static org.apache.cordova.unittests.WebViewTestUtils.awaitJavascript;


@RunWith(AndroidJUnit4.class)
public class BackButtonMultipageTest {

    private static final String START_URL = "https://localhost/backbuttonmultipage/index.html";
    private static final String SAMPLE3_URL = "https://localhost/backbuttonmultipage/sample3.html";
    private static final String SAMPLE2_URL = "https://localhost/backbuttonmultipage/sample2.html";
    private TestActivity mActivity;

    // Don't launch the activity, we're going to send it intents
    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(
            TestActivity.class, true, false);

    @Before
    public void launchApplicationWithIntent() {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        mActivity = mActivityRule.launchActivity(intent);
    }

    @Test
    public void testViaHref() throws Throwable {
        final CordovaWebView webInterface = mActivity.getWebInterface();
        assertPageSample(START_URL);

        clickElement(mActivityRule, mActivity, "next_page");
        assertPageSample(SAMPLE2_URL);
        clickElement(mActivityRule, mActivity, "next_page");
        assertPageSample(SAMPLE3_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertPageSample(SAMPLE2_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertPageSample(START_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertFalse(webInterface.backHistory());
            }
        });
    }

    @Test
    public void testViaLoadUrl() throws Throwable {
        final CordovaWebView webInterface = mActivity.getWebInterface();
        assertPageSample(START_URL);

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webInterface.loadUrl(SAMPLE2_URL);
            }
        });
        assertPageSample(SAMPLE2_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webInterface.loadUrl(SAMPLE3_URL);
            }
        });
        assertPageSample(SAMPLE3_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertPageSample(SAMPLE2_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertTrue(webInterface.backHistory());
            }
        });
        assertPageSample(START_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertFalse(webInterface.backHistory());
            }
        });
    }

    @Test
    public void testViaBackButtonOnView() throws Throwable {
        final CordovaWebView webInterface = mActivity.getWebInterface();
        assertPageSample(START_URL);

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webInterface.loadUrl(SAMPLE2_URL);
            }
        });
        assertPageSample(SAMPLE2_URL);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webInterface.loadUrl(SAMPLE3_URL);
            }
        });
        assertPageSample(SAMPLE3_URL);
        pressBackOnWebView();
        assertPageSample(SAMPLE2_URL);
        pressBackOnWebView();
        assertPageSample(START_URL);
    }

    private void pressBackOnWebView() throws Throwable {
        mActivityRule.runOnUiThread(() -> {
            View view = mActivity.getWebInterface().getView();
            long time = SystemClock.uptimeMillis();
            // Espresso's global Back action can finish the activity through the
            // system dispatcher. This test specifically covers WebView key handling.
            assertTrue(view.dispatchKeyEvent(new KeyEvent(time, time,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0)));
            assertTrue(view.dispatchKeyEvent(new KeyEvent(time, time,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0)));
        });
    }

    private void assertPageSample(String url) throws Throwable {
        awaitPageFinished(mActivity, url);
        awaitJavascript(mActivityRule, mActivity,
                "document.getElementById('deviceready').textContent === 'fired'");
    }
}
