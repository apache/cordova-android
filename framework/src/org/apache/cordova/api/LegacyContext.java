package org.apache.cordova.api;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

@Deprecated
public class LegacyContext implements CordovaInterface {
    private static final String LOG_TAG = "Deprecation Notice";
    private CordovaInterface cordova;

    public LegacyContext(CordovaInterface cordova) {
        this.cordova = cordova;
    }

    @Deprecated
    public void cancelLoadUrl() {
        Log.i(LOG_TAG, "Replace ctx.cancelLoadUrl() with cordova.cancelLoadUrl()");
        this.cordova.cancelLoadUrl();
    }

    @Deprecated
    public Activity getActivity() {
        Log.i(LOG_TAG, "Replace ctx.getActivity() with cordova.getActivity()");
        return this.cordova.getActivity();
    }

    @Deprecated
    public Context getContext() {
        Log.i(LOG_TAG, "Replace ctx.getContext() with cordova.getContext()");
        return this.cordova.getContext();
    }

    @Deprecated
    public Object onMessage(String arg0, Object arg1) {
        Log.i(LOG_TAG, "Replace ctx.onMessage() with cordova.onMessage()");
        return this.cordova.onMessage(arg0, arg1);
    }

    @Deprecated
    public void setActivityResultCallback(IPlugin arg0) {
        Log.i(LOG_TAG, "Replace ctx.setActivityResultCallback() with cordova.setActivityResultCallback()");
        this.cordova.setActivityResultCallback(arg0);
    }

    @Deprecated
    public void startActivityForResult(IPlugin arg0, Intent arg1, int arg2) {
        Log.i(LOG_TAG, "Replace ctx.startActivityForResult() with cordova.startActivityForResult()");
        this.cordova.startActivityForResult(arg0, arg1, arg2);
    }

    @Deprecated
    public void startActivity(Intent intent) {
        Log.i(LOG_TAG, "Replace ctx.startActivity() with cordova.getActivity().startActivity()");
        this.cordova.getActivity().startActivity(intent);
    }

    @Deprecated
    public Object getSystemService(String name) {
        Log.i(LOG_TAG, "Replace ctx.getSystemService() with cordova.getActivity().getSystemService()");
        return this.cordova.getActivity().getSystemService(name);
    }

    @Deprecated
    public AssetManager getAssets() {
        Log.i(LOG_TAG, "Replace ctx.getAssets() with cordova.getActivity().getAssets()");
        return this.cordova.getActivity().getAssets();
    }

    @Deprecated
    public void runOnUiThread(Runnable runnable) {
        Log.i(LOG_TAG, "Replace ctx.runOnUiThread() with cordova.getActivity().runOnUiThread()");
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    @Deprecated
    public Context getApplicationContext() {
        Log.i(LOG_TAG, "Replace ctx.getApplicationContext() with cordova.getActivity().getApplicationContext()");
        return this.cordova.getActivity().getApplicationContext();
    }

    @Deprecated
    public PackageManager getPackageManager() {
        Log.i(LOG_TAG, "Replace ctx.getPackageManager() with cordova.getActivity().getPackageManager()");
        return this.cordova.getActivity().getPackageManager();
    }

    @Deprecated
    public SharedPreferences getSharedPreferences(String name, int mode) {
        Log.i(LOG_TAG, "Replace ctx.getSharedPreferences() with cordova.getActivity().getSharedPreferences()");
        return this.cordova.getActivity().getSharedPreferences(name, mode);
    }

    @Deprecated
    public void unregisterReceiver(BroadcastReceiver receiver) {
        Log.i(LOG_TAG, "Replace ctx.unregisterReceiver() with cordova.getActivity().unregisterReceiver()");
        this.cordova.getActivity().unregisterReceiver(receiver);
    }

    @Deprecated
    public Resources getResources() {
        Log.i(LOG_TAG, "Replace ctx.getResources() with cordova.getActivity().getResources()");
        return this.cordova.getActivity().getResources();
    }
}
