package org.apache.cordova;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CordovaWebView extends WebView {

  public CordovaWebView(Context context) {
    super(context);
  }
  
  public CordovaWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public CordovaWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }
  
  public CordovaWebView(Context context, AttributeSet attrs, int defStyle,
      boolean privateBrowsing) {
    super(context, attrs, defStyle, privateBrowsing);
  }

}
