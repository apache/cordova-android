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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.LOG;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class ConfigXmlParser {
    private static String TAG = "ConfigXmlParser";

    private String launchUrl = "file:///android_asset/www/index.html";
    private CordovaPreferences prefs = new CordovaPreferences();
    private Whitelist internalWhitelist = new Whitelist();
    private Whitelist externalWhitelist = new Whitelist();
    private ArrayList<PluginEntry> pluginEntries = new ArrayList<PluginEntry>(20);

    public Whitelist getInternalWhitelist() {
        return internalWhitelist;
    }

    public Whitelist getExternalWhitelist() {
        return externalWhitelist;
    }

    public CordovaPreferences getPreferences() {
        return prefs;
    }

    public ArrayList<PluginEntry> getPluginEntries() {
        return pluginEntries;
    }

    public String getLaunchUrl() {
        return launchUrl;
    }
    
    public void parse(Activity action) {
        // First checking the class namespace for config.xml
        int id = action.getResources().getIdentifier("config", "xml", action.getClass().getPackage().getName());
        if (id == 0) {
            // If we couldn't find config.xml there, we'll look in the namespace from AndroidManifest.xml
            id = action.getResources().getIdentifier("config", "xml", action.getPackageName());
            if (id == 0) {
                LOG.e(TAG, "res/xml/config.xml is missing!");
                return;
            }
        }
        parse(action.getResources().getXml(id));
    }

    public void parse(XmlResourceParser xml) {
        int eventType = -1;
        String service = "", pluginClass = "", paramType = "";
        boolean onload = false;
        boolean insideFeature = false;
        ArrayList<String> urlMap = null;

        // Add implicitly allowed URLs
        internalWhitelist.addWhiteListEntry("file:///*", false);
        internalWhitelist.addWhiteListEntry("content:///*", false);
        internalWhitelist.addWhiteListEntry("data:*", false);

        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals("url-filter")) {
                    Log.w(TAG, "Plugin " + service + " is using deprecated tag <url-filter>");
                    if (urlMap == null) {
                        urlMap = new ArrayList<String>(2);
                    }
                    urlMap.add(xml.getAttributeValue(null, "value"));
                } else if (strNode.equals("feature")) {
                    //Check for supported feature sets  aka. plugins (Accelerometer, Geolocation, etc)
                    //Set the bit for reading params
                    insideFeature = true;
                    service = xml.getAttributeValue(null, "name");
                }
                else if (insideFeature && strNode.equals("param")) {
                    paramType = xml.getAttributeValue(null, "name");
                    if (paramType.equals("service")) // check if it is using the older service param
                        service = xml.getAttributeValue(null, "value");
                    else if (paramType.equals("package") || paramType.equals("android-package"))
                        pluginClass = xml.getAttributeValue(null,"value");
                    else if (paramType.equals("onload"))
                        onload = "true".equals(xml.getAttributeValue(null, "value"));
                }
                else if (strNode.equals("access")) {
                    String origin = xml.getAttributeValue(null, "origin");
                    String subdomains = xml.getAttributeValue(null, "subdomains");
                    boolean external = (xml.getAttributeValue(null, "launch-external") != null);
                    if (origin != null) {
                        if (external) {
                            externalWhitelist.addWhiteListEntry(origin, (subdomains != null) && (subdomains.compareToIgnoreCase("true") == 0));
                        } else {
                            if ("*".equals(origin)) {
                                // Special-case * origin to mean http and https when used for internal
                                // whitelist. This prevents external urls like sms: and geo: from being
                                // handled internally.
                                internalWhitelist.addWhiteListEntry("http://*/*", false);
                                internalWhitelist.addWhiteListEntry("https://*/*", false);
                            } else {
                                internalWhitelist.addWhiteListEntry(origin, (subdomains != null) && (subdomains.compareToIgnoreCase("true") == 0));
                            }
                        }
                    }
                }
                else if (strNode.equals("preference")) {
                    String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.ENGLISH);
                    String value = xml.getAttributeValue(null, "value");
                    prefs.set(name, value);
                }
                else if (strNode.equals("content")) {
                    String src = xml.getAttributeValue(null, "src");
                    if (src != null) {
                        setStartUrl(src);
                    }
                }
            }
            else if (eventType == XmlResourceParser.END_TAG)
            {
                String strNode = xml.getName();
                if (strNode.equals("feature")) {
                    pluginEntries.add(new PluginEntry(service, pluginClass, onload, urlMap));

                    service = "";
                    pluginClass = "";
                    insideFeature = false;
                    onload = false;
                    urlMap = null;
                }
            }
            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setStartUrl(String src) {
        Pattern schemeRegex = Pattern.compile("^[a-z-]+://");
        Matcher matcher = schemeRegex.matcher(src);
        if (matcher.find()) {
            launchUrl = src;
        } else {
            if (src.charAt(0) == '/') {
                src = src.substring(1);
            }
            launchUrl = "file:///android_asset/www/" + src;
        }
    }
}
