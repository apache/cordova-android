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

import android.os.SystemClock;
import android.webkit.WebView;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

final class WebViewTestUtils {
    private static final long TIMEOUT_MS = 10000;

    private WebViewTestUtils() {}

    static void awaitPageFinished(TestActivity activity, String expectedUrl) throws InterruptedException {
        long deadline = SystemClock.uptimeMillis() + TIMEOUT_MS;
        List<String> observed = new ArrayList<>();
        while (SystemClock.uptimeMillis() < deadline) {
            String url = activity.onPageFinishedUrl.poll(
                    Math.max(1, deadline - SystemClock.uptimeMillis()), TimeUnit.MILLISECONDS);
            if (expectedUrl.equals(url)) {
                return;
            }
            if (url == null) {
                break;
            }
            observed.add(url);
        }
        fail("Timed out waiting for page " + expectedUrl + "; finished URLs: " + observed);
    }

    static String evaluateJavascript(ActivityTestRule<?> rule, TestActivity activity,
            String expression) throws Throwable {
        ArrayBlockingQueue<String> result = new ArrayBlockingQueue<>(1);
        rule.runOnUiThread(() -> ((WebView) activity.getWebInterface().getView())
                .evaluateJavascript(expression, result::offer));
        String value = result.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        assertNotNull("JavaScript evaluation timed out: " + expression, value);
        return value;
    }

    static void clickElement(ActivityTestRule<?> rule, TestActivity activity, String id) throws Throwable {
        JSONArray position = new JSONArray(evaluateJavascript(rule, activity,
                "(function () { var element = document.getElementById('" + id + "');"
                + "element.scrollIntoView(); var rect = element.getBoundingClientRect();"
                + "return [(rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2, innerWidth]; })()"));
        float x = (float) position.getDouble(0);
        float y = (float) position.getDouble(1);
        float viewportWidth = (float) position.getDouble(2);
        // DOM-generated clicks do not activate a document. Chromium can skip those
        // history entries during Back, so navigation tests need a real touch gesture.
        onView(sameInstance(activity.getWebInterface().getView())).perform(new GeneralClickAction(
                Tap.SINGLE, view -> {
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    float scale = view.getWidth() / viewportWidth;
                    return new float[] {location[0] + x * scale, location[1] + y * scale};
                }, Press.FINGER));
    }

    static void awaitJavascript(ActivityTestRule<?> rule, TestActivity activity,
            String condition) throws Throwable {
        long deadline = SystemClock.uptimeMillis() + TIMEOUT_MS;
        String value = null;
        while (SystemClock.uptimeMillis() < deadline) {
            value = evaluateJavascript(rule, activity, condition);
            if ("true".equals(value)) {
                return;
            }
            SystemClock.sleep(20);
        }
        fail("Timed out waiting for JavaScript condition: " + condition + "; last value: " + value);
    }
}
