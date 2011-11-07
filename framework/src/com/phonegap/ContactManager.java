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
package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import android.util.Log;

public class ContactManager extends Plugin {
	
    private ContactAccessor contactAccessor;
	private static final String LOG_TAG = "Contact Query";

	public static final int UNKNOWN_ERROR = 0;
	public static final int INVALID_ARGUMENT_ERROR = 1;
	public static final int TIMEOUT_ERROR = 2;
	public static final int PENDING_OPERATION_ERROR = 3;
	public static final int IO_ERROR = 4;
	public static final int NOT_SUPPORTED_ERROR = 5;
	public static final int PERMISSION_DENIED_ERROR = 20;

	
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
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";     

        /**
         * Check to see if we are on an Android 1.X device.  If we are return an error as we 
         * do not support this as of PhoneGap 1.0.
         */
        if (android.os.Build.VERSION.RELEASE.startsWith("1.")) {
            JSONObject res = null;
            try {
                res = new JSONObject();
                res.put("code", NOT_SUPPORTED_ERROR);
                res.put("message", "Contacts are not supported in Android 1.X devices");
            } catch (JSONException e) {
                // This should never happen
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return new PluginResult(PluginResult.Status.ERROR, res);
        }

        /**
         * Only create the contactAccessor after we check the Android version or the program will crash
         * older phones.
         */
        if (this.contactAccessor == null) {
            this.contactAccessor = new ContactAccessorSdk5(this.webView, this.ctx);
        }
        
		try {
			if (action.equals("search")) {
				JSONArray res = contactAccessor.search(args.getJSONArray(0), args.optJSONObject(1));
				return new PluginResult(status, res, "navigator.contacts.cast");
			}
			else if (action.equals("save")) {
			    String id = contactAccessor.save(args.getJSONObject(0));
				if (id != null) {
	                JSONObject res = contactAccessor.getContactById(id);
	                if (res != null) {
	                    return new PluginResult(status, res);
	                }
				}
			}
			else if (action.equals("remove")) {
				if (contactAccessor.remove(args.getString(0))) {
					return new PluginResult(status, result);					
				}
			}
			// If we get to this point an error has occurred
            JSONObject r = new JSONObject();
            r.put("code", UNKNOWN_ERROR);
            return new PluginResult(PluginResult.Status.ERROR, r);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}
}
