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
        if(url.contains("?") || url.contains("#") || needsIceCreamSpaceInAssetUrlFix(url)){
            WebResourceResponse ret = generateWebResourceResponse(url);
            if (ret != null) {
                return ret;
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    private WebResourceResponse generateWebResourceResponse(String url) {
        if (url.startsWith("file:///android_asset/")) {
            String mimetype = FileHelper.getMimeType(url, cordova);

            try {
                InputStream stream = FileHelper.getInputStreamFromUriString(url, cordova);
                WebResourceResponse response = new WebResourceResponse(mimetype, "UTF-8", stream);
                return response;
            } catch (IOException e) {
                LOG.e("generateWebResourceResponse", e.getMessage(), e);
            }
        }
        return null;
    }
    
    private static boolean needsIceCreamSpaceInAssetUrlFix(String url) {
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
