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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.cordova.api.CordovaInterface;

import android.os.Message;
import android.util.Log;
import android.webkit.WebView;

/**
 * Holds the list of messages to be sent to the WebView.
 */
public class NativeToJsMessageQueue {
    private static final String LOG_TAG = "JsMessageQueue";

    // This must match the default value in incubator-cordova-js/lib/android/exec.js
    private static final int DEFAULT_BRIDGE_MODE = 1;
    
    /**
     * The index into registeredListeners to treat as active. 
     */
    private int activeListenerIndex;
    
    /**
     * The list of JavaScript statements to be sent to JavaScript.
     */
    private final LinkedList<String> queue = new LinkedList<String>();

    /**
     * The array of listeners that can be used to send messages to JS.
     */
    private final BridgeMode[] registeredListeners;    
    
    private final CordovaInterface cordova;
    private final CordovaWebView webView;
        
    public NativeToJsMessageQueue(CordovaWebView webView, CordovaInterface cordova) {
        this.cordova = cordova;
        this.webView = webView;
        registeredListeners = new BridgeMode[5];
        registeredListeners[0] = null;  // Polling. Requires no logic.
        registeredListeners[1] = new CallbackBridgeMode();
        registeredListeners[2] = new LoadUrlBridgeMode();
        registeredListeners[3] = new OnlineEventsBridgeMode();
        registeredListeners[4] = new PrivateApiBridgeMode();
        reset();
    }
    
    /**
     * Changes the bridge mode.
     */
    public void setBridgeMode(int value) {
        if (value < 0 || value >= registeredListeners.length) {
            Log.d(LOG_TAG, "Invalid NativeToJsBridgeMode: " + value);
        } else {
            if (value != activeListenerIndex) {
                Log.d(LOG_TAG, "Set native->JS mode to " + value);
                synchronized (this) {
                    activeListenerIndex = value;
                    BridgeMode activeListener = registeredListeners[value];
                    if (!queue.isEmpty() && activeListener != null) {
                        activeListener.onNativeToJsMessageAvailable();
                    }
                }
            }
        }
    }
    
    /**
     * Clears all messages and resets to the default bridge mode.
     */
    public void reset() {
        synchronized (this) {
            queue.clear();
            setBridgeMode(DEFAULT_BRIDGE_MODE);
        }
    }

    /**
     * Removes and returns the last statement in the queue.
     * Returns null if the queue is empty.
     */
    public String pop() {
        synchronized (this) {
            if (queue.isEmpty()) {
                return null;
            }
            return queue.remove(0);
        }
    }

    /**
     * Combines and returns all statements. Clears the queue.
     * Returns null if the queue is empty.
     */
    public String popAll() {
        synchronized (this) {
            int length = queue.size();
            if (length == 0) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            // Wrap each statement in a try/finally so that if one throws it does 
            // not affect the next.
            int i = 0;
            for (String message : queue) {
                if (++i == length) {
                    sb.append(message);
                } else {
                    sb.append("try{")
                      .append(message)
                      .append("}finally{");
                }
            }
            for ( i = 1; i < length; ++i) {
                sb.append('}');
            }
            queue.clear();
            return sb.toString();
        }
    }    

    /**
     * Add a JavaScript statement to the list.
     */
    public void add(String statement) {
        synchronized (this) {
            queue.add(statement);
            if (registeredListeners[activeListenerIndex] != null) {
                registeredListeners[activeListenerIndex].onNativeToJsMessageAvailable();
            }
        }
    }

    private interface BridgeMode {
        void onNativeToJsMessageAvailable();
    }
    
    /** Uses a local server to send messages to JS via an XHR */
    private class CallbackBridgeMode implements BridgeMode {
        public void onNativeToJsMessageAvailable() {
            if (webView.callbackServer != null) {
                webView.callbackServer.onNativeToJsMessageAvailable(NativeToJsMessageQueue.this);
            }
        }
    }
    
    /** Uses webView.loadUrl("javascript:") to execute messages. */
    private class LoadUrlBridgeMode implements BridgeMode {
        public void onNativeToJsMessageAvailable() {
            webView.loadUrlNow("javascript:" + popAll());
        }
    }

    /** Uses online/offline events to tell the JS when to poll for messages. */
    private class OnlineEventsBridgeMode implements BridgeMode {
        boolean online = true;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!queue.isEmpty()) {
                    online = !online;
                    webView.setNetworkAvailable(online);
                }
            }                
        };
        OnlineEventsBridgeMode() {
            webView.setNetworkAvailable(true);
        }
        public void onNativeToJsMessageAvailable() {
            // TODO(agrieve): consider running this *not* on the main thread, since it just
            // sends a message under-the-hood anyways.
            cordova.getActivity().runOnUiThread(runnable);
        }
    }
    
    /**
     * Uses Java reflection to access an API that lets us eval JS.
     * Requires Android 3.2.4 or above. 
     */
    private class PrivateApiBridgeMode implements BridgeMode {
    	// Message added in commit:
    	// http://omapzoom.org/?p=platform/frameworks/base.git;a=commitdiff;h=9497c5f8c4bc7c47789e5ccde01179abc31ffeb2
    	// Which first appeared in 3.2.4ish.
    	private static final int EXECUTE_JS = 194;
    	
    	Method sendMessageMethod;
    	Object webViewCore;
    	boolean initFailed;

    	@SuppressWarnings("rawtypes")
    	private void initReflection() {
        	Object webViewObject = webView;
    		Class webViewClass = WebView.class;
        	try {
    			Field f = webViewClass.getDeclaredField("mProvider");
    			f.setAccessible(true);
    			webViewObject = f.get(webView);
    			webViewClass = webViewObject.getClass();
        	} catch (Throwable e) {
        		// mProvider is only required on newer Android releases.
    		}
        	
        	try {
    			Field f = webViewClass.getDeclaredField("mWebViewCore");
                f.setAccessible(true);
    			webViewCore = f.get(webViewObject);
    			
    			if (webViewCore != null) {
    				sendMessageMethod = webViewCore.getClass().getDeclaredMethod("sendMessage", Message.class);
	    			sendMessageMethod.setAccessible(true);	    			
    			}
    		} catch (Throwable e) {
    			initFailed = true;
				Log.e(LOG_TAG, "PrivateApiBridgeMode failed to find the expected APIs.", e);
    		}
    	}
    	
        public void onNativeToJsMessageAvailable() {
        	if (sendMessageMethod == null && !initFailed) {
        		initReflection();
        	}
        	// webViewCore is lazily initialized, and so may not be available right away.
        	if (sendMessageMethod != null) {
	        	String js = popAll();
	        	Message execJsMessage = Message.obtain(null, EXECUTE_JS, js);
				try {
				    sendMessageMethod.invoke(webViewCore, execJsMessage);
				} catch (Throwable e) {
					Log.e(LOG_TAG, "Reflection message bridge failed.", e);
				}
        	}
        }
    }    
}
