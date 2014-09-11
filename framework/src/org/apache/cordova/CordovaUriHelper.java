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

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebView;

public class CordovaUriHelper {
    
    private static final String TAG = "CordovaUriHelper";
    
    private CordovaWebView appView;
    private CordovaInterface cordova;
    
    public CordovaUriHelper(CordovaInterface cdv, CordovaWebView webView)
    {
        appView = webView;
        cordova = cdv;
    }
    
    /**
     * Give the host application a chance to take over the control when a new url
     * is about to be loaded in the current WebView.
     *
     * @param view          The WebView that is initiating the callback.
     * @param url           The url to be loaded.
     * @return              true to override, false for default behavior
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public boolean shouldOverrideUrlLoading(String url) {
        // Give plugins the chance to handle the url
        if (this.appView.getPluginManager().onOverrideUrlLoading(url)) {
            // Do nothing other than what the plugins wanted.
            // If any returned true, then the request was handled.
            return true;
        }
        else if(url.startsWith("file://") | url.startsWith("data:"))
        {
            //This directory on WebKit/Blink based webviews contains SQLite databases!
            //DON'T CHANGE THIS UNLESS YOU KNOW WHAT YOU'RE DOING!
            return url.contains("app_webview");
        }
        else if (appView.getWhitelist().isUrlWhiteListed(url)) {
            // Allow internal navigation
            return false;
        }
        else if (appView.getExternalWhitelist().isUrlWhiteListed(url))
        {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    intent.setSelector(null);
                }
                this.cordova.getActivity().startActivity(intent);
                return true;
            } catch (android.content.ActivityNotFoundException e) {
                LOG.e(TAG, "Error loading url " + url, e);
            }
        }
        // Intercept the request and do nothing with it -- block it
        return true;
    }
}
