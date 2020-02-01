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
package org.apache.cordova.unittests;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;

public class LifeCyclePlugin extends CordovaPlugin {

    static String TAG = "LifeCyclePlugin";
    String calls = "";

    @Override
    public void onStart() {
        calls += "start,";
        LOG.d(TAG, "onStart");
    }
    @Override
    public void onPause(boolean multitasking) {
        calls += "pause,";
        LOG.d(TAG, "onPause");
    }
    @Override
    public void onResume(boolean multitasking) {
        calls += "resume,";
        LOG.d(TAG, "onResume");
    }
    @Override
    public void onStop() {
        calls += "stop,";
        LOG.d(TAG, "onStop");
    }
}
