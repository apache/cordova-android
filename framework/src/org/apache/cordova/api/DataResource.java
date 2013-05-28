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
package org.apache.cordova.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.FileHelper;

import android.net.Uri;

/*
 * All requests to access files, browser network requests etc have to go through this class.
 */
public class DataResource {
    private CordovaInterface cordova;

    // Uri of the request. Always required.
    private Uri uri;
    // Remaining fields may or may not be null
    private InputStream is;
    private OutputStream os;
    private String mimeType;
    private Boolean writable;
    private File realFile;
    private boolean retryLoad = true;

    public DataResource(CordovaInterface cordova, Uri uri) {
        super();
        this.cordova = cordova;
        this.uri = uri;
    }
    public DataResource(CordovaInterface cordova, Uri uri, InputStream is,
            OutputStream os, String mimeType, boolean writable, File realFile) {
        this(cordova, uri);
        this.is = is;
        this.mimeType = mimeType;
        this.writable = Boolean.valueOf(writable);
        this.realFile = realFile;
    }
    public Uri getUri() {
        // Uri is always provided
        return uri;
    }
    public InputStream getIs() throws IOException {
        if(is == null && retryLoad) {
            try {
                is = FileHelper.getInputStreamFromUriString(uri.toString(), cordova);
            } finally {
                // We failed loading once, don't try loading anymore
                if(is == null) {
                    retryLoad = false;
                }
            }
        }
        return is;
    }
    public OutputStream getOs() throws FileNotFoundException {
        if(os == null && retryLoad) {
            try {
                os = FileHelper.getOutputStreamFromUriString(uri.toString(), cordova);
            } finally {
                // We failed loading once, don't try loading anymore
                if(os == null) {
                    retryLoad = false;
                }
            }
        }
        return os;
    }
    public String getMimeType() {
        if(mimeType == null && retryLoad) {
            try {
                mimeType = FileHelper.getMimeType(uri.toString(), cordova);
            } finally {
                // We failed loading once, don't try loading anymore
                if(mimeType == null) {
                    retryLoad = false;
                }
            }
        }
        return mimeType;
    }
    public boolean isWritable() {
        if(writable == null && retryLoad) {
            try {
                writable = FileHelper.isUriWritable(uri.toString());
            } finally {
                // We failed loading once, don't try loading anymore
                if(writable == null) {
                    retryLoad = false;
                }
            }
        }
        // default to false
        return writable != null? writable.booleanValue() : false;
    }
    public File getRealFile() {
        if(realFile == null && retryLoad) {
            try {
                String realPath = FileHelper.getRealPath(uri, cordova);
                if(realPath != null) {
                    realFile = new File(realPath);
                }
            } finally {
                // We failed loading once, don't try loading anymore
                if(realFile == null) {
                    retryLoad = false;
                }
            }
        }
        return realFile;
    }

    // static instantiation methods
    public static DataResource initiateNewDataRequestForUri(String uriString, PluginManager pluginManager, CordovaInterface cordova, String requestSourceTag){
        // if no protocol is specified, assume its file:
        uriString = FileHelper.insertFileProtocol(uriString);
        return initiateNewDataRequestForUri(Uri.parse(uriString), pluginManager, cordova, requestSourceTag);
    }
    public static DataResource initiateNewDataRequestForUri(Uri uri, PluginManager pluginManager, CordovaInterface cordova, String requestSourceTag){
        return initiateNewDataRequestForUri(uri, pluginManager, cordova, new DataResourceContext(requestSourceTag, false /* Assume, not a browser request by default */ ));
    }
    public static DataResource initiateNewDataRequestForUri(String uriString, PluginManager pluginManager, CordovaInterface cordova, DataResourceContext dataResourceContext){
     // if no protocol is specified, assume its file:
        uriString = FileHelper.insertFileProtocol(uriString);
        return initiateNewDataRequestForUri(Uri.parse(uriString), pluginManager, cordova, dataResourceContext);
    }
    public static DataResource initiateNewDataRequestForUri(Uri uri, PluginManager pluginManager, CordovaInterface cordova, DataResourceContext dataResourceContext){
        DataResource dataResource = new DataResource(cordova, uri);
        if (pluginManager != null) {
            // get the resource as returned by plugins
            dataResource = pluginManager.shouldInterceptDataResourceRequest(dataResource, dataResourceContext);
        }
        return dataResource;
    }
}
