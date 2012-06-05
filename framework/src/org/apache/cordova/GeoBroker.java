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

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/*
 * This class is the interface to the Geolocation.  It's bound to the geo object.
 *
 * This class only starts and stops various GeoListeners, which consist of a GPS and a Network Listener
 */

public class GeoBroker extends Plugin {
    private GPSListener gpsListener;
    private NetworkListener networkListener;
    private LocationManager locationManager;

    /**
     * Constructor.
     */
    public GeoBroker() {
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action 		The action to execute.
     * @param args 			JSONArry of arguments for the plugin.
     * @param callbackId	The callback id used when calling back into JavaScript.
     * @return 				A PluginResult object with a status and message.
     */
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        if (this.locationManager == null) {
            this.locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
            this.networkListener = new NetworkListener(this.locationManager, this);
            this.gpsListener = new GPSListener(this.locationManager, this);
        }
        PluginResult.Status status = PluginResult.Status.NO_RESULT;
        String message = "";
        PluginResult result = new PluginResult(status, message);
        result.setKeepCallback(true);

        try {
            if (action.equals("getLocation")) {
                boolean enableHighAccuracy = args.getBoolean(0);
                int maximumAge = args.getInt(1);
                Location last = this.locationManager.getLastKnownLocation((enableHighAccuracy ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER));
                // Check if we can use lastKnownLocation to get a quick reading and use less battery
                if ((System.currentTimeMillis() - last.getTime()) <= maximumAge) {
                    result = new PluginResult(PluginResult.Status.OK, this.returnLocationJSON(last));
                } else {
                    this.getCurrentLocation(callbackId, enableHighAccuracy);
                }
            }
            else if (action.equals("addWatch")) {
                String id = args.getString(0);
                boolean enableHighAccuracy = args.getBoolean(1);
                this.addWatch(id, callbackId, enableHighAccuracy);
            }
            else if (action.equals("clearWatch")) {
                String id = args.getString(0);
                this.clearWatch(id);
            }
        } catch (JSONException e) {
            result = new PluginResult(PluginResult.Status.JSON_EXCEPTION, e.getMessage());
        }
        return result;
    }

    private void clearWatch(String id) {
        this.gpsListener.clearWatch(id);
        this.networkListener.clearWatch(id);
    }

    private void getCurrentLocation(String callbackId, boolean enableHighAccuracy) {
        if (enableHighAccuracy) {
            this.gpsListener.addCallback(callbackId);
        } else {
            this.networkListener.addCallback(callbackId);
        }
    }

    private void addWatch(String timerId, String callbackId, boolean enableHighAccuracy) {
        if (enableHighAccuracy) {
            this.gpsListener.addWatch(timerId, callbackId);
        } else {
            this.networkListener.addWatch(timerId, callbackId);
        }
    }

    /**
     * Identifies if action to be executed returns a value and should be run synchronously.
     *
     * @param action	The action to execute
     * @return			T=returns value
     */
    public boolean isSynch(String action) {
        // Starting listeners is easier to run on main thread, so don't run async.
        return true;
    }

    /**
     * Called when the activity is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {
        this.networkListener.destroy();
        this.gpsListener.destroy();
        this.networkListener = null;
        this.gpsListener = null;
    }

    public JSONObject returnLocationJSON(Location loc) {
        JSONObject o = new JSONObject();

        try {
            o.put("latitude", loc.getLatitude());
            o.put("longitude", loc.getLongitude());
            o.put("altitude", (loc.hasAltitude() ? loc.getAltitude() : null));
                o.put("accuracy", loc.getAccuracy());
                o.put("heading", (loc.hasBearing() ? (loc.hasSpeed() ? loc.getBearing() : null) : null));
                o.put("speed", loc.getSpeed());
            o.put("timestamp", loc.getTime());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return o;
      }
      public void win(Location loc, String callbackId) {
          PluginResult result = new PluginResult(PluginResult.Status.OK, this.returnLocationJSON(loc));
          this.success(result, callbackId);
      }
      /**
       * Location failed.  Send error back to JavaScript.
       *
       * @param code			The error code
       * @param msg			The error message
       * @throws JSONException
       */
      public void fail(int code, String msg, String callbackId) {
          JSONObject obj = new JSONObject();
          String backup = null;
          try {
              obj.put("code", code);
              obj.put("message", msg);
          } catch (JSONException e) {
              obj = null;
              backup = "{'code':" + code + ",'message':'" + msg.replaceAll("'", "\'") + "'}";
          }
          PluginResult result;
          if (obj != null) {
              result = new PluginResult(PluginResult.Status.ERROR, obj);
          } else {
              result = new PluginResult(PluginResult.Status.ERROR, backup);
          }

          this.error(result, callbackId);
      }
}
