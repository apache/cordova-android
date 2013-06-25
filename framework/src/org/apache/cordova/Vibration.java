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

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.os.Vibrator;

/**
 * This class provides access to vibrations on the device.
 */
public class Vibration extends CordovaPlugin {

    /**
     * Constructor.
     */
    public Vibration() {
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("vibrate")) {
            this.vibrate(args.getLong(0));
        }
        else {
            return false;
        }

        // Only alert and confirm are async.
        callbackContext.success();
        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Vibrates the device for the specified amount of time.
     *
     * @param time      Time to vibrate in ms.
     */
    public void vibrate(long time) {
        // Start the vibration, 0 defaults to half a second.
        if (time == 0) {
            time = 500;
        }
        Vibrator vibrator = (Vibrator) this.cordova.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
    }
}
