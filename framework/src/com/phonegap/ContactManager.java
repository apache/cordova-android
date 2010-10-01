package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.util.Log;
import android.webkit.WebView;
import android.content.Intent;

public class ContactManager implements Plugin {
	
    private static ContactAccessor contactAccessor;
    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object

	private static final String LOG_TAG = "Contact Query";

	/**
	 * Constructor.
	 */
	public ContactManager()	{
	}
	
	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(DroidGap ctx) {
		this.ctx = ctx;
	}

	/**
	 * Sets the main View of the application, this is the WebView within which 
	 * a PhoneGap app runs.
	 * 
	 * @param webView The PhoneGap WebView
	 */
	public void setView(WebView webView) {
		this.webView = webView;
	}

	/**
	 * Executes the request and returns CommandResult.
	 * 
	 * @param action The command to execute.
	 * @param args JSONArry of arguments for the command.
	 * @return A CommandResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args) {
		if (contactAccessor == null) {
			contactAccessor = ContactAccessor.getInstance(webView, ctx);
		}
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("search")) {
				contactAccessor.search(args.getJSONArray(0), args.getJSONObject(1));
			}
			else if (action.equals("create")) {
				// TODO Coming soon!
			}
			else if (action.equals("save")) {
				// TODO Coming soon!			
			}
			else if (action.equals("remove")) {
				if (contactAccessor.remove(args.getString(0))) {
					return new PluginResult(status, result);					
				}
				else {
					JSONObject r = new JSONObject();
					r.put("code", 2);
					return new PluginResult(PluginResult.Status.ERROR, r);
				}
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		return false;
	}

	/**
     * Called when the system is about to start resuming a previous activity. 
     */
    public void onPause() {
    }

    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {   	
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }
}
