package com.phonegap;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;


public class GapClient extends WebChromeClient {
	
	private static final String LOG_TAG = "DroidGap";
	private long MAX_QUOTA = 2000000;
	private WebChromeClient mInstance;
	
	 /* class initialization fails when this throws an exception */
	static {
		try {
			
		} catch (Exception ex) {
	           throw new RuntimeException(ex);
		}
	}

	Context mCtx;
	GapClient(Context ctx)
	{
		mCtx = ctx;
	}
	
	@Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        Log.d(LOG_TAG, message);
        // This shows the dialog box.  This can be commented out for dev
        AlertDialog.Builder alertBldr = new AlertDialog.Builder(mCtx);
        alertBldr.setMessage(message);
        alertBldr.setTitle("Alert");
        alertBldr.show();
        result.confirm();
        return true;
    }

    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
    	     long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
    {
    	
    	if( estimatedSize < MAX_QUOTA)
    	{	
    		long newQuota = estimatedSize;
    		quotaUpdater.updateQuota(newQuota);
    	}
    	else
    	{
    		// Set the quota to whatever it is and force an error
    		// TODO: get docs on how to handle this properly
    		quotaUpdater.updateQuota(currentQuota);
    	}
    }

}
