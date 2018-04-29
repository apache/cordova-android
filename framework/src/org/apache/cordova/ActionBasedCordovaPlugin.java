package org.apache.cordova;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

import java.util.List;

/**
 * Plugin implementation that accepts array of actions that implement desired functionality
 *  and can be executed from JavaScript side.
 *
 * Example:
 * class MyAction implements CordovaNativeAction { ... }
 *
 * class MyPlugin extends ActionBasedCordovaPlugin {
 *     public ActionBasedCordovaPlugin(){
 *         super(new MyAction())
 *     }
 * }
 *
 * @see CordovaNativeAction
 */
public class ActionBasedCordovaPlugin extends CordovaPlugin {

    private final List<CordovaNativeAction> actions;

    public ActionBasedCordovaPlugin(CordovaNativeAction ...actions) {
        this.actions = Arrays.asList(actions);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        for(CordovaNativeAction actionImpl: this.actions){
            if(actionImpl.supportsAction(action)){
                return actionImpl.execute(args, callbackContext);
            }
        }
        return false;
    }
}

