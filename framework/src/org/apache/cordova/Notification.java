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

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

/**
 * This class provides access to notifications on the device.
 */
public class Notification extends Plugin {
  
  public int confirmResult = -1;
  public ProgressDialog spinnerDialog = null;
  public ProgressDialog progressDialog = null;  
  
  /**
   * Constructor.
   */
  public Notification() {
  }

  /**
   * Executes the request and returns PluginResult.
   * 
   * @param action    The action to execute.
   * @param args      JSONArry of arguments for the plugin.
   * @param callbackId  The callback id used when calling back into JavaScript.
   * @return        A PluginResult object with a status and message.
   */
  public PluginResult execute(String action, JSONArray args, String callbackId) {
    PluginResult.Status status = PluginResult.Status.OK;
    String result = "";   
    
    try {
      if (action.equals("beep")) {
        this.beep(args.getLong(0));
      }
      else if (action.equals("vibrate")) {
        this.vibrate(args.getLong(0));
      }
      else if (action.equals("alert")) {
        this.alert(args.getString(0),args.getString(1),args.getString(2), callbackId);
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
      }
      else if (action.equals("confirm")) {
        this.confirm(args.getString(0),args.getString(1),args.getString(2), callbackId);
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
      }
      else if (action.equals("activityStart")) {
        this.activityStart(args.getString(0),args.getString(1));
      }
      else if (action.equals("activityStop")) {
        this.activityStop();
      }
      else if (action.equals("progressStart")) {
        this.progressStart(args.getString(0),args.getString(1));
      }
      else if (action.equals("progressValue")) {
        this.progressValue(args.getInt(0));
      }
      else if (action.equals("progressStop")) {
        this.progressStop();
      }
      return new PluginResult(status, result);
    } catch (JSONException e) {
      return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
    }
  }

  /**
   * Identifies if action to be executed returns a value and should be run synchronously.
   * 
   * @param action  The action to execute
   * @return      T=returns value
   */
  public boolean isSynch(String action) {
    if (action.equals("alert")) {
      return true;
    }
    else if (action.equals("confirm")) {
      return true;
    }
    else if (action.equals("activityStart")) {
      return true;
    }
    else if (action.equals("activityStop")) {
      return true;
    }
    else if (action.equals("progressStart")) {
      return true;
    }
    else if (action.equals("progressValue")) {
      return true;
    }
    else if (action.equals("progressStop")) {
      return true;
    }
    else {
      return false;
    }
  }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

  /**
   * Beep plays the default notification ringtone.
   * 
   * @param count     Number of times to play notification
   */
  public void beep(long count) {
    Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    Ringtone notification = RingtoneManager.getRingtone(this.ctx.getContext(), ringtone);
    
    // If phone is not set to silent mode
    if (notification != null) {
      for (long i = 0; i < count; ++i) {
        notification.play();
        long timeout = 5000;
        while (notification.isPlaying() && (timeout > 0)) {
          timeout = timeout - 100;
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
          }
        }
      }
    }
  }
  
  /**
   * Vibrates the device for the specified amount of time.
   * 
   * @param time      Time to vibrate in ms.
   */
  public void vibrate(long time){
        // Start the vibration, 0 defaults to half a second.
    if (time == 0) {
      time = 500;
    }
        Vibrator vibrator = (Vibrator) this.ctx.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
  }
  
  /**
   * Builds and shows a native Android alert with given Strings
   * @param message     The message the alert should display
   * @param title     The title of the alert
   * @param buttonLabel   The label of the button 
   * @param callbackId  The callback id
   */
  public synchronized void alert(final String message, final String title, final String buttonLabel, final String callbackId) {

    final CordovaInterface ctx = this.ctx;
    final Notification notification = this;
    
    Runnable runnable = new Runnable() {
      public void run() {
    
        AlertDialog.Builder dlg = new AlertDialog.Builder(ctx.getContext());
        dlg.setMessage(message);
        dlg.setTitle(title);
        dlg.setCancelable(false);
        dlg.setPositiveButton(buttonLabel,
            new AlertDialog.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            notification.success(new PluginResult(PluginResult.Status.OK, 0), callbackId);
          }
        });
        dlg.create();
        dlg.show();
      };
    };
    this.ctx.runOnUiThread(runnable);
  }

  /**
   * Builds and shows a native Android confirm dialog with given title, message, buttons.
   * This dialog only shows up to 3 buttons.  Any labels after that will be ignored.
   * The index of the button pressed will be returned to the JavaScript callback identified by callbackId.
   * 
   * @param message     The message the dialog should display
   * @param title     The title of the dialog
   * @param buttonLabels  A comma separated list of button labels (Up to 3 buttons)
   * @param callbackId  The callback id
   */
  public synchronized void confirm(final String message, final String title, String buttonLabels, final String callbackId) {

    final CordovaInterface ctx = this.ctx;
    final Notification notification = this;
    final String[] fButtons = buttonLabels.split(",");

    Runnable runnable = new Runnable() {
      public void run() {
        AlertDialog.Builder dlg = new AlertDialog.Builder(ctx.getContext());
        dlg.setMessage(message);
        dlg.setTitle(title);
        dlg.setCancelable(false);

        // First button
        if (fButtons.length > 0) {
          dlg.setPositiveButton(fButtons[0],
              new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              notification.success(new PluginResult(PluginResult.Status.OK, 1), callbackId);
            }
          });
        }

        // Second button
        if (fButtons.length > 1) {
          dlg.setNeutralButton(fButtons[1], 
              new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              notification.success(new PluginResult(PluginResult.Status.OK, 2), callbackId);
            }
          });
        }

        // Third button
        if (fButtons.length > 2) {
          dlg.setNegativeButton(fButtons[2],
              new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              notification.success(new PluginResult(PluginResult.Status.OK, 3), callbackId);
            }
          }
          );
        }

        dlg.create();
        dlg.show();
      };
    };
    this.ctx.runOnUiThread(runnable);
  }

  /**
   * Show the spinner.
   * 
   * @param title     Title of the dialog
   * @param message   The message of the dialog
   */
  public synchronized void activityStart(final String title, final String message) {
    if (this.spinnerDialog != null) {
      this.spinnerDialog.dismiss();
      this.spinnerDialog = null;
    }
    final Notification notification = this;
    final CordovaInterface ctx = this.ctx;
    Runnable runnable = new Runnable() {
      public void run() {
        notification.spinnerDialog = ProgressDialog.show(ctx.getContext(), title , message, true, true, 
          new DialogInterface.OnCancelListener() { 
            public void onCancel(DialogInterface dialog) {
              notification.spinnerDialog = null;
            }
          });
        }
      };
    this.ctx.runOnUiThread(runnable);
  }
  
  /**
   * Stop spinner.
   */
  public synchronized void activityStop() {
    if (this.spinnerDialog != null) {
      this.spinnerDialog.dismiss();
      this.spinnerDialog = null;
    }
  }

  /**
   * Show the progress dialog.
   * 
   * @param title     Title of the dialog
   * @param message   The message of the dialog
   */
  public synchronized void progressStart(final String title, final String message) {
    if (this.progressDialog != null) {
      this.progressDialog.dismiss();
      this.progressDialog = null;
    }
    final Notification notification = this;
    final CordovaInterface ctx = this.ctx;
    Runnable runnable = new Runnable() {
      public void run() {
        notification.progressDialog = new ProgressDialog(ctx.getContext());
        notification.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        notification.progressDialog.setTitle(title);
        notification.progressDialog.setMessage(message);
        notification.progressDialog.setCancelable(true);
        notification.progressDialog.setMax(100);
        notification.progressDialog.setProgress(0);
        notification.progressDialog.setOnCancelListener(
          new DialogInterface.OnCancelListener() { 
            public void onCancel(DialogInterface dialog) {
              notification.progressDialog = null;
            }
          });
        notification.progressDialog.show();
      }
    };
    this.ctx.runOnUiThread(runnable);
  }
  
  /**
   * Set value of progress bar.
   * 
   * @param value     0-100
   */
  public synchronized void progressValue(int value) {
    if (this.progressDialog != null) {
      this.progressDialog.setProgress(value);
    }   
  }
  
  /**
   * Stop progress dialog.
   */
  public synchronized void progressStop() {
    if (this.progressDialog != null) {
      this.progressDialog.dismiss();
      this.progressDialog = null;
    }
  }

}
