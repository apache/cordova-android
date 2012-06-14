package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.CordovaChromeClient;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class UserWebViewTest extends ActivityInstrumentationTestCase2<userwebview> {

  public UserWebViewTest ()
  {
    super(userwebview.class);
  }
  
  private int TIMEOUT = 1000;
  userwebview testActivity;
  private FrameLayout containerView;
  private LinearLayout innerContainer;
  private CordovaWebView testView;
  

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
  
  public void customTest()
  {
    assertTrue(CordovaWebView.class.isInstance(testView));
    assertTrue(CordovaWebViewClient.class.isInstance(testActivity.testViewClient));
    assertTrue(CordovaChromeClient.class.isInstance(testActivity.testChromeClient));
  }
  

  private void sleep() {
      try {
        Thread.sleep(TIMEOUT);
      } catch (InterruptedException e) {
        fail("Unexpected Timeout");
      }
    }

}
