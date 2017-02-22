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


public class HtmlNotFoundTest extends BaseCordovaIntegrationTest {
  private static final String START_URL = "file:///android_asset/www/htmlnotfound/index.html";

  protected void setUp() throws Exception {
    super.setUp();
    setUpWithStartUrl(START_URL);
  }
  public void testUrl() throws Throwable
  {
      runTestOnUiThread(new Runnable() {
          public void run() {
              assertTrue(START_URL.equals(testActivity.getCordovaWebView().getUrl()));
          }
      });

      //loading a not-found file causes an application error and displayError is called
      //the test activity overrides displayError to add message to onPageFinishedUrl
      String message = testActivity.onPageFinishedUrl.take();
      assertTrue(message.contains(START_URL));
      assertTrue(message.contains("ERR_FILE_NOT_FOUND"));
  }

}
