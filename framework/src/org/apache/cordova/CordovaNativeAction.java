package org.apache.cordova;

import org.json.JSONArray;

/**
 * Interface that needs to be implemented in order to add new action that can be executed from
 * JavaScript
 */
public interface CordovaNativeAction {

     /**
      * Executes the request for specific action.
      *
      * This method is called from the WebView thread. To do a non-trivial amount of work, use:
      *     cordova.getThreadPool().execute(runnable);
      *
      * To run on the UI thread, use:
      *     cordova.getActivity().runOnUiThread(runnable);
      *
      * @param args         The exec() arguments in JSON form.
      * @param callbackContext The callback context used when calling back into JavaScript.
      * @return                Whether the action was valid.
      */
     boolean execute(JSONArray args, CallbackContext callbackContext);

    /**
     *
     * Method used to match action to implementation
     *
     * @param action - action that needs to be performed
     * @return true if action matches this implementation
     */
     boolean supportsAction(String action);
}

