package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.PluginManager;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class JailTest extends ActivityInstrumentationTestCase2<JailActivity> {

    private JailActivity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private CordovaWebView testView;
    private static final long TIMEOUT = 2000;

    public JailTest()
    {
        super("com.phonegap.test.activities",JailActivity.class);
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
    

    public void testForCordovaView() {
        String className = testView.getClass().getSimpleName();
        assertTrue(className.equals("CordovaWebView"));
    }
    
    public void testForJailedItems() {
        sleep();
        String url = testView.getUrl();
        assertTrue(url.contains("file:///data/data/"));
    }
    
    public void testForJailCheck() {
       sleep();
       assertTrue(testActivity.areAssetsInJail());
    }

    private void sleep() {
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            fail("Unexpected Timeout");
        }
    }

}
