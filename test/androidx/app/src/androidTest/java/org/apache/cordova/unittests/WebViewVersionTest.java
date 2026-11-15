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

import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WebViewVersionTest {
    @Test
    public void loadedProviderMatchesRequestedVersion() {
        AtomicReference<PackageInfo> loaded = new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            WebView view = new WebView(InstrumentationRegistry.getInstrumentation().getTargetContext());
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    loaded.set(WebView.getCurrentWebViewPackage());
                } else {
                    // API 24/25 have no public provider API and their AOSP dumpsys
                    // service can return no output. Query the initialized factory.
                    java.lang.reflect.Method method = Class.forName("android.webkit.WebViewFactory")
                            .getDeclaredMethod("getLoadedPackageInfo");
                    method.setAccessible(true);
                    loaded.set((PackageInfo) method.invoke(null));
                }
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("Cannot identify loaded WebView provider", e);
            } finally {
                view.destroy();
            }
        });
        PackageInfo provider = loaded.get();
        assertNotNull("No loaded WebView provider", provider);
        Log.i("WebViewVersionTest", "Loaded provider: " + provider.packageName + " " + provider.versionName);
        String expected = InstrumentationRegistry.getArguments().getString("expectedWebViewMajor", "");
        if (!expected.isEmpty()) {
            assertEquals("Expected the AOSP WebView provider", "com.android.webview", provider.packageName);
            assertTrue("Expected WebView " + expected + ".x, loaded " + provider.versionName,
                    provider.versionName != null && provider.versionName.startsWith(expected + "."));
        }
    }
}
