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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.Uri;

/*
 * Interface for a class that can resolve URIs.
 * See CordovaUriResolver for an example.
 */
public interface UriResolver {

    /** Returns the URI that this instance will resolve. */
    Uri getUri();
    
    /** 
     * Returns the InputStream for the resource. 
     * Throws an exception if it cannot be read. 
     * Never returns null.
     */
    InputStream getInputStream() throws IOException;

    /** 
     * Returns the OutputStream for the resource. 
     * Throws an exception if it cannot be written to. 
     * Never returns null.
     */
    OutputStream getOutputStream() throws IOException; 
    
    /** 
     * Returns the MIME type of the resource.
     * Returns null if the MIME type cannot be determined (e.g. content: that doesn't exist).
     */
    String getMimeType();

    /** Returns whether the resource is writable. */
    boolean isWritable();

    /**
     * Returns a File that points to the resource, or null if the resource
     * is not on the local file system.
     */
    File getLocalFile();
}
