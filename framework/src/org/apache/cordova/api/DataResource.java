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
    private boolean retryIsLoad = true;
    private boolean retryOsLoad = true;
    private boolean retryMimeTypeLoad = true;
    private boolean retryWritableLoad = true;
    private boolean retryRealFileLoad = true;

    public DataResource(CordovaInterface cordova, Uri uri) {
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
    public InputStream getInputStream() throws IOException {
        if(is == null && retryIsLoad) {
            try {
                is = FileHelper.getInputStreamFromUriString(uri.toString(), cordova);
            } finally {
                // We failed loading once, don't try loading anymore
                if(is == null) {
                    retryIsLoad = false;
                }
            }
        }
        return is;
    }
    public OutputStream getOutputStream() throws FileNotFoundException {
        if(os == null && retryOsLoad) {
            try {
                os = FileHelper.getOutputStreamFromUriString(uri.toString(), cordova);
            } finally {
                // We failed loading once, don't try loading anymore
                if(os == null) {
                    retryOsLoad = false;
                }
            }
        }
        return os;
    }
    public String getMimeType() {
        if(mimeType == null && retryMimeTypeLoad) {
            try {
                mimeType = FileHelper.getMimeType(uri.toString(), cordova);
            } finally {
                // We failed loading once, don't try loading anymore
                if(mimeType == null) {
                    retryMimeTypeLoad = false;
                }
            }
        }
        return mimeType;
    }
    public boolean isWritable() {
        if(writable == null && retryWritableLoad) {
            try {
                writable = FileHelper.isUriWritable(uri.toString());
            } finally {
                // We failed loading once, don't try loading anymore
                if(writable == null) {
                    retryWritableLoad = false;
                }
            }
        }
        // default to false
        return writable != null && writable.booleanValue();
    }
    public File getRealFile() {
        if(realFile == null && retryRealFileLoad) {
            try {
                String realPath = FileHelper.getRealPath(uri, cordova);
                if(realPath != null) {
                    realFile = new File(realPath);
                }
            } finally {
                // We failed loading once, don't try loading anymore
                if(realFile == null) {
                    retryRealFileLoad = false;
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
        return initiateNewDataRequestForUri(uri, pluginManager, cordova, new DataResourceContext(requestSourceTag));
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
            dataResource = pluginManager.handleDataResourceRequestWithPlugins(dataResource, dataResourceContext);
        }
        return dataResource;
    }
}
