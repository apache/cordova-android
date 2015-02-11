
package org.apache.cordova.test.junit;

/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaResourceApi.OpenForReadResult;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.test.CordovaWebViewTestActivity;
import org.apache.cordova.test.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class CordovaResourceApiTest extends ActivityInstrumentationTestCase2<CordovaWebViewTestActivity> {

    public CordovaResourceApiTest()
    {
        super(CordovaWebViewTestActivity.class);
    }

    CordovaWebView cordovaWebView;
    CordovaResourceApi resourceApi;

    private CordovaWebViewTestActivity activity;
    String execPayload;
    Integer execStatus;

    protected void setUp() throws Exception {
        super.setUp();
        activity = this.getActivity();
        cordovaWebView = activity.cordovaWebView;
        resourceApi = cordovaWebView.getResourceApi();
        resourceApi.setThreadCheckingEnabled(false);
        cordovaWebView.getPluginManager().addService(new PluginEntry("CordovaResourceApiTestPlugin1", new CordovaPlugin() {
            @Override
            public Uri remapUri(Uri uri) {
                if (uri.getQuery() != null && uri.getQuery().contains("pluginRewrite")) {
                    return cordovaWebView.getResourceApi().remapUri(
                            Uri.parse("data:text/plain;charset=utf-8,pass"));
                }
                return null;
            }
            public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
                synchronized (CordovaResourceApiTest.this) {
                    execPayload = args.getString(0);
                    execStatus = args.getInt(1);
                    CordovaResourceApiTest.this.notify();
                }
                return true;
            }
        }));
    }

    private Uri createTestImageContentUri() {
        Bitmap imageBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        String stored = MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                imageBitmap, "app-icon", "desc");
        return Uri.parse(stored);
    }

    private void performApiTest(Uri uri, String expectedMimeType, File expectedLocalFile,
            boolean expectRead, boolean expectWrite) throws IOException {
        uri = resourceApi.remapUri(uri);
        assertEquals(expectedLocalFile, resourceApi.mapUriToFile(uri));
        
        try {
            OpenForReadResult readResult = resourceApi.openForRead(uri);
            String mimeType2 = resourceApi.getMimeType(uri);
            assertEquals("openForRead mime-type", expectedMimeType, readResult.mimeType);
            assertEquals("getMimeType mime-type", expectedMimeType, mimeType2);
            readResult.inputStream.read();
            if (!expectRead) {
                fail("Expected getInputStream to throw.");
            }
        } catch (IOException e) {
            if (expectRead) {
                throw e;
            }
        }
        try {
            OutputStream outStream = resourceApi.openOutputStream(uri);
            outStream.write(123);
            if (!expectWrite) {
                fail("Expected getOutputStream to throw.");
            }
            outStream.close();
        } catch (IOException e) {
            if (expectWrite) {
                throw e;
            }
        }
    }

    public void testValidContentUri() throws IOException
    {
        Uri contentUri = createTestImageContentUri();
        File localFile = resourceApi.mapUriToFile(contentUri);
        assertNotNull(localFile);
        performApiTest(contentUri, "image/jpeg", localFile, true, true);
    }

    public void testInvalidContentUri() throws IOException
    {
        Uri contentUri = Uri.parse("content://media/external/images/media/999999999");
        performApiTest(contentUri, null, null, false, false);
    }

    public void testValidAssetUri() throws IOException
    {
        Uri assetUri = Uri.parse("file:///android_asset/www/index.html?foo#bar"); // Also check for stripping off ? and # correctly.
        performApiTest(assetUri, "text/html", null, true, false);
    }

    public void testInvalidAssetUri() throws IOException
    {
        Uri assetUri = Uri.parse("file:///android_asset/www/missing.html");
        performApiTest(assetUri, "text/html", null, false, false);
    }

    public void testFileUriToExistingFile() throws IOException
    {
        File f = File.createTempFile("te s t", ".txt"); // Also check for dealing with spaces.
        try {
            Uri fileUri = Uri.parse(f.toURI().toString() + "?foo#bar"); // Also check for stripping off ? and # correctly.
            performApiTest(fileUri, "text/plain", f, true, true);
        } finally {
            f.delete();
        }
    }

    public void testFileUriToMissingFile() throws IOException
    {
        File f = new File(Environment.getExternalStorageDirectory() + "/somefilethatdoesntexist");
        Uri fileUri = Uri.parse(f.toURI().toString());
        try {
            performApiTest(fileUri, null, f, false, true);
        } finally {
            f.delete();
        }
    }
    
    public void testFileUriToMissingFileWithMissingParent() throws IOException
    {
        File f = new File(Environment.getExternalStorageDirectory() + "/somedirthatismissing" + System.currentTimeMillis() + "/somefilethatdoesntexist");
        Uri fileUri = Uri.parse(f.toURI().toString());
        performApiTest(fileUri, null, f, false, true);
    }

    public void testUnrecognizedUri() throws IOException
    {
        Uri uri = Uri.parse("somescheme://foo");
        performApiTest(uri, null, null, false, false);
    }

    public void testRelativeUri()
    {
        try {
            resourceApi.openForRead(Uri.parse("/foo"));
            fail("Should have thrown for relative URI 1.");
        } catch (Throwable t) {
        }
        try {
            resourceApi.openForRead(Uri.parse("//foo/bar"));
            fail("Should have thrown for relative URI 2.");
        } catch (Throwable t) {
        }
        try {
            resourceApi.openForRead(Uri.parse("foo.png"));
            fail("Should have thrown for relative URI 3.");
        } catch (Throwable t) {
        }
    }
    
    public void testPluginOverride() throws IOException
    {
        Uri uri = Uri.parse("plugin-uri://foohost/android_asset/www/index.html?pluginRewrite=yes");
        performApiTest(uri, "text/plain", null, true, false);
    }

    public void testMainThreadUsage() throws IOException
    {
        Uri assetUri = Uri.parse("file:///android_asset/www/index.html");
        resourceApi.setThreadCheckingEnabled(true);
        try {
            resourceApi.openForRead(assetUri);
            fail("Should have thrown for main thread check.");
        } catch (Throwable t) {
        }
    }
    
    
    public void testDataUriPlain() throws IOException
    {
        Uri uri = Uri.parse("data:text/plain;charset=utf-8,pa%20ss");
        OpenForReadResult readResult = resourceApi.openForRead(uri);
        assertEquals("text/plain", readResult.mimeType);
        String data = new Scanner(readResult.inputStream, "UTF-8").useDelimiter("\\A").next();
        assertEquals("pa ss", data);
    }
    
    public void testDataUriBase64() throws IOException
    {
        Uri uri = Uri.parse("data:text/js;charset=utf-8;base64,cGFzcw==");
        OpenForReadResult readResult = resourceApi.openForRead(uri);
        assertEquals("text/js", readResult.mimeType);
        String data = new Scanner(readResult.inputStream, "UTF-8").useDelimiter("\\A").next();
        assertEquals("pass", data);
    }
    
    public void testWebViewRequestIntercept() throws IOException
    {
        cordovaWebView.sendJavascript(
            "var x = new XMLHttpRequest;\n" +
            "x.open('GET', 'file://foo?pluginRewrite=1', false);\n" + 
            "x.send();\n" + 
            "cordova.require('cordova/exec')(null,null,'CordovaResourceApiTestPlugin1', 'foo', [x.responseText, x.status])");
        execPayload = null;
        execStatus = null;
        try {
            synchronized (this) {
                this.wait(2000);
            }
        } catch (InterruptedException e) {
        }
        assertEquals("pass", execPayload);
        assertEquals(execStatus.intValue(), 200);
    }
    
    public void testWebViewWhiteListRejection() throws IOException
    {
        cordovaWebView.sendJavascript(
            "var x = new XMLHttpRequest;\n" +
            "x.open('GET', 'http://foo/bar', false);\n" + 
            "x.send();\n" + 
            "cordova.require('cordova/exec')(null,null,'CordovaResourceApiTestPlugin1', 'foo', [x.responseText, x.status])");
        execPayload = null;
        execStatus = null;
        try {
            synchronized (this) {
                this.wait(2000);
            }
        } catch (InterruptedException e) {
        }
        assertEquals("", execPayload);
        assertEquals(execStatus.intValue(), 404);
    }    
}
