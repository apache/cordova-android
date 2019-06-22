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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * This class exposes methods in Cordova that can be called from JavaScript.
 */
public class CoreAndroid extends CordovaPlugin {

    public static final String PLUGIN_NAME = "CoreAndroid";
    protected static final String TAG = "CordovaApp";
    private BroadcastReceiver telephonyReceiver;
    private CallbackContext messageChannel;
    private PluginResult pendingResume;
    private PluginResult pendingPause;
    private final Object messageChannelLock = new Object();

    /**
     * Send an event to be fired on the Javascript side.
     *
     * @param action The name of the event to be fired
     */
    public void fireJavascriptEvent(String action) {
        sendEventMessage(action);
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     */
    @Override
    public void pluginInitialize() {
        this.initTelephonyReceiver();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback context from which we were invoked.
     * @return                  A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        try {
            if (action.equals("clearCache")) {
                this.clearCache();
            }
            else if (action.equals("show")) {
                // This gets called from JavaScript onCordovaReady to show the webview.
                // I recommend we change the name of the Message as spinner/stop is not
                // indicative of what this actually does (shows the webview).
                cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        webView.getPluginManager().postMessage("spinner", "stop");
                    }
                });
            }
            else if (action.equals("loadUrl")) {
                this.loadUrl(args.getString(0), args.optJSONObject(1));
            }
            else if (action.equals("cancelLoadUrl")) {
                //this.cancelLoadUrl();
            }
            else if (action.equals("clearHistory")) {
                this.clearHistory();
            }
            else if (action.equals("backHistory")) {
                this.backHistory();
            }
            else if (action.equals("overrideButton")) {
                this.overrideButton(args.getString(0), args.getBoolean(1));
            }
            else if (action.equals("overrideBackbutton")) {
                this.overrideBackbutton(args.getBoolean(0));
            }
            else if (action.equals("exitApp")) {
                this.exitApp();
            }
			else if (action.equals("messageChannel")) {
                synchronized(messageChannelLock) {
                    messageChannel = callbackContext;
                    if (pendingPause != null) {
                        sendEventMessage(pendingPause);
                        pendingPause = null;
                    }
                    if (pendingResume != null) {
                        sendEventMessage(pendingResume);
                        pendingResume = null;
                    }
                }
                return true;
            }

            callbackContext.sendPluginResult(new PluginResult(status, result));
            return true;
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Clear the resource cache.
     */
    public void clearCache() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                webView.clearCache();
            }
        });
    }

    /**
     * Load the url into the webview.
     *
     * @param url
     * @param props			Properties that can be passed in to the Cordova activity (i.e. loadingDialog, wait, ...)
     * @throws JSONException
     */
    public void loadUrl(String url, JSONObject props) throws JSONException {
        LOG.d("App", "App.loadUrl("+url+","+props+")");
        int wait = 0;
        boolean openExternal = false;
        boolean clearHistory = false;

        // If there are properties, then set them on the Activity
        HashMap<String, Object> params = new HashMap<String, Object>();
        if (props != null) {
            JSONArray keys = props.names();
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                if (key.equals("wait")) {
                    wait = props.getInt(key);
                }
                else if (key.equalsIgnoreCase("openexternal")) {
                    openExternal = props.getBoolean(key);
                }
                else if (key.equalsIgnoreCase("clearhistory")) {
                    clearHistory = props.getBoolean(key);
                }
                else {
                    Object value = props.get(key);
                    if (value == null) {

                    }
                    else if (value.getClass().equals(String.class)) {
                        params.put(key, (String)value);
                    }
                    else if (value.getClass().equals(Boolean.class)) {
                        params.put(key, (Boolean)value);
                    }
                    else if (value.getClass().equals(Integer.class)) {
                        params.put(key, (Integer)value);
                    }
                }
            }
        }

        // If wait property, then delay loading

        if (wait > 0) {
            try {
                synchronized(this) {
                    this.wait(wait);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.webView.showWebPage(url, openExternal, clearHistory, params);
    }

    /**
     * Clear page history for the app.
     */
    public void clearHistory() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                webView.clearHistory();
            }
        });
    }

    /**
     * Go to previous page displayed.
     * This is the same as pressing the backbutton on Android device.
     */
    public void backHistory() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                webView.backHistory();
            }
        });
    }

    /**
     * Override the default behavior of the Android back button.
     * If overridden, when the back button is pressed, the "backKeyDown" JavaScript event will be fired.
     *
     * @param override		T=override, F=cancel override
     */
    public void overrideBackbutton(boolean override) {
        LOG.i("App", "WARNING: Back Button Default Behavior will be overridden.  The backbutton event will be fired!");
        webView.setButtonPlumbedToJs(KeyEvent.KEYCODE_BACK, override);
    }

    /**
     * Override the default behavior of the Android volume buttons.
     * If overridden, when the volume button is pressed, the "volume[up|down]button" JavaScript event will be fired.
     *
     * @param button        volumeup, volumedown
     * @param override      T=override, F=cancel override
     */
    public void overrideButton(String button, boolean override) {
        LOG.i("App", "WARNING: Volume Button Default Behavior will be overridden.  The volume event will be fired!");
        if (button.equals("volumeup")) {
            webView.setButtonPlumbedToJs(KeyEvent.KEYCODE_VOLUME_UP, override);
        }
        else if (button.equals("volumedown")) {
            webView.setButtonPlumbedToJs(KeyEvent.KEYCODE_VOLUME_DOWN, override);
        }
        else if (button.equals("menubutton")) {
            webView.setButtonPlumbedToJs(KeyEvent.KEYCODE_MENU, override);
        }
    }

    /**
     * Return whether the Android back button is overridden by the user.
     *
     * @return boolean
     */
    public boolean isBackbuttonOverridden() {
        return webView.isButtonPlumbedToJs(KeyEvent.KEYCODE_BACK);
    }

    /**
     * Exit the Android application.
     */
    public void exitApp() {
        this.webView.getPluginManager().postMessage("exit", null);
    }


    /**
     * Listen for telephony events: RINGING, OFFHOOK and IDLE
     * Send these events to all plugins using
     *      CordovaActivity.onMessage("telephone", "ringing" | "offhook" | "idle")
     */
    private void initTelephonyReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        //final CordovaInterface mycordova = this.cordova;
        this.telephonyReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                // If state has changed
                if ((intent != null) && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                    if (intent.hasExtra(TelephonyManager.EXTRA_STATE)) {
                        String extraData = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        if (extraData.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            LOG.i(TAG, "Telephone RINGING");
                            webView.getPluginManager().postMessage("telephone", "ringing");
                        }
                        else if (extraData.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            LOG.i(TAG, "Telephone OFFHOOK");
                            webView.getPluginManager().postMessage("telephone", "offhook");
                        }
                        else if (extraData.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            LOG.i(TAG, "Telephone IDLE");
                            webView.getPluginManager().postMessage("telephone", "idle");
                        }
                    }
                }
            }
        };

        // Register the receiver
        webView.getContext().registerReceiver(this.telephonyReceiver, intentFilter);
    }

    private void sendEventMessage(String action) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("action", action);
        } catch (JSONException e) {
            LOG.e(TAG, "Failed to create event message", e);
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, obj);

        if (messageChannel == null) {
            LOG.i(TAG, "Request to send event before messageChannel initialised: " + action);
            if ("pause".equals(action)) {
                pendingPause = result;
            } else if ("resume".equals(action)) {
                // When starting normally onPause then onResume is called
                pendingPause = null;
            }
        } else {
            sendEventMessage(result);
        }
    }

    private void sendEventMessage(PluginResult payload) {
        payload.setKeepCallback(true);
        if (messageChannel != null) {
            messageChannel.sendPluginResult(payload);
        }
    }

    /*
     * Unregister the receiver
     *
     */
    public void onDestroy()
    {
        webView.getContext().unregisterReceiver(this.telephonyReceiver);
    }

    /**
     * Used to send the resume event in the case that the Activity is destroyed by the OS
     *
     * @param resumeEvent PluginResult containing the payload for the resume event to be fired
     */
    public void sendResumeEvent(PluginResult resumeEvent) {
        // This operation must be synchronized because plugin results that trigger resume
        // events can be processed asynchronously
        synchronized(messageChannelLock) {
            if (messageChannel != null) {
                sendEventMessage(resumeEvent);
            } else {
                // Might get called before the page loads, so we need to store it until the
                // messageChannel gets created
                this.pendingResume = resumeEvent;
            }
        }
    }

      /*
     * This needs to be implemented if you wish to use the Camera Plugin or other plugins
     * that read the Build Configuration.
     *
     * Thanks to Phil@Medtronic and Graham Borland for finding the answer and posting it to
     * StackOverflow.  This is annoying as hell!
     *
     */

    public static Object getBuildConfigValue(Context ctx, String key)
    {
        try
        {
            Class<?> clazz = Class.forName(ctx.getClass().getPackage().getName() + ".BuildConfig");
            Field field = clazz.getField(key);
            return field.get(null);
        } catch (ClassNotFoundException e) {
            LOG.d(TAG, "Unable to get the BuildConfig, is this built with ANT?");
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            LOG.d(TAG, key + " is not a valid field. Check your build.gradle");
        } catch (IllegalAccessException e) {
            LOG.d(TAG, "Illegal Access Exception: Let's print a stack trace.");
            e.printStackTrace();
        } catch (NullPointerException e) {
            LOG.d(TAG, "Null Pointer Exception: Let's print a stack trace.");
            e.printStackTrace();
        }

        return null;
    }
}
