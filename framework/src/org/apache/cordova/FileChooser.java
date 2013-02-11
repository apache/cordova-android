package org.apache.cordova;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;

public class FileChooser extends CordovaPlugin {
	private CallbackContext callbackContext;

	/**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	this.callbackContext = callbackContext;

        if (action.equals("chooseFile")) {
        	this.chooseFile(args.getJSONArray(0));
            return true;
        }

        return false;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Choose a file from the file system.
     * @throws JSONException
     */
    public void chooseFile(JSONArray mimeTypes) throws JSONException {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Intent.setType doesn't support handling multiple mime types, so we need to settle on one.
        intent.setType(getIntentType(mimeTypes));

        if (this.cordova != null) {
            this.cordova.startActivityForResult((CordovaPlugin) this, Intent.createChooser(intent,
                    "Get File"), 0);
        }
    }

    /**
     * Called when the the file chooser exits.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        this.callbackContext.success(intent.getData().toString());
    }

    private static String getIntentType(JSONArray mimeTypes) throws JSONException {
    	String intentType = null;
    	String intentTypePrefix = null;

        // Iterate through all the given mime types and determine the lowest common denominator.
    	for (int i = 0; i < mimeTypes.length(); i++) {
        	String mimeType = mimeTypes.getString(i);
        	if (intentType == null) {
        		intentType = mimeType;
        		intentTypePrefix = intentType.substring(0, intentType.indexOf('/'));
        	} else {
        		if (!intentType.equals(mimeType)) {
        			String mimeTypePrefix = mimeType.substring(0, mimeType.indexOf('/'));
        			if (intentTypePrefix.equals(mimeTypePrefix)) {
        				// We've encountered two suffixes with the same prefix, so we allow <prefix>/*.
        				intentType = new StringBuilder(intentTypePrefix).append("/*").toString();
        			} else {
        				// We've encountered two different prefixes, so we allow */*.
        				return "*/*";
        			}
        		}
        	}
        }

        if (intentType == null) {
        	return "*/*";
        }

        return intentType;
    }
}
