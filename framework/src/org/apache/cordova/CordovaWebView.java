package org.apache.cordova;

import java.util.HashMap;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.LinearLayout.LayoutParams;

public abstract class CordovaWebView {

    String OVER_SCROLL_NEVER = null;
    public PluginManager pluginManager = null;
    NativeToJsMessageQueue jsMessageQueue = null;

    public abstract View getView();

    public abstract CordovaWebViewClient makeWebViewClient();

    public abstract CordovaChromeClient makeWebChromeClient();

    public abstract void setWebViewClient(CordovaWebViewClient webViewClient);

    public abstract void setWebChromeClient(CordovaChromeClient webChromeClient);

    public abstract void setId(int i);

    public abstract void setLayoutParams(LayoutParams layoutParams);

    public abstract void setVisibility(int invisible);

    public abstract Object getParent();

    public abstract void loadUrl(String url);

    public abstract void loadUrl(String url, int splashscreenTime);

    public abstract void loadUrlNow(String url);

    public abstract void loadUrlIntoView(final String url);

    public abstract void loadUrlIntoView(final String url, boolean recreatePlugins);

    public abstract void loadUrlIntoView(final String url, final int splashscreenTime);

    public abstract void stopLoading();

    public abstract boolean canGoBack();

    public abstract void clearCache(boolean b);

    public abstract void clearHistory();

    public abstract boolean backHistory();

    public abstract void handlePause(boolean keepRunning);

    public abstract void onNewIntent(Intent intent);

    public abstract void handleResume(boolean keepRunning, boolean activityResultKeepRunning);

    public abstract void handleDestroy();

    public abstract void postMessage(String id, Object data);

    public abstract void addJavascript(String statement);

    public abstract void sendJavascript(String statememt);

    public abstract CordovaChromeClient getWebChromeClient();

    public abstract CordovaPlugin getPlugin(String initCallbackClass);

    public abstract void showWebPage(String errorUrl, boolean b, boolean c, HashMap<String, Object> params);

    public abstract Object getFocusedChild();

    public abstract boolean isCustomViewShowing();

    public abstract boolean onKeyUp(int keyCode, KeyEvent event);

    public abstract boolean onKeyDown(int keyCode, KeyEvent event);

    public abstract String exec(String service, String action, String callbackId, String message) throws JSONException;

    public abstract void setNativeToJsBridgeMode(int parseInt);

    public abstract String retrieveJsMessages(boolean equals);

    public abstract void showCustomView(View view, CustomViewCallback callback);

    public abstract void hideCustomView();

    public abstract Context getContext();

    public abstract boolean onOverrideUrlLoading(String url);

    public abstract void resetJsMessageQueue();

    public abstract void onReset();

    public abstract int getVisibility();

    public abstract void incUrlTimeout();

    public abstract void setOverScrollMode(int overScrollNever);

    public abstract void setNetworkAvailable(boolean online);

    public abstract CordovaResourceApi getResourceApi();

    public abstract void bindButton(boolean override);
    public abstract void bindButton(String button, boolean override);

    public abstract boolean isBackButtonBound();

    public abstract void sendPluginResult(PluginResult cr, String callbackId);

    public abstract PluginManager getPluginManager();

    public abstract void setLayoutParams(android.widget.FrameLayout.LayoutParams layoutParams);
    
    // Required for test
    
    public abstract String getUrl();
    public abstract boolean isPaused();
}
