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
import org.apache.cordova.api.DataResource;
import org.apache.cordova.api.DataResourceContext;
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
        // We need to support the new DataResource intercepts without breaking the shouldInterceptRequest mechanism.
        DataResource dataResource = DataResource.initiateNewDataRequestForUri(url, this.appView.pluginManager, cordova,
                "WebViewClient.shouldInterceptRequest");
        url = dataResource.getUri().toString();

        // This mechanism is no longer needed due to the dataResource mechanism. It would be awesome to just get rid of it.
        //Check if plugins intercept the request
        WebResourceResponse ret = super.shouldInterceptRequest(view, url);

        if(ret == null) {
            try {
                InputStream is;
                String mimeType;
                if((is = dataResource.getInputStream()) != null && (mimeType = dataResource.getMimeType()) != null) {
                    // If we don't know how to open this file, let the browser continue loading
                    ret = new WebResourceResponse(mimeType, "UTF-8", is);
                }
            } catch(IOException e) {
                LOG.e("IceCreamCordovaWebViewClient", "Error occurred while loading a file.", e);
            }
        }
        return ret;
    }
}
