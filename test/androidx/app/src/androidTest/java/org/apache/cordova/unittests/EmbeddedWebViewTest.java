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

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNotNull;

/*
 * This test is to cover the use case of Cordova Android used as a component in a larger Android
 * application.  This test is strictly used to cover this use case.  In this example, the WebView
 * should load, not be null, and the Plugin Manager should also be initialized.
 *

 */


@RunWith(AndroidJUnit4.class)
public class EmbeddedWebViewTest {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            EmbeddedWebViewActivity.class);

    @Test
    public void checkWebViewTest() {
        EmbeddedWebViewActivity activity = (EmbeddedWebViewActivity) mActivityRule.getActivity();
        assertNotNull(activity.webInterface);
        assertNotNull(activity.webInterface.getPluginManager());
    }
}
