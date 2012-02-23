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

import java.util.HashSet;

import org.apache.cordova.PreferenceNode;


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
