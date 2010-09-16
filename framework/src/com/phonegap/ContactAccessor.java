// Taken from Android Tutorials

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonegap;

import java.lang.reflect.Constructor;

import android.app.Activity;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This abstract class defines SDK-independent API for communication with
 * Contacts Provider. The actual implementation used by the application depends
 * on the level of API available on the device. If the API level is Cupcake or
 * Donut, we want to use the {@link ContactAccessorSdk3_4} class. If it is
 * Eclair or higher, we want to use {@link ContactAccessorSdk5}.
 */
public abstract class ContactAccessor {
	
	public class ContactTriplet
	{
		public String name = "";
		public String email = "";
		public String phone = "";
	}
	
    /**
     * Static singleton instance of {@link ContactAccessor} holding the
     * SDK-specific implementation of the class.
     */
    private static ContactAccessor sInstance;
    protected final String LOG_TAG = "ContactsAccessor";
    protected Activity mApp;
    protected WebView mView;

    public static ContactAccessor getInstance(WebView view, Activity app) {
        if (sInstance == null) {
            String className;

            /*
             * Check the version of the SDK we are running on. Choose an
             * implementation class designed for that version of the SDK.
             *
             * Unfortunately we have to use strings to represent the class
             * names. If we used the conventional ContactAccessorSdk5.class.getName()
             * syntax, we would get a ClassNotFoundException at runtime on pre-Eclair SDKs.
             * Using the above syntax would force Dalvik to load the class and try to
             * resolve references to all other classes it uses. Since the pre-Eclair
             * does not have those classes, the loading of ContactAccessorSdk5 would fail.
             */
            
            if (android.os.Build.VERSION.RELEASE.startsWith("1.")) {
                className = "com.phonegap.ContactAccessorSdk3_4";
            } else {
                className = "com.phonegap.ContactAccessorSdk5";
            }

            /*
             * Find the required class by name and instantiate it.
             */
            try {
                Class<? extends ContactAccessor> clazz =
                        Class.forName(className).asSubclass(ContactAccessor.class);
                // Grab constructor for contactsmanager class dynamically.
                Constructor<? extends ContactAccessor> classConstructor = clazz.getConstructor(Class.forName("android.webkit.WebView"), Class.forName("android.app.Activity"));
                sInstance = classConstructor.newInstance(view, app);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        return sInstance;
    }

    /**
     * Handles searching through SDK-specific contacts API.
     */
    public abstract void search(JSONArray filter, JSONObject options);
}