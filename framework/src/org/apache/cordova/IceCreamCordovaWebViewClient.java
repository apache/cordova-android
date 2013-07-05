/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.LOG;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class IceCreamCordovaWebViewClient extends CordovaWebViewClient {


    public IceCreamCordovaWebViewClient(CordovaInterface cordova) {
        super(cordova);
    }
    
    public IceCreamCordovaWebViewClient(CordovaInterface cordova, CordovaWebView view) {
        super(cordova, view);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        // Disable checks during shouldInterceptRequest since there is no way to avoid IO here :(.
        UriResolvers.webCoreThread = null;
        try {
            UriResolver uriResolver = appView.resolveUri(Uri.parse(url), true);
            
            if (uriResolver == null && url.startsWith("file:///android_asset/")) {
                if (url.contains("?") || url.contains("#") || needsIceCreamSpecialsInAssetUrlFix(url)) {
                    uriResolver = appView.resolveUri(Uri.parse(url), false);
                }
            }
            
            if (uriResolver != null) {
                try {
                    InputStream stream = uriResolver.getInputStream();
                    String mimeType = uriResolver.getMimeType();
                    // If we don't know how to open this file, let the browser continue loading
                    return new WebResourceResponse(mimeType, "UTF-8", stream);
                } catch (IOException e) {
                    LOG.e("IceCreamCordovaWebViewClient", "Error occurred while loading a file.", e);
                    // Results in a 404.
                    return new WebResourceResponse("text/plain", "UTF-8", null);
                }
            }
            return null;
        } finally {
            // Tell the Thread-Checking resolve what thread the WebCore thread is.
            UriResolvers.webCoreThread = Thread.currentThread();
        }
    }
        
    private static boolean needsIceCreamSpecialsInAssetUrlFix(String url) {
        if (!url.contains("%20")){
            return false;
        }

        switch(android.os.Build.VERSION.SDK_INT){
            case android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH:
            case android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1:
                return true;
            default:
                return false;
        }
    }
}
