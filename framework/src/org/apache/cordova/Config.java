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

import java.util.List;

import android.app.Activity;
import android.util.Log;

@Deprecated // Use Whitelist, CordovaPrefences, etc. directly.
public class Config {
    private static final String TAG = "Config";

    static ConfigXmlParser parser;

    private Config() {
    }

    public static void init(Activity action) {
        parser = new ConfigXmlParser();
        parser.parse(action);
        parser.getPreferences().setPreferencesBundle(action.getIntent().getExtras());
        parser.getPreferences().copyIntoIntentExtras(action);
    }

    // Intended to be used for testing only; creates an empty configuration.
    public static void init() {
        if (parser == null) {
            parser = new ConfigXmlParser();
        }
    }
    
    /**
     * Add entry to approved list of URLs (whitelist)
     *
     * @param origin        URL regular expression to allow
     * @param subdomains    T=include all subdomains under origin
     */
    public static void addWhiteListEntry(String origin, boolean subdomains) {
        if (parser == null) {
            Log.e(TAG, "Config was not initialised. Did you forget to Config.init(this)?");
            return;
        }
        parser.getInternalWhitelist().addWhiteListEntry(origin, subdomains);
    }

    /**
     * Determine if URL is in approved list of URLs to load.
     *
     * @param url
     * @return true if whitelisted
     */
    public static boolean isUrlWhiteListed(String url) {
        if (parser == null) {
            Log.e(TAG, "Config was not initialised. Did you forget to Config.init(this)?");
            return false;
        }
        return parser.getInternalWhitelist().isUrlWhiteListed(url);
    }

    /**
     * Determine if URL is in approved list of URLs to launch external applications.
     *
     * @param url
     * @return true if whitelisted
     */
    public static boolean isUrlExternallyWhiteListed(String url) {
        if (parser == null) {
            Log.e(TAG, "Config was not initialised. Did you forget to Config.init(this)?");
            return false;
        }
        return parser.getExternalWhitelist().isUrlWhiteListed(url);
    }

    public static String getStartUrl() {
        if (parser == null) {
            return "file:///android_asset/www/index.html";
        }
        return parser.getLaunchUrl();
    }

    public static String getErrorUrl() {
        return parser.getPreferences().getString("errorurl", null);
    }

    public static Whitelist getWhitelist() {
        return parser.getInternalWhitelist();
    }

    public static Whitelist getExternalWhitelist() {
        return parser.getExternalWhitelist();
    }

    public static List<PluginEntry> getPluginEntries() {
        return parser.getPluginEntries();
    }
    
    public static CordovaPreferences getPreferences() {
        return parser.getPreferences();
    }

    public static boolean isInitialized() {
        return parser != null;
    }
}
