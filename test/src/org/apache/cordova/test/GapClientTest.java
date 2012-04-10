package org.apache.cordova.test;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.api.PluginManager;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class GapClientTest extends ActivityInstrumentationTestCase2<PhoneGapViewTestActivity> {
	
	private PhoneGapViewTestActivity testActivity;
	private FrameLayout containerView;
	private LinearLayout innerContainer;
	private View testView;
	private String rString;
	private CordovaChromeClient appCode;

	public GapClientTest() {
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
	
	public void testForCordovaView() {
	    String className = testView.getClass().getSimpleName();
	    assertTrue(className.equals("CordovaWebView"));
	}
	
	
}
