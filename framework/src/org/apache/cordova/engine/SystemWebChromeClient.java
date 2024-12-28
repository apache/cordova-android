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
package org.apache.cordova.engine;

import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.PermissionRequest;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.core.content.FileProvider;

import org.apache.cordova.CordovaDialogsHelper;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;

/**
 * This class is the WebChromeClient that implements callbacks for our web view.
 * The kind of callbacks that happen here are on the chrome outside the document,
 * such as onCreateWindow(), onConsoleMessage(), onProgressChanged(), etc. Related
 * to but different than CordovaWebViewClient.
 */
public class SystemWebChromeClient extends WebChromeClient {

    private static final int FILECHOOSER_RESULTCODE = 5173;
    private static final String LOG_TAG = "SystemWebChromeClient";
    private long MAX_QUOTA = 100 * 1024 * 1024;
    protected final SystemWebViewEngine parentEngine;

    // the video progress view
    private View mVideoProgressView;

    private CordovaDialogsHelper dialogsHelper;
    private Context appContext;

    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private View mCustomView;

    public SystemWebChromeClient(SystemWebViewEngine parentEngine) {
        this.parentEngine = parentEngine;
        appContext = parentEngine.webView.getContext();
        dialogsHelper = new CordovaDialogsHelper(appContext);
    }

    /**
     * Tell the client to display a javascript alert dialog.
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        dialogsHelper.showAlert(message, new CordovaDialogsHelper.Result() {
            @Override public void gotResult(boolean success, String value) {
                if (success) {
                    result.confirm();
                } else {
                    result.cancel();
                }
            }
        });
        return true;
    }

    /**
     * Tell the client to display a confirm dialog to the user.
     */
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        dialogsHelper.showConfirm(message, new CordovaDialogsHelper.Result() {
            @Override
            public void gotResult(boolean success, String value) {
                if (success) {
                    result.confirm();
                } else {
                    result.cancel();
                }
            }
        });
        return true;
    }

    /**
     * Tell the client to display a prompt dialog to the user.
     * If the client returns true, WebView will assume that the client will
     * handle the prompt dialog and call the appropriate JsPromptResult method.
     *
     * Since we are hacking prompts for our own purposes, we should not be using them for
     * this purpose, perhaps we should hack console.log to do this instead!
     */
    @Override
    public boolean onJsPrompt(WebView view, String origin, String message, String defaultValue, final JsPromptResult result) {
        // Unlike the @JavascriptInterface bridge, this method is always called on the UI thread.
        String handledRet = parentEngine.bridge.promptOnJsPrompt(origin, message, defaultValue);
        if (handledRet != null) {
            result.confirm(handledRet);
        } else {
            dialogsHelper.showPrompt(message, defaultValue, new CordovaDialogsHelper.Result() {
                @Override
                public void gotResult(boolean success, String value) {
                    if (success) {
                        result.confirm(value);
                    } else {
                        result.cancel();
                    }
                }
            });
        }
        return true;
    }

    /**
     * Handle database quota exceeded notification.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
            long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
    {
        LOG.d(LOG_TAG, "onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", estimatedSize, currentQuota, totalUsedQuota);
        quotaUpdater.updateQuota(MAX_QUOTA);
    }

    @Override
    /**
     * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin.
     *
     * This also checks for the Geolocation Plugin and requests permission from the application  to use Geolocation.
     *
     * @param origin
     * @param callback
     */
    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
        //Get the plugin, it should be loaded
        CordovaPlugin geolocation = parentEngine.pluginManager.getPlugin("Geolocation");
        if(geolocation != null && !geolocation.hasPermisssion())
        {
            geolocation.requestPermissions(0);
        }
    }

    // API level 7 is required for this, see if we could lower this using something else
    @Override
    @SuppressWarnings("deprecation")
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        parentEngine.getCordovaWebView().showCustomView(view, callback);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onHideCustomView() {
        parentEngine.getCordovaWebView().hideCustomView();
    }

    @Override
    /**
     * Ask the host application for a custom progress view to show while
     * a <video> is loading.
     *
     * @return View The progress view.
     */
    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            // Create a new Loading view programmatically.

            // create the linear layout
            LinearLayout layout = new LinearLayout(parentEngine.getView().getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.setLayoutParams(layoutParams);
            // the proress bar
            ProgressBar bar = new ProgressBar(parentEngine.getView().getContext());
            LinearLayout.LayoutParams barLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            barLayoutParams.gravity = Gravity.CENTER;
            bar.setLayoutParams(barLayoutParams);
            layout.addView(bar);

            mVideoProgressView = layout;
        }
        return mVideoProgressView;
    }

    @Override
    public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> filePathsCallback,
            final WebChromeClient.FileChooserParams fileChooserParams) {
        Intent fileIntent = fileChooserParams.createIntent();

        // Check if multiple-select is specified
        Boolean selectMultiple = false;
        if (fileChooserParams.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
            selectMultiple = true;
        }
        fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);

        // Uses Intent.EXTRA_MIME_TYPES to pass multiple mime types.
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (acceptTypes.length > 1) {
            fileIntent.setType("*/*"); // Accept all, filter mime types by Intent.EXTRA_MIME_TYPES.
            fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes);
        }

        // Image from camera intent
        Uri tempUri = null;
        Intent captureIntent = null;
        if (fileChooserParams.isCaptureEnabled()) {
            captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Context context = parentEngine.getView().getContext();
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                    && captureIntent.resolveActivity(context.getPackageManager()) != null) {
                try {
                    File tempFile = createTempFile(context);
                    LOG.d(LOG_TAG, "Temporary photo capture file: " + tempFile);
                    tempUri = createUriForFile(context, tempFile);
                    LOG.d(LOG_TAG, "Temporary photo capture URI: " + tempUri);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                } catch (IOException e) {
                    LOG.e(LOG_TAG, "Unable to create temporary file for photo capture", e);
                    captureIntent = null;
                }
            } else {
                LOG.w(LOG_TAG, "Device does not support photo capture");
                captureIntent = null;
            }
        }
        final Uri captureUri = tempUri;

        // Chooser intent
        Intent chooserIntent = Intent.createChooser(fileIntent, null);
        if (captureIntent != null) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { captureIntent });
        }

        try {
            LOG.i(LOG_TAG, "Starting intent for file chooser");
            parentEngine.cordova.startActivityForResult(new CordovaPlugin() {
                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                    // Handle result
                    Uri[] result = null;
                    if (resultCode == Activity.RESULT_OK) {
                        List<Uri> uris = new ArrayList<Uri>();

                        if (intent != null && intent.getData() != null) { // single file
                            LOG.v(LOG_TAG, "Adding file (single): " + intent.getData());
                            uris.add(intent.getData());
                        } else if (captureUri != null) { // camera
                            LOG.v(LOG_TAG, "Adding camera capture: " + captureUri);
                            uris.add(captureUri);
                        } else if (intent != null && intent.getClipData() != null) { // multiple files
                            ClipData clipData = intent.getClipData();
                            int count = clipData.getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri uri = clipData.getItemAt(i).getUri();
                                LOG.v(LOG_TAG, "Adding file (multiple): " + uri);
                                if (uri != null) {
                                    uris.add(uri);
                                }
                            }
                        }

                        if (!uris.isEmpty()) {
                            LOG.d(LOG_TAG, "Receive file chooser URL: " + uris.toString());
                            result = uris.toArray(new Uri[uris.size()]);
                        }
                    }
                    filePathsCallback.onReceiveValue(result);
                }
            }, chooserIntent, FILECHOOSER_RESULTCODE);
        } catch (ActivityNotFoundException e) {
            LOG.w(LOG_TAG, "No activity found to handle file chooser intent.", e);
            filePathsCallback.onReceiveValue(null);
        }
        return true;
    }

    private File createTempFile(Context context) throws IOException {
        // Create an image file name
        File tempFile = File.createTempFile("temp", ".jpg", context.getCacheDir());
        return tempFile;
    }

    private Uri createUriForFile(Context context, File tempFile) throws IOException {
        String appId = context.getPackageName();
        Uri uri = FileProvider.getUriForFile(context, appId + ".cdv.core.file.provider", tempFile);
        return uri;
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        LOG.d(LOG_TAG, "onPermissionRequest: " + Arrays.toString(request.getResources()));
        request.grant(request.getResources());
    }

    public void destroyLastDialog(){
        dialogsHelper.destroyLastDialog();
    }
}
