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


public class ErrorUrlTest extends BaseCordovaIntegrationTest {
    private static final String START_URL = "file:///android_asset/www/htmlnotfound/index.html";
    private static final String ERROR_URL = "file:///android_asset/www/htmlnotfound/error.html";
    private static final String INVALID_URL = "file:///android_asset/www/invalid.html";

    protected void setUp() throws Exception {
        super.setUp();
        // INVALID_URL tests that errorUrl and url are *not* settable via the intent.
        setUpWithStartUrl(START_URL, "testErrorUrl", ERROR_URL, "errorurl", INVALID_URL, "url", INVALID_URL);
    }

    public void testUrl() throws Throwable {
        assertEquals(START_URL, testActivity.onPageFinishedUrl.take());
        assertEquals(ERROR_URL, testActivity.onPageFinishedUrl.take());
        runTestOnUiThread(new Runnable() {
            public void run() {
                assertEquals(ERROR_URL, testActivity.getCordovaWebView().getUrl());
            }
        });
    }
}