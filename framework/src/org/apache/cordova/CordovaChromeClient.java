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
import org.apache.cordova.api.LOG;
import org.json.JSONArray;
import org.json.JSONException;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.GeolocationPermissions.Callback;
import android.widget.EditText;

/**
 * This class is the WebChromeClient that implements callbacks for our web view.
 */
public class CordovaChromeClient extends WebChromeClient {
    

    private String TAG = "CordovaLog";
    private long MAX_QUOTA = 100 * 1024 * 1024;
    private DroidGap ctx;
    
    /**
     * Constructor.
     * 
     * @param ctx
     */
    public CordovaChromeClient(Context ctx) {
        this.ctx = (DroidGap) ctx;
    }

    /**
     * Tell the client to display a javascript alert dialog.
     * 
     * @param view
     * @param url
     * @param message
     * @param result
     */
    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
        dlg.setMessage(message);
        dlg.setTitle("Alert");
        //Don't let alerts break the back button
        dlg.setCancelable(true);
        dlg.setPositiveButton(android.R.string.ok,
            new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            });
        dlg.setOnCancelListener(
           new DialogInterface.OnCancelListener() {
               public void onCancel(DialogInterface dialog) {
                   result.confirm();
                   }
               });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //DO NOTHING
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK)
                {
                    result.confirm();
                    return false;
                }
                else
                    return true;
                }
            });
        dlg.create();
        dlg.show();
        return true;
    }       

    /**
     * Tell the client to display a confirm dialog to the user.
     * 
     * @param view
     * @param url
     * @param message
     * @param result
     */
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
        dlg.setMessage(message);
        dlg.setTitle("Confirm");
        dlg.setCancelable(true);
        dlg.setPositiveButton(android.R.string.ok, 
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            });
        dlg.setNegativeButton(android.R.string.cancel, 
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            });
        dlg.setOnCancelListener(
            new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    result.cancel();
                    }
                });
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            //DO NOTHING
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK)
                {
                    result.cancel();
                    return false;
                }
                else
                    return true;
                }
            });
        dlg.create();
        dlg.show();
        return true;
    }

    /**
     * Tell the client to display a prompt dialog to the user. 
     * If the client returns true, WebView will assume that the client will 
     * handle the prompt dialog and call the appropriate JsPromptResult method.
     * 
     * Since we are hacking prompts for our own purposes, we should not be using them for 
     * this purpose, perhaps we should hack console.log to do this instead!
     * 
     * @param view
     * @param url
     * @param message
     * @param defaultValue
     * @param result
     */
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        
        // Security check to make sure any requests are coming from the page initially
        // loaded in webview and not another loaded in an iframe.
        boolean reqOk = false;
        if (url.startsWith("file://") || url.indexOf(this.ctx.baseUrl) == 0 || ctx.isUrlWhiteListed(url)) {
            reqOk = true;
        }
        
        // Calling PluginManager.exec() to call a native service using 
        // prompt(this.stringify(args), "gap:"+this.stringify([service, action, callbackId, true]));
        if (reqOk && defaultValue != null && defaultValue.length() > 3 && defaultValue.substring(0, 4).equals("gap:")) {
            JSONArray array;
            try {
                array = new JSONArray(defaultValue.substring(4));
                String service = array.getString(0);
                String action = array.getString(1);
                String callbackId = array.getString(2);
                boolean async = array.getBoolean(3);
                String r = ctx.pluginManager.exec(service, action, callbackId, message, async);
                result.confirm(r);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        // Polling for JavaScript messages 
        else if (reqOk && defaultValue != null && defaultValue.equals("gap_poll:")) {
            String r = ctx.callbackServer.getJavascript();
            result.confirm(r);
        }
        
        // Calling into CallbackServer
        else if (reqOk && defaultValue != null && defaultValue.equals("gap_callbackServer:")) {
            String r = "";
            if (message.equals("usePolling")) {
                r = ""+ ctx.callbackServer.usePolling();
            }
            else if (message.equals("restartServer")) {
                ctx.callbackServer.restartServer();
            }
            else if (message.equals("getPort")) {
                r = Integer.toString(ctx.callbackServer.getPort());
            }
            else if (message.equals("getToken")) {
                r = ctx.callbackServer.getToken();
            }
            result.confirm(r);
        }
        
        // Cordova JS has initialized, so show webview
        // (This solves white flash seen when rendering HTML)
        else if (reqOk && defaultValue != null && defaultValue.equals("gap_init:")) {
            if (ctx.splashscreen != 0) {
                ctx.root.setBackgroundResource(0);
            }
            ctx.appView.setVisibility(View.VISIBLE);
            ctx.spinnerStop();
            result.confirm("OK");
        }

        // Show dialog
        else {
            final JsPromptResult res = result;
            AlertDialog.Builder dlg = new AlertDialog.Builder(this.ctx);
            dlg.setMessage(message);
            final EditText input = new EditText(this.ctx);
            if (defaultValue != null) {
                input.setText(defaultValue);
            }
            dlg.setView(input);
            dlg.setCancelable(false);
            dlg.setPositiveButton(android.R.string.ok, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    String usertext = input.getText().toString();
                    res.confirm(usertext);
                }
            });
            dlg.setNegativeButton(android.R.string.cancel, 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    res.cancel();
                }
            });
            dlg.create();
            dlg.show();
        }
        return true;
    }
    
    /**
     * Handle database quota exceeded notification.
     *
     * @param url
     * @param databaseIdentifier
     * @param currentQuota
     * @param estimatedSize
     * @param totalUsedQuota
     * @param quotaUpdater
     */
    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
            long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
    {
        LOG.d(TAG, "DroidGap:  onExceededDatabaseQuota estimatedSize: %d  currentQuota: %d  totalUsedQuota: %d", estimatedSize, currentQuota, totalUsedQuota);

        if( estimatedSize < MAX_QUOTA)
        {
            //increase for 1Mb
            long newQuota = estimatedSize;
            LOG.d(TAG, "calling quotaUpdater.updateQuota newQuota: %d", newQuota);
            quotaUpdater.updateQuota(newQuota);
        }
        else
        {
            // Set the quota to whatever it is and force an error
            // TODO: get docs on how to handle this properly
            quotaUpdater.updateQuota(currentQuota);
        }
    }

    // console.log in api level 7: http://developer.android.com/guide/developing/debug-tasks.html
    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID)
    {       
        LOG.d(TAG, "%s: Line %d : %s", sourceID, lineNumber, message);
        super.onConsoleMessage(message, lineNumber, sourceID);
    }
    
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage)
    {       
        if(consoleMessage.message() != null)
            LOG.d(TAG, consoleMessage.message());
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    /**
     * Instructs the client to show a prompt to ask the user to set the Geolocation permission state for the specified origin. 
     * 
     * @param origin
     * @param callback
     */
    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, false);
    }


}
