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

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginManager;
import org.apache.cordova.test.CordovaWebViewTestActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class GapClientTest extends ActivityInstrumentationTestCase2<CordovaWebViewTestActivity> {
	
	private CordovaWebViewTestActivity testActivity;
	private View testView;
	private String rString;

	public GapClientTest() {
		super("org.apache.cordova.test.activities",CordovaWebViewTestActivity.class);
	}
	
	protected void setUp() throws Exception{
		super.setUp();
		testActivity = this.getActivity();
		testView = testActivity.cordovaWebView.getView();
		
	}
	
	public void testPreconditions(){
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
