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

import android.annotation.SuppressLint;
import android.os.Handler;

import androidx.core.splashscreen.SplashScreen;

import org.json.JSONArray;
import org.json.JSONException;

@SuppressLint("LongLogTag")
public class SplashScreenPlugin extends CordovaPlugin {
    public static final String PLUGIN_NAME = "CordovaSplashScreenPlugin";
    // Config Preferences
    boolean splashScreenAutoHide;
    int splashScreenDelay;
    // Flag that determines if the splash screen is still visible or hidden.
    private boolean splashScreenKeepOnScreen = true;

    @Override
    protected void pluginInitialize() {
        // Update Config Preferences
        splashScreenAutoHide = preferences.getBoolean("AutoHideSplashScreen", true);
        splashScreenDelay = preferences.getInteger("SplashScreenDelay", 3000);
    }

    @Override
    public boolean execute(
        String action,
        JSONArray args,
        CallbackContext callbackContext
    ) throws JSONException {
        if (action.equals("hide") && splashScreenAutoHide == false) {
            /*
             * The `.hide()` method can only be triggered if the `splashScreenAutoHide`
             * is set to `false`.
             */
            splashScreenKeepOnScreen = false;
        } else {
            return false;
        }

        callbackContext.success();
        return true;
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("setupSplashScreenDelay".equals(id)) {
            setupSplashScreenDelay((SplashScreen) data);
        }

        return null;
    }

    private void setupSplashScreenDelay(SplashScreen splashScreen) {
        LOG.d(PLUGIN_NAME, "Auto Hide: " + splashScreenAutoHide);

        splashScreen.setKeepOnScreenCondition(() -> splashScreenKeepOnScreen);

        if (splashScreenAutoHide) {
            LOG.d(PLUGIN_NAME, "Delay: " + splashScreenDelay + "ms");
            Handler splashScreenDelayHandler = new Handler();
            splashScreenDelayHandler.postDelayed(
                () -> splashScreenKeepOnScreen = false,
                splashScreenDelay
            );
        }

        // If splashScreenAutoHide = false, nothing needs to be handled at this level
        // The `splashScreenKeepOnScreen` variable will be updated in the `execute` method.
        // It is to be triggered by the `navigator.splashscreen.hide()` method call.
    }
}
