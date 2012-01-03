package com.phonegap;

import java.util.HashSet;

import com.phonegap.PreferenceNode;

public class PreferenceSet {
    private HashSet<PreferenceNode> innerSet;

    public PreferenceSet() {
        this.innerSet = new HashSet<PreferenceNode>();
    }

    public void add(PreferenceNode node) {
        this.innerSet.add(node);
    }

    public int size() {
        return this.innerSet.size();
    }

    public void clear() {
        this.innerSet.clear();
    }

    public String pref(String prefName) {
        for (PreferenceNode n : innerSet)
            if (prefName.equals(n.name))
                return n.value;

        return null;
    }

    public boolean prefMatches(String prefName, String prefValue) {
        String value = pref(prefName);

        if (value == null) {
            return false;
        } else {
            return value.equals(prefValue);
        }
    }
}
