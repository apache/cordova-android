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


import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;

public class BackButtonMultiPageTest extends BaseCordovaIntegrationTest {
  private static final String START_URL = "file:///android_asset/www/backbuttonmultipage/index.html";

  @Override
  public void setUp() throws Exception {
      super.setUp();
      setUpWithStartUrl(START_URL);
  }

  public void testViaHref() throws Throwable {
      assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              cordovaWebView.sendJavascript("window.location = 'sample2.html';");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              cordovaWebView.sendJavascript("window.location = 'sample3.html';");          }
      });

      assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              assertTrue(cordovaWebView.backHistory());
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              assertTrue(cordovaWebView.backHistory());
          }
      });
      assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              assertFalse(cordovaWebView.backHistory());
          }
      });
  }
  
  public void testViaLoadUrl() throws Throwable {
      assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              cordovaWebView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              cordovaWebView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              assertTrue(cordovaWebView.backHistory());
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              assertTrue(cordovaWebView.backHistory());
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/index.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              assertFalse(cordovaWebView.backHistory());
          }
      });
  }

  public void testViaBackButtonOnView() throws Throwable {
      assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run() {
              cordovaWebView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run() {
              cordovaWebView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = cordovaWebView.getUrl();
              assertTrue(url.endsWith("sample3.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(cordovaWebView.getView(), true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run()
          {
              String url = cordovaWebView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(cordovaWebView.getView(), true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/index.html", testActivity.onPageFinishedUrl.take());
  }
  
  public void testViaBackButtonOnLayout() throws Throwable {
      assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run() {
              cordovaWebView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run() {
              cordovaWebView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample3.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run() {
              String url = cordovaWebView.getUrl();
              assertTrue(url.endsWith("sample3.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(containerView, true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/sample2.html", testActivity.onPageFinishedUrl.take());
      runTestOnUiThread(new Runnable() {
          public void run() {
              String url = cordovaWebView.getUrl();
              assertTrue(url.endsWith("sample2.html"));
              BaseInputConnection viewConnection = new BaseInputConnection(containerView, true);
              KeyEvent backDown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
              KeyEvent backUp = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
              viewConnection.sendKeyEvent(backDown);
              viewConnection.sendKeyEvent(backUp);
          }
      });
      assertEquals("file:///android_asset/www/backbuttonmultipage/index.html", testActivity.onPageFinishedUrl.take());
  }
}

