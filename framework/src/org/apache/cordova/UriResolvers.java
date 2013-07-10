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
import android.os.Looper;

/*
 * UriResolver implementations.
 */
public final class UriResolvers {
    static Thread webCoreThread;

    private UriResolvers() {}

    private static final class FileUriResolver implements UriResolver {
        private final File localFile;
        private String mimeType;
    
        FileUriResolver(Uri uri) {
            localFile = new File(uri.getPath());
        }
        
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(localFile);
        }
        
        public OutputStream getOutputStream() throws FileNotFoundException {
            return new FileOutputStream(localFile);
        }
        
        public String getMimeType() {
            if (mimeType == null) {
                mimeType = FileHelper.getMimeTypeForExtension(localFile.getName());
            }
            return mimeType;
        }
        
        public boolean isWritable() {
            if (localFile.isDirectory()) {
                return false;
            }
            if (localFile.exists()) {
                return localFile.canWrite();
            }
            return localFile.getParentFile().canWrite();
        }
        
        public File getLocalFile() {
            return localFile;
        }
    }
    
    private static final class AssetUriResolver implements UriResolver {
        private final AssetManager assetManager;
        private final String assetPath;
        private String mimeType;
    
        AssetUriResolver(Uri uri, AssetManager assetManager) {
            this.assetManager = assetManager;
            this.assetPath = uri.getPath().substring(15);
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
    
    private static final class ErrorUriResolver implements UriResolver {
        final String errorMsg;
        
        ErrorUriResolver(String errorMsg) {
            this.errorMsg = errorMsg;
        }
        
        public boolean isWritable() {
            return false;
        }
        
        public File getLocalFile() {
            return null;
        }
        
        public OutputStream getOutputStream() throws IOException {
            throw new FileNotFoundException(errorMsg);
        }
        
        public String getMimeType() {
            return null;
        }
        
        public InputStream getInputStream() throws IOException {
            throw new FileNotFoundException(errorMsg);
        }
    }
    
    private static final class ReadOnlyResolver implements UriResolver {
        private InputStream inputStream;
        private String mimeType;
        
        public ReadOnlyResolver(Uri uri, InputStream inputStream, String mimeType) {
            this.inputStream = inputStream;
            this.mimeType = mimeType;
        }
        
        public boolean isWritable() {
            return false;
        }
        
        public File getLocalFile() {
            return null;
        }
        
        public OutputStream getOutputStream() throws IOException {
            throw new FileNotFoundException("URI is not writable");
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
    }
    
    private static final class ThreadCheckingResolver implements UriResolver {
        final UriResolver delegate;
        
        ThreadCheckingResolver(UriResolver delegate) {
            this.delegate = delegate;
        }

        private static void checkThread() {
            Thread curThread = Thread.currentThread();
            if (curThread == Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("Do not perform IO operations on the UI thread. Use CordovaInterface.getThreadPool() instead.");
            }
            if (curThread == webCoreThread) {
                throw new IllegalStateException("Tried to perform an IO operation on the WebCore thread. Use CordovaInterface.getThreadPool() instead.");
            }
        }
        
        public boolean isWritable() {
            checkThread();
            return delegate.isWritable();
        }
        

        public File getLocalFile() {
            checkThread();
            return delegate.getLocalFile();
        }
        
        public OutputStream getOutputStream() throws IOException {
            checkThread();
            return delegate.getOutputStream();
        }
        
        public String getMimeType() {
            checkThread();
            return delegate.getMimeType();
        }
        
        public InputStream getInputStream() throws IOException {
            checkThread();
            return delegate.getInputStream();
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
    
    public static UriResolver createError(String errorMsg) {
        return new ErrorUriResolver(errorMsg);
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
    
    /* Used only by CordovaWebView.resolveUri(). */
    static UriResolver makeThreadChecking(UriResolver resolver) {
        if (resolver instanceof ThreadCheckingResolver) {
            return resolver;
        }
        return new ThreadCheckingResolver(resolver);
    }
}
