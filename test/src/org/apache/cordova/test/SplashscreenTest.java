package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;

import android.app.Dialog;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SplashscreenTest extends ActivityInstrumentationTestCase2<splashscreen> {
  
  private splashscreen testActivity;
  private Dialog containerView;

  public SplashscreenTest()
  {
      super("org.apache.cordova.test",splashscreen.class);
  }
  
  protected void setUp() throws Exception {
      super.setUp();
      testActivity = this.getActivity();
      //containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
      //containerView = (Dialog) testActivity.findViewById(id);
    }

}
