/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
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
   * Executes the request and returns PluginResult.
   * 
   * @param action    The action to execute.
   * @param args      JSONArry of arguments for the plugin.
   * @param callbackId  The callback id used when calling back into JavaScript.
   * @return        A PluginResult object with a status and message.
   */
  public PluginResult execute(String action, JSONArray args, String callbackId) {
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
