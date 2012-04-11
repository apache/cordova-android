package org.apache.cordova.test;

import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaChromeClient;

import org.apache.cordova.test.CordovaViewFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.android.library.AndroidWebDriver;

import android.test.ActivityInstrumentationTestCase2;

public class WebDriverTest extends ActivityInstrumentationTestCase2<CordovaDriverAction> {
	
    private static final long TIMEOUT = 5000;
    private CordovaDriverAction testActivity;
    private CordovaWebView testView;
    private CordovaViewFactory viewFactory;
    private CordovaChromeClient appCode;
    private CordovaWebViewClient viewHandler;
    private AndroidWebDriver testDriver;

	public WebDriverTest() {
		super("com.phonegap.test.activities",CordovaDriverAction.class);
	}
	
	protected void setUp() throws Exception{
		super.setUp();
		
		testActivity = this.getActivity();
		viewFactory = new CordovaViewFactory();
		appCode = new CordovaChromeClient(testActivity);
		viewHandler = new CordovaWebViewClient(testActivity);
		testDriver = new AndroidWebDriver(testActivity, viewFactory, viewHandler, appCode);
		testView = (CordovaWebView) testDriver.getWebView();
	}
	
	public void testPreconditions(){
		assertNotNull(testView);
	}
	
	public void testWebLoad() {
	    testDriver.get("file:///android_asset/www/index.html");
	    sleep();
	    String url = testView.getUrl();
	    //Check the sanity!
	    boolean result = url.equals("file:///android_asset/www/index.html");
	    assertTrue(result);
	    WebElement platformSpan = testDriver.findElement(By.id("platform"));
	    String text = platformSpan.getText();
	    assertTrue(text.equals("Android"));
	}
	
	
	private void sleep() {
	    try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            fail("Unexpected Timeout");
        }
	}
}
