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

import android.graphics.Color;
import android.os.Bundle;
import org.apache.cordova.*;

public class backgroundcolor extends CordovaActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // backgroundColor can also be set in cordova.xml, but you must use the number equivalent of the color.  For example, Color.RED is
        //      <preference name="backgroundColor" value="-65536" />
        preferences.set("backgroundColor", Color.GREEN);

        super.loadUrl("file:///android_asset/www/backgroundcolor/index.html");
    }

}
