package org.apache.cordova;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;

public class SplashScreen extends Plugin {

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        if (action.equals("hide")) {
            ((DroidGap)this.ctx).removeSplashScreen();
        }
        else {
            status = PluginResult.Status.INVALID_ACTION;
        }
        return new PluginResult(status, result);
    }

}
