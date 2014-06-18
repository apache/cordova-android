package org.apache.cordova;

public interface CordovaWebViewClient {
    void onReceivedError(int errorCode, String description, String url);
}
