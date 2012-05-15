package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ErrorUrlTest extends ActivityInstrumentationTestCase2<errorurl> {

  errorurl testActivity;
  private FrameLayout containerView;
  private LinearLayout innerContainer;
  private CordovaWebView testView;
  
  public ErrorUrlTest() {
    super("org.apache.cordova.test",errorurl.class);
  }
  
  
  protected void setUp() throws Exception {
      super.setUp();
      testActivity = this.getActivity();
      containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
      innerContainer = (LinearLayout) containerView.getChildAt(0);
      testView = (CordovaWebView) innerContainer.getChildAt(0);
      
  }
  
  public void testPreconditions(){
      assertNotNull(innerContainer);
      assertNotNull(testView);
  }
  
}
