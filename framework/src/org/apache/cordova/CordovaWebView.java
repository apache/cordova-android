package org.apache.cordova;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

public interface CordovaWebView {
    public static final String CORDOVA_VERSION = "4.0.0-dev";

    void init(CordovaInterface cordova, List<PluginEntry> pluginEntries,
            Whitelist internalWhitelist, Whitelist externalWhitelist,
            CordovaPreferences preferences);

    View getView();

    void loadUrlIntoView(String url, boolean recreatePlugins);

    void stopLoading();

    boolean canGoBack();

    void clearCache(boolean b);

    void clearHistory();

    boolean backHistory();

    void handlePause(boolean keepRunning);

    void onNewIntent(Intent intent);

    void handleResume(boolean keepRunning, boolean activityResultKeepRunning);

    void handleDestroy();

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

    void showWebPage(String errorUrl, boolean b, boolean c, HashMap<String, Object> params);

    boolean isCustomViewShowing();

    void showCustomView(View view, CustomViewCallback callback);

    void hideCustomView();

    CordovaResourceApi getResourceApi();

    void setButtonPlumbedToJs(int keyCode, boolean override);
    boolean isButtonPlumbedToJs(int keyCode);

    void sendPluginResult(PluginResult cr, String callbackId);

    PluginManager getPluginManager();

    Whitelist getWhitelist();
    Whitelist getExternalWhitelist();
    CordovaPreferences getPreferences();

    void setNetworkAvailable(boolean online);
    
    String getUrl();

    // TODO: Work on deleting these by removing refs from plugins.
    Context getContext();
    void loadUrl(String url);
    Object postMessage(String id, Object data);
}
