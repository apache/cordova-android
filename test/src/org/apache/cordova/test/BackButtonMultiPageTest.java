package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class BackButtonMultiPageTest extends ActivityInstrumentationTestCase2<backbuttonmultipage> {

  private int TIMEOUT = 1000;
  backbuttonmultipage testActivity;
  private FrameLayout containerView;
  private LinearLayout innerContainer;
  private CordovaWebView testView;
  

  public BackButtonMultiPageTest() {
    super("org.apache.cordova.test", backbuttonmultipage.class);
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
  
  public void testViaHref() {
      testView.sendJavascript("window.location = 'sample2.html';");
      sleep();
      String url = testView.getUrl();
      assertTrue(url.endsWith("sample2.html"));
      testView.sendJavascript("window.location = 'sample3.html';");
      sleep();
      url = testView.getUrl();
      assertTrue(url.endsWith("sample3.html"));
      boolean didGoBack = testView.backHistory();
      sleep();
      url = testView.getUrl();
      assertTrue(url.endsWith("sample2.html"));
      assertTrue(didGoBack);
      didGoBack = testView.backHistory();
      sleep();
      url = testView.getUrl();
      assertTrue(url.endsWith("index.html"));
      assertTrue(didGoBack);
  }
  
  public void testViaLoadUrl() {
      testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample2.html");
      sleep();
      String url = testView.getUrl();
      assertTrue(url.endsWith("sample2.html"));
      testView.loadUrl("file:///android_asset/www/backbuttonmultipage/sample3.html");
      sleep();
      url = testView.getUrl();
      assertTrue(url.endsWith("sample3.html"));
      boolean didGoBack = testView.backHistory();
      sleep();
      url = testView.getUrl();
      assertTrue(url.endsWith("sample2.html"));
      assertTrue(didGoBack);
      didGoBack = testView.backHistory();
      sleep();
      url = testView.getUrl();
      assertTrue(url.endsWith("index.html"));
      assertTrue(didGoBack);
  }

  private void sleep() {
      try {
          Thread.sleep(TIMEOUT);
      } catch (InterruptedException e) {
          fail("Unexpected Timeout");
      }
  }

}

