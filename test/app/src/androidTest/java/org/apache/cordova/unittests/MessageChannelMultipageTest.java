package org.apache.cordova.unittests;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MessageChannelMultipageTest {
    private static final String START_URL = "file:///android_asset/www/backbuttonmultipage/index.html";
    //I have no idea why we picked 100, but we did.
    private static final int WEBVIEW_ID = 100;
    private TestActivity testActivity;

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            TestActivity.class);

    @Before
    public void launchApplicationWithIntent() {
        Intent intent = new Intent();
        intent.putExtra("startUrl", START_URL);
        testActivity = (TestActivity) mActivityRule.launchActivity(intent);
    }



    //test that after a page load the cached callback id and the live callback id match
    //this is to prevent a regression
    //the issue was that CordovaWebViewImpl's cached instance of CoreAndroid would become stale on page load
    //this is because the cached instance was not being cleared when the pluginManager was reset on page load
    //the plugin manager would get a new instance which would be updated with a new callback id
    //the cached instance's message channel callback id would become stale
    //effectively this caused message channel events to not be delivered
    @Test
    public void testThatCachedCallbackIdIsValid() throws Throwable {
        final CordovaWebView cordovaWebView = testActivity.getWebInterface();
        Class cordovaWebViewImpl = CordovaWebViewImpl.class;
        //send a test event - this initializes cordovaWebViewImpl.appPlugin (the cached instance of CoreAndroid)
        Method method = cordovaWebViewImpl.getDeclaredMethod("sendJavascriptEvent", String.class);
        method.setAccessible(true);
        method.invoke(cordovaWebView, "testEvent");
        sleep(1000);

        //load a page - this resets the plugin manager and nulls cordovaWebViewImpl.appPlugin
        //(previously this resets plugin manager but did not null cordovaWebViewImpl.appPlugin, leading to the issue)
        mActivityRule.runOnUiThread(new Runnable() {
            public void run() {
                cordovaWebView.loadUrl(START_URL);
            }
        });
        assertEquals(START_URL, testActivity.onPageFinishedUrl.take());

        //send a test event - this initializes cordovaWebViewImpl.appPlugin (the cached instance of CoreAndroid)
        method.invoke(cordovaWebView, "testEvent");
        sleep(1000);

        //get reference to package protected class CoreAndroid
        Class coreAndroid = Class.forName("org.apache.cordova.CoreAndroid");

        //get cached CoreAndroid
        Field appPluginField = cordovaWebViewImpl.getDeclaredField("appPlugin");
        appPluginField.setAccessible(true);
        Object cachedAppPlugin = appPluginField.get(cordovaWebView);
        //get cached CallbackContext
        Field messageChannelField = coreAndroid.getDeclaredField("messageChannel");
        messageChannelField.setAccessible(true);
        CallbackContext cachedCallbackContext = (CallbackContext) messageChannelField.get(cachedAppPlugin);

        //get live CoreAndroid
        PluginManager pluginManager = cordovaWebView.getPluginManager();
        Field coreAndroidPluginNameField = coreAndroid.getField("PLUGIN_NAME");
        String coreAndroidPluginName = (String) coreAndroidPluginNameField.get(null);
        Object liveAppPlugin = pluginManager.getPlugin(coreAndroidPluginName);
        //get live CallbackContext
        CallbackContext liveCallbackContext = (CallbackContext) messageChannelField.get(liveAppPlugin);

        //get callback id from live callbackcontext
        String liveCallbackId = (liveCallbackContext != null) ? liveCallbackContext.getCallbackId() : null;
        //get callback id from cached callbackcontext
        String cachedCallbackId = (cachedCallbackContext != null) ? cachedCallbackContext.getCallbackId() : null;

        //verify that the live message channel has been initialized
        assertNotNull(liveCallbackId);
        //verify that the cached message channel and the live message channel have the same id
        assertEquals(liveCallbackId, cachedCallbackId);
    }

    private void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            fail("Unexpected Timeout");
        }
    }
}
