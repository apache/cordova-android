/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.cordova.test;


import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import org.apache.cordova.CordovaWebView;

import java.io.IOException;
import java.lang.reflect.Method;

public class CordovaPluginTest extends BaseCordovaIntegrationTest {

    protected void setUp() throws Exception {
        super.setUp();
        setUpWithStartUrl(null);
    }

    private void invokeBlockingCallToLifeCycleEvent(final String lifeCycleEventName) {
        testActivity.runOnUiThread( new Runnable() {
            public void run() {
                try {
                    Method method = getInstrumentation().getClass().getMethod(lifeCycleEventName, Activity.class);
                    method.invoke(getInstrumentation(), testActivity);
                } catch (Exception e) {
                    fail("An Exception occurred in invokeBlockingCallToLifeCycleEvent while invoking " + lifeCycleEventName);
                }
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    public void testPluginLifeCycle() throws IOException {
        //TODO: add coverage for both cases where handleOnStart is called in CordovaActivity (onStart and init)
        //currently only one of the cases is covered
        //TODO: add coverage for both cases where onStart is called in CordovaWebViewImpl (handleOnStart and init)
        //currently only one of the cases is covered
        LifeCyclePlugin testPlugin = (LifeCyclePlugin)cordovaWebView.getPluginManager().getPlugin("LifeCycle");
        testPlugin.calls = "";
        // testOnStart
        invokeBlockingCallToLifeCycleEvent("callActivityOnStart");
        invokeBlockingCallToLifeCycleEvent("callActivityOnResume");
        invokeBlockingCallToLifeCycleEvent("callActivityOnPause");
        invokeBlockingCallToLifeCycleEvent("callActivityOnStop");
        assertEquals("start,resume,pause,stop,", testPlugin.calls);
    }
}
