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

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.test.SabotagedActivity;
import org.apache.cordova.test.splashscreen;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class IntentUriOverrideTest extends ActivityInstrumentationTestCase2<SabotagedActivity> {
    
    private int TIMEOUT = 1000;
    
    private SabotagedActivity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private CordovaWebView testView;
    private Instrumentation mInstr;
    private String BAD_URL = "file:///sdcard/download/wl-exploit.htm";


    @SuppressWarnings("deprecation")
    public IntentUriOverrideTest()
    {
        super("org.apache.cordova.test",SabotagedActivity.class);
    }
    
    
    protected void setUp() throws Exception {
        super.setUp();
        mInstr = this.getInstrumentation();
        Intent badIntent = new Intent();
        badIntent.setClassName("org.apache.cordova.test", "org.apache.cordova.test.SabotagedActivity");
        badIntent.putExtra("url", BAD_URL);
        setActivityIntent(badIntent);
        testActivity = getActivity();
        containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
        innerContainer = (LinearLayout) containerView.getChildAt(0);
        testView = (CordovaWebView) innerContainer.getChildAt(0);
    }
    
    
    public void testPreconditions(){
        assertNotNull(innerContainer);
        assertNotNull(testView);
    }
    
    public void testChangeStartUrl() throws Throwable
    {
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                sleep();
                boolean isBadUrl = testView.getUrl().equals(BAD_URL);
                assertFalse(isBadUrl);
            }
        });
    }

    private void sleep() {
        try {
          Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
          fail("Unexpected Timeout");
        }
    }
    

}
