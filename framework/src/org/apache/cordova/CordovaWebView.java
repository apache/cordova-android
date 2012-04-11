package org.apache.cordova;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.api.LOG;
import org.apache.cordova.api.PluginManager;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;

public class CordovaWebView extends WebView {
  
  public static final String TAG = "CordovaWebView";
  
  /** The authorization tokens. */
  private Hashtable<String, AuthenticationToken> authenticationTokens = new Hashtable<String, AuthenticationToken>();
  
  /** The whitelist **/
  private ArrayList<Pattern> whiteList = new ArrayList<Pattern>();
  private HashMap<String, Boolean> whiteListCache = new HashMap<String,Boolean>();
  protected PluginManager pluginManager;
  
  /** Actvities and other important classes **/
  private Context mCtx;
  private CordovaWebViewClient viewClient;
  private CordovaChromeClient chromeClient;

  public CordovaWebView(Context context) {
    super(context);
    mCtx = context;
    setup();
  }
  
  public CordovaWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mCtx = context;
    setup();
  }
  
  public CordovaWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mCtx = context;
    setup();
  }
  
  public CordovaWebView(Context context, AttributeSet attrs, int defStyle,
      boolean privateBrowsing) {
    super(context, attrs, defStyle, privateBrowsing);
    mCtx = context;
    setup();
  }
  
  private void setup()
  {
    this.setInitialScale(0);
    this.setVerticalScrollBarEnabled(false);
    this.requestFocusFromTouch();

    // Enable JavaScript
    WebSettings settings = this.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setJavaScriptCanOpenWindowsAutomatically(true);
    settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
    
    //Set the nav dump for HTC
    settings.setNavDump(true);

    // Enable database
    settings.setDatabaseEnabled(true);
    String databasePath = mCtx.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath(); 
    settings.setDatabasePath(databasePath);
    
    //Setup the WebChromeClient and WebViewClient
    setWebViewClient(new CordovaWebViewClient(mCtx, this));
    setWebChromeClient(new CordovaChromeClient(mCtx, this));
    
    // Enable DOM storage
    settings.setDomStorageEnabled(true);
    
    // Enable built-in geolocation
    settings.setGeolocationEnabled(true);
    
    //Start up the plugin manager
    this.pluginManager = new PluginManager(this, (DroidGap) mCtx);
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
  

  /**
   * Add entry to approved list of URLs (whitelist)
   * 
   * @param origin        URL regular expression to allow
   * @param subdomains    T=include all subdomains under origin
   */
  public void addWhiteListEntry(String origin, boolean subdomains) {
    try {
      // Unlimited access to network resources
      if(origin.compareTo("*") == 0) {
          LOG.d(TAG, "Unlimited access to network resources");
          whiteList.add(Pattern.compile(".*"));
      } else { // specific access
        // check if subdomains should be included
        // TODO: we should not add more domains if * has already been added
        if (subdomains) {
            // XXX making it stupid friendly for people who forget to include protocol/SSL
            if(origin.startsWith("http")) {
              whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://(.*\\.)?")));
            } else {
              whiteList.add(Pattern.compile("^https?://(.*\\.)?"+origin));
            }
            LOG.d(TAG, "Origin to allow with subdomains: %s", origin);
        } else {
            // XXX making it stupid friendly for people who forget to include protocol/SSL
            if(origin.startsWith("http")) {
              whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://")));
            } else {
              whiteList.add(Pattern.compile("^https?://"+origin));
            }
            LOG.d(TAG, "Origin to allow: %s", origin);
        }    
      }
    } catch(Exception e) {
      LOG.d(TAG, "Failed to add origin %s", origin);
    }
  }

  /**
   * Determine if URL is in approved list of URLs to load.
   * 
   * @param url
   * @return
   */
  public boolean isUrlWhiteListed(String url) {

      // Check to see if we have matched url previously
      if (whiteListCache.get(url) != null) {
          return true;
      }

      // Look for match in white list
      Iterator<Pattern> pit = whiteList.iterator();
      while (pit.hasNext()) {
          Pattern p = pit.next();
          Matcher m = p.matcher(url);

          // If match found, then cache it to speed up subsequent comparisons
          if (m.find()) {
              whiteListCache.put(url, true);
              return true;
          }
      }
      return false;
  }
  
}
