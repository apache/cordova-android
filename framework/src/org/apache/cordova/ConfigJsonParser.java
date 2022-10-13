package org.apache.cordova;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigJsonParser implements IConfigParser {
    private final CordovaPreferences prefs = new CordovaPreferences();
    private final ArrayList<PluginEntry> pluginEntries = new ArrayList<PluginEntry>(20);
    private String launchUrl;
    private String contentSrc;

    @Override
    public CordovaPreferences getPreferences() {
        return prefs;
    }

    @Override
    public ArrayList<PluginEntry> getPluginEntries() {
        return pluginEntries;
    }

    @Override
    public String getLaunchUrl() {
        if (launchUrl == null) {
            setStartUrl(contentSrc);
        }

        return launchUrl;
    }

    @Override
    public void parse(Context action) {
        String configContent = null;

        try (InputStream configFileInputStream = action.getAssets().open("config.json")) {
            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int length = -1;
            while ((length = configFileInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            configContent = byteArrayOutputStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(configContent)) {
            throw new IllegalStateException("no config text is detected");
        }

        pluginEntries.add(new PluginEntry(AllowListPlugin.PLUGIN_NAME, "org.apache.cordova.AllowListPlugin", true));

        pluginEntries.add(new PluginEntry(SplashScreenPlugin.PLUGIN_NAME, "org.apache.cordova.SplashScreenPlugin", true));

        try {
            parseJSONText(configContent);
        } catch (JSONException e) {
            throw new RuntimeException("parsing config.json error: " + e);
        }
    }

    private void parseJSONText(String text) throws JSONException {
        JSONObject config = new JSONObject(text);

        // parse preferences
        if (config.has("preferences")) {
            JSONArray preferences = config.getJSONArray("preferences");

            for (int i = 0; i < preferences.length(); i++) {
                Object item = preferences.get(i);
                if (item instanceof JSONObject) {
                    prefs.set(((JSONObject) item).getString("name"), ((JSONObject) item).getString("value"));
                }
            }
        }

        // parse content
        if (config.has("content")) {
            JSONObject content = config.getJSONObject("content");
            contentSrc = content.getString("src");
            // default case
            if (TextUtils.isEmpty(contentSrc)) {
                contentSrc = "index.html";
            }
        }

        // parse features
        if (config.has("features")) {
            JSONArray features = config.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                String service = feature.getString("service");
                JSONObject param = feature.getJSONObject("param");
                String pluginClass = param.getString("android-package");
                boolean onload = false;
                if (param.has("onload")) {
                    onload = param.getBoolean("onload");
                }
                pluginEntries.add(new PluginEntry(service, pluginClass, onload));
            }
        }
    }

    private void setStartUrl(String src) {
        Pattern schemeRegex = Pattern.compile("^[a-z-]+://");
        Matcher matcher = schemeRegex.matcher(src);

        if (matcher.find()) {
            launchUrl = src;
        } else {
            String launchUrlPrefix = getLaunchUrlPrefix();

            // remove leading slash, "/", from content src if existing,
            if (src.charAt(0) == '/') {
                src = src.substring(1);
            }

            launchUrl = launchUrlPrefix + src;
        }
    }

    private String getLaunchUrlPrefix() {
        if (prefs.getBoolean("AndroidInsecureFileModeEnabled", false)) {
            return "file:///android_asset/www/";
        } else {
            String scheme = prefs.getString("scheme", SCHEME_HTTPS).toLowerCase();
            String hostname = prefs.getString("hostname", DEFAULT_HOSTNAME).toLowerCase();

            if (!scheme.contentEquals(SCHEME_HTTP) && !scheme.contentEquals(SCHEME_HTTPS)) {
                LOG.d(TAG, "The provided scheme \"" + scheme + "\" is not valid. " + "Defaulting to \"" + SCHEME_HTTPS + "\". " + "(Valid Options=" + SCHEME_HTTP + "," + SCHEME_HTTPS + ")");

                scheme = SCHEME_HTTPS;
            }

            return scheme + "://" + hostname + '/';
        }
    }
}
