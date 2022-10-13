package org.apache.cordova;

import android.content.Context;

import java.util.ArrayList;

public interface IConfigParser {
    String TAG = IConfigParser.class.getSimpleName();
    String SCHEME_HTTP = "http";
    String SCHEME_HTTPS = "https";
    String DEFAULT_HOSTNAME = "localhost";

    CordovaPreferences getPreferences();

    ArrayList<PluginEntry> getPluginEntries();

    String getLaunchUrl();

    void parse(Context action);

}
