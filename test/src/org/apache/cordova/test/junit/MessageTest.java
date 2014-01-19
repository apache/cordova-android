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

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.ScrollEvent;
import org.apache.cordova.pluginApi.pluginStub;
import org.apache.cordova.test.CordovaWebViewTestActivity;
import org.apache.cordova.test.R;

import com.jayway.android.robotium.solo.By;
import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

public class MessageTest extends
ActivityInstrumentationTestCase2<CordovaWebViewTestActivity> { 
    private CordovaWebViewTestActivity testActivity;
    private CordovaWebView testView;
    private pluginStub testPlugin;
    private int TIMEOUT = 1000;
    
    private Solo solo;

    public MessageTest() {
        super("org.apache.cordova.test.activities", CordovaWebViewTestActivity.class);
      }

      protected void setUp() throws Exception {
        super.setUp();
        testActivity = this.getActivity();
        testView = (CordovaWebView) testActivity.findViewById(R.id.cordovaWebView);
        testPlugin = (pluginStub) testView.pluginManager.getPlugin("PluginStub");
        solo = new Solo(getInstrumentation(), getActivity());
      }
      
      public void testOnScrollChanged()
      {
          solo.waitForWebElement(By.textContent("Cordova Android Tests"));
          solo.scrollDown();
          sleep();
          Object data = testPlugin.data;
          assertTrue(data.getClass().getSimpleName().equals("ScrollEvent"));
      }

      
      
      private void sleep() {
          try {
            Thread.sleep(TIMEOUT);
          } catch (InterruptedException e) {
            fail("Unexpected Timeout");
          }
        }
}
