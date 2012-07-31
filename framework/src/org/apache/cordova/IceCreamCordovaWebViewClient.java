package org.apache.cordova;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.LOG;

import android.content.res.AssetManager;
import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

public class IceCreamCordovaWebViewClient extends CordovaWebViewClient {


    public IceCreamCordovaWebViewClient(CordovaInterface cordova) {
        super(cordova);
    }
    
    public IceCreamCordovaWebViewClient(CordovaInterface cordova, CordovaWebView view) {
        super(cordova, view);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if(url.contains("?") || url.contains("#")){
            return generateWebResourceResponse(url);
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }

    private WebResourceResponse generateWebResourceResponse(String url) {
        final String ANDROID_ASSET = "file:///android_asset/";
        if (url.startsWith(ANDROID_ASSET)) {
            String niceUrl = url;
            niceUrl = url.replaceFirst(ANDROID_ASSET, "");
            if(niceUrl.contains("?")){
                niceUrl = niceUrl.split("\\?")[0];
            }
            else if(niceUrl.contains("#"))
            {
                niceUrl = niceUrl.split("#")[0];
            }

            String mimetype = null;
            if(niceUrl.endsWith(".html")){
                mimetype = "text/html";
            }

            try {
                AssetManager assets = cordova.getActivity().getAssets();
                Uri uri = Uri.parse(niceUrl);
                InputStream stream = assets.open(uri.getPath(), AssetManager.ACCESS_STREAMING);
                WebResourceResponse response = new WebResourceResponse(mimetype, "UTF-8", stream);
                return response;
            } catch (IOException e) {
                LOG.e("generateWebResourceResponse", e.getMessage(), e);
            }
        }
        return null;
    }
    
}
