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

import org.json.JSONException;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;

public class CordovaUriHelper {
    
    private static final String TAG = "CordovaUriHelper";
    private static final String CORDOVA_EXEC_URL_PREFIX = "http://cdv_exec/";
    
    private CordovaWebView appView;
    private CordovaInterface cordova;
    
    CordovaUriHelper(CordovaInterface cdv, CordovaWebView webView)
    {
        appView = webView;
        cordova = cdv;
    }
    
    
    // Parses commands sent by setting the webView's URL to:
    // cdvbrg:service/action/callbackId#jsonArgs
    void handleExecUrl(String url) {
        int idx1 = CORDOVA_EXEC_URL_PREFIX.length();
        int idx2 = url.indexOf('#', idx1 + 1);
        int idx3 = url.indexOf('#', idx2 + 1);
        int idx4 = url.indexOf('#', idx3 + 1);
        if (idx1 == -1 || idx2 == -1 || idx3 == -1 || idx4 == -1) {
            Log.e(TAG, "Could not decode URL command: " + url);
            return;
        }
        String service    = url.substring(idx1, idx2);
        String action     = url.substring(idx2 + 1, idx3);
        String callbackId = url.substring(idx3 + 1, idx4);
        String jsonArgs   = url.substring(idx4 + 1);
        appView.pluginManager.exec(service, action, callbackId, jsonArgs);
        //There is no reason to not send this directly to the pluginManager
    }
    

    /**
     * Give the host application a chance to take over the control when a new url
     * is about to be loaded in the current WebView.
     *
     * @param view          The WebView that is initiating the callback.
     * @param url           The url to be loaded.
     * @return              true to override, false for default behavior
     */
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // The WebView should support http and https when going on the Internet
        if(url.startsWith("http:") || url.startsWith("https:"))
        {
            // Check if it's an exec() bridge command message.
            if (NativeToJsMessageQueue.ENABLE_LOCATION_CHANGE_EXEC_MODE && url.startsWith(CORDOVA_EXEC_URL_PREFIX)) {
                handleExecUrl(url);
            }
            // We only need to whitelist sites on the Internet! 
            else if(Config.isUrlWhiteListed(url))
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
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
                this.cordova.getActivity().startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                LOG.e(TAG, "Error loading url " + url, e);
            }
        }
        //Default behaviour should be to load the default intent, let's see what happens! 
        return true;
    }

    

}
