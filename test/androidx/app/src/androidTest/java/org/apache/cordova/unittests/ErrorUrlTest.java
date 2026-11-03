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
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.apache.cordova.unittests.WebViewTestUtils.awaitPageFinished;
import static org.apache.cordova.unittests.WebViewTestUtils.awaitJavascript;

@RunWith(AndroidJUnit4.class)
public class ErrorUrlTest {
    private static final String START_URL = "https://localhost/htmlnotfound/index.html";
    private static final String ERROR_URL = "https://localhost/htmlnotfound/error.html";
    private static final String INVALID_URL = "file:///android_asset/www/invalid.html";

    @Rule
    public ActivityTestRule<TestActivity> activityRule = new ActivityTestRule<>(TestActivity.class, true, false);

    @Test
    public void errorUrlTest() throws Throwable {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        TestActivity activity = activityRule.launchActivity(intent);
        awaitPageFinished(activity, START_URL);
        // A missing local file produces a real WebView load error without network I/O.
        activityRule.runOnUiThread(() -> activity.getWebInterface().loadUrl(INVALID_URL));

        assertEquals(INVALID_URL, activity.onReceivedErrorUrl.poll(10, TimeUnit.SECONDS));
        // WebView need not finish the failed document before Cordova opens the fallback.
        awaitPageFinished(activity, ERROR_URL);
        awaitJavascript(activityRule, activity, "document.title === 'Expected Error'");
        activityRule.runOnUiThread(() -> assertEquals(ERROR_URL, activity.getWebInterface().getUrl()));
    }
}
