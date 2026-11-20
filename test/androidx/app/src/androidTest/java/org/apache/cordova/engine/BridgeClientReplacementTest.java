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

package org.apache.cordova.engine;

import android.content.Intent;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.apache.cordova.unittests.TestActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BridgeClientReplacementTest {
    @Rule
    public ActivityTestRule<TestActivity> rule = new ActivityTestRule<>(TestActivity.class, true, false);

    @Test
    public void navigationAfterClientReplacementResetsBridge() throws Throwable {
        String startUrl = "https://localhost/bridgestartup/index.html";
        String nextUrl = "https://localhost/bridgestartup/plain.html";
        Intent intent = new Intent();
        intent.putExtra("startUrl", startUrl);
        TestActivity activity = rule.launchActivity(intent);
        assertEquals(startUrl, activity.onPageFinishedUrl.poll(10, TimeUnit.SECONDS));
        SystemWebViewEngine engine = (SystemWebViewEngine) activity.getWebInterface().getEngine();
        rule.runOnUiThread(() -> {
            assertTrue(engine.bridge.isSecretEstablished());
            engine.webView.setWebViewClient(new SystemWebViewClient(engine));
            engine.webView.loadUrl(nextUrl);
        });
        assertEquals(nextUrl, activity.onPageFinishedUrl.poll(10, TimeUnit.SECONDS));
        rule.runOnUiThread(() -> assertFalse(engine.bridge.isSecretEstablished()));
    }
}
