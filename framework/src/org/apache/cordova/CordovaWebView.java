package org.apache.cordova;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.LinearLayout.LayoutParams;

public interface CordovaWebView {
    public static final String CORDOVA_VERSION = "4.0.0-dev";

    void init(CordovaInterface cordova, CordovaWebViewClient webViewClient, CordovaChromeClient webChromeClient,
              List<PluginEntry> pluginEntries, Whitelist whitelist, CordovaPreferences preferences);

    View getView();

    CordovaWebViewClient makeWebViewClient(CordovaInterface cordova);

    CordovaChromeClient makeWebChromeClient(CordovaInterface cordova);
        
    void setWebViewClient(CordovaWebViewClient webViewClient);

    void setWebChromeClient(CordovaChromeClient webChromeClient);

    void setId(int i);

    void setLayoutParams(LayoutParams layoutParams);

    void setVisibility(int invisible);

    Object getParent();

    void loadUrl(String url);

    void loadUrl(String url, int splashscreenTime);

    void loadUrlNow(String url);

    void loadUrlIntoView(final String url);

    void loadUrlIntoView(final String url, boolean recreatePlugins);

    void loadUrlIntoView(final String url, final int splashscreenTime);

    void stopLoading();

    boolean canGoBack();

    void clearCache(boolean b);

    void clearHistory();

    boolean backHistory();

    void handlePause(boolean keepRunning);

    void onNewIntent(Intent intent);

    void handleResume(boolean keepRunning, boolean activityResultKeepRunning);

    void handleDestroy();

    void postMessage(String id, Object data);

    void addJavascript(String statement);

    /**
     * Send JavaScript statement back to JavaScript.
     * (This is a convenience method)
     *
     * @param statement
     * Deprecated (https://issues.apache.org/jira/browse/CB-6851)
     * Instead of executing snippets of JS, you should use the exec bridge
     * to create a Java->JS communication channel.
     * To do this:
     * 1. Within plugin.xml (to have your JS run before deviceready):
     *    <js-module><runs/></js-module>
     * 2. Within your .js (call exec on start-up):
     *    require('cordova/channel').onCordovaReady.subscribe(function() {
     *      require('cordova/exec')(win, null, 'Plugin', 'method', []);
     *      function win(message) {
     *        ... process message from java here ...
     *      }
     *    });
     * 3. Within your .java:
     *    PluginResult dataResult = new PluginResult(PluginResult.Status.OK, CODE);
     *    dataResult.setKeepCallback(true);
     *    savedCallbackContext.sendPluginResult(dataResult);
     */
    @Deprecated
    void sendJavascript(String statememt);

    CordovaChromeClient getWebChromeClient();

    CordovaPlugin getPlugin(String initCallbackClass);

    void showWebPage(String errorUrl, boolean b, boolean c, HashMap<String, Object> params);

    Object getFocusedChild();

    boolean isCustomViewShowing();

    void showCustomView(View view, CustomViewCallback callback);

    void hideCustomView();

    Context getContext();

    boolean onOverrideUrlLoading(String url);

    int getVisibility();

    void incUrlTimeout();

    void setOverScrollMode(int overScrollNever);

    void setNetworkAvailable(boolean online);

    CordovaResourceApi getResourceApi();

    void setButtonPlumbedToJs(int keyCode, boolean override);
    boolean isButtonPlumbedToJs(int keyCode);

    void sendPluginResult(PluginResult cr, String callbackId);

    PluginManager getPluginManager();

    void setLayoutParams(android.widget.FrameLayout.LayoutParams layoutParams);
    
    // Required for test
    String getUrl();
    boolean isPaused();
    
    Whitelist getWhitelist();
    CordovaPreferences getPreferences();
}
