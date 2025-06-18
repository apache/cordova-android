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
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.json.JSONArray;
import org.json.JSONException;

public class SystemBarPlugin extends CordovaPlugin {
    static final String PLUGIN_NAME = "SystemBarPlugin";

    static final int INVALID_COLOR = -1;

    // Internal variables
    private Context context;
    private Resources resources;
    private int overrideStatusBarBackgroundColor = INVALID_COLOR;


    @Override
    protected void pluginInitialize() {
        context = cordova.getContext();
        resources = context.getResources();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cordova.getActivity().runOnUiThread(this::updateSystemBars);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        cordova.getActivity().runOnUiThread(this::updateSystemBars);
    }

    @Override
    public Object onMessage(String id, Object data) {
        if (id.equals("updateSystemBars")) {
            cordova.getActivity().runOnUiThread(this::updateSystemBars);
        }
        return null;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
                && preferences.getBoolean("AndroidEdgeToEdge", false)
        ) {
            // Disable JS API in E2E mode (SDK >= 35)
            return false;
        }

        if ("setStatusBarVisible".equals(action)) {
            boolean visible = args.getBoolean(0);
            cordova.getActivity().runOnUiThread(() -> setStatusBarVisible(visible));
        } else if ("setStatusBarBackgroundColor".equals(action)) {
            String bgColor = args.getString(0);
            cordova.getActivity().runOnUiThread(() -> setStatusBarBackgroundColor(bgColor));
        } else {
            return false;
        }

        callbackContext.success();
        return true;
    }

    private void setStatusBarVisible(final boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            View statusBar = getStatusBarView(webView);
            if (statusBar != null) {
                statusBar.setVisibility(visible ? View.VISIBLE : View.GONE);

                FrameLayout rootLayout = getRootLayout(webView);
                if (rootLayout != null) {
                    ViewCompat.requestApplyInsets(rootLayout);
                }
            }
        } else {
            Window window = cordova.getActivity().getWindow();
            int uiOptions = window.getDecorView().getSystemUiVisibility();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (visible) {
                uiOptions &= ~flags;
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                uiOptions |= flags;
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            window.getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    private void setStatusBarBackgroundColor(final String colorPref) {
        int parsedColor = parseColorFromString(colorPref);
        if (parsedColor == INVALID_COLOR) return;

        overrideStatusBarBackgroundColor = Color.parseColor(colorPref);
        updateStatusBar(overrideStatusBarBackgroundColor);
    }

    private void updateSystemBars() {
        // Update Root View Background Color
        int rootViewBackgroundColor = getPreferenceBackgroundColor();
        if (rootViewBackgroundColor == INVALID_COLOR) {
            rootViewBackgroundColor = getUiModeColor();
        }
        updateRootView(rootViewBackgroundColor);

        // Update StatusBar Background Color
        int statusBarBackgroundColor;
        if (overrideStatusBarBackgroundColor != INVALID_COLOR) {
            statusBarBackgroundColor = overrideStatusBarBackgroundColor;
        } else if (preferences.contains("StatusBarBackgroundColor")) {
            statusBarBackgroundColor = getPreferenceStatusBarBackgroundColor();
        } else if(preferences.contains("BackgroundColor")){
            statusBarBackgroundColor =  rootViewBackgroundColor;
        } else {
            statusBarBackgroundColor = getUiModeColor();
        }

        updateStatusBar(statusBarBackgroundColor);
    }

    private void updateRootView(int bgColor) {
        Window window = cordova.getActivity().getWindow();

        // Set the root view's background color. Works on SDK 36+
        View root = cordova.getActivity().findViewById(android.R.id.content);
        if (root != null) root.setBackgroundColor(bgColor);

        // Automatically set the font and icon color of the system bars based on background color.
        boolean isBackgroundColorLight = isColorLight(bgColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                int appearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS;
                if (isBackgroundColorLight) {
                    controller.setSystemBarsAppearance(0, appearance);
                } else {
                    controller.setSystemBarsAppearance(appearance, appearance);
                }
            }
        }
        WindowInsetsControllerCompat controllerCompat = WindowCompat.getInsetsController(window, window.getDecorView());
        controllerCompat.setAppearanceLightNavigationBars(isBackgroundColorLight);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Allow custom navigation bar background color for SDK 26 and greater.
            window.setNavigationBarColor(bgColor);
        } else {
            // Force navigation bar to black for SDK 25 and less.
            window.setNavigationBarColor(Color.BLACK);
        }
    }

    private void updateStatusBar(int bgColor) {
        Window window = cordova.getActivity().getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
                && !preferences.getBoolean("AndroidEdgeToEdge", false)
        ) {
            View statusBar = getStatusBarView(webView);
            if (statusBar != null) {
                statusBar.setBackgroundColor(bgColor);
            }
        }

        // Automatically set the font and icon color of the system bars based on background color.
        boolean isStatusBarBackgroundColorLight = isColorLight(bgColor);
        WindowInsetsControllerCompat controllerCompat = WindowCompat.getInsetsController(window, window.getDecorView());
        controllerCompat.setAppearanceLightStatusBars(isStatusBarBackgroundColorLight);

        // Allow custom background color for StatusBar.
        window.setStatusBarColor(bgColor);
    }

    private static boolean isColorLight(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;
        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance > 0.5;
    }

    private int getPreferenceStatusBarBackgroundColor() {
        String colorString = preferences.getString("StatusBarBackgroundColor", null);

        int parsedColor = parseColorFromString(colorString);
        if (parsedColor != INVALID_COLOR) return parsedColor;

        return getUiModeColor(); // fallback
    }

    private int getPreferenceBackgroundColor() {
        try {
            return preferences.getInteger("BackgroundColor", INVALID_COLOR);
        } catch (NumberFormatException e) {
            LOG.e(PLUGIN_NAME, "Invalid background color argument. Example valid string: '0x00000000'");
            return INVALID_COLOR;
        }
    }

    private FrameLayout getRootLayout(CordovaWebView webView) {
        ViewParent parent = webView.getView().getParent();
        if (parent instanceof FrameLayout) {
            return (FrameLayout) parent;
        }

        return null;
    }

    private View getStatusBarView(CordovaWebView webView) {
        FrameLayout rootView = getRootLayout(webView);
        for (int i = 0; i < (rootView != null ? rootView.getChildCount() : 0); i++) {
            View child = rootView.getChildAt(i);
            Object tag = child.getTag();
            if ("statusBarView".equals(tag)) {
                return child;
            }
        }
        return null;
    }

    private int getUiModeColor() {
        // Hardcoded fallback values matches system ui values (R.color) which were added in SDK 34.
        return isNightMode()
                ? getThemeColor("cdv_background_color_dark", "#121318")
                : getThemeColor("cdv_background_color_light", "#FAF8FF");
    }

    private boolean isNightMode() {
        return (resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private int parseColorFromString(final String colorPref) {
        if (colorPref.isEmpty()) return INVALID_COLOR;

        try {
            return Color.parseColor(colorPref);
        } catch (IllegalArgumentException ignore) {
            LOG.e(PLUGIN_NAME, "Invalid color hex code. Valid format: #RRGGBB or #AARRGGBB");
            return INVALID_COLOR;
        }
    }

    @SuppressLint("DiscouragedApi")
    private int getThemeColor(String colorKey, String fallbackColor) {
        int colorResId = resources.getIdentifier(colorKey, "color", context.getPackageName());
        return colorResId != 0
                ? ContextCompat.getColor(context, colorResId)
                : Color.parseColor(fallbackColor);
    }
}
