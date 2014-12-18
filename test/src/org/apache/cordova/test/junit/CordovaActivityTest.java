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

package org.apache.cordova.test.junit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.cordova.AndroidWebView;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginManager;
import org.apache.cordova.test.MainTestActivity;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaPreferences;

import android.app.Instrumentation;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CordovaActivityTest extends ActivityInstrumentationTestCase2<MainTestActivity> {

    private MainTestActivity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private CordovaWebView testView;
    private Instrumentation mInstr;
    private int TIMEOUT = 1000;
    
    @SuppressWarnings("deprecation")
    public CordovaActivityTest()
    {
        super("org.apache.cordova.test",MainTestActivity.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        mInstr = this.getInstrumentation();
        testActivity = this.getActivity();
        containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
        innerContainer = (LinearLayout) containerView.getChildAt(0);
        testView = testActivity.cordovaWebView;
        
    }
    
    public void testPreconditions(){
        assertNotNull(innerContainer);
        assertNotNull(testView);
    }
    

    public void testForAndroidWebView() {
        String className = testView.getClass().getSimpleName();
        if (androidWebView()) {
            assertTrue(className.equals("AndroidWebView"));
        } else {
            assertFalse(className.equals("AndroidWebView"));
        }
    }
    
    public void testForLinearLayout() {
        String className = innerContainer.getClass().getSimpleName();
        assertTrue(className.equals("LinearLayoutSoftKeyboardDetect"));
    }

    private void sleep() {
        try {
          Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
          fail("Unexpected Timeout");
        }
    }

    private boolean androidWebView () {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(testActivity);
        CordovaPreferences preferences = parser.getPreferences();
        String r = preferences.getString("webView", null);
        if (r != null) {
            return false;
        }
        return true;
    }
}
