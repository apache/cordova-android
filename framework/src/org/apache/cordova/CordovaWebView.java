package org.apache.cordova;

import java.util.Hashtable;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CordovaWebView extends WebView {
  
  /** The authorization tokens. */
  private Hashtable<String, AuthenticationToken> authenticationTokens = new Hashtable<String, AuthenticationToken>();
  
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
  
  /**
   * Sets the authentication token.
   * 
   * @param authenticationToken
   *            the authentication token
   * @param host
   *            the host
   * @param realm
   *            the realm
   */
  public void setAuthenticationToken(AuthenticationToken authenticationToken, String host, String realm) {
      
      if(host == null) {
          host = "";
      }
      
      if(realm == null) {
          realm = "";
      }
      
      authenticationTokens.put(host.concat(realm), authenticationToken);
  }
  
  /**
   * Removes the authentication token.
   * 
   * @param host
   *            the host
   * @param realm
   *            the realm
   * @return the authentication token or null if did not exist
   */
  public AuthenticationToken removeAuthenticationToken(String host, String realm) {
      return authenticationTokens.remove(host.concat(realm));
  }
  
  /**
   * Gets the authentication token.
   * 
   * In order it tries:
   * 1- host + realm
   * 2- host
   * 3- realm
   * 4- no host, no realm
   * 
   * @param host
   *            the host
   * @param realm
   *            the realm
   * @return the authentication token
   */
  public AuthenticationToken getAuthenticationToken(String host, String realm) {
      AuthenticationToken token = null;
      
      token = authenticationTokens.get(host.concat(realm));
      
      if(token == null) {
          // try with just the host
          token = authenticationTokens.get(host);
          
          // Try the realm
          if(token == null) {
              token = authenticationTokens.get(realm);
          }
          
          // if no host found, just query for default
          if(token == null) {      
              token = authenticationTokens.get("");
          }
      }
      
      return token;
  }
  
  /**
   * Clear all authentication tokens.
   */
  public void clearAuthenticationTokens() {
      authenticationTokens.clear();
  }
  
}
