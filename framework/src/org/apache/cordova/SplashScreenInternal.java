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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;

// This file is a copy of SplashScreen.java from cordova-plugin-splashscreen, and is required only
// for pre-4.0 Cordova as a transition path to it being extracted into the plugin.
public class SplashScreenInternal extends CordovaPlugin {
    private static final String LOG_TAG = "SplashScreenInternal";
    private static Dialog splashDialog;
    private static ProgressDialog spinnerDialog;
    private static boolean firstShow = true;

    @Override
    protected void pluginInitialize() {
        if (!firstShow) {
            return;
        }
        // Make WebView invisible while loading URL
        webView.setVisibility(View.INVISIBLE);
        int drawableId = preferences.getInteger("SplashDrawableId", 0);
        if (drawableId == 0) {
            String splashResource = preferences.getString("SplashScreen", null);
            if (splashResource != null) {
                drawableId = cordova.getActivity().getResources().getIdentifier(splashResource, "drawable", cordova.getActivity().getClass().getPackage().getName());
                if (drawableId == 0) {
                    drawableId = cordova.getActivity().getResources().getIdentifier(splashResource, "drawable", cordova.getActivity().getPackageName());
                }
                preferences.set("SplashDrawableId", drawableId);
            }
        }

        firstShow = false;
        loadSpinner();
        showSplashScreen(true);
    }

    @Override
    public void onPause(boolean multitasking) {
        // hide the splash screen to avoid leaking a window
        this.removeSplashScreen();
    }

    @Override
    public void onDestroy() {
        // hide the splash screen to avoid leaking a window
        this.removeSplashScreen();
        firstShow = true;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("hide")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    webView.postMessage("splashscreen", "hide");
                }
            });
        } else if (action.equals("show")) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    webView.postMessage("splashscreen", "show");
                }
            });
        } else if (action.equals("spinnerStart")) {
            final String title = args.getString(0);
            final String message = args.getString(1);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    spinnerStart(title, message);
                }
            });
        } else {
            return false;
        }

        callbackContext.success();
        return true;
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("splashscreen".equals(id)) {
            if ("hide".equals(data.toString())) {
                this.removeSplashScreen();
            } else {
                this.showSplashScreen(false);
            }
        } else if ("spinner".equals(id)) {
            if ("stop".equals(data.toString())) {
                this.spinnerStop();
                webView.setVisibility(View.VISIBLE);
            }
        } else if ("onReceivedError".equals(id)) {
            spinnerStop();
        }
        return null;
    }

    private void removeSplashScreen() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (splashDialog != null && splashDialog.isShowing()) {
                    splashDialog.dismiss();
                    splashDialog = null;
                }
            }
        });
    }

    /**
     * Shows the splash screen over the full Activity
     */
    @SuppressWarnings("deprecation")
    private void showSplashScreen(final boolean hideAfterDelay) {
        final int splashscreenTime = preferences.getInteger("SplashScreenDelay", 3000);
        final int drawableId = preferences.getInteger("SplashDrawableId", 0);

        // If the splash dialog is showing don't try to show it again
        if (this.splashDialog != null && splashDialog.isShowing()) {
            return;
        }
        if (drawableId == 0 || (splashscreenTime <= 0 && hideAfterDelay)) {
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // Get reference to display
                Display display = cordova.getActivity().getWindowManager().getDefaultDisplay();
                Context context = webView.getContext();

                // Create the layout for the dialog
                LinearLayout root = new LinearLayout(context);
                root.setMinimumHeight(display.getHeight());
                root.setMinimumWidth(display.getWidth());
                root.setOrientation(LinearLayout.VERTICAL);

                // TODO: Use the background color of the webview's parent instead of using the
                // preference.
                root.setBackgroundColor(preferences.getInteger("backgroundColor", Color.BLACK));
                root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
                root.setBackgroundResource(drawableId);

                // Create and show the dialog
                splashDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
                // check to see if the splash screen should be full screen
                if ((cordova.getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                    splashDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                splashDialog.setContentView(root);
                splashDialog.setCancelable(false);
                splashDialog.show();

                // Set Runnable to remove splash screen just in case
                if (hideAfterDelay) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            removeSplashScreen();
                        }
                    }, splashscreenTime);
                }
            }
        });
    }

    /*
     * Load the spinner
     */
    private void loadSpinner() {
        // If loadingDialog property, then show the App loading dialog for first page of app
        String loading = null;
        if (webView.canGoBack()) {
            loading = preferences.getString("LoadingDialog", null);
        }
        else {
            loading = preferences.getString("LoadingPageDialog", null);
        }
        if (loading != null) {
            String title = "";
            String message = "Loading Application...";

            if (loading.length() > 0) {
                int comma = loading.indexOf(',');
                if (comma > 0) {
                    title = loading.substring(0, comma);
                    message = loading.substring(comma + 1);
                }
                else {
                    title = "";
                    message = loading;
                }
            }
            spinnerStart(title, message);
        }
    }

    private void spinnerStart(final String title, final String message) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                spinnerStop();
                spinnerDialog = ProgressDialog.show(webView.getContext(), title, message, true, true,
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                spinnerDialog = null;
                            }
                        });
            }
        });
    }

    private void spinnerStop() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (spinnerDialog != null && spinnerDialog.isShowing()) {
                    spinnerDialog.dismiss();
                    spinnerDialog = null;
                }
            }
        });
    }
}
