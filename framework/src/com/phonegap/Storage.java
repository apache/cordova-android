package com.phonegap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;
import android.webkit.WebView;

public class Storage {
	
	private static final String LOG_TAG = "SQLite Storage:";
	SQLiteDatabase myDb;
	String path;
	String txid = "";
	WebView appView;
	Context mCtx;
	
	Storage(WebView view)
	{    	
		appView = view;
	}
	
	public void setStorage(String appPackage)
	{
		path = "/data/data/" + appPackage + "/databases/";
	}
	
	public void openDatabase(String db, String version, String display_name, long size)
	{
		if (path != null)
		{
			path += db + ".db";
			myDb = SQLiteDatabase.openOrCreateDatabase(path, null);
		}
	}
	
	public void executeSql(String query, String[] params, String tx_id)
	{
			try{
				txid = tx_id;
				Cursor myCursor = myDb.rawQuery(query, params);			
				processResults(myCursor);
			}
			catch (SQLiteException ex)
			{
				Log.d(LOG_TAG, ex.getMessage());	
				txid = "";
				appView.loadUrl("droiddb.fail(" + ex.getMessage() + "," +  txid + ")");			
			}
	}
	
	public void processResults(Cursor cur)
	{		
		String key = "";
		String value = "";
		String resultString = "";
		if (cur.moveToFirst()) {
			 int colCount = cur.getColumnCount();
			 do {
				 resultString = "{";
				 for(int i = 0; i < colCount; ++i)
				 {
					 key  = cur.getColumnName(i);
					 value = cur.getString(i);
					 resultString += " \"" + key + "\" : \"" + value + "\"";
					 if (i != (colCount - 1))
						 resultString += ",";
				 }
				 resultString += "}";
				 appView.loadUrl("javascript:droiddb.addResult('" + resultString + "', " + txid + ")");
			 } while (cur.moveToNext());
			 appView.loadUrl("javascript:droiddb.completeQuery(" + txid + ")");
			 txid = "";
			 myDb.close();
		 }
	}
		
}