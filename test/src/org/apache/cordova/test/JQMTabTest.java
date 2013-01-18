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


import org.apache.cordova.CordovaWebView;
import org.apache.cordova.test.util.Purity;
import org.apache.cordova.test.actions.jqmtabbackbutton;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class JQMTabTest extends ActivityInstrumentationTestCase2<jqmtabbackbutton> {

  private Instrumentation mInstr;
  private jqmtabbackbutton testActivity;
  private FrameLayout containerView; 
  private LinearLayout innerContainer;
  private CordovaWebView testView;
  private Purity touchTool;

  public JQMTabTest(Class<jqmtabbackbutton> activityClass) {
    super(activityClass);
    // TODO Auto-generated constructor stub
  }
  
  protected void setUp() throws Exception {
      super.setUp();
      mInstr = this.getInstrumentation();
      testActivity = this.getActivity();
      containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
      innerContainer = (LinearLayout) containerView.getChildAt(0);
      testView = (CordovaWebView) innerContainer.getChildAt(0);
      touchTool = new Purity(testActivity, getInstrumentation());
  }
  
  

}
