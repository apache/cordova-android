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
