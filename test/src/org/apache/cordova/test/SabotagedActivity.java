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

package org.apache.cordova.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaActivity;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class SabotagedActivity extends CordovaActivity {

    private String BAD_ASSET = "www/error.html";
    private String LOG_TAG = "SabotagedActivity";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        
//        copyErrorAsset();
        super.init();
        super.loadUrl(Config.getStartUrl());
    }

    /* 
     * Sometimes we need to move code around before we can do anything.  This will
     * copy the bad code out of the assets before we initalize Cordova so that when Cordova actually
     * initializes, we have something for it to navigate to.
     */ 
    
    private void copyErrorAsset () {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(BAD_ASSET);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
              in = assetManager.open(BAD_ASSET);
              out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() +"/" + filename);
              copy(in, out);
              in.close();
              in = null;
              out.flush();
              out.close();
              out = null;
            } catch(Exception e) {
                Log.e("tag", e.getMessage());
            }
        }
    }
    
    
    //Quick and Dirty Copy! 
    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
}
