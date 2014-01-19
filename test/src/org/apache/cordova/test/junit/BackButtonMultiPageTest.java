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
import org.apache.cordova.test.backbuttonmultipage;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class BackButtonMultiPageTest extends ActivityInstrumentationTestCase2<backbuttonmultipage> {

  private int TIMEOUT = 2000;
  backbuttonmultipage testActivity;
  private FrameLayout containerView;
  private LinearLayout innerContainer;
  private CordovaWebView testView;
  

  public BackButtonMultiPageTest() {
    super(backbuttonmultipage.class);
  }

  @UiThreadTest
  protected void setUp() throws Exception {
      super.setUp();
      testActivity = this.getActivity();
      containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
      innerContainer = (LinearLayout) containerView.getChildAt(0);
      testView = (CordovaWebView) innerContainer.getChildAt(0);
      testView.loadUrl("file:///android_asset/www/backbuttonmultipage/index.html");
      sleep();
  }

  @UiThreadTest
  public void testPreconditions(){
      assertNotNull(innerContainer);
      assertNotNull(testView);
      String url = testView.getUrl();
      assertTrue(url.endsWith("index.html"));
  }
  
  public void testViaHref() throws Throwable {
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              testView.sendJavascript("window.location = 'sample2.html';");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              testView.sendJavascript("window.location = 'sample3.html';");          }
      });
     
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample3.html"));
              testView.backHistory();
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              testView.backHistory();
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("index.html"));
          }
      });
  }
  
  public void testViaLoadUrl() throws Throwable {
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample3.html"));
              testView.backHistory();
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              testView.backHistory();
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("index.html"));
              testView.backHistory();
          }
      });
  }

  public void testViaBackButtonOnView() throws Throwable {
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample3.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(testView, true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(testView, true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("index.html"));
          }
      });
      
  }
  
  public void testViaBackButtonOnLayout() throws Throwable {
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample3.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(containerView, true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(containerView, true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      sleep();
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = testView.getUrl();
              assertTrue(url.endsWith("index.html"));
          }
      });
      
  }
  
  @UiThreadTest
  private void sleep() {
      try {
          Thread.sleep(TIMEOUT);
      } catch (InterruptedException e) {
          fail("Unexpected Timeout");
      }
  }

}

