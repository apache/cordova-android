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
package org.apache.cordova.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.webkit.WebView;

/**
 * PluginManager is exposed to JavaScript in the Cordova WebView.
 *
 * Calling native plugin code can be done by calling PluginManager.exec(...)
 * from JavaScript.
 */
public class PluginManager {
    private static String TAG = "PluginManager";

    // List of service entries
    private final HashMap<String, PluginEntry> entries = new HashMap<String, PluginEntry>();

    private final CordovaInterface ctx;
    private final WebView app;

    // Flag to track first time through
    private boolean firstRun;

    // Map URL schemes like foo: to plugins that want to handle those schemes
    // This would allow how all URLs are handled to be offloaded to a plugin
    protected HashMap<String, String> urlMap = new HashMap<String, String>();

    /**
     * Constructor.
     *
     * @param app
     * @param ctx
     */
    public PluginManager(WebView app, CordovaInterface ctx) {
        this.ctx = ctx;
        this.app = app;
        this.firstRun = true;
    }

    /**
     * Init when loading a new HTML page into webview.
     */
    public void init() {
        LOG.d(TAG, "init()");

        // If first time, then load plugins from plugins.xml file
        if (firstRun) {
            this.loadPlugins();
            firstRun = false;
        }

        // Stop plugins on current HTML page and discard plugin objects
        else {
            this.onPause(false);
            this.onDestroy();
            this.clearPluginObjects();
        }

        // Start up all plugins that have onload specified
        this.startupPlugins();
    }

    /**
     * Load plugins from res/xml/plugins.xml
     */
    public void loadPlugins() {
        int id = ctx.getResources().getIdentifier("plugins", "xml", ctx.getPackageName());
        if (id == 0) {
            pluginConfigurationMissing();
        }
        XmlResourceParser xml = ctx.getResources().getXml(id);
        int eventType = -1;
        String service = "", pluginClass = "";
        boolean onload = false;
        PluginEntry entry = null;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals("plugin")) {
                    service = xml.getAttributeValue(null, "name");
                    pluginClass = xml.getAttributeValue(null, "value");
                    // System.out.println("Plugin: "+name+" => "+value);
                    onload = "true".equals(xml.getAttributeValue(null, "onload"));
                    entry = new PluginEntry(service, pluginClass, onload);
                    this.addService(entry);
                } else if (strNode.equals("url-filter")) {
                    this.urlMap.put(xml.getAttributeValue(null, "value"), service);
                }
            }
            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete all plugin objects.
     */
    public void clearPluginObjects() {
        for (PluginEntry entry : this.entries.values()) {
            entry.plugin = null;
        }
    }

    /**
     * Create plugins objects that have onload set.
     */
    public void startupPlugins() {
        for (PluginEntry entry : this.entries.values()) {
            if (entry.onload) {
                entry.createPlugin(this.app, this.ctx);
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
     * @param action        String containt the action that the class is supposed to perform. This is
     *                      passed to the plugin execute method and it is up to the plugin developer
     *                      how to deal with it.
     * @param callbackId    String containing the id of the callback that is execute in JavaScript if
     *                      this is an async plugin call.
     * @param args          An Array literal string containing any arguments needed in the
     *                      plugin execute method.
     * @param async         Boolean indicating whether the calling JavaScript code is expecting an
     *                      immediate return value. If true, either Cordova.callbackSuccess(...) or
     *                      Cordova.callbackError(...) is called once the plugin code has executed.
     *
     * @return              JSON encoded string with a response message and status.
     */
    @SuppressWarnings("unchecked")
    public String exec(final String service, final String action, final String callbackId, final String jsonArgs, final boolean async) {
        PluginResult cr = null;
        boolean runAsync = async;
        try {
            final JSONArray args = new JSONArray(jsonArgs);
            final IPlugin plugin = this.getPlugin(service);
            final CordovaInterface ctx = this.ctx;
            if (plugin != null) {
                runAsync = async && !plugin.isSynch(action);
                if (runAsync) {
                    // Run this on a different thread so that this one can return back to JS
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                // Call execute on the plugin so that it can do it's thing
                                PluginResult cr = plugin.execute(action, args, callbackId);
                                int status = cr.getStatus();

                                // If no result to be sent and keeping callback, then no need to sent back to JavaScript
                                if ((status == PluginResult.Status.NO_RESULT.ordinal()) && cr.getKeepCallback()) {
                                }

                                // Check the success (OK, NO_RESULT & !KEEP_CALLBACK)
                                else if ((status == PluginResult.Status.OK.ordinal()) || (status == PluginResult.Status.NO_RESULT.ordinal())) {
                                    ctx.sendJavascript(cr.toSuccessCallbackString(callbackId));
                                }

                                // If error
                                else {
                                    ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
                                }
                            } catch (Exception e) {
                                PluginResult cr = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                                ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
                            }
                        }
                    });
                    thread.start();
                    return "";
                } else {
                    // Call execute on the plugin so that it can do it's thing
                    cr = plugin.execute(action, args, callbackId);

                    // If no result to be sent and keeping callback, then no need to sent back to JavaScript
                    if ((cr.getStatus() == PluginResult.Status.NO_RESULT.ordinal()) && cr.getKeepCallback()) {
                        return "";
                    }
                }
            }
        } catch (JSONException e) {
            System.out.println("ERROR: " + e.toString());
            cr = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        }
        // if async we have already returned at this point unless there was an error...
        if (runAsync) {
            if (cr == null) {
                cr = new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION);
            }
            ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
        }
        return (cr != null ? cr.getJSONString() : "{ status: 0, message: 'all good' }");
    }

    /**
     * Get the plugin object that implements the service.
     * If the plugin object does not already exist, then create it.
     * If the service doesn't exist, then return null.
     *
     * @param service       The name of the service.
     * @return              IPlugin or null
     */
    private IPlugin getPlugin(String service) {
        PluginEntry entry = entries.get(service);
        if (entry == null) {
            return null;
        }
        IPlugin plugin = entry.plugin;
        if (plugin == null) {
            plugin = entry.createPlugin(this.app, this.ctx);
        }
        return plugin;
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
        this.entries.put(entry.service, entry);
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking      Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
        for (PluginEntry entry : this.entries.values()) {
            if (entry.plugin != null) {
                entry.plugin.onPause(multitasking);
            }
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking      Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
        for (PluginEntry entry : this.entries.values()) {
            if (entry.plugin != null) {
                entry.plugin.onResume(multitasking);
            }
        }
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
        for (PluginEntry entry : this.entries.values()) {
            if (entry.plugin != null) {
                entry.plugin.onDestroy();
            }
        }
    }

    /**
     * Send a message to all plugins.
     *
     * @param id                The message id
     * @param data              The message data
     */
    public void postMessage(String id, Object data) {
        for (PluginEntry entry : this.entries.values()) {
            if (entry.plugin != null) {
                entry.plugin.onMessage(id, data);
            }
        }
    }

    /**
     * Called when the activity receives a new intent.
     */
    public void onNewIntent(Intent intent) {
        for (PluginEntry entry : this.entries.values()) {
            if (entry.plugin != null) {
                entry.plugin.onNewIntent(intent);
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
        Iterator<Entry<String, String>> it = this.urlMap.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<String, String> pairs = it.next();
            if (url.startsWith(pairs.getKey())) {
                return this.getPlugin(pairs.getValue()).onOverrideUrlLoading(url);
            }
        }
        return false;
    }

    private void pluginConfigurationMissing() {
        System.err.println("=====================================================================================");
        System.err.println("ERROR: plugin.xml is missing.  Add res/xml/plugins.xml to your project.");
        System.err.println("https://git-wip-us.apache.org/repos/asf?p=incubator-cordova-android.git;a=blob;f=framework/res/xml/plugins.xml");
        System.err.println("=====================================================================================");
    }
}
