// Taken from Android Tutorials
/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
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
import java.util.HashMap;

import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This abstract class defines SDK-independent API for communication with
 * Contacts Provider. The actual implementation used by the application depends
 * on the level of API available on the device. If the API level is Cupcake or
 * Donut, we want to use the {@link ContactAccessorSdk3_4} class. If it is
 * Eclair or higher, we want to use {@link ContactAccessorSdk5}.
 */
public abstract class ContactAccessor {
	
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
     * Check to see if the data associated with the key is required to 
     * be populated in the Contact object.
     * @param key 
     * @param map created by running buildPopulationSet.
     * @return true if the key data is required
     */
    protected boolean isRequired(String key, HashMap<String,Boolean> map) {
		Boolean retVal = map.get(key);
		return (retVal == null) ? false : retVal.booleanValue();
	}
    
    /**
     * Create a hash map of what data needs to be populated in the Contact object
     * @param fields the list of fields to populate
     * @return the hash map of required data
     */
	protected HashMap<String,Boolean> buildPopulationSet(JSONArray fields) {
		HashMap<String,Boolean> map = new HashMap<String,Boolean>();
		
		String key;
		try {
			for (int i=0; i<fields.length(); i++) {
				key = fields.getString(i);
				if (key.startsWith("displayName")) {
					map.put("displayName", true);
				}
				else if (key.startsWith("name")) {
					map.put("name", true);
				}
				else if (key.startsWith("nickname")) {
					map.put("nickname", true);
				}
				else if (key.startsWith("phoneNumbers")) {
					map.put("phoneNumbers", true);
				}
				else if (key.startsWith("emails")) {
					map.put("emails", true);
				}
				else if (key.startsWith("addresses")) {
					map.put("addresses", true);
				}
				else if (key.startsWith("ims")) {
					map.put("ims", true);
				}
				else if (key.startsWith("organizations")) {
					map.put("organizations", true);
				}
				else if (key.startsWith("birthday")) {
					map.put("birthday", true);
				}
				else if (key.startsWith("anniversary")) {
					map.put("anniversary", true);
				}
				else if (key.startsWith("note")) {
					map.put("note", true);
				}
				else if (key.startsWith("relationships")) {
					map.put("relationships", true);
				}
				else if (key.startsWith("urls")) {
					map.put("urls", true);
				}
			}
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return map;
	}

    /**
     * Handles adding a JSON Contact object into the database.
     */
	public abstract void save(JSONObject contact);

    /**
     * Handles searching through SDK-specific contacts API.
     */
    public abstract JSONArray search(JSONArray filter, JSONObject options);

    /**
     * Handles removing a contact from the database.
     */
	public abstract boolean remove(String id);
	
	/**
	 * A class that represents the where clause to be used in the database query 
	 */
	class WhereOptions {
		private String where;
		private String[] whereArgs;
		public void setWhere(String where) {
			this.where = where;
		}
		public String getWhere() {
			return where;
		}
		public void setWhereArgs(String[] whereArgs) {
			this.whereArgs = whereArgs;
		}
		public String[] getWhereArgs() {
			return whereArgs;
		}
	}
}