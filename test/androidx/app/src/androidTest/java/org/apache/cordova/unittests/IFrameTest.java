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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.apache.cordova.unittests.WebViewTestUtils.awaitPageFinished;
import static org.apache.cordova.unittests.WebViewTestUtils.clickElement;
import static org.apache.cordova.unittests.WebViewTestUtils.awaitJavascript;

@RunWith(AndroidJUnit4.class)
public class IFrameTest {
    private static final String START_URL = "https://localhost/iframe/index.html";

    @Rule
    public ActivityTestRule<TestActivity> activityRule = new ActivityTestRule<>(TestActivity.class, true, false);

    private void awaitFrame(TestActivity activity, String page) throws Throwable {
        awaitJavascript(activityRule, activity,
                "document.getElementById('iframe').contentWindow.location.pathname === '/iframe/" + page + "'");
        activityRule.runOnUiThread(() -> assertEquals(START_URL, activity.getWebInterface().getUrl()));
    }

    @Test
    public void iFrameHistory() throws Throwable {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        TestActivity activity = activityRule.launchActivity(intent);
        awaitPageFinished(activity, START_URL);

        clickElement(activityRule, activity, "first_page");
        awaitFrame(activity, "frame1.html");
        clickElement(activityRule, activity, "second_page");
        awaitFrame(activity, "index2.html");
        activityRule.runOnUiThread(() -> assertTrue(activity.getWebInterface().backHistory()));
        awaitFrame(activity, "frame1.html");
        activityRule.runOnUiThread(() -> assertFalse(activity.getWebInterface().backHistory()));
    }
}
