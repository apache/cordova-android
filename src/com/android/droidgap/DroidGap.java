package com.android.droidgap;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class DroidGap extends Activity {
	
	private WebView appView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
         
        appView = (WebView) findViewById(R.id.appView);        
        appView.getSettings().setJavaScriptEnabled(true);
        appView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);        
        
        /* Bind the appView object to the gap class methods */
        bindBrowser(appView);
        
        /* 
         * We need to decide whether this is a local or a remote app.  For the sake of clarity
         * we can use HTML with both local and remote applications, but it means that we have to open the local file         
         */
                
        appView.loadUrl("http://www.infil00p.org/gap/");       
        
    }
    
    private void loadFile(WebView appView){
        try {
            InputStream is = getAssets().open("index.html");
                      
            int size = is.available();
            
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            // Convert the buffer into a Java string.
            String text = new String(buffer);
            
            // Load the local file into the webview
            appView.loadData(text, "text/html", "UTF-8");
            
        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
    }
    
    private void bindBrowser(WebView appView)
    {
    	PhoneGap gap = new PhoneGap(this);
    	appView.addJavascriptInterface(gap, "DroidGap");
    }
    
}