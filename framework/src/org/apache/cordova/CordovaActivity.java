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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

/**
 * This class is the main Android activity that represents the Cordova
 * application. It should be extended by the user to load the specific
 * html file that contains the application.
 *
 * As an example:
 * 
 * <pre>
 *     package org.apache.cordova.examples;
 *
 *     import android.os.Bundle;
 *     import org.apache.cordova.*;
 *
 *     public class Example extends CordovaActivity {
 *       &#64;Override
 *       public void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         // Load your application
 *         loadUrl(launchUrl);
 *       }
 *     }
 * </pre>
 * 
 * Cordova xml configuration: Cordova uses a configuration file at 
 * res/xml/config.xml to specify its settings. See "The config.xml File"
 * guide in cordova-docs at http://cordova.apache.org/docs for the documentation
 * for the configuration. The use of the set*Property() methods is
 * deprecated in favor of the config.xml file.
 *
 */
public class CordovaActivity extends Activity implements CordovaInterface {
    public static String TAG = "CordovaActivity";

    // The webview for our app
    protected CordovaWebView appView;

    protected ProgressDialog spinnerDialog = null;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static int ACTIVITY_STARTING = 0;
    private static int ACTIVITY_RUNNING = 1;
    private static int ACTIVITY_EXITING = 2;
    private int activityState = 0;  // 0=starting, 1=running (after 1st resume), 2=shutting down

    // Plugin to call when activity result is received
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;

    /*
     * The variables below are used to cache some of the activity properties.
     */

    // Draw a splash screen using an image located in the drawable resource directory.
    // This is not the same as calling super.loadSplashscreen(url)
    protected int splashscreen = 0;

    // LoadUrl timeout value in msec (default of 20 sec)
    protected int loadUrlTimeoutValue = 20000;

    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    private String initCallbackClass;

    // Read from config.xml:
    protected CordovaPreferences preferences;
    protected Whitelist whitelist;
    protected String launchUrl;
    protected ArrayList<PluginEntry> pluginEntries;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.i(TAG, "Apache Cordova native platform version " + CordovaWebView.CORDOVA_VERSION + " is starting");
        LOG.d(TAG, "CordovaActivity.onCreate()");
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
        {
            initCallbackClass = savedInstanceState.getString("callbackClass");
        }
        
        loadConfig();
    }
    
    protected void init() {
        if(!preferences.getBoolean("ShowTitle", false))
        {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if(preferences.getBoolean("SetFullscreen", false))
        {
            Log.d(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (preferences.getBoolean("Fullscreen", false)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        appView = makeWebView();

        // TODO: Have the views set this themselves.
        if (preferences.getBoolean("DisallowOverscroll", false)) {
            appView.getView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        createViews();

        // TODO: Make this a preference (CB-6153)
        // Setup the hardware volume controls to handle volume control
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @SuppressWarnings("deprecation")
    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this);
        preferences = parser.getPreferences();
        preferences.setPreferencesBundle(getIntent().getExtras());
        preferences.copyIntoIntentExtras(this);
        whitelist = parser.getWhitelist();
        launchUrl = parser.getLaunchUrl();
        pluginEntries = parser.getPluginEntries();
        Config.parser = parser;
    }

    @SuppressWarnings("deprecation")
    protected void createViews() {
        // This builds the view.  We could probably get away with NOT having a LinearLayout, but I like having a bucket!
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        LinearLayoutSoftKeyboardDetect root = new LinearLayoutSoftKeyboardDetect(this, width, height);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));

        appView.getView().setId(100);
        appView.getView().setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0F));

        // Add web view but make it invisible while loading URL
        appView.getView().setVisibility(View.INVISIBLE);
        root.addView(appView.getView());
        setContentView(root);

        // TODO: Setting this on the appView causes it to show when <html style="opacity:0">.
        int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
        root.setBackgroundColor(backgroundColor);
        appView.getView().setBackgroundColor(backgroundColor);
    }

    /**
     * Get the Android activity.
     */
    @Override public Activity getActivity() {
        return this;
    }

    /**
     * Construct the default web view object.
     *
     * This is intended to be overridable by subclasses of CordovaIntent which
     * require a more specialized web view.
     */
    protected CordovaWebView makeWebView() {
        String r = preferences.getString("webView", null);
        CordovaWebView ret = null;
        if (r != null) {
            try {
                Class<?> webViewClass = Class.forName(r);
                Constructor<?> constructor = webViewClass.getConstructor(Context.class);
                ret = (CordovaWebView) constructor.newInstance((Context)this);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
            
        if (ret == null) {
            // If all else fails, return a default WebView
            ret = new AndroidWebView(this);
        }
        ret.init(this, pluginEntries, whitelist, preferences);
        return ret;
    }

    /**
     * Load the url into the webview.
     */
    public void loadUrl(String url, int splashscreenTime) {
        if (appView == null) {
            init();
        }
        String splash = preferences.getString("SplashScreen", null);
        if(splashscreenTime > 0 && splash != null)
        {
            this.splashscreen = getResources().getIdentifier(splash, "drawable", getClass().getPackage().getName());;
            if(this.splashscreen != 0)
            {
                this.showSplashScreen(splashscreenTime);
            }
        }
        
        // If keepRunning
        this.keepRunning = preferences.getBoolean("KeepRunning", true);

        //Check if the view is attached to anything
        if(appView.getView().getParent() != null)
        {
            // Then load the spinner
            this.loadSpinner();
        }
        //Load the correct splashscreen
        if(this.splashscreen != 0)
        {
            appView.getPluginManager().postMessage("splashscreen", "show");
        }
        this.appView.loadUrlIntoView(url, true);
    }

    /**
     * Load the url into the webview after waiting for period of time.
     * This is used to display the splashscreen for certain amount of time.
     *
     * @param url
     * @param time              The number of ms to wait before loading webview
     */
    public void loadUrl(final String url) {
        if (appView == null) {
            init();
        }
        this.loadUrl(url, preferences.getInteger("SplashScreenDelay", 3000));
    }
    
    /*
     * Load the spinner
     */
    void loadSpinner() {

        // If loadingDialog property, then show the App loading dialog for first page of app
        String loading = null;
        if ((this.appView == null) || !this.appView.canGoBack()) {
            loading = preferences.getString("LoadingDialog", null);
        }
        else {
            loading = preferences.getString("LoadingPageDialog", null);
        }
        if (loading != null) {

            String title = "";
            String message = "Loading Application...";

            if (loading.length() > 0) {
                int comma = loading.indexOf(',');
                if (comma > 0) {
                    title = loading.substring(0, comma);
                    message = loading.substring(comma + 1);
                }
                else {
                    title = "";
                    message = loading;
                }
            }
            this.spinnerStart(title, message);
        }
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     */
    @Override
    protected void onPause() {
        super.onPause();

        LOG.d(TAG, "Paused the application!");

        // Don't process pause if shutting down, since onDestroy() will be called
        if (this.activityState == ACTIVITY_EXITING) {
            return;
        }

        if (this.appView == null) {
            return;
        }
        else
        {
            this.appView.handlePause(this.keepRunning);
        }

        // hide the splash screen to avoid leaking a window
        this.removeSplashScreen();
    }

    /**
     * Called when the activity receives a new intent
     **/
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Forward to plugins
        if (this.appView != null)
           this.appView.onNewIntent(intent);
    }

    /**
     * Called when the activity will start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        LOG.d(TAG, "Resuming the App");
        
        if (this.activityState == ACTIVITY_STARTING) {
            this.activityState = ACTIVITY_RUNNING;
            return;
        }

        if (this.appView == null) {
            return;
        }
        // Force window to have focus, so application always
        // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at least)
        this.getWindow().getDecorView().requestFocus();

        this.appView.handleResume(this.keepRunning, this.activityResultKeepRunning);

        // If app doesn't want to run in background
        if (!this.keepRunning || this.activityResultKeepRunning) {

            // Restore multitasking state
            if (this.activityResultKeepRunning) {
                this.keepRunning = this.activityResultKeepRunning;
                this.activityResultKeepRunning = false;
            }
        }
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    @Override
    public void onDestroy() {
        LOG.d(TAG, "CordovaActivity.onDestroy()");
        super.onDestroy();

        // hide the splash screen to avoid leaking a window
        this.removeSplashScreen();

        if (this.appView != null) {
            appView.handleDestroy();
        }
        else {
            this.activityState = ACTIVITY_EXITING; 
        }
    }

    /**
     * Show the spinner.  Must be called from the UI thread.
     *
     * @param title         Title of the dialog
     * @param message       The message of the dialog
     */
    public void spinnerStart(final String title, final String message) {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
        final CordovaActivity me = this;
        this.spinnerDialog = ProgressDialog.show(CordovaActivity.this, title, message, true, true,
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        me.spinnerDialog = null;
                    }
                });
    }

    /**
     * Stop spinner - Must be called from UI thread
     */
    public void spinnerStop() {
        if (this.spinnerDialog != null && this.spinnerDialog.isShowing()) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
    }

    /**
     * End this activity by calling finish for activity
     */
    public void endActivity() {
        finish();
    }
    
    @Override
    public void finish() {
        this.activityState = ACTIVITY_EXITING;
        super.finish();
    }


    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits,
     * your onActivityResult() method will be called.
     *
     * @param command           The command object
     * @param intent            The intent to start
     * @param requestCode       The request code that is passed to callback to identify the activity
     */
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        LOG.d(TAG, "Incoming Result");
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "Request code = " + requestCode);
        if (appView != null && requestCode == AndroidChromeClient.FILECHOOSER_RESULTCODE) {
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            appView.onFilePickerResult(result);
        }
        CordovaPlugin callback = this.activityResultCallback;
        if(callback == null && initCallbackClass != null) {
            // The application was restarted, but had defined an initial callback
            // before being shut down.
            //this.activityResultCallback = appView.pluginManager.getPlugin(initCallbackClass);
            this.activityResultCallback = appView.getPluginManager().getPlugin(initCallbackClass);
            callback = this.activityResultCallback;
        }
        if(callback != null) {
            LOG.d(TAG, "We have a callback to send this result to");
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    /**
     * Report an error to the host application. These errors are unrecoverable (i.e. the main resource is unavailable).
     * The errorCode parameter corresponds to one of the ERROR_* constants.
     *
     * @param errorCode    The error code corresponding to an ERROR_* value.
     * @param description  A String describing the error.
     * @param failingUrl   The url that failed to load.
     */
    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
        final CordovaActivity me = this;

        // If errorUrl specified, then load it
        final String errorUrl = preferences.getString("errorUrl", null);
        if ((errorUrl != null) && (errorUrl.startsWith("file://") || whitelist.isUrlWhiteListed(errorUrl)) && (!failingUrl.equals(errorUrl))) {
            // Load URL on UI thread
            me.runOnUiThread(new Runnable() {
                public void run() {
                    // Stop "app loading" spinner if showing
                    me.spinnerStop();
                    me.appView.showWebPage(errorUrl, false, true, null);
                }
            });
        }
        // If not, then display error dialog
        else {
            final boolean exit = !(errorCode == WebViewClient.ERROR_HOST_LOOKUP);
            me.runOnUiThread(new Runnable() {
                public void run() {
                    if (exit) {
                        me.appView.getView().setVisibility(View.GONE);
                        me.displayError("Application Error", description + " (" + failingUrl + ")", "OK", exit);
                    }
                }
            });
        }
    }

    /**
     * Display an error dialog and optionally exit application.
     */
    public void displayError(final String title, final String message, final String button, final boolean exit) {
        final CordovaActivity me = this;
        me.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(me);
                    dlg.setMessage(message);
                    dlg.setTitle(title);
                    dlg.setCancelable(false);
                    dlg.setPositiveButton(button,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (exit) {
                                        me.endActivity();
                                    }
                                }
                            });
                    dlg.create();
                    dlg.show();
                } catch (Exception e) {
                    finish();
                }
            }
        });
    }

    /*
     * Hook in Cordova for menu plugins
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onCreateOptionsMenu", menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onPrepareOptionsMenu", menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onOptionsItemSelected", item);
        }
        return true;
    }

    protected Dialog splashDialog;

    /**
     * Removes the Dialog that displays the splash screen
     */
    public void removeSplashScreen() {
        if (splashDialog != null && splashDialog.isShowing()) {
            splashDialog.dismiss();
            splashDialog = null;
        }
    }

    /**
     * Shows the splash screen over the full Activity
     */
    @SuppressWarnings("deprecation")
    protected void showSplashScreen(final int time) {
        final CordovaActivity that = this;

        Runnable runnable = new Runnable() {
            public void run() {
                // Get reference to display
                Display display = getWindowManager().getDefaultDisplay();

                // Create the layout for the dialog
                LinearLayout root = new LinearLayout(that.getActivity());
                root.setMinimumHeight(display.getHeight());
                root.setMinimumWidth(display.getWidth());
                root.setOrientation(LinearLayout.VERTICAL);
                root.setBackgroundColor(preferences.getInteger("backgroundColor", Color.BLACK));
                root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
                root.setBackgroundResource(that.splashscreen);
                
                // Create and show the dialog
                splashDialog = new Dialog(that, android.R.style.Theme_Translucent_NoTitleBar);
                // check to see if the splash screen should be full screen
                if ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                    splashDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                splashDialog.setContentView(root);
                splashDialog.setCancelable(false);
                splashDialog.show();

                // Set Runnable to remove splash screen just in case
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        removeSplashScreen();
                    }
                }, time);
            }
        };
        this.runOnUiThread(runnable);
    }

    /**
     * Called when a message is sent to plugin.
     *
     * @param id            The message id
     * @param data          The message data
     * @return              Object or null
     */
    public Object onMessage(String id, Object data) {
        if (!"onScrollChanged".equals(id)) {
            LOG.d(TAG, "onMessage(" + id + "," + data + ")");
        }

        if ("splashscreen".equals(id)) {
            if ("hide".equals(data.toString())) {
                this.removeSplashScreen();
            }
            else {
                // If the splash dialog is showing don't try to show it again
                if (this.splashDialog == null || !this.splashDialog.isShowing()) {
                    String splashResource = preferences.getString("SplashScreen", null);
                    if (splashResource != null) {
                        splashscreen = getResources().getIdentifier(splashResource, "drawable", getClass().getPackage().getName());
                    }
                    this.showSplashScreen(preferences.getInteger("SplashScreenDelay", 3000));
                }
            }
        }
        else if ("spinner".equals(id)) {
            if ("stop".equals(data.toString())) {
                this.spinnerStop();
                this.appView.getView().setVisibility(View.VISIBLE);
            }
        }
        else if ("onReceivedError".equals(id)) {
            JSONObject d = (JSONObject) data;
            try {
                this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if ("exit".equals(id)) {
            this.endActivity();
        }
        return null;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }
    
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if(this.activityResultCallback != null)
        {
            String cClass = this.activityResultCallback.getClass().getName();
            outState.putString("callbackClass", cClass);
        }
    }
}
