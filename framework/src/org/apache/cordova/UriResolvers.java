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
package org.apache.cordova;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.FileHelper;
import org.apache.http.util.EncodingUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

/*
 * UriResolver implementations.
 */
public final class UriResolvers {
    private UriResolvers() {}

    private static final class FileUriResolver implements UriResolver {
        private final Uri uri;
        private String mimeType;
        private File localFile;
    
        FileUriResolver(Uri uri) {
            this.uri = uri;
        }
        
        public Uri getUri() {
            return uri;
        }
        
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(getLocalFile());
        }
        
        public OutputStream getOutputStream() throws FileNotFoundException {
            return new FileOutputStream(getLocalFile());
        }
        
        public String getMimeType() {
            if (mimeType == null) {
                mimeType = FileHelper.getMimeTypeForExtension(getLocalFile().getName());
            }
            return mimeType;
        }
        
        public boolean isWritable() {
            File f = getLocalFile();
            if (f.isDirectory()) {
                return false;
            }
            if (f.exists()) {
                return f.canWrite();
            }
            return f.getParentFile().canWrite();
        }
        
        public File getLocalFile() {
            if (localFile == null) {
                localFile = new File(uri.getPath());
            }
            return localFile;
        }
    }
    
    private static final class AssetUriResolver implements UriResolver {
        private final Uri uri;
        private final AssetManager assetManager;
        private final String assetPath;
        private String mimeType;
    
        AssetUriResolver(Uri uri, AssetManager assetManager) {
            this.uri = uri;
            this.assetManager = assetManager;
            this.assetPath = uri.getPath().substring(15);
        }
        
        public Uri getUri() {
            return uri;
        }
        
        public InputStream getInputStream() throws IOException {
            return assetManager.open(assetPath);
        }
        
        public OutputStream getOutputStream() throws FileNotFoundException {
            throw new FileNotFoundException("URI not writable.");
        }
        
        public String getMimeType() {
            if (mimeType == null) {
                mimeType = FileHelper.getMimeTypeForExtension(assetPath);
            }
            return mimeType;
        }
        
        public boolean isWritable() {
            return false;
        }
        
        public File getLocalFile() {
            return null;
        }
    }
    
    private static final class ContentUriResolver implements UriResolver {
        private final Uri uri;
        private final ContentResolver contentResolver;
        private String mimeType;
    
        ContentUriResolver(Uri uri, ContentResolver contentResolver) {
            this.uri = uri;
            this.contentResolver = contentResolver;
        }
        
        public Uri getUri() {
            return uri;
        }
        
        public InputStream getInputStream() throws IOException {
            return contentResolver.openInputStream(uri);
        }
        
        public OutputStream getOutputStream() throws FileNotFoundException {
            return contentResolver.openOutputStream(uri);
        }
        
        public String getMimeType() {
            if (mimeType == null) {
                mimeType = contentResolver.getType(uri);
            }
            return mimeType;
        }
        
        public boolean isWritable() {
            return uri.getScheme().equals(ContentResolver.SCHEME_CONTENT);
        }
        
        public File getLocalFile() {
            return null;
        }
    }
    
    static final class ErrorUriResolver implements UriResolver {
        final Uri uri;
        final String errorMsg;
        
        ErrorUriResolver(Uri uri, String errorMsg) {
            this.uri = uri;
            this.errorMsg = errorMsg;
        }
        
        @Override
        public boolean isWritable() {
            return false;
        }
        
        @Override
        public Uri getUri() {
            return uri;
        }
        
        @Override
        public File getLocalFile() {
            return null;
        }
        
        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new FileNotFoundException(errorMsg);
        }
        
        @Override
        public String getMimeType() {
            return null;
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            throw new FileNotFoundException(errorMsg);
        }
    }
    
    private static final class ReadOnlyResolver implements UriResolver {
        private Uri uri;
        private InputStream inputStream;
        private String mimeType;
        
        public ReadOnlyResolver(Uri uri, InputStream inputStream, String mimeType) {
            this.uri = uri;
            this.inputStream = inputStream;
            this.mimeType = mimeType;
        }
        
        @Override
        public boolean isWritable() {
            return false;
        }
        
        @Override
        public Uri getUri() {
            return uri;
        }
        
        @Override
        public File getLocalFile() {
            return null;
        }
        
        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new FileNotFoundException("URI is not writable");
        }
        
        @Override
        public String getMimeType() {
            return mimeType;
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
    }
    
    public static UriResolver createInline(Uri uri, String response, String mimeType) {
        return createInline(uri, EncodingUtils.getBytes(response, "UTF-8"), mimeType);
    }
    
    public static UriResolver createInline(Uri uri, byte[] response, String mimeType) {
        return new ReadOnlyResolver(uri, new ByteArrayInputStream(response), mimeType);
    }

    public static UriResolver createReadOnly(Uri uri, InputStream inputStream, String mimeType) {
        return new ReadOnlyResolver(uri, inputStream, mimeType);
    }
    
    /* Package-private to force clients to go through CordovaWebView.resolveUri(). */
    static UriResolver forUri(Uri uri, Context context) {
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            return new ContentUriResolver(uri, context.getContentResolver());
        }
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            if (uri.getPath().startsWith("/android_asset/")) {
                return new AssetUriResolver(uri, context.getAssets());
            }
            return new FileUriResolver(uri);
        }
        return null;
    }
}