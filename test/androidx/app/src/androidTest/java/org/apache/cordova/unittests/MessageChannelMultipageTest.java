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

import org.apache.cordova.CordovaWebViewImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static org.apache.cordova.unittests.WebViewTestUtils.awaitPageFinished;
import static org.apache.cordova.unittests.WebViewTestUtils.awaitJavascript;

@RunWith(AndroidJUnit4.class)
public class MessageChannelMultipageTest {
    private static final String START_URL = "https://localhost/messagechannel/index.html";

    @Rule
    public ActivityTestRule<TestActivity> activityRule = new ActivityTestRule<>(TestActivity.class, true, false);

    private void sendTestEvent(TestActivity activity) throws Throwable {
        // Exercise CordovaWebViewImpl's cached CoreAndroid path, rather than calling
        // PluginManager directly and bypassing the cache this regression covers.
        Method method = CordovaWebViewImpl.class.getDeclaredMethod("sendJavascriptEvent", String.class);
        method.setAccessible(true);
        activityRule.runOnUiThread(() -> {
            try {
                method.invoke(activity.getWebInterface(), "menubutton");
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    public void testThatCachedCallbackIdIsValid() throws Throwable {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        TestActivity activity = activityRule.launchActivity(intent);
        awaitPageFinished(activity, START_URL);
        awaitJavascript(activityRule, activity, "window.messageChannelReady === true");

        sendTestEvent(activity);
        awaitJavascript(activityRule, activity, "window.testEventCount === 1");

        // loadUrl recreates the plugins. The new document must receive events via
        // its new channel, not the cached callback belonging to the old document.
        activityRule.runOnUiThread(() -> activity.getWebInterface().loadUrl(START_URL));
        awaitPageFinished(activity, START_URL);
        awaitJavascript(activityRule, activity,
                "window.messageChannelReady === true && window.testEventCount === 0");
        sendTestEvent(activity);
        awaitJavascript(activityRule, activity, "window.testEventCount === 1");
    }
}
