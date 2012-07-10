package org.apache.cordova.api;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class LegacyContext implements CordovaInterface {
    private CordovaInterface cordova;

    public LegacyContext(CordovaInterface cordova) {
        this.cordova = cordova;
    }

    public void cancelLoadUrl() {
        this.cordova.cancelLoadUrl();
    }

    public Activity getActivity() {
        return this.cordova.getActivity();
    }

    public Context getContext() {
        return this.cordova.getContext();
    }

    public Object onMessage(String arg0, Object arg1) {
        return this.cordova.onMessage(arg0, arg1);
    }

    public void setActivityResultCallback(IPlugin arg0) {
        this.cordova.setActivityResultCallback(arg0);
    }

    public void startActivityForResult(IPlugin arg0, Intent arg1, int arg2) {
        this.cordova.startActivityForResult(arg0, arg1, arg2);
    }

    public void startActivity(Intent intent) {
        this.cordova.getActivity().startActivity(intent);
    }

    public Object getSystemService(String name) {
        return this.cordova.getActivity().getSystemService(name);
    }

    public AssetManager getAssets() {
        return this.cordova.getActivity().getAssets();
    }

    public void runOnUiThread(Runnable runnable) {
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    public Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    public PackageManager getPackageManager() {
        return this.cordova.getActivity().getPackageManager();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return this.cordova.getActivity().getSharedPreferences(name, mode);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        this.cordova.getActivity().unregisterReceiver(receiver);
    }

    public Resources getResources() {
        return this.cordova.getActivity().getResources();
    }
}
