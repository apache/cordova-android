package org.apache.cordova.unittests;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.apache.cordova.CordovaWebView;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.web.sugar.Web.onWebView;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class EmbeddedWebViewTest {

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            EmbeddedWebViewActivity.class);

    @Test
    public void checkWebViewTest() {
        EmbeddedWebViewActivity activity = (EmbeddedWebViewActivity) mActivityRule.getActivity();
        assertNotNull(activity.webInterface);
        assertNotNull(activity.webInterface.getPluginManager());
    }
}
