package org.apache.cordova;

import java.util.HashMap;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.LinearLayout.LayoutParams;

public interface CordovaWebView {

    String OVER_SCROLL_NEVER = null;
    Object pluginManager = null;
    Object jsMessageQueue = null;

    View getView();

    CordovaWebViewClient makeWebViewClient();

    CordovaChromeClient makeWebChromeClient();

    void setWebViewClient(CordovaWebViewClient webViewClient);

    void setWebChromeClient(CordovaChromeClient webChromeClient);

    void setId(int i);

    void setLayoutParams(LayoutParams layoutParams);

    void setVisibility(int invisible);

    Object getParent();

    void loadUrl(String url, int splashscreenTime);

    void loadUrl(String url);

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
    
    void sendJavascript(String statememt);

    CordovaChromeClient getWebChromeClient();

    CordovaPlugin getPlugin(String initCallbackClass);

    void showWebPage(String errorUrl, boolean b, boolean c, HashMap<String, Object> params);

    Object getFocusedChild();

    boolean isCustomViewShowing();

    boolean onKeyUp(int keyCode, KeyEvent event);

    boolean onKeyDown(int keyCode, KeyEvent event);

    String exec(String service, String action, String callbackId, String message) throws JSONException;

    void setNativeToJsBridgeMode(int parseInt);

    String retrieveJsMessages(boolean equals);

    void showCustomView(View view, CustomViewCallback callback);

    void hideCustomView();

    Context getContext();

    boolean onOverrideUrlLoading(String url);

    void resetJsMessageQueue();

    void onReset();

    int getVisibility();

    void incUrlTimeout();

    void setOverScrollMode(int overScrollNever);

    void loadUrlNow(String string);

    void setNetworkAvailable(boolean online);

    CordovaResourceApi getResourceApi();

    void bindButton(boolean override);
    void bindButton(String button, boolean override);

    boolean isBackButtonBound();

    void sendPluginResult(PluginResult cr, String callbackId);

    PluginManager getPluginManager();

    void setLayoutParams(android.widget.FrameLayout.LayoutParams layoutParams);
}
