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

import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.LOG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    private static final String LOG_TAG = "FileUtils";
    private static final String _DATA = "_data";

    /**
     * Returns the real path of the given URI string.
     * If the given URI string represents a content:// URI, the real path is retrieved from the media store.
     *
     * @param uriString the URI string of the audio/image/video
     * @param cordova the current application context
     * @return the full path to the file
     */
    @SuppressWarnings("deprecation")
    public static String getRealPath(String uriString, CordovaInterface cordova) {
        String realPath = null;

        if (uriString.startsWith("content://")) {
            String[] proj = { _DATA };
            Cursor cursor = cordova.getActivity().managedQuery(Uri.parse(uriString), proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(_DATA);
            cursor.moveToFirst();
            realPath = cursor.getString(column_index);
            if (realPath == null) {
                LOG.e(LOG_TAG, "Could get real path for URI string %s", uriString);
            }
        } else if (uriString.startsWith("file://")) {
            realPath = uriString.substring(7);
            if (realPath.startsWith("/android_asset/")) {
                LOG.e(LOG_TAG, "Cannot get real path for URI string %s because it is a file:///android_asset/ URI.", uriString);
                realPath = null;
            }
        } else {
            realPath = uriString;
        }

        return realPath;
    }

    /**
     * Returns the real path of the given URI.
     * If the given URI is a content:// URI, the real path is retrieved from the media store.
     *
     * @param uri the URI of the audio/image/video
     * @param cordova the current application context
     * @return the full path to the file
     */
    public static String getRealPath(Uri uri, CordovaInterface cordova) {
        return FileHelper.getRealPath(uri.toString(), cordova);
    }

    /**
     * Returns an input stream based on given URI string.
     *
     * @param uriString the URI string from which to obtain the input stream
     * @param cordova the current application context
     * @return an input stream into the data at the given URI or null if given an invalid URI string
     * @throws IOException
     */
    public static InputStream getInputStreamFromUriString(String uriString, CordovaInterface cordova) throws IOException {
        if (uriString.startsWith("content:")) {
            Uri uri = Uri.parse(uriString);
            return cordova.getActivity().getContentResolver().openInputStream(uri);
        } else if (uriString.startsWith("file:///android_asset/")) {
            Uri uri = Uri.parse(uriString);
            String relativePath = uri.getPath().substring(15);
            return cordova.getActivity().getAssets().open(relativePath);
        } else if (uriString.startsWith("file://")) {
            return new FileInputStream(getRealPath(uriString, cordova));
        } else {
            return null;
        }
    }

    public static OutputStream getOutputStreamFromUriString(String uriString, CordovaInterface cordova) throws FileNotFoundException{
        if (uriString.startsWith("content:")) {
            Uri uri = Uri.parse(uriString);
            return cordova.getActivity().getContentResolver().openOutputStream(uri);
        } else if (uriString.startsWith("file:") && !uriString.startsWith("file:///android_asset/")) {
            String realPath = uriString.substring(7);
            return new FileOutputStream(realPath);
        } else {
            return null;
        }
    }
    /**
     * Returns whether the uri can be written to by openeing a File to that uri
     *
     * @param the URI to test
     * @return boolean indicating whether the uri is writable
     */
    public static boolean isUriWritable(String uriString) {
        String scheme = uriString.split(":")[0];

        if(scheme.equals("file")){
            // special case file
            return !uriString.startsWith("file:///android_asset/");
        }
        return "content".equals(scheme);
    }

    /**
     * Ensures the "file://" prefix exists for the given string
     * If the given URI string already has a scheme, it is returned unchanged
     *
     * @param path - the path string to operate on
     * @return a String with the "file://" scheme set
     */
    public static String insertFileProtocol(String path) {
        if(!path.matches("^[a-z0-9+.-]+:.*")){
            //Ensure it is not a relative path
            if(!path.startsWith("/")){
                throw new IllegalArgumentException("Relative paths" + path + "are not allowed.");
            }
            path = "file://" + path;
        }
        return path;
    }

    /**
     * Removes the "file://" prefix from the given URI string, if applicable.
     * If the given URI string doesn't have a "file://" prefix, it is returned unchanged.
     *
     * @param uriString the URI string to operate on
     * @return a path without the "file://" prefix
     */
    public static String stripFileProtocol(String uriString) {
        if (uriString.startsWith("file://")) {
            uriString = uriString.substring(7);
        }
        return uriString;
    }

    /**
     * Returns the mime type of the data specified by the given URI string.
     *
     * @param uriString the URI string of the data
     * @return the mime type of the specified data
     */
    public static String getMimeType(String uriString, CordovaInterface cordova) {
        String mimeType = null;

        Uri uri = Uri.parse(uriString);
        if (uriString.startsWith("content://")) {
            mimeType = cordova.getActivity().getContentResolver().getType(uri);
        } else {
            // MimeTypeMap.getFileExtensionFromUrl() fails when there are query parameters.
            String extension = uri.getPath();
            int lastDot = extension.lastIndexOf('.');
            if (lastDot != -1) {
                extension = extension.substring(lastDot + 1);
            }
            // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see CB-2185).
            extension = extension.toLowerCase();
            if (extension.equals("3ga")) {
                mimeType = "audio/3gpp";
            } else {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
        }

        return mimeType;
    }
}
