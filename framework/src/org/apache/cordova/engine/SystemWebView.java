package org.apache.cordova.engine;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.ScrollEvent;

/**
 * Custom WebView subclass that enables us to capture events needed for Cordova.
 */
public class SystemWebView extends WebView implements CordovaWebViewEngine.EngineView {
    private SystemWebViewClient viewClient;
    SystemWebChromeClient chromeClient;
    private SystemWebViewEngine parentEngine;
    private CordovaInterface cordova;

    public SystemWebView(Context context) {
        this(context, null);
    }

    public SystemWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Package visibility to enforce that only SystemWebViewEngine should call this method.
    void init(SystemWebViewEngine parentEngine, CordovaInterface cordova) {
        this.cordova = cordova;
        this.parentEngine = parentEngine;
        if (this.viewClient == null) {
            setWebViewClient(new SystemWebViewClient(parentEngine));
        }

        if (this.chromeClient == null) {
            setWebChromeClient(new SystemWebChromeClient(parentEngine));
        }
    }

    @Override
    public CordovaWebView getCordovaWebView() {
        return parentEngine != null ? parentEngine.getCordovaWebView() : null;
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        viewClient = (SystemWebViewClient)client;
        super.setWebViewClient(client);
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        chromeClient = (SystemWebChromeClient)client;
        super.setWebChromeClient(client);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
        //We should post a message that the scroll changed
        ScrollEvent myEvent = new ScrollEvent(l, t, oldl, oldt, this);
        parentEngine.pluginManager.postMessage("onScrollChanged", myEvent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Boolean ret = parentEngine.client.onKeyDown(keyCode, event);
        if (ret != null) {
            return ret.booleanValue();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Boolean ret = parentEngine.client.onKeyUp(keyCode, event);
        if (ret != null) {
            return ret.booleanValue();
        }
        return super.onKeyUp(keyCode, event);
    }
}
