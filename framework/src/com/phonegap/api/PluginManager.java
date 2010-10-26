/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap.api;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;

import android.webkit.WebView;
import com.phonegap.DroidGap;

/**
 * PluginManager is exposed to JavaScript in the PhoneGap WebView.
 * 
 * Calling native plugin code can be done by calling PluginManager.exec(...)
 * from JavaScript.
 */
public final class PluginManager {	

	private HashMap<String, Plugin> plugins = new HashMap<String,Plugin>();
	private HashMap<String, String> services = new HashMap<String,String>();
	
	private final DroidGap ctx;
	private final WebView app;
	
	/**
	 * Constructor.
	 * 
	 * @param app
	 * @param ctx
	 */
	public PluginManager(WebView app, DroidGap ctx) {
		System.out.println("PluginManager()");
		this.ctx = ctx;
		this.app = app;
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
		System.out.println("PluginManager.exec("+service+", "+action+", "+callbackId+", "+jsonArgs+", "+async+")");
		PluginResult cr = null;
		boolean runAsync = async;
		try {
			final JSONArray args = new JSONArray(jsonArgs);
			String clazz = this.services.get(service);
			Class c = null;
			if (clazz != null) {
				c = getClassByName(clazz);
			}
			if (isPhoneGapPlugin(c)) {
				final Plugin plugin = this.addPlugin(clazz, c); 
				final DroidGap ctx = this.ctx;
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
								PluginResult cr = new PluginResult(PluginResult.Status.ERROR);
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
		} catch (ClassNotFoundException e) {
			cr = new PluginResult(PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION);
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
		if (cr != null) {
			System.out.println(" -- returning result: "+cr.getJSONString());
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
		return Class.forName(clazz);
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
     * @return						The plugin
     */
	public Plugin addPlugin(String className) {
	    try {
            return this.addPlugin(className, this.getClassByName(className)); 
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Error adding plugin "+className+".");
        }
        return null;
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
	private Plugin addPlugin(String className, Class clazz) { 
    	if (this.plugins.containsKey(className)) {
    		return this.getPlugin(className);
    	}
    	System.out.println("PluginManager.addPlugin("+className+")");
    	try {
              Plugin plugin = (Plugin)clazz.newInstance();
              this.plugins.put(className, plugin);
              plugin.setContext((DroidGap)this.ctx);
              plugin.setView(this.app);
              return plugin;
    	}
    	catch (Exception e) {
    		  e.printStackTrace();
    		  System.out.println("Error adding plugin "+className+".");
    	}
    	return null;
    }
    
    /**
     * Get the loaded plugin.
     * 
     * @param className				The class of the loaded plugin.
     * @return
     */
    private Plugin getPlugin(String className) {
    	Plugin plugin = this.plugins.get(className);
    	return plugin;
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
     */
    public void onPause() {
    	java.util.Set<Entry<String,Plugin>> s = this.plugins.entrySet();
    	java.util.Iterator<Entry<String,Plugin>> it = s.iterator();
    	while(it.hasNext()) {
    		Entry<String,Plugin> entry = it.next();
    		Plugin plugin = entry.getValue();
    		plugin.onPause();
    	}
    }
    
    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    	java.util.Set<Entry<String,Plugin>> s = this.plugins.entrySet();
    	java.util.Iterator<Entry<String,Plugin>> it = s.iterator();
    	while(it.hasNext()) {
    		Entry<String,Plugin> entry = it.next();
    		Plugin plugin = entry.getValue();
    		plugin.onResume();
    	}    	
    }

    /**
     * The final call you receive before your activity is destroyed. 
     */
    public void onDestroy() {
    	java.util.Set<Entry<String,Plugin>> s = this.plugins.entrySet();
    	java.util.Iterator<Entry<String,Plugin>> it = s.iterator();
    	while(it.hasNext()) {
    		Entry<String,Plugin> entry = it.next();
    		Plugin plugin = entry.getValue();
    		plugin.onDestroy();
    	}
    }
}