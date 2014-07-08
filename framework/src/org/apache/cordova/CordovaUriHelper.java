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

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

class CordovaUriHelper {
    
    private static final String TAG = "CordovaUriHelper";
    
    private CordovaWebView appView;
    private CordovaInterface cordova;
    
    CordovaUriHelper(CordovaInterface cdv, CordovaWebView webView)
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
    boolean shouldOverrideUrlLoading(WebView view, String url) {
        // The WebView should support http and https when going on the Internet
        if(url.startsWith("http:") || url.startsWith("https:"))
        {
            // We only need to whitelist sites on the Internet! 
            if(appView.getWhitelist().isUrlWhiteListed(url))
            {
                return false;
            }
        }
        // Give plugins the chance to handle the url
        else if (this.appView.pluginManager.onOverrideUrlLoading(url)) {
            
        }
        else if(url.startsWith("file://") | url.startsWith("data:"))
        {
            //This directory on WebKit/Blink based webviews contains SQLite databases!
            //DON'T CHANGE THIS UNLESS YOU KNOW WHAT YOU'RE DOING!
            return url.contains("app_webview");
        }
        else
        {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                this.cordova.getActivity().startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                LOG.e(TAG, "Error loading url " + url, e);
            }
        }
        //Default behaviour should be to load the default intent, let's see what happens! 
        return true;
    }
}
