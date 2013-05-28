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
package org.apache.cordova.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/*
 * Some context information associated with a DataRequest.
 */
public class DataResourceContext {
    // A random id that is unique for a particular request.
    private int requestId;
    // A tag associated with the source of this dataResourceContext
    private String source;
    // If needed, any data associated with core plugins can be a part of the context object
    // If needed, any data associated with non core plugins  should store data in a Map so as to not clutter the context object
    private Map<String, Object> dataMap;
    public DataResourceContext(String source) {
        this.requestId = new Random().nextInt();
        this.source = source;
        this.dataMap = new HashMap<String, Object>();
    }
    public int getRequestId() {
        return requestId;
    }
    public String getSource() {
        return source;
    }
    public Map<String, Object> getDataMap() {
        return dataMap;
    }
}
