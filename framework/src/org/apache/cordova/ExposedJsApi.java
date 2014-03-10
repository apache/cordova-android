package org.apache.cordova;

import org.json.JSONException;

import android.webkit.JavascriptInterface;

/*
 * Any exposed Javascript API MUST implement these three things!
 */

public interface ExposedJsApi {

    @JavascriptInterface
    public String exec(String service, String action, String callbackId, String arguments) throws JSONException;
    public void setNativeToJsBridgeMode(int value);
    public String retrieveJsMessages(boolean fromOnlineEvent);
}
