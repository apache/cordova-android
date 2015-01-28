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

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebView;

class AndroidCookieManager implements ICordovaCookieManager {

    protected WebView webView;

    public AndroidCookieManager(WebView webview) {
        webView = webview;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
    }

    public void setCookiesEnabled(boolean accept) {
        CookieManager.getInstance().setAcceptCookie(accept);
    }

    public void setCookie(final String url, final String value) {
        CookieManager.getInstance().setCookie(url, value);
    }

    public String getCookie(final String url) {
        return CookieManager.getInstance().getCookie(url);
    }

    public void clearCookies() {
        CookieManager.getInstance().removeAllCookie();
    }

    public void flush() {
        CookieManager.getInstance().flush();
    }
};

