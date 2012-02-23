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

// represents the <preference> element from the W3C config.xml spec
// see http://www.w3.org/TR/widgets/#the-preference-element-and-its-attributes
public class PreferenceNode {
    public String name;
    public String value;
    public boolean readonly;

    // constructor
    public PreferenceNode(String name, String value, boolean readonly) {
        this.name = name;
        this.value = value;
        this.readonly = readonly;
    }
}
