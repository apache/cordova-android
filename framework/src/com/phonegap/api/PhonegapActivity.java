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

import android.app.Activity;
import android.content.Intent;

/**
 * The Phonegap activity abstract class that is extended by DroidGap.
 * It is used to isolate plugin development, and remove dependency on entire Phonegap library.
 */
public abstract class PhonegapActivity extends Activity {

    /**
     * @deprecated
     * Add services to res/xml/plugins.xml instead.
     * 
     * Add a class that implements a service.
     * 
     * @param serviceType
     * @param className
     */
    @Deprecated
    abstract public void addService(String serviceType, String className);
    
    /**
     * Send JavaScript statement back to JavaScript.
     * 
     * @param message
     */
    abstract public void sendJavascript(String statement);

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits, 
     * your onActivityResult() method will be called.
     *  
     * @param command			The command object
     * @param intent			The intent to start
     * @param requestCode		The request code that is passed to callback to identify the activity
     */
    abstract public void startActivityForResult(IPlugin command, Intent intent, int requestCode);

    /**
     * Set the plugin to be called when a sub-activity exits.
     * 
     * @param plugin			The plugin on which onActivityResult is to be called
     */
    abstract public void setActivityResultCallback(IPlugin plugin);

    /**
     * Load the specified URL in the PhoneGap webview.
     * 
     * @param url				The URL to load.
     */
    abstract public void loadUrl(String url);
    
    /**
     * Send a message to all plugins. 
     * 
     * @param id            The message id
     * @param data          The message data
     */
    abstract public void postMessage(String id, Object data);
}
