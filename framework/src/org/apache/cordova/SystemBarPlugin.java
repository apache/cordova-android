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

    private boolean canEdgeToEdge = false;

    @Override
    protected void pluginInitialize() {
        context = cordova.getContext();
        resources = context.getResources();
        canEdgeToEdge = preferences.getBoolean("AndroidEdgeToEdge", false)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM;
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
        if(canEdgeToEdge) {
            return false;
        }

        if ("setStatusBarVisible".equals(action)) {
            boolean visible = args.getBoolean(0);
            cordova.getActivity().runOnUiThread(() -> setStatusBarVisible(visible));
        } else if ("setStatusBarBackgroundColor".equals(action)) {
            cordova.getActivity().runOnUiThread(() -> setStatusBarBackgroundColor(args));
        } else {
            return false;
        }

        callbackContext.success();
        return true;
    }

    /**
     * Allow the app to override the status bar visibility from JS API.
     * If for some reason the statusBarView could not be discovered, it will silently ignore
     * the change request
     *
     * @param visible should the status bar be visible?
     */
    private void setStatusBarVisible(final boolean visible) {
        View statusBar = getStatusBarView(webView);
        if (statusBar != null) {
            statusBar.setVisibility(visible ? View.VISIBLE : View.GONE);

            FrameLayout rootLayout = getRootLayout(webView);
            if (rootLayout != null) {
                ViewCompat.requestApplyInsets(rootLayout);
            }
        }
    }

    /**
     * Allow the app to override the status bar background color from JS API.
     * If the supplied ARGB is invalid or fails to parse, it will silently ignore
     * the change request.
     *
     * @param argbVals {A, R, G, B}
     */
    private void setStatusBarBackgroundColor(JSONArray argbVals) {
        try {
            int a = argbVals.getInt(0);
            int r = argbVals.getInt(1);
            int g = argbVals.getInt(2);
            int b = argbVals.getInt(3);
            String hexColor = String.format("#%02X%02X%02X%02X", a, r, g, b);

            int parsedColor = parseColorFromString(hexColor);
            if (parsedColor == INVALID_COLOR) return;

            overrideStatusBarBackgroundColor = parsedColor;
            updateStatusBar(overrideStatusBarBackgroundColor);
        } catch (JSONException e) {
            // Silently skip
        }
    }

    /**
     * Attempt to update all system bars (status, navigation and gesture bars) in various points
     * of the apps life cycle.
     * For example:
     *  1. Device configurations between (E.g. between dark and light mode)
     *  2. User resumes the app
     *  3. App transitions from SplashScreen Theme to App's Theme
     */
    private void updateSystemBars() {
        // Update Root View Background Color
        int rootViewBackgroundColor = getPreferenceBackgroundColor();
        if (rootViewBackgroundColor == INVALID_COLOR) {
            rootViewBackgroundColor = canEdgeToEdge ? Color.TRANSPARENT : getUiModeColor();
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
            statusBarBackgroundColor = canEdgeToEdge ? Color.TRANSPARENT : getUiModeColor();
        }

        updateStatusBar(statusBarBackgroundColor);
    }

    /**
     * Updates the root layout's background color with the supplied color int.
     * It will also determine if the background color is light or dark to properly adjust the
     * appearance of the navigation/gesture bar's icons so it will not clash with the background.
     * <p>
     * System bars (navigation & gesture) on SDK 25 or lower is forced to black as the appearance
     * of the fonts can not be updated.
     * System bars (navigation & gesture) on SDK 26 or greater allows custom background color.
     * <p/>
     *
     * @param bgColor Background color
     */
    @SuppressWarnings("deprecation")
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
            window.setNavigationBarColor(bgColor);
        } else {
            window.setNavigationBarColor(Color.BLACK);
        }
    }

    /**
     * Updates the statusBarView background color with the supplied color int.
     * It will also determine if the background color is light or dark to properly adjust the
     * appearance of the status bar so the font will not clash with the background.
     *
     * @param bgColor Background color
     */
    private void updateStatusBar(int bgColor) {
        Window window = cordova.getActivity().getWindow();

        View statusBar = getStatusBarView(webView);
        if (statusBar != null) {
            statusBar.setBackgroundColor(bgColor);
        }

        // Automatically set the font and icon color of the system bars based on background color.
        boolean isStatusBarBackgroundColorLight = isColorLight(bgColor);
        WindowInsetsControllerCompat controllerCompat = WindowCompat.getInsetsController(window, window.getDecorView());
        controllerCompat.setAppearanceLightStatusBars(isStatusBarBackgroundColorLight);
    }

    /**
     * Determines if the supplied color's appearance is light.
     *
     * @param color color
     * @return boolean value true is returned when the color is light.
     */
    private static boolean isColorLight(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;
        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance > 0.5;
    }

    /**
     * Returns the StatusBarBackgroundColor preference value.
     * If the value is missing or fails to parse, it will attempt to try to guess the background
     * color by extracting from the apps R.color.cdv_background_color or determine from the uiModes.
     * If all fails, the color normally used in light mode is returned.
     *
     * @return int
     */
    private int getPreferenceStatusBarBackgroundColor() {
        String colorString = preferences.getString("StatusBarBackgroundColor", null);

        int parsedColor = parseColorFromString(colorString);
        if (parsedColor != INVALID_COLOR) return parsedColor;

        return getUiModeColor(); // fallback
    }

    /**
     * Returns the BackgroundColor preference value.
     * If missing or fails to decode, it will return INVALID_COLOR (-1).
     *
     * @return int
     */
    private int getPreferenceBackgroundColor() {
        try {
            return preferences.getInteger("BackgroundColor", INVALID_COLOR);
        } catch (NumberFormatException e) {
            LOG.e(PLUGIN_NAME, "Invalid background color argument. Example valid string: '0x00000000'");
            return INVALID_COLOR;
        }
    }

    /**
     * Tries to find and return the rootLayout.
     *
     * @param webView CordovaWebView
     * @return FrameLayout|null
     */
    private FrameLayout getRootLayout(CordovaWebView webView) {
        ViewParent parent = webView.getView().getParent();
        if (parent instanceof FrameLayout) {
            return (FrameLayout) parent;
        }

        return null;
    }

    /**
     * Tries to find and return the statusBarView.
     *
     * @param webView CordovaWebView
     * @return View|null
     */
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

    /**
     * Determines the background color for status bar & root layer.
     * The color will come from the app's R.color.cdv_background_color.
     * If for some reason the resource is missing, it will try to fallback on the uiMode.
     * <p>
     * The uiMode as follows.
     *   If night mode: "#121318" (android.R.color.system_background_dark)
     *   If day mode: "#FAF8FF" (android.R.color.system_background_light)
     * If all fails, light mode will be returned.
     * </p>
     * The hex values are supplied instead of "android.R.color" for backwards compatibility.
     *
     * @return int color
     */
    @SuppressLint("DiscouragedApi")
    private int getUiModeColor() {
        boolean isNightMode = (resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        String fallbackColor = isNightMode ? "#121318" : "#FAF8FF";
        int colorResId = resources.getIdentifier("cdv_background_color", "color", context.getPackageName());
        return colorResId != 0
                ? ContextCompat.getColor(context, colorResId)
                : Color.parseColor(fallbackColor);
    }

    /**
     * Parse color string that would be provided by app developers.
     * If the color string is empty or unable to parse, it will return INVALID_COLOR (-1).
     *
     * @param colorPref hex string value, #AARRGGBB or #RRGGBB
     * @return int
     */
    private int parseColorFromString(final String colorPref) {
        if (colorPref.isEmpty()) return INVALID_COLOR;

        try {
            return Color.parseColor(colorPref);
        } catch (IllegalArgumentException ignore) {
            LOG.e(PLUGIN_NAME, "Invalid color hex code. Valid format: #RRGGBB or #AARRGGBB");
            return INVALID_COLOR;
        }
    }
}
