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

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

/**
 * Main interface for interacting with a Cordova WebView - implemented by CordovaWebViewImpl.
 * This is an interface so that it can be easily mocked in tests.
 * Methods may be added to this interface without a major version bump, as plugins/developer
 * are not expected to implement it.
 */
public interface CordovaWebView {
    public static final String CORDOVA_VERSION = "14.0.0-dev";

    void init(CordovaInterface cordova, List<PluginEntry> pluginEntries, CordovaPreferences preferences);

    boolean isInitialized();

    View getView();

    void loadUrlIntoView(String url, boolean recreatePlugins);

    void stopLoading();

    boolean canGoBack();

    void clearCache();

    /** Use parameter-less overload */
    @Deprecated
    void clearCache(boolean b);

    void clearHistory();

    boolean backHistory();

    void handlePause(boolean keepRunning);

    void onNewIntent(Intent intent);

    void handleResume(boolean keepRunning);

    void handleStart();

    void handleStop();

    void handleDestroy();

    /**
     * Send JavaScript statement back to JavaScript.
     *
     * <p>Deprecated (<a href="https://issues.apache.org/jira/browse/CB-6851">CB-6851</a>)
     * Instead of executing snippets of JS, you should use the exec bridge
     * to create a Java->JS communication channel.</p>
     *
     * <p>To do this:</p>
     *
     * <p>1. Within plugin.xml (to have your JS run before deviceready):</p>
     *
     * <pre>
     * <js-module><runs/></js-module>
     * </pre>
     *
     * <p>2. Within your .js (call exec on start-up):</p>
     *
     * <pre>
     *    require('cordova/channel').onCordovaReady.subscribe(function() {
     *      require('cordova/exec')(win, null, 'Plugin', 'method', []);
     *      function win(message) {
     *        ... process message from java here ...
     *      }
     *    });
     * </pre>
     *
     * <p>3. Within your .java:</p>
     *
     * <pre>
     *    PluginResult dataResult = new PluginResult(PluginResult.Status.OK, CODE);
     *    dataResult.setKeepCallback(true);
     *    savedCallbackContext.sendPluginResult(dataResult);
     * </pre>
     */
    @Deprecated
    void sendJavascript(String statememt);

    /**
     * Load the specified URL in the Cordova WebView or a new browser instance.
     *
     * <p>NOTE: If openExternal is false, only allow listed URLs can be loaded.</p>
     *
     * @param url           The url to load.
     * @param openExternal  Load url in browser instead of Cordova WebView.
     * @param clearHistory  Clear the history stack, so new page becomes top of history
     * @param params        Parameters for new app
     */
    void showWebPage(String url, boolean openExternal, boolean clearHistory, Map<String, Object> params);

    /**
     * Deprecated in 4.0.0. Use your own View-toggling logic.
     */
    @Deprecated
    boolean isCustomViewShowing();

    /**
     * Deprecated in 4.0.0. Use your own View-toggling logic.
     */
    @Deprecated
    void showCustomView(View view, CustomViewCallback callback);

    /**
     * Deprecated in 4.0.0. Use your own View-toggling logic.
     */
    @Deprecated
    void hideCustomView();

    CordovaResourceApi getResourceApi();

    void setButtonPlumbedToJs(int keyCode, boolean override);
    boolean isButtonPlumbedToJs(int keyCode);

    void sendPluginResult(PluginResult cr, String callbackId);

    PluginManager getPluginManager();
    CordovaWebViewEngine getEngine();
    CordovaPreferences getPreferences();
    ICordovaCookieManager getCookieManager();

    String getUrl();

    // TODO: Work on deleting these by removing refs from plugins.
    Context getContext();
    void loadUrl(String url);
    Object postMessage(String id, Object data);
}
