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
package com.phonegap.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.webkit.WebView;

/**
 * PluginManager is exposed to JavaScript in the PhoneGap WebView.
 * 
 * Calling native plugin code can be done by calling PluginManager.exec(...)
 * from JavaScript.
 */
public final class PluginManager {

	private HashMap<String, IPlugin> plugins = new HashMap<String,IPlugin>();
	private HashMap<String, String> services = new HashMap<String,String>();
	
	private final PhonegapActivity ctx;
	private final WebView app;
	
    // Map URL schemes like foo: to plugins that want to handle those schemes
    // This would allow how all URLs are handled to be offloaded to a plugin
    protected HashMap<String, String> urlMap = new HashMap<String,String>();
	
	/**
	 * Constructor.
	 * 
	 * @param app
	 * @param ctx
	 */
	public PluginManager(WebView app, PhonegapActivity ctx) {
		this.ctx = ctx;
		this.app = app;
		this.loadPlugins();
	}
	
	/**
	 * Re-init when loading a new HTML page into webview.
	 */
	public void reinit() {
	    
	    // Stop plugins on current HTML page and discard
	    this.onPause(false);
	    this.onDestroy();
	    this.plugins = new HashMap<String, IPlugin>();
	}
	
	/**
	 * Load plugins from res/xml/plugins.xml
	 */
	public void loadPlugins() {
		int id = ctx.getResources().getIdentifier("plugins", "xml", ctx.getPackageName());
		if (id == 0) { pluginConfigurationMissing(); }
		XmlResourceParser xml = ctx.getResources().getXml(id);
		int eventType = -1;
		String pluginClass = "", pluginName = "";
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_TAG) {
				String strNode = xml.getName();
				if (strNode.equals("plugin")) {
					pluginClass = xml.getAttributeValue(null, "value");
					pluginName = xml.getAttributeValue(null, "name");
					//System.out.println("Plugin: "+name+" => "+value);
					this.addService(pluginName, pluginClass);
					
					// Create plugin at load time if attribute "onload"
					if ("true".equals(xml.getAttributeValue(null, "onload"))) {
					    this.getPlugin(pluginName);
					}
				} else if (strNode.equals("url-filter")) {
					this.urlMap.put(xml.getAttributeValue(null, "value"), pluginName);
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
	 * Receives a request for execution and fulfills it by finding the appropriate
	 * Java class and calling it's execute method.
	 * 
	 * PluginManager.exec can be used either synchronously or async. In either case, a JSON encoded 
	 * string is returned that will indicate if any errors have occurred when trying to find
	 * or execute the class denoted by the clazz argument.
	 * 
	 * @param service 		String containing the service to run
	 * @param action 		String containt the action that the class is supposed to perform. This is
	 * 						passed to the plugin execute method and it is up to the plugin developer 
	 * 						how to deal with it.
	 * @param callbackId 	String containing the id of the callback that is execute in JavaScript if
	 * 						this is an async plugin call.
	 * @param args 			An Array literal string containing any arguments needed in the
	 * 						plugin execute method.
	 * @param async 		Boolean indicating whether the calling JavaScript code is expecting an
	 * 						immediate return value. If true, either PhoneGap.callbackSuccess(...) or 
	 * 						PhoneGap.callbackError(...) is called once the plugin code has executed.
	 * 
	 * @return 				JSON encoded string with a response message and status.
	 */
	@SuppressWarnings("unchecked")
	public String exec(final String service, final String action, final String callbackId, final String jsonArgs, final boolean async) {
		PluginResult cr = null;
		boolean runAsync = async;
		try {
			final JSONArray args = new JSONArray(jsonArgs);
			final IPlugin plugin = this.getPlugin(service); 
			final PhonegapActivity ctx = this.ctx;
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
			System.out.println("ERROR: "+e.toString());
			cr = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
		// if async we have already returned at this point unless there was an error...
		if (runAsync) {
			if (cr == null) {
				cr = new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION);				
			}
			ctx.sendJavascript(cr.toErrorCallbackString(callbackId));
		}
		return ( cr != null ? cr.getJSONString() : "{ status: 0, message: 'all good' }" );
	}
	
	/**
	 * Get the class.
	 * 
	 * @param clazz
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private Class getClassByName(final String clazz) throws ClassNotFoundException {
		Class c = null;
		if (clazz != null) {
			c = Class.forName(clazz);
		}
		return c;
	}

	/**
	 * Get the interfaces that a class implements and see if it implements the
	 * com.phonegap.api.Plugin interface.
	 * 
	 * @param c The class to check the interfaces of.
	 * @return Boolean indicating if the class implements com.phonegap.api.Plugin
	 */
	@SuppressWarnings("unchecked")
	private boolean isPhoneGapPlugin(Class c) {
		if (c != null) {
			return com.phonegap.api.Plugin.class.isAssignableFrom(c) || com.phonegap.api.IPlugin.class.isAssignableFrom(c);
		}
		return false;
	}

    /**
     * Add plugin to be loaded and cached.  This creates an instance of the plugin.
     * If plugin is already created, then just return it.
     * 
     * @param className				The class to load
     * @param clazz					The class object (must be a class object of the className)
     * @param callbackId			The callback id to use when calling back into JavaScript
     * @return						The plugin
     */
	@SuppressWarnings("unchecked")
	private IPlugin addPlugin(String pluginName, String className) {
		try {
			Class c = getClassByName(className);
			if (isPhoneGapPlugin(c)) {
				IPlugin plugin = (IPlugin)c.newInstance();
				this.plugins.put(className, plugin);
				plugin.setContext(this.ctx);
				plugin.setView(this.app);
				return plugin;
			}
    	} catch (Exception e) {
    		  e.printStackTrace();
    		  System.out.println("Error adding plugin "+className+".");
    	}
    	return null;
    }
    
    /**
     * Get the loaded plugin.
     * 
     * If the plugin is not already loaded then load it.
     * 
     * @param className				The class of the loaded plugin.
     * @return
     */
    private IPlugin getPlugin(String pluginName) {
		String className = this.services.get(pluginName);
    	if (this.plugins.containsKey(className)) {
    		return this.plugins.get(className);
    	} else {
	    	return this.addPlugin(pluginName, className);
	    }
    }
    
    /**
     * Add a class that implements a service.
     * This does not create the class instance.  It just maps service name to class name.
     * 
     * @param serviceType
     * @param className
     */
    public void addService(String serviceType, String className) {
    	this.services.put(serviceType, className);
    }

    /**
     * Called when the system is about to start resuming a previous activity. 
     * 
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onPause(boolean multitasking) {
        for (IPlugin plugin : this.plugins.values()) {
            plugin.onPause(multitasking);
        }
    }
    
    /**
     * Called when the activity will start interacting with the user. 
     * 
     * @param multitasking		Flag indicating if multitasking is turned on for app
     */
    public void onResume(boolean multitasking) {
        for (IPlugin plugin : this.plugins.values()) {
            plugin.onResume(multitasking);
        }
    }

    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
        for (IPlugin plugin : this.plugins.values()) {
            plugin.onDestroy();
        }
    }

    /**
     * Send a message to all plugins. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
    public void postMessage(String id, Object data) {
        for (IPlugin plugin : this.plugins.values()) {
            plugin.onMessage(id, data);
        }
    }

    /**
     * Called when the activity receives a new intent. 
     */    
    public void onNewIntent(Intent intent) {
        for (IPlugin plugin : this.plugins.values()) {
            plugin.onNewIntent(intent);
        }
    }

    /**
     * Called when the URL of the webview changes.
     * 
     * @param url The URL that is being changed to.
     * @return Return false to allow the URL to load, return true to prevent the URL from loading.
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
		System.err.println("https://raw.github.com/phonegap/phonegap-android/master/framework/res/xml/plugins.xml");        
		System.err.println("=====================================================================================");
	}
}
