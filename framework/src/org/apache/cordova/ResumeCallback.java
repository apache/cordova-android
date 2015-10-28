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
