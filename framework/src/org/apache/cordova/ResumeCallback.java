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


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResumeCallback extends CallbackContext {
    private CordovaInterface cordovaInterface;
    private String serviceName;
    private PluginManager pluginManager;

    public ResumeCallback(CordovaInterface cordovaInterface, String serviceName, PluginManager pluginManager) {
        super("resumecallback", null);
        this.cordovaInterface = cordovaInterface;
        this.serviceName = serviceName;
        this.pluginManager = pluginManager;
    }

    @Override
    public void sendPluginResult(PluginResult pluginResult) {
        synchronized (this) {
            if (finished) {
                return;
            } else {
                finished = true;
            }
        }

        JSONObject event = cordovaInterface.getSavedApplicationState();
        JSONObject pluginResultObject = new JSONObject();

        try {
            pluginResultObject.put("pluginServiceName", this.serviceName);
            pluginResultObject.put("pluginStatus", PluginResult.StatusMessages[pluginResult.getStatus()]);

            event.put("action", "resume");
            event.put("pendingResult", pluginResultObject);
        } catch (JSONException e) {
        }

        PluginResult eventResult = new PluginResult(PluginResult.Status.OK, event);

        List<PluginResult> result = new ArrayList<PluginResult>();
        result.add(eventResult);
        result.add(pluginResult);

        CoreAndroid appPlugin = (CoreAndroid) pluginManager.getPlugin(CoreAndroid.PLUGIN_NAME);
        appPlugin.sendResumeEvent(new PluginResult(PluginResult.Status.OK, result));
    }
}
