package org.apache.cordova;

import android.net.Uri;
import android.webkit.ValueCallback;

public interface CordovaChromeClient {

    int FILECHOOSER_RESULTCODE = 0;

    void setWebView(CordovaWebView appView);

    ValueCallback<Uri> getValueCallback();

}
