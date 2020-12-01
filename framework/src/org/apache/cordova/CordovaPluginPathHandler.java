package org.apache.cordova;

import androidx.webkit.WebViewAssetLoader;

/**
 * Wrapper class for path and handler
 */
public class CordovaPluginPathHandler {

    private final WebViewAssetLoader.PathHandler handler;

    public  CordovaPluginPathHandler(WebViewAssetLoader.PathHandler handler) {
        this.handler = handler;
    }

    public WebViewAssetLoader.PathHandler getPathHandler() {
        return handler;
    }
}
