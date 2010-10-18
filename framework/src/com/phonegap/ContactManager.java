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
import org.json.JSONObject;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import android.util.Log;

public class ContactManager extends Plugin {
	
    private static ContactAccessor contactAccessor;
	private static final String LOG_TAG = "Contact Query";

	/**
	 * Constructor.
	 */
	public ContactManager()	{
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
		if (contactAccessor == null) {
			contactAccessor = ContactAccessor.getInstance(webView, ctx);
		}
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("search")) {
				JSONArray res = contactAccessor.search(args.getJSONArray(0), args.getJSONObject(1));
				return new PluginResult(status, res);
			}
			else if (action.equals("save")) {
				// TODO Coming soon!			
			}
			else if (action.equals("remove")) {
				if (contactAccessor.remove(args.getString(0))) {
					return new PluginResult(status, result);					
				}
				else {
					JSONObject r = new JSONObject();
					r.put("code", 2);
					return new PluginResult(PluginResult.Status.ERROR, r);
				}
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}
}
