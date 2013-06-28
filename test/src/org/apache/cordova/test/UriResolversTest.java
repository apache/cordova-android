
package org.apache.cordova.test;

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

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.UriResolver;
import org.apache.cordova.UriResolvers;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginEntry;
import org.apache.cordova.test.actions.CordovaWebViewTestActivity;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class UriResolversTest extends ActivityInstrumentationTestCase2<CordovaWebViewTestActivity> {

    public UriResolversTest()
    {
        super(CordovaWebViewTestActivity.class);
    }

    CordovaWebView cordovaWebView;
    private CordovaWebViewTestActivity activity;
    String execPayload;
    Integer execStatus;

    protected void setUp() throws Exception {
        super.setUp();
        activity = this.getActivity();
        cordovaWebView = activity.cordovaWebView;
        cordovaWebView.pluginManager.addService(new PluginEntry("UriResolverTestPlugin1", new CordovaPlugin() {
            @Override
            public UriResolver resolveUri(Uri uri) {
                if ("plugin-uri".equals(uri.getScheme())) {
                    return cordovaWebView.resolveUri(uri.buildUpon().scheme("file").build());
                }
                return null;
            }
            public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
                synchronized (UriResolversTest.this) {
                    execPayload = args.getString(0);
                    execStatus = args.getInt(1);
                    UriResolversTest.this.notify();
                }
                return true;
            }
        }));
        cordovaWebView.pluginManager.addService(new PluginEntry("UriResolverTestPlugin2", new CordovaPlugin() {
            @Override
            public UriResolver resolveUri(Uri uri) {
                if (uri.getQueryParameter("pluginRewrite") != null) {
                    return UriResolvers.createInline(uri, "pass", "my/mime");
                }
                return null;
            }
        }));
    }

    private Uri createTestImageContentUri() {
        Bitmap imageBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.icon);
        String stored = MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                imageBitmap, "app-icon", "desc");
        return Uri.parse(stored);
    }

    private void performResolverTest(Uri uri, String expectedMimeType, File expectedLocalFile,
            boolean expectedIsWritable,
            boolean expectRead, boolean expectWrite) throws IOException {
        UriResolver resolver = cordovaWebView.resolveUri(uri);
        assertEquals(expectedLocalFile, resolver.getLocalFile());
        assertEquals(expectedMimeType, resolver.getMimeType());
        if (expectedIsWritable) {
            assertTrue(resolver.isWritable());
        } else {
            assertFalse(resolver.isWritable());
        }
        try {
            resolver.getInputStream().read();
            if (!expectRead) {
                fail("Expected getInputStream to throw.");
            }
        } catch (IOException e) {
            if (expectRead) {
                throw e;
            }
        }
        try {
            resolver.getOutputStream().write(123);
            if (!expectWrite) {
                fail("Expected getOutputStream to throw.");
            }
        } catch (IOException e) {
            if (expectWrite) {
                throw e;
            }
        }
    }

    public void testValidContentUri() throws IOException
    {
        Uri contentUri = createTestImageContentUri();
        performResolverTest(contentUri, "image/jpeg", null, true, true, true);
    }

    public void testInvalidContentUri() throws IOException
    {
        Uri contentUri = Uri.parse("content://media/external/images/media/999999999");
        performResolverTest(contentUri, null, null, true, false, false);
    }

    public void testValidAssetUri() throws IOException
    {
        Uri assetUri = Uri.parse("file:///android_asset/www/index.html?foo#bar"); // Also check for stripping off ? and # correctly.
        performResolverTest(assetUri, "text/html", null, false, true, false);
    }

    public void testInvalidAssetUri() throws IOException
    {
        Uri assetUri = Uri.parse("file:///android_asset/www/missing.html");
        performResolverTest(assetUri, "text/html", null, false, false, false);
    }

    public void testFileUriToExistingFile() throws IOException
    {
        File f = File.createTempFile("te s t", ".txt"); // Also check for dealing with spaces.
        try {
            Uri fileUri = Uri.parse(f.toURI().toString() + "?foo#bar"); // Also check for stripping off ? and # correctly.
            performResolverTest(fileUri, "text/plain", f, true, true, true);
        } finally {
            f.delete();
        }
    }

    public void testFileUriToMissingFile() throws IOException
    {
        File f = new File(Environment.getExternalStorageDirectory() + "/somefilethatdoesntexist");
        Uri fileUri = Uri.parse(f.toURI().toString());
        try {
            performResolverTest(fileUri, null, f, true, false, true);
        } finally {
            f.delete();
        }
    }
    
    public void testFileUriToMissingFileWithMissingParent() throws IOException
    {
        File f = new File(Environment.getExternalStorageDirectory() + "/somedirthatismissing/somefilethatdoesntexist");
        Uri fileUri = Uri.parse(f.toURI().toString());
        performResolverTest(fileUri, null, f, false, false, false);
    }

    public void testUnrecognizedUri() throws IOException
    {
        Uri uri = Uri.parse("somescheme://foo");
        performResolverTest(uri, null, null, false, false, false);
    }

    public void testRelativeUri()
    {
        try {
            cordovaWebView.resolveUri(Uri.parse("/foo"));
            fail("Should have thrown for relative URI 1.");
        } catch (Throwable t) {
        }
        try {
            cordovaWebView.resolveUri(Uri.parse("//foo/bar"));
            fail("Should have thrown for relative URI 2.");
        } catch (Throwable t) {
        }
        try {
            cordovaWebView.resolveUri(Uri.parse("foo.png"));
            fail("Should have thrown for relative URI 3.");
        } catch (Throwable t) {
        }
    }
    
    public void testPluginOverrides1() throws IOException
    {
        Uri uri = Uri.parse("plugin-uri://foohost/android_asset/www/index.html");
        performResolverTest(uri, "text/html", null, false, true, false);
    }

    public void testPluginOverrides2() throws IOException
    {
        Uri uri = Uri.parse("plugin-uri://foohost/android_asset/www/index.html?pluginRewrite=yes");
        performResolverTest(uri, "my/mime", null, false, true, false);
    }

    public void testWhitelistRejection() throws IOException
    {
        Uri uri = Uri.parse("http://foohost.com/");
        performResolverTest(uri, null, null, false, false, false);
    }
    
    public void testWebViewRequestIntercept() throws IOException
    {
        cordovaWebView.sendJavascript(
            "var x = new XMLHttpRequest;\n" +
            "x.open('GET', 'file://foo?pluginRewrite=1', false);\n" + 
            "x.send();\n" + 
            "cordova.require('cordova/exec')(null,null,'UriResolverTestPlugin1', 'foo', [x.responseText, x.status])");
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
            "cordova.require('cordova/exec')(null,null,'UriResolverTestPlugin1', 'foo', [x.responseText, x.status])");
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
