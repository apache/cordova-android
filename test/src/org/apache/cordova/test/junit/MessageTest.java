package org.apache.cordova.test.junit;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.ScrollEvent;
import org.apache.cordova.pluginApi.pluginStub;
import org.apache.cordova.test.CordovaWebViewTestActivity;
import org.apache.cordova.test.R;

import com.jayway.android.robotium.solo.By;
import com.jayway.android.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

public class MessageTest extends
ActivityInstrumentationTestCase2<CordovaWebViewTestActivity> { 
    private CordovaWebViewTestActivity testActivity;
    private CordovaWebView testView;
    private pluginStub testPlugin;
    private int TIMEOUT = 1000;
    
    private Solo solo;

    public MessageTest() {
        super("org.apache.cordova.test.activities", CordovaWebViewTestActivity.class);
      }

      protected void setUp() throws Exception {
        super.setUp();
        testActivity = this.getActivity();
        testView = (CordovaWebView) testActivity.findViewById(R.id.cordovaWebView);
        testPlugin = (pluginStub) testView.pluginManager.getPlugin("PluginStub");
        solo = new Solo(getInstrumentation(), getActivity());
      }
      
      public void testOnScrollChanged()
      {
          solo.waitForWebElement(By.textContent("Cordova Android Tests"));
          solo.scrollDown();
          sleep();
          Object data = testPlugin.data;
          assertTrue(data.getClass().getSimpleName().equals("ScrollEvent"));
      }

      
      
      private void sleep() {
          try {
            Thread.sleep(TIMEOUT);
          } catch (InterruptedException e) {
            fail("Unexpected Timeout");
          }
        }
}
