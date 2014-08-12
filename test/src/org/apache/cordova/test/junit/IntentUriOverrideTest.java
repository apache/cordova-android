package org.apache.cordova.test.junit;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.test.SabotagedActivity;
import org.apache.cordova.test.splashscreen;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class IntentUriOverrideTest extends ActivityInstrumentationTestCase2<SabotagedActivity> {
    
    private int TIMEOUT = 1000;
    
    private SabotagedActivity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private CordovaWebView testView;
    private Instrumentation mInstr;
    private String BAD_URL = "file:///sdcard/download/wl-exploit.htm";


    public IntentUriOverrideTest()
    {
        super("org.apache.cordova.test",SabotagedActivity.class);
    }
    
    
    protected void setUp() throws Exception {
        super.setUp();
        mInstr = this.getInstrumentation();
        Intent badIntent = new Intent();
        badIntent.setClassName("org.apache.cordova.test", "org.apache.cordova.test.SabotagedActivity");
        badIntent.putExtra("url", BAD_URL);
        setActivityIntent(badIntent);
        testActivity = getActivity();
        containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
        innerContainer = (LinearLayout) containerView.getChildAt(0);
        testView = (CordovaWebView) innerContainer.getChildAt(0);
    }
    
    
    public void testPreconditions(){
        assertNotNull(innerContainer);
        assertNotNull(testView);
    }
    
    public void testChangeStartUrl() throws Throwable
    {
        runTestOnUiThread(new Runnable() {
            public void run()
            {
                boolean isBadUrl = testView.getUrl().equals(BAD_URL);
                assertFalse(isBadUrl);
            }
        });
    }

    private void sleep() {
        try {
          Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
          fail("Unexpected Timeout");
        }
    }
    

}
