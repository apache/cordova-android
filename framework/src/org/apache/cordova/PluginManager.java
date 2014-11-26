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

import java.util.Collection;
import java.util.HashMap;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import android.content.Intent;
import android.net.Uri;
import android.os.Debug;
import android.util.Log;

/**
 * PluginManager is exposed to JavaScript in the Cordova WebView.
 *
 * Calling native plugin code can be done by calling PluginManager.exec(...)
 * from JavaScript.
 */
public class PluginManager {
    private static String TAG = "PluginManager";
    private static final int SLOW_EXEC_WARNING_THRESHOLD = Debug.isDebuggerConnected() ? 60 : 16;

    // List of service entries
    private final HashMap<String, CordovaPlugin> pluginMap = new HashMap<String, CordovaPlugin>();
    private final HashMap<String, PluginEntry> entryMap = new HashMap<String, PluginEntry>();

    private final CordovaInterface ctx;
    private final CordovaWebView app;

    public PluginManager(CordovaWebView cordovaWebView, CordovaInterface cordova, Collection<PluginEntry> pluginEntries) {
        this.ctx = cordova;
        this.app = cordovaWebView;
        setPluginEntries(pluginEntries);
    }

    public Collection<PluginEntry> getPluginEntries() {
        return entryMap.values();
    }

    public void setPluginEntries(Collection<PluginEntry> pluginEntries) {
        this.onPause(false);
        this.onDestroy();
        pluginMap.clear();
        entryMap.clear();
        for (PluginEntry entry : pluginEntries) {
            addService(entry);
        }
    }

    /**
     * Init when loading a new HTML page into webview.
     */
    public void init() {
        LOG.d(TAG, "init()");
        this.onPause(false);
        this.onDestroy();
        pluginMap.clear();
        this.startupPlugins();
    }

    /**
     * Create plugins objects that have onload set.
     */
    private void startupPlugins() {
        for (PluginEntry entry : entryMap.values()) {
            // Add a null entry to for each non-startup plugin to avoid ConcurrentModificationException
            // When iterating plugins.
            if (entry.onload) {
                getPlugin(entry.service);
            } else {
                pluginMap.put(entry.service, null);
            }
        }
    }

    /**
     * Receives a request for execution and fulfills it by finding the appropriate
     * Java class and calling it's execute method.
     *
     * PluginManager.exec can be used either synchronously or async. In either case, a JSON encoded
     * string is returned that will indicate if any errors have occurred when trying to find
     * or execute the class denoted by the clazz argument.
     *
     * @param service       String containing the service to run
     * @param action        String containing the action that the class is supposed to perform. This is
     *                      passed to the plugin execute method and it is up to the plugin developer
     *                      how to deal with it.
     * @param callbackId    String containing the id of the callback that is execute in JavaScript if
     *                      this is an async plugin call.
     * @param rawArgs       An Array literal string containing any arguments needed in the
     *                      plugin execute method.
     */
    public void exec(final String service, final String action, final String callbackId, final String rawArgs) {
        CordovaPlugin plugin = getPlugin(service);
        if (plugin == null) {
            Log.d(TAG, "exec() call to unknown plugin: " + service);
            PluginResult cr = new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION);
            app.sendPluginResult(cr, callbackId);
            return;
        }
        CallbackContext callbackContext = new CallbackContext(callbackId, app);
        try {
            long pluginStartTime = System.currentTimeMillis();
            boolean wasValidAction = plugin.execute(action, rawArgs, callbackContext);
            long duration = System.currentTimeMillis() - pluginStartTime;

            if (duration > SLOW_EXEC_WARNING_THRESHOLD) {
                Log.w(TAG, "THREAD WARNING: exec() call to " + service + "." + action + " blocked the main thread for " + duration + "ms. Plugin should use CordovaInterface.getThreadPool().");
            }
            if (!wasValidAction) {
                PluginResult cr = new PluginResult(PluginResult.Status.INVALID_ACTION);
                callbackContext.sendPluginResult(cr);
            }
        } catch (JSONException e) {
            PluginResult cr = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
            callbackContext.sendPluginResult(cr);
        } catch (Exception e) {
            Log.e(TAG, "Uncaught exception from plugin", e);
            callbackContext.error(e.getMessage());
        }
    }

    /**
     * Get the plugin object that implements the service.
     * If the plugin object does not already exist, then create it.
     * If the service doesn't exist, then return null.
     *
     * @param service       The name of the service.
     * @return              CordovaPlugin or null
     */
    public CordovaPlugin getPlugin(String service) {
        CordovaPlugin ret = pluginMap.get(service);
        if (ret == null) {
            PluginEntry pe = entryMap.get(service);
            if (pe == null) {
                return null;
            }
            if (pe.plugin != null) {
                ret = pe.plugin;
            } else {
                ret = instantiatePlugin(pe.pluginClass);
            }
            ret.privateInitialize(ctx, app, app.getPreferences());
            pluginMap.put(service, ret);
        }
        return ret;
    }

    /**
     * Add a plugin class that implements a service to the service entry table.
     * This does not create the plugin object instance.
     *
     * @param service           The service name
     * @param className         The plugin class name
     */
    public void addService(String service, String className) {
        PluginEntry entry = new PluginEntry(service, className, false);
        this.addService(entry);
    }

    /**
     * Add a plugin class that implements a service to the service entry table.
     * This does not create the plugin object instance.
     *
     * @param entry             The plugin entry
     */
    public void addService(PluginEntry entry) {
        this.entryMap.put(entry.service, entry);
        if (entry.plugin != null) {
            entry.plugin.privateInitialize(ctx, app, app.getPreferences());
            pluginMap.put(entry.service, entry.plugin);
        }
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking      Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                plugin.onPause(multitasking);
            }
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking      Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                plugin.onResume(multitasking);
            }
        }
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                plugin.onDestroy();
            }
        }
    }

    /**
     * Send a message to all plugins.
     *
     * @param id                The message id
     * @param data              The message data
     * @return                  Object to stop propagation or null
     */
    public Object postMessage(String id, Object data) {
        Object obj = this.ctx.onMessage(id, data);
        if (obj != null) {
            return obj;
        }
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                obj = plugin.onMessage(id, data);
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * Called when the activity receives a new intent.
     */
    public void onNewIntent(Intent intent) {
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                plugin.onNewIntent(intent);
            }
        }
    }

    /**
     * Called when the URL of the webview changes.
     *
     * @param url               The URL that is being changed to.
     * @return                  Return false to allow the URL to load, return true to prevent the URL from loading.
     */
    public boolean onOverrideUrlLoading(String url) {
        // Deprecated way to intercept URLs. (process <url-filter> tags).
        // Instead, plugins should not include <url-filter> and instead ensure
        // that they are loaded before this function is called (either by setting
        // the onload <param> or by making an exec() call to them)
        for (PluginEntry entry : this.entryMap.values()) {
            CordovaPlugin plugin = pluginMap.get(entry.service);
            if (plugin != null && plugin.onOverrideUrlLoading(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when the app navigates or refreshes.
     */
    public void onReset() {
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                plugin.onReset();
            }
        }
    }

    Uri remapUri(Uri uri) {
        for (CordovaPlugin plugin : this.pluginMap.values()) {
            if (plugin != null) {
                Uri ret = plugin.remapUri(uri);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * Create a plugin based on class name.
     */
    private CordovaPlugin instantiatePlugin(String className) {
        CordovaPlugin ret = null;
        try {
            Class<?> c = null;
            if ((className != null) && !("".equals(className))) {
                c = Class.forName(className);
            }
            if (c != null & CordovaPlugin.class.isAssignableFrom(c)) {
                ret = (CordovaPlugin) c.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding plugin " + className + ".");
        }
        return ret;
    }
}
