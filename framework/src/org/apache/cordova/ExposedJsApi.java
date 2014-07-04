package org.apache.cordova;

import org.json.JSONException;

/*
 * Any exposed Javascript API MUST implement these three things!
 */
public interface ExposedJsApi {
    public String exec(int bridgeSecret, String service, String action, String callbackId, String arguments) throws JSONException, IllegalAccessException;
    public void setNativeToJsBridgeMode(int bridgeSecret, int value) throws IllegalAccessException;
    public String retrieveJsMessages(int bridgeSecret, boolean fromOnlineEvent) throws IllegalAccessException;
}
