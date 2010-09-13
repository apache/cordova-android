package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;
import android.webkit.WebView;

public class Storage implements Plugin {
	
	private static final String LOG_TAG = "SQLite Storage:";

    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object
	
	SQLiteDatabase myDb;
	String path;
	String txid = "";
	
	/**
	 * Constructor.
	 */
	public Storage() {
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
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("setStorage")) {
				this.setStorage(args.getString(0));
			}
			else if (action.equals("openDatabase")) {
				this.openDatabase(args.getString(0), args.getString(1), args.getString(2), args.getLong(3));
			}
			else if (action.equals("executeSql")) {
				JSONArray a = args.getJSONArray(1);
				int len = a.length();
				String[] s = new String[len];
				for (int i=0; i<len; i++) {
					s[i] = a.getString(i);
				}
				this.executeSql(args.getString(0), s, args.getString(2));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
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

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	public void setStorage(String appPackage) {
		path = "/data/data/" + appPackage + "/databases/";
	}
	
	public void openDatabase(String db, String version, String display_name, long size)	{
		if (path != null) {
			path += db + ".db";
			myDb = SQLiteDatabase.openOrCreateDatabase(path, null);
		}
	}
	
	public void executeSql(String query, String[] params, String tx_id) {
		try {
			txid = tx_id;
			Cursor myCursor = myDb.rawQuery(query, params);
			processResults(myCursor);
		} catch (SQLiteException ex) {
			Log.d(LOG_TAG, ex.getMessage());
			txid = "";
			this.ctx.sendJavascript("droiddb.fail(" + ex.getMessage() + "," + txid + ");");
		}
	}
	
	public void processResults(Cursor cur) {
		String key = "";
		String value = "";
		String resultString = "";
		if (cur.moveToFirst()) {
			int colCount = cur.getColumnCount();
			do {
				resultString = "{";
				for (int i = 0; i < colCount; ++i) {
					key = cur.getColumnName(i);
					value = cur.getString(i);
					resultString += " \"" + key + "\" : \"" + value + "\"";
					if (i != (colCount - 1)) {
						resultString += ",";
					}
				}
				resultString += "}";
				this.ctx.sendJavascript("droiddb.addResult('" + resultString + "', " + txid + ");");
			 } while (cur.moveToNext());
			 this.ctx.sendJavascript("droiddb.completeQuery(" + txid + ");");
			 txid = "";
			 myDb.close();
		 }
	}
		
}