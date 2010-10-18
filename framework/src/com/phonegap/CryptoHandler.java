/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class CryptoHandler extends Plugin {
		
	/**
	 * Constructor.
	 */
	public CryptoHandler() {
	}

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("encrypt")) {
				this.encrypt(args.getString(0), args.getString(1));
			}
			else if (action.equals("decrypt")) {
				this.decrypt(args.getString(0), args.getString(1));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	public void encrypt(String pass, String text) {
		try {
			String encrypted = SimpleCrypto.encrypt(pass,text);
			// TODO: Why not just return text now?
			this.sendJavascript("Crypto.gotCryptedString('" + text + "')");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void decrypt(String pass, String text) {
		try {
			String decrypted = SimpleCrypto.decrypt(pass,text);
			this.sendJavascript("Crypto.gotPlainString('" + text + "')");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
