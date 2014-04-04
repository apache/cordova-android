package org.apache.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

/**
 * Default `PluginFactory` object. This does the same things as `PluginEntry` did before
 * but is much more easily extensible.
 */
public class PluginFactory {

    /** The default factory method.
     *
     * @param service               The name of the service; might need this to decide how to construct the plugin.
     * @param pluginClass           Name of the plugin class
     * @param webView               The `CordovaWebView` object to pass to the plugin's init method
     * @param ctx                   The `CordovaInterface` object to pass to the plugin's init method
     *
     * @return                      The newly created instance of the given plugin. May return null on exception.
     */
    public CordovaPlugin createPlugin(String service, String pluginClass, CordovaWebView webView, CordovaInterface ctx) {

        try {
            @SuppressWarnings("rawtypes")
            Class c = getClassByName(pluginClass);
            if (isCordovaPlugin(c)) {
                CordovaPlugin plugin = (CordovaPlugin) c.newInstance();
                plugin.initialize(ctx, webView);
                return plugin;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error adding plugin " + pluginClass + ".");
        }
        return null;
    }

    /**
     * Returns whether the given class extends CordovaPlugin.
     */
    @SuppressWarnings("rawtypes")
    private boolean isCordovaPlugin(Class c) {
        if (c != null) {
            return org.apache.cordova.CordovaPlugin.class.isAssignableFrom(c);
        }
        return false;
    }

    /**
     * Get the class.
     *
     * @param clazz
     * @return a reference to the named class
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("rawtypes")
    private Class getClassByName(final String clazz) throws ClassNotFoundException {
        Class c = null;
        if ((clazz != null) && !("".equals(clazz))) {
            c = Class.forName(clazz);
        }
        return c;
    }

}
