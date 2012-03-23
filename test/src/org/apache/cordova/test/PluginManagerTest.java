package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;
import com.phonegap.api.PluginManager;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class PluginManagerTest extends ActivityInstrumentationTestCase2<PhoneGapViewTestActivity> {
	
	private PhoneGapViewTestActivity testActivity;
	private FrameLayout containerView;
	private LinearLayout innerContainer;
	private View testView;
	private String rString;
	private PluginManager pMan;

	public PluginManagerTest() {
		super("com.phonegap.test.activities",PhoneGapViewTestActivity.class);
	}
	
	protected void setUp() throws Exception{
		super.setUp();
		testActivity = this.getActivity();
		containerView = (FrameLayout) testActivity.findViewById(android.R.id.content);
		innerContainer = (LinearLayout) containerView.getChildAt(0);
		testView = innerContainer.getChildAt(0);
		
	}
	
	public void testPreconditions(){
	    assertNotNull(innerContainer);
	    assertNotNull(testView);
	}
	
	
	public void testForPluginManager() {
	    CordovaWebView v = (CordovaWebView) testView;
	    pMan = v.getPluginManager();
	    assertNotNull(pMan);
	    String className = pMan.getClass().getSimpleName();
	    assertTrue(className.equals("PluginManager"));
	}
	

}
