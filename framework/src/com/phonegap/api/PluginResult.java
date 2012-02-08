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

import org.json.JSONArray;
import org.json.JSONObject;

public class PluginResult extends org.apache.cordova.api.PluginResult {
	
	public PluginResult(Status status) {
		super(status);
	}
	
	public PluginResult(Status status, String message) {
		super(status, message);
	}

	public PluginResult(Status status, JSONArray message) {
        super(status, message);
	}

	public PluginResult(Status status, JSONObject message) {
        super(status, message);
	}

	public PluginResult(Status status, int i) {
        super(status, i);
	}

	public PluginResult(Status status, float f) {
        super(status, f);
	}

	public PluginResult(Status status, boolean b) {
        super(status, b);
	}
}
