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

import android.content.Intent;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.apache.cordova.CordovaWebView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class IFrameTest {

    private static final String START_URL = "file:///android_asset/www/iframe/index.html";
    //I have no idea why we picked 100, but we did.
    private static final int WEBVIEW_ID = 100;
    private int WEBVIEW_LOAD_DELAY = 500;

    private TestActivity testActivity;

    // Don't launch the activity, we're going to send it intents
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            TestActivity.class, true, false);

    @Before
    public void launchApplicationWithIntent() {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        testActivity = (TestActivity) mActivityRule.launchActivity(intent);
    }

    @Test
    public void iFrameHistory() throws Throwable {
        final CordovaWebView cordovaWebView = (CordovaWebView) testActivity.getWebInterface();
        onWebView().withElement(findElement(Locator.ID, "google_maps")).perform(webClick());
        sleep(WEBVIEW_LOAD_DELAY);
        mActivityRule.runOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
        sleep(WEBVIEW_LOAD_DELAY);
        onWebView().withElement(findElement(Locator.ID, "javascript_load")).perform(webClick());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
        sleep(WEBVIEW_LOAD_DELAY);
        //Espresso will kill the application and not trigger the backHistory method, which correctly
        //navigates the iFrame history.  backHistory is tied to the back button.
        mActivityRule.runOnUiThread(new Runnable() {
            public void run()
            {
                assertTrue(cordovaWebView.backHistory());
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
                assertFalse(cordovaWebView.backHistory());
            }
        });

    }


    //BRUTE FORCE THE CRAP OUT OF CONCURRENCY ERRORS
    private void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            fail("Unexpected Timeout");
        }
    }

}
