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

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class PluginResult {
	private final int status;
	private final String message;
	private boolean keepCallback = false;
	
	public PluginResult(Status status) {
		this.status = status.ordinal();
		this.message = "'" + PluginResult.StatusMessages[this.status] + "'";
	}
	
	public PluginResult(Status status, String message) {
		this.status = status.ordinal();
		this.message = JSONObject.quote(message);
	}

	public PluginResult(Status status, JSONArray message) {
		this.status = status.ordinal();
		this.message = message.toString();
	}

	public PluginResult(Status status, JSONObject message) {
		this.status = status.ordinal();
		this.message = message.toString();
	}

	public PluginResult(Status status, int i) {
		this.status = status.ordinal();
		this.message = ""+i;
	}

	public PluginResult(Status status, float f) {
		this.status = status.ordinal();
		this.message = ""+f;
	}

	public PluginResult(Status status, boolean b) {
		this.status = status.ordinal();
		this.message = ""+b;
	}
	
	public void setKeepCallback(boolean b) {
		this.keepCallback = b;
	}
	
	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
	public boolean getKeepCallback() {
		return this.keepCallback;
	}
	
	public String getJSONString() {
		return "{status:" + this.status + ",message:" + this.message + ",keepCallback:" + this.keepCallback + "}";
	}
	
	public String toSuccessCallbackString(String callbackId) {
		return "cordova.callbackSuccess('"+callbackId+"',"+this.getJSONString()+");";
	}
	
	public String toErrorCallbackString(String callbackId) {
		return "cordova.callbackError('"+callbackId+"', " + this.getJSONString()+ ");";
	}
	
	public static String[] StatusMessages = new String[] {
		"No result",
		"OK",
		"Class not found",
		"Illegal access",
		"Instantiation error",
		"Malformed url",
		"IO error",
		"Invalid action",
		"JSON error",
		"Error"
	};
	
	public enum Status {
		NO_RESULT,
		OK,
		CLASS_NOT_FOUND_EXCEPTION,
		ILLEGAL_ACCESS_EXCEPTION,
		INSTANTIATION_EXCEPTION,
		MALFORMED_URL_EXCEPTION,
		IO_EXCEPTION,
		INVALID_ACTION,
		JSON_EXCEPTION,
		ERROR
	}
}
