package org.apache.cordova.test.junit;
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


import org.apache.cordova.CordovaWebView;
import org.apache.cordova.test.iframe;
import org.apache.cordova.test.util.Purity;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class IFrameTest extends ActivityInstrumentationTestCase2 {
  
    
    private Instrumentation mInstr;
    private Activity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private CordovaWebView testView;
    private TouchUtils touch;
    private Purity touchTool;
    
    public IFrameTest() {
        super("org.apache.cordova.test",iframe.class);
    }

  
    protected void setUp() throws Exception {
      super.setUp();
      mInstr = this.getInstrumentation();
      testActivity = this.getActivity();
      containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
      innerContainer = (LinearLayout) containerView.getChildAt(0);
      testView = (CordovaWebView) innerContainer.getChildAt(0);
      touch = new TouchUtils();
      touchTool = new Purity(testActivity, getInstrumentation());
    }
  
  
    public void testIframeDest() throws Throwable
    {
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                testView.sendJavascript("loadUrl('http://maps.google.com/maps?output=embed');");
            }
        });
        sleep(3000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                testView.sendJavascript("loadUrl('index2.html')");
            }
        });
        sleep(1000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                String url = testView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
    }
    
    public void testIframeHistory() throws Throwable
    {
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                testView.sendJavascript("loadUrl('http://maps.google.com/maps?output=embed');");
            }
        });
        sleep(3000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                testView.sendJavascript("loadUrl('index2.html')");
            }
        });
        sleep(1000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                String url = testView.getUrl();
                testView.backHistory();
            }
        });
        sleep(1000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                String url = testView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
    }
    
    private void sleep(int timeout) {
        try {
          Thread.sleep(timeout);
        } catch (InterruptedException e) {
          fail("Unexpected Timeout");
        }
    }
}
