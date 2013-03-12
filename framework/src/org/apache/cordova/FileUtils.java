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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.cordova.api.CordovaInterface;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.net.Uri;
import android.webkit.MimeTypeMap;

public class FileUtils {
	private static final String _DATA = "_data";

	/**
	 * Returns the real path of the given URI.
	 * If the given URI is a content:// URI, the real path is retrieved from the media store.
	 *
	 * @param uri the URI of the audio/image/video
	 * @param cordova the current application context
	 * @return the full path to the file
	 */
	@SuppressWarnings("deprecation")
	public static String getRealPathFromUri(Uri uri, CordovaInterface cordova) {
	    final String scheme = uri.getScheme();

	    if (scheme == null) {
	    	return uri.toString();
		} else if (scheme.compareTo("content") == 0) {
	        String[] proj = { _DATA };
	        Cursor cursor = cordova.getActivity().managedQuery(uri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(_DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
	    } else if (scheme.compareTo("file") == 0) {
	        return uri.getPath();
	    } else {
	        return uri.toString();
	    }
	}

	/**
	 * Returns an input stream based on given URI string.
	 *
	 * @param uriString the URI string from which to obtain the input stream
	 * @param cordova the current application context
	 * @return an input stream into the data at the given URI
	 * @throws FileNotFoundException
	 */
	public static InputStream getInputStreamFromUriString(String uriString, CordovaInterface cordova) throws FileNotFoundException {
	    if (uriString.startsWith("content")) {
	        Uri uri = Uri.parse(uriString);
	        return cordova.getActivity().getContentResolver().openInputStream(uri);
	    }
	    else {
	        uriString = getRealPathFromUri(Uri.parse(uriString), cordova);
	        return new FileInputStream(uriString);
	    }
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
	public static String getMimeType(String uriString) {
	    if (uriString != null) {
	        // Stupid bug in getFileExtensionFromUrl when the file name has a space
	        // So we need to replace the space with a url encoded %20

	        // CB-2185: Stupid bug not putting JPG extension in the mime-type map
	        String url = uriString.replace(" ", "%20").toLowerCase();
	        MimeTypeMap map = MimeTypeMap.getSingleton();
	        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
	        if (extension.toLowerCase().equals("3ga")) {
	            return "audio/3gpp";
	        } else {
	            return map.getMimeTypeFromExtension(extension);
	        }
	    } else {
	        return "";
	    }
	}

	/**
	 * Returns a JSON object representing the given File.
	 *
	 * @param file the File to convert
	 * @return a JSON representation of the given File
	 * @throws JSONException
	 */
	public static JSONObject getEntry(File file) throws JSONException {
	    JSONObject entry = new JSONObject();

	    entry.put("isFile", file.isFile());
	    entry.put("isDirectory", file.isDirectory());
	    entry.put("name", file.getName());
	    entry.put("fullPath", "file://" + file.getAbsolutePath());
	    // The file system can't be specified, as it would lead to an infinite loop.
	    // entry.put("filesystem", null);

	    return entry;
	}
}
