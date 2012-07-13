package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ErrorUrlTest extends ActivityInstrumentationTestCase2<errorurl> {

  private int TIMEOUT = 1000;
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
  
  public void testUrl()
  {
    sleep();
    String good_url = "file:///android_asset/www/htmlnotfound/error.html";
    String url = testView.getUrl();
    assertNotNull(url);
    assertTrue(url.equals(good_url));
  }
  

  private void sleep() {
      try {
          Thread.sleep(TIMEOUT);
      } catch (InterruptedException e) {
          fail("Unexpected Timeout");
      }
  }

  
}
