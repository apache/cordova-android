package org.apache.cordova.unittests;

import android.content.Intent;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.apache.cordova.CordovaWebView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class IFrameTest {

    private static final String START_URL = "file:///android_asset/www/iframe/index.html";
    //I have no idea why we picked 100, but we did.
    private static final int WEBVIEW_ID = 100;

    private TestActivity testActivity;

    // Don't launch the activity, we're going to send it intents
    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            TestActivity.class, true, false);

    @Before
    public void launchApplicationWithIntent() {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        testActivity = (TestActivity) mActivityRule.launchActivity(intent);
    }

    @Test
    public void iFrameHistory() throws Throwable {
        final CordovaWebView cordovaWebView = (CordovaWebView) testActivity.getWebInterface();
        onWebView().withElement(findElement(Locator.ID, "google_maps")).perform(webClick());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
        onWebView().withElement(findElement(Locator.ID, "javascript_load")).perform(webClick());
        mActivityRule.runOnUiThread(new Runnable() {
            public void run()
            {
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
            }
        });
        //Espresso will kill the application and not trigger the backHistory method, which correctly
        //navigates the iFrame history.  backHistory is tied to the back button.
        mActivityRule.runOnUiThread(new Runnable() {
            public void run()
            {
                assertTrue(cordovaWebView.backHistory());
                String url = cordovaWebView.getUrl();
                assertTrue(url.endsWith("index.html"));
                assertFalse(cordovaWebView.backHistory());
            }
        });
    }



}
