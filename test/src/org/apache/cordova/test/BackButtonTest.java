package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class BackButtonTest extends ActivityInstrumentationTestCase2<backbuttonmultipage> {

  private backbuttonmultipage testActivity;
  private FrameLayout containerView;
  private LinearLayout innerContainer;
  private CordovaWebView testView;
  private TouchUtils touchTest;
  private long TIMEOUT = 5000;
  
  public BackButtonTest() {
    super("org.apache.cordova.test",backbuttonmultipage.class);
  }

  protected void setUp() throws Exception {
    super.setUp();
    testActivity = this.getActivity();
    containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
    innerContainer = (LinearLayout) containerView.getChildAt(0);
    testView = (CordovaWebView) innerContainer.getChildAt(0);
    touchTest = new TouchUtils();
  }
  
  public void testPreconditions(){
    assertNotNull(innerContainer);
    assertNotNull(testView);
  }

  public void testClick() {
    touchTest.tapView(this, testView);
  }
  
  private void sleep() {
    try {
      Thread.sleep(TIMEOUT );
    } catch (InterruptedException e) {
      fail("Unexpected Timeout");
    }
  }

}
