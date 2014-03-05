package org.apache.cordova;

public interface CordovaWebViewClient {

    void setWebView(CordovaWebView appView);

    void onReceivedError(CordovaWebView me, int i, String string, String url);

}
