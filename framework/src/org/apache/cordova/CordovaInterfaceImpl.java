package org.apache.cordova;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of CordovaInterface.
 */
public class CordovaInterfaceImpl implements CordovaInterface {
    private static final String TAG = "CordovaInterfaceImpl";
    protected Activity activity;
    protected ExecutorService threadPool;
    protected PluginManager pluginManager;

    protected CordovaPlugin activityResultCallback;
    protected String initCallbackClass;
    protected int activityResultRequestCode;

    public CordovaInterfaceImpl(Activity activity) {
        this(activity, Executors.newCachedThreadPool());
    }

    public CordovaInterfaceImpl(Activity activity, ExecutorService threadPool) {
        this.activity = activity;
        this.threadPool = threadPool;
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        setActivityResultCallback(command);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) { // E.g.: ActivityNotFoundException
            activityResultCallback = null;
            throw e;
        }
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        // Cancel any previously pending activity.
        if (activityResultCallback != null) {
            activityResultCallback.onActivityResult(activityResultRequestCode, Activity.RESULT_CANCELED, null);
        }
        activityResultCallback = plugin;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            activity.finish();
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Routes the result to the awaiting plugin. Returns false if no plugin was waiting.
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        CordovaPlugin callback = activityResultCallback;
        if(callback == null && initCallbackClass != null) {
            // The application was restarted, but had defined an initial callback
            // before being shut down.
            callback = pluginManager.getPlugin(initCallbackClass);
        }
        initCallbackClass = null;
        activityResultCallback = null;

        if (callback != null) {
            Log.d(TAG, "Sending activity result to plugin");
            callback.onActivityResult(requestCode, resultCode, intent);
            return true;
        }
        Log.w(TAG, "Got an activity result, but no plugin was registered to receive it.");
        return false;
    }

    /**
     * Call this from your startActivityForResult() overload. This is required to catch the case
     * where plugins use Activity.startActivityForResult() + CordovaInterface.setActivityResultCallback()
     * rather than CordovaInterface.startActivityForResult().
     */
    public void setActivityResultRequestCode(int requestCode) {
        activityResultRequestCode = requestCode;
    }

    /**
     * Saves parameters for startActivityForResult().
     */
    public void onSaveInstanceState(Bundle outState) {
        if (activityResultCallback != null) {
            String cClass = activityResultCallback.getClass().getName();
            outState.putString("callbackClass", cClass);
        }
    }

    /**
     * Call this from onCreate() so that any saved startActivityForResult parameters will be restored.
     */
    public void restoreInstanceState(Bundle savedInstanceState) {
        initCallbackClass = savedInstanceState.getString("callbackClass");
    }
}
