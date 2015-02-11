package org.apache.cordova.test;
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


import android.test.TouchUtils;

import org.apache.cordova.test.util.Purity;

public class IFrameTest extends BaseCordovaIntegrationTest {
    private static final String START_URL = "file:///android_asset/www/iframe/index.html";

    private TouchUtils touch;
    private Purity touchTool;
    
    protected void setUp() throws Exception {
      super.setUp();
      setUpWithStartUrl(START_URL);
      touch = new TouchUtils();
      touchTool = new Purity(testActivity, getInstrumentation());
    }
  
  
    public void testIframeDest() throws Throwable {
        assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                cordovaWebView.sendJavascript("loadUrl('http://maps.google.com/maps?output=embed');");
            }
        });
        sleep(3000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                cordovaWebView.sendJavascript("loadUrl('index2.html')");
            }
        });
        sleep(1000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
    }
    
    public void testIframeHistory() throws Throwable
    {
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                cordovaWebView.sendJavascript("loadUrl('http://maps.google.com/maps?output=embed');");
            }
        });
        sleep(3000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                cordovaWebView.sendJavascript("loadUrl('index2.html')");
            }
        });
        sleep(1000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
                cordovaWebView.backHistory();
            }
        });
        sleep(1000);
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
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
