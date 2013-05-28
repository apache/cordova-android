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
                new DataResourceContext("WebViewClient.shouldInterceptRequest", true /* this is from a browser request*/));
        url = dataResource.getUri().toString();

        //Check if plugins intercept the request
        WebResourceResponse ret = super.shouldInterceptRequest(view, url);
//      The below bugfix is taken care of by the dataResource mechanism
//        if(ret == null && (url.contains("?") || url.contains("#") || needsIceCreamSpaceInAssetUrlFix(url))){
//            ret = generateWebResourceResponse(url);
//        }
        if(ret == null) {
            try {
               ret = new WebResourceResponse(dataResource.getMimeType(), "UTF-8", dataResource.getInputStream());
            } catch(IOException e) {
                LOG.e("IceCreamCordovaWebViewClient", "Error occurred while loading a file.", e);
            }
        }
        return ret;
    }
}
