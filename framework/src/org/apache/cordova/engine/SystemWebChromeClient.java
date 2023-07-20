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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ConsoleMessage;
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
import android.provider.MediaStore;
import android.content.pm.ResolveInfo;

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

    private Uri outputFileUri;
    @Override
    public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> filePathsCallback, final FileChooserParams fileChooserParams) {
        // Check if multiple-select is specified
        Boolean selectMultiple = false;
        if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
            selectMultiple = true;
        }

       
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
      final String imageFileName = "JPEG_" + timeStamp + "_.jpg";
      File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), imageFileName);
      
      final String path = file.getAbsolutePath();
      
      outputFileUri = Uri.fromFile(file);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = appContext.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, selectMultiple);
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (acceptTypes.length > 1) {
            galleryIntent.setType("*/*"); // Accept all, filter mime types by Intent.EXTRA_MIME_TYPES.
            galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes);
        }

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        try {
            parentEngine.cordova.startActivityForResult(new CordovaPlugin() {
                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                    Uri[] result = null;
                    if (data != null) {
                        LOG.d(LOG_TAG, "Debug: ");
                        // LOG.d(LOG_TAG, "Data action: " + data.getAction());
                        LOG.d(LOG_TAG, "Data uri: " + outputFileUri);
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            for (String key : bundle.keySet()) {
                                LOG.d(LOG_TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                            }
                        }


                        if (resultCode == Activity.RESULT_OK) {

                            final boolean isCamera;
                            if (data == null) {
                                isCamera = true;
                            } else {
                                final String action = data.getAction();
                                if (action == null) {
                                    isCamera = false;
                                } else {
                                    isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE) || action.equals("inline-data");
                                }
                            }
                            result = new Uri[1];
                            Uri selectedImageUri;
                            if (isCamera) {
                                
                                 Bitmap bmp = (Bitmap) bundle.get("data");
                                 try (FileOutputStream out = new FileOutputStream(path)) {
                                     bmp.compress(Bitmap.CompressFormat.JPEG, 80, out); // bmp is your Bitmap instance
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                                selectedImageUri = outputFileUri;
                                result[0] = selectedImageUri;
                            } else {
                                selectedImageUri = data == null ? null : data.getData();
                                if (data.getClipData() != null) {
                                            // handle multiple-selected files
                                            final int numSelectedFiles = data.getClipData().getItemCount();
                                            result = new Uri[numSelectedFiles];
                                            for (int i = 0; i < numSelectedFiles; i++) {
                                                result[i] = data.getClipData().getItemAt(i).getUri();
                                                LOG.d(LOG_TAG, "Receive file chooser URL: " + result[i]);
                                            }
                                        }
                                        else if (data.getData() != null) {
                                            // handle single-selected file
                                            result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                                            LOG.d(LOG_TAG, "Receive file chooser URL: " + result);
                                        }
                            }
                            LOG.d(LOG_TAG, "IsCamera: " + isCamera);
                            if (selectedImageUri == null) {
                                selectedImageUri = Uri.EMPTY;
                            }
                            LOG.d(LOG_TAG, "Receive file chooser URL: " + result);
                        }
                        
                    }
                    filePathsCallback.onReceiveValue(result);
                }
            }, chooserIntent, FILECHOOSER_RESULTCODE);
        } catch (ActivityNotFoundException e) {
            LOG.w("No activity found to handle file chooser intent.", e);
            filePathsCallback.onReceiveValue(null);
        }
        return true;
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
