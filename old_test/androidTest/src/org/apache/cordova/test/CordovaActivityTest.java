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

package org.apache.cordova.test;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.engine.SystemWebView;

public class CordovaActivityTest extends BaseCordovaIntegrationTest {
    private ViewGroup innerContainer;
    private View testView;

    protected void setUp() throws Exception {
        super.setUp();
        setUpWithStartUrl(null);
        testView = (ViewGroup)containerView.getChildAt(0);
    }

    public void testBasicLoad() throws Exception {
        assertTrue(testView instanceof SystemWebView);
        assertTrue(((CordovaWebViewEngine.EngineView)testView).getCordovaWebView() != null);
        String onPageFinishedUrl = testActivity.onPageFinishedUrl.take();
        assertEquals(MainTestActivity.START_URL, onPageFinishedUrl);
    }
    protected void createViews() {
        assertTrue(testView instanceof SystemWebView);
    }
}
