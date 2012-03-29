package org.apache.cordova.test;

import org.apache.cordova.api.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

/**
 * This class provides a service.
 */
public class ActivityPlugin extends Plugin {

    static String TAG = "ActivityPlugin";

    /**
     * Constructor.
     */
    public ActivityPlugin() {
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        try {
            if (action.equals("start")) {
                this.startActivity(args.getString(0));
            }
            return new PluginResult(status, result);
        } catch (JSONException e) {
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        }
    }

    // --------------------------------------------------------------------------
    // LOCAL METHODS
    // --------------------------------------------------------------------------

    public void startActivity(String className) {
        try {
            Intent intent = new Intent().setClass(this.ctx.getContext(), Class.forName(className));
            LOG.d(TAG, "Starting activity %s", className);
            this.ctx.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOG.e(TAG, "Error starting activity %s", className);
        }
    }

}
