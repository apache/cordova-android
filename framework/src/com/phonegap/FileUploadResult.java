/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi
 * Copyright (c) 2010, IBM Corporation
 */ 
package com.phonegap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Encapsulates the result and/or status of uploading a file to a remote server.
 */
public class FileUploadResult {
    
    private long bytesSent = 0;         // bytes sent
    private int responseCode = -1;      // HTTP response code
    private String response = null;     // HTTP response
       
    public long getBytesSent() {
        return bytesSent;
    }
    
    public void setBytesSent(long bytes) {
        this.bytesSent = bytes;
    }
    
    public int getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }

    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject(
                "{bytesSent:" + bytesSent + 
                ",responseCode:" + responseCode + 
                ",response:" + JSONObject.quote(response) + "}");
    }
}
