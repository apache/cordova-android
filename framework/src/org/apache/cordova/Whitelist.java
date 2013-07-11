package org.apache.cordova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cordova.LOG;

public class Whitelist {
    private ArrayList<Pattern> whiteList;
    private HashMap<String, Boolean> whiteListCache;

    public static final String TAG = "Whitelist";

	public Whitelist() {
		this.whiteList = new ArrayList<Pattern>();
		this.whiteListCache = new HashMap<String, Boolean>();
	}

    /*
     * Trying to figure out how to match * is a pain
     * So, we don't use a regex here
     */
    
    private boolean originHasWildcard(String origin){
        //First, check for a protocol, then split it if it has one.
        if(origin.contains("//"))
        {
            origin = origin.split("//")[1];
        }
        return origin.startsWith("*");
    }

    public void addWhiteListEntry(String origin, boolean subdomains) {
        try {
            // Unlimited access to network resources
            if (origin.compareTo("*") == 0) {
                LOG.d(TAG, "Unlimited access to network resources");
                whiteList.add(Pattern.compile(".*"));
            }
            else { // specific access
                // check if subdomains should be included
                if(originHasWildcard(origin))
                {
                    subdomains = true;
                    //Remove the wildcard so this works properly
                    origin = origin.replace("*.", "");
                }
                
                // TODO: we should not add more domains if * has already been added
                Pattern schemeRegex = Pattern.compile("^[a-z-]+://");
                Matcher matcher = schemeRegex.matcher(origin);
                if (subdomains) {
                    // Check for http or https protocols
                    if (origin.startsWith("http")) {
                        whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://(.*\\.)?")));
                    }
                    // Check for other protocols
                    else if(matcher.find()){
                        whiteList.add(Pattern.compile("^" + origin.replaceFirst("//", "//(.*\\.)?")));
                    }
                    // XXX making it stupid friendly for people who forget to include protocol/SSL
                    else {
                        whiteList.add(Pattern.compile("^https?://(.*\\.)?" + origin));
                    }
                    LOG.d(TAG, "Origin to allow with subdomains: %s", origin);
                } else {
                    // Check for http or https protocols
                    if (origin.startsWith("http")) {
                        whiteList.add(Pattern.compile(origin.replaceFirst("https?://", "^https?://")));
                    }
                    // Check for other protocols
                    else if(matcher.find()){
                        whiteList.add(Pattern.compile("^" + origin));
                    }
                    // XXX making it stupid friendly for people who forget to include protocol/SSL
                    else {
                        whiteList.add(Pattern.compile("^https?://" + origin));
                    }
                    LOG.d(TAG, "Origin to allow: %s", origin);
                }
            }
        } catch (Exception e) {
            LOG.d(TAG, "Failed to add origin %s", origin);
        }
    }


    /**
     * Determine if URL is in approved list of URLs to load.
     *
     * @param url
     * @return
     */
    public boolean isUrlWhiteListed(String url) {

        // Check to see if we have matched url previously
        if (whiteListCache.get(url) != null) {
            return true;
        }

        // Look for match in white list
        Iterator<Pattern> pit = whiteList.iterator();
        while (pit.hasNext()) {
            Pattern p = pit.next();
            Matcher m = p.matcher(url);

            // If match found, then cache it to speed up subsequent comparisons
            if (m.find()) {
                whiteListCache.put(url, true);
                return true;
            }
        }
        return false;
    }

}
