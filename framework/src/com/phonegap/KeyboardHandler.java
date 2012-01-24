package com.phonegap;

import org.json.JSONArray;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class KeyboardHandler extends Plugin {

    
    /*
     * This will never be called! 
     * (non-Javadoc)
     * @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
     */
    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onMessage(String id, Object data)
    {
        if(id.equals("keyboardHidden"))
        {
            super.sendJavascript("PhoneGap.fireDocumentEvent('hidekeyboard');");
        }
        else if(id.equals("keyboardVisible"))
        {
            super.sendJavascript("PhoneGap.fireDocumentEvent('showkeyboard');");
        }
    }
}
