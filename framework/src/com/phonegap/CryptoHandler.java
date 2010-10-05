package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class CryptoHandler extends Plugin {
		
	/**
	 * Constructor.
	 */
	public CryptoHandler() {
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
			if (action.equals("encrypt")) {
				this.encrypt(args.getString(0), args.getString(1));
			}
			else if (action.equals("decrypt")) {
				this.decrypt(args.getString(0), args.getString(1));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	public void encrypt(String pass, String text) {
		try {
			String encrypted = SimpleCrypto.encrypt(pass,text);
			// TODO: Why not just return text now?
			this.sendJavascript("Crypto.gotCryptedString('" + text + "')");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void decrypt(String pass, String text) {
		try {
			String decrypted = SimpleCrypto.decrypt(pass,text);
			this.sendJavascript("Crypto.gotPlainString('" + text + "')");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
