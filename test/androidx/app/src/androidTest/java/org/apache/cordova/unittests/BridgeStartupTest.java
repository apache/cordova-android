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
import android.webkit.WebView;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BridgeStartupTest {
    private static final String START_URL = "https://localhost/bridgestartup/index.html";

    @Rule
    public ActivityTestRule<TestActivity> rule = new ActivityTestRule<>(TestActivity.class, true, false);

    private TestActivity launchSample() throws InterruptedException {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        TestActivity activity = rule.launchActivity(intent);
        assertEquals(START_URL, activity.onPageFinishedUrl.poll(10, TimeUnit.SECONDS));
        return activity;
    }

    private boolean awaitOverride(TestActivity activity) throws Throwable {
        AtomicBoolean ready = new AtomicBoolean();
        long deadline = SystemClock.uptimeMillis() + 2000;
        do {
            rule.runOnUiThread(() -> ready.set(activity.getWebInterface()
                    .isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK)));
            if (!ready.get()) {
                SystemClock.sleep(20);
            }
        } while (!ready.get() && SystemClock.uptimeMillis() < deadline);
        return ready.get();
    }

    // Older WebViews can deliver gap_init before the first onPageStarted callback.
    // Repeat fresh activity loads to exercise that ordering without retrying failures.
    @Test
    public void repeatedStartup() throws Throwable {
        int failures = 0;
        for (int i = 0; i < 60; i++) {
            TestActivity activity = launchSample();
            if (!awaitOverride(activity)) {
                failures++;
            }
            rule.finishActivity();
        }
        assertEquals("Initial back-button overrides dropped out of 60 page loads", 0, failures);
    }

    @Test
    public void navigationRejectsPreviousDocumentBridge() throws Throwable {
        TestActivity activity = launchSample();
        assertTrue("Initial back-button override was dropped", awaitOverride(activity));
        WebView webView = (WebView) activity.getWebInterface().getView();
        ArrayBlockingQueue<String> results = new ArrayBlockingQueue<>(1);
        rule.runOnUiThread(() -> webView.evaluateJavascript("prompt('', 'gap_init:3')", results::add));
        String secret = results.poll(10, TimeUnit.SECONDS);
        assertNotNull(secret);
        assertTrue("Expected a bridge secret", secret.matches("\"[0-9]+\""));
        rule.runOnUiThread(() -> webView.loadUrl("https://localhost/bridgestartup/plain.html"));
        assertEquals("https://localhost/bridgestartup/plain.html",
                activity.onPageFinishedUrl.poll(10, TimeUnit.SECONDS));
        // The new document has no cordova.js. Even with the old document's token,
        // it must not be able to invoke a plugin through the disabled bridge.
        String attempt = "prompt('[true]', 'gap:' + JSON.stringify([" + secret
                + ", 'CoreAndroid', 'overrideBackbutton', 'stale']))";
        rule.runOnUiThread(() -> webView.evaluateJavascript(attempt, results::add));
        assertNotNull(results.poll(10, TimeUnit.SECONDS));
        rule.runOnUiThread(() -> assertFalse(activity.getWebInterface()
                .isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK)));
    }
}
