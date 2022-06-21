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

package __ID__;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.apache.cordova.*;
import androidx.core.splashscreen.SplashScreen;

public class __ACTIVITY__ extends CordovaActivity
{
    private boolean splashScreenKeepOnScreen = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Setup the splash screen delay based on preference settings
        setupSplashScreenDelay(splashScreen);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
    }

    private void setupSplashScreenDelay(SplashScreen splashScreen) {
        boolean splashScreenAutoHide = preferences.getBoolean("AutoHideSplashScreen", true);
        int splashScreenDelay = preferences.getInteger("SplashScreenDelay", 3000);

        Log.d("CdvSplashScreen", "Auto Hide: " + splashScreenAutoHide);

        if (splashScreenAutoHide) {
            Log.d("CdvSplashScreen", "Delay: " + splashScreenDelay + "ms");

            splashScreen.setKeepOnScreenCondition(() -> splashScreenKeepOnScreen);
            Handler splashScreenDelayHandler = new Handler();
            splashScreenDelayHandler.postDelayed(
                () -> splashScreenKeepOnScreen = false,
                splashScreenDelay
            );
        }
    }
}
