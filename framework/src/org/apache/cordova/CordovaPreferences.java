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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.cordova.LOG;

import android.app.Activity;
import android.os.Bundle;

public class CordovaPreferences {
    private HashMap<String, String> prefs = new HashMap<String, String>(20);
    private Bundle preferencesBundleExtras;

    public void setPreferencesBundle(Bundle extras) {
        preferencesBundleExtras = extras;
    }

    public void set(String name, String value) {
        prefs.put(name.toLowerCase(Locale.ENGLISH), value);
    }

    public void set(String name, boolean value) {
        set(name, "" + value);
    }

    public void set(String name, int value) {
        set(name, "" + value);
    }
    
    public void set(String name, double value) {
        set(name, "" + value);
    }
    
    public Map<String, String> getAll() {
        return prefs;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        name = name.toLowerCase(Locale.ENGLISH);
        String value = prefs.get(name);
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else if (preferencesBundleExtras != null) {
            Object bundleValue = preferencesBundleExtras.get(name);
            if (bundleValue instanceof String) {
                return "true".equals(bundleValue);
            }
            // Gives a nice warning if type is wrong.
            return preferencesBundleExtras.getBoolean(name, defaultValue);
        }
        return defaultValue;
    }

    public int getInteger(String name, int defaultValue) {
        name = name.toLowerCase(Locale.ENGLISH);
        String value = prefs.get(name);
        if (value != null) {
            // Use Integer.decode() can't handle it if the highest bit is set.
            return (int)(long)Long.decode(value);
        } else if (preferencesBundleExtras != null) {
            Object bundleValue = preferencesBundleExtras.get(name);
            if (bundleValue instanceof String) {
                return Integer.valueOf((String)bundleValue);
            }
            // Gives a nice warning if type is wrong.
            return preferencesBundleExtras.getInt(name, defaultValue);
        }
        return defaultValue;
    }

    public double getDouble(String name, double defaultValue) {
        name = name.toLowerCase(Locale.ENGLISH);
        String value = prefs.get(name);
        if (value != null) {
            return Double.valueOf(value);
        } else if (preferencesBundleExtras != null) {
            Object bundleValue = preferencesBundleExtras.get(name);
            if (bundleValue instanceof String) {
                return Double.valueOf((String)bundleValue);
            }
            // Gives a nice warning if type is wrong.
            return preferencesBundleExtras.getDouble(name, defaultValue);
        }
        return defaultValue;
    }

    public String getString(String name, String defaultValue) {
        name = name.toLowerCase(Locale.ENGLISH);
        String value = prefs.get(name);
        if (value != null) {
            return value;
        } else if (preferencesBundleExtras != null && !"errorurl".equals(name)) {
            Object bundleValue = preferencesBundleExtras.get(name);
            if (bundleValue != null) {
                return bundleValue.toString();
            }
        }
        return defaultValue;
    }

    // Plugins should not rely on values within the intent since this does not work
    // for apps with multiple webviews. Instead, they should retrieve prefs from the
    // Config object associated with their webview.
    public void copyIntoIntentExtras(Activity action) {
        for (String name : prefs.keySet()) {
            String value = prefs.get(name);
            if (value == null) {
                continue;
            }
            if (name.equals("loglevel")) {
                LOG.setLogLevel(value);
            } else if (name.equals("splashscreen")) {
                // Note: We should probably pass in the classname for the variable splash on splashscreen!
                int resource = action.getResources().getIdentifier(value, "drawable", action.getClass().getPackage().getName());
                action.getIntent().putExtra(name, resource);
            }
            else if(name.equals("backgroundcolor")) {
                int asInt = (int)(long)Long.decode(value);
                action.getIntent().putExtra(name, asInt);
            }
            else if(name.equals("loadurltimeoutvalue")) {
                int asInt = Integer.decode(value);
                action.getIntent().putExtra(name, asInt);
            }
            else if(name.equals("splashscreendelay")) {
                int asInt = Integer.decode(value);
                action.getIntent().putExtra(name, asInt);
            }
            else if(name.equals("keeprunning"))
            {
                boolean asBool = Boolean.parseBoolean(value);
                action.getIntent().putExtra(name, asBool);
            }
            else if(name.equals("inappbrowserstorageenabled"))
            {
                boolean asBool = Boolean.parseBoolean(value);
                action.getIntent().putExtra(name, asBool);
            }
            else if(name.equals("disallowoverscroll"))
            {
                boolean asBool = Boolean.parseBoolean(value);
                action.getIntent().putExtra(name, asBool);
            }
            else
            {
                action.getIntent().putExtra(name, value);
            }
        }
        // In the normal case, the intent extras are null until the first call to putExtra().
        if (preferencesBundleExtras == null) {
            preferencesBundleExtras = action.getIntent().getExtras();
        }
    }
}
