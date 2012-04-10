package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;
import com.phonegap.api.PluginManager;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CordovaActivityTest extends ActivityInstrumentationTestCase2<PhoneGapActivity> {

    private PhoneGapActivity testActivity;
    private FrameLayout containerView;
    private LinearLayout innerContainer;
    private CordovaWebView testView;
    
    public CordovaActivityTest()
    {
        super("com.phonegap.test.activities",PhoneGapActivity.class);
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
    
    public void testForLinearLayout() {
        String className = innerContainer.getClass().getSimpleName();
        assertTrue(className.equals("LinearLayoutSoftKeyboardDetect"));
    }


}
