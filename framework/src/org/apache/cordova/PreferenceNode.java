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
