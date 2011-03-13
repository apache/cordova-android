/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
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
     * Add a class that implements a service.
     * 
     * @param serviceType
     * @param className
     */
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
    abstract public void startActivityForResult(Plugin command, Intent intent, int requestCode);
}
