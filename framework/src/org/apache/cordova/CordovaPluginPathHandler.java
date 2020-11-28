package org.apache.cordova;

import androidx.webkit.WebViewAssetLoader;

/**
 * Wrapper class for path and handler
 */
public class CordovaPluginPathHandler {

    private final String path;
    private final WebViewAssetLoader.PathHandler handler;

    public  CordovaPluginPathHandler(String path, WebViewAssetLoader.PathHandler handler) {
        this.path = path;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public WebViewAssetLoader.PathHandler getPathHandler() {
        return handler;
    }
}
