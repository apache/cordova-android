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
import android.webkit.WebView;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BackButtonDispatchTest {
    @Rule
    public ActivityTestRule<TestActivity> activityRule = new ActivityTestRule<>(
            TestActivity.class, true, false);

    private TestActivity activity;
    private WebView webView;

    @Before
    public void launchSample() throws Throwable {
        Intent intent = new Intent();
        intent.putExtra("startUrl", "https://localhost/backbuttondispatch/index.html");
        activity = activityRule.launchActivity(intent);
        webView = (WebView) activity.getWebInterface().getView();
        assertNotNull(activity.onPageFinishedUrl.poll(10, TimeUnit.SECONDS));
        awaitValue("window.backTestReady === true", "true");
        long deadline = SystemClock.uptimeMillis() + 10000;
        while (!activity.getWebInterface().isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK)
                && SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(activity.getWebInterface().isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK));
    }

    @Test
    public void callbackAndOriginalKeyFireOnlyOnce() throws Throwable {
        activityRule.runOnUiThread(() -> {
            // Simulate Android 16 invoking the callback before forwarding the same
            // physical key. Its timestamp precedes the callback's back action.
            long keyTime = SystemClock.uptimeMillis() - 1;
            activity.getOnBackPressedDispatcher().onBackPressed();
            assertTrue(webView.dispatchKeyEvent(new KeyEvent(keyTime, keyTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0)));
            assertTrue(webView.dispatchKeyEvent(new KeyEvent(keyTime, keyTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0)));
        });
        assertEventCount(1);
        assertFalse(activity.isFinishing());
    }

    @Test
    public void consecutiveCallbacksAreNotDebounced() throws Throwable {
        activityRule.runOnUiThread(() -> {
            for (int i = 0; i < 3; i++) {
                activity.getOnBackPressedDispatcher().onBackPressed();
            }
        });
        assertEventCount(3);
    }

    @Test
    public void laterLegacyKeyStillFires() throws Throwable {
        activityRule.runOnUiThread(() -> activity.getOnBackPressedDispatcher().onBackPressed());
        assertEventCount(1);
        activityRule.runOnUiThread(() -> {
            long keyTime = SystemClock.uptimeMillis();
            assertTrue(webView.dispatchKeyEvent(new KeyEvent(keyTime, keyTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0)));
            assertTrue(webView.dispatchKeyEvent(new KeyEvent(keyTime, keyTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0)));
        });
        assertEventCount(2);
    }

    @Test
    public void closingCustomViewDoesNotAlsoFireBackbutton() throws Throwable {
        AtomicBoolean customViewHidden = new AtomicBoolean();
        activityRule.runOnUiThread(() -> {
            activity.getWebInterface().showCustomView(new View(activity),
                    () -> customViewHidden.set(true));
            long keyTime = SystemClock.uptimeMillis() - 1;
            activity.getOnBackPressedDispatcher().onBackPressed();
            assertTrue(customViewHidden.get());
            webView.dispatchKeyEvent(new KeyEvent(keyTime, keyTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0));
        });
        assertEventCount(0);
    }

    @Test
    public void sharedHandlerNavigatesHistoryWithoutListener() throws Throwable {
        removeBackListener();
        activityRule.runOnUiThread(() -> webView.loadUrl("https://localhost/backbuttondispatch/second.html"));
        assertNotNull(activity.onPageFinishedUrl.poll(10, TimeUnit.SECONDS));
        awaitValue("window.backHistoryPageReady === true", "true");
        activityRule.runOnUiThread(() -> {
            assertTrue("History size=" + webView.copyBackForwardList().getSize()
                    + ", index=" + webView.copyBackForwardList().getCurrentIndex()
                    + ", URL=" + webView.getUrl(), activity.getWebInterface().canGoBack());
            assertTrue(activity.getWebInterface().handleBackButton());
        });
        awaitValue("window.backTestReady === true", "true");
        assertEventCount(0);
    }

    @Test
    public void sharedHandlerLeavesUnhandledBackToAndroid() throws Throwable {
        removeBackListener();
        activityRule.runOnUiThread(() -> {
            assertFalse(activity.getWebInterface().canGoBack());
            assertFalse(activity.getWebInterface().handleBackButton());
        });
        assertEventCount(0);
    }

    private void removeBackListener() throws Throwable {
        evaluate("document.removeEventListener('backbutton', window.backTestHandler)");
        long deadline = SystemClock.uptimeMillis() + 10000;
        while (activity.getWebInterface().isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK)
                && SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(activity.getWebInterface().isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK));
    }

    private void assertEventCount(int expected) throws Throwable {
        awaitValue("window.backEventCount", Integer.toString(expected));
        // Allow pending bridge deliveries to expose an unexpected second event.
        Thread.sleep(200);
        assertEquals(Integer.toString(expected), evaluate("window.backEventCount"));
    }

    private void awaitValue(String script, String expected) throws Throwable {
        long deadline = SystemClock.uptimeMillis() + 10000;
        while (SystemClock.uptimeMillis() < deadline) {
            if (expected.equals(evaluate(script))) {
                return;
            }
            Thread.sleep(20);
        }
        assertEquals(expected, evaluate(script));
    }

    private String evaluate(String script) throws Throwable {
        ArrayBlockingQueue<String> result = new ArrayBlockingQueue<>(1);
        activityRule.runOnUiThread(() -> webView.evaluateJavascript(script, result::add));
        String value = result.poll(10, TimeUnit.SECONDS);
        assertNotNull("JavaScript evaluation timed out", value);
        return value;
    }
}
