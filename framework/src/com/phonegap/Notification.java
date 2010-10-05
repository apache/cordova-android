package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

/**
 * This class provides access to notifications on the device.
 */
public class Notification extends Plugin {
		
	/**
	 * Constructor.
	 */
	public Notification() {
	}

	/**
	 * Executes the request and returns CommandResult.
	 * 
	 * @param action The command to execute.
	 * @param args JSONArry of arguments for the command.
	 * @return A CommandResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("beep")) {
				this.beep(args.getLong(0));
			}
			else if (action.equals("vibrate")) {
				this.vibrate(args.getLong(0));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		return false;
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	/**
	 * Beep plays the default notification ringtone.
	 * 
	 * @param count			Number of times to play notification
	 */
	public void beep(long count) {
		Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone notification = RingtoneManager.getRingtone(this.ctx, ringtone);
		
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
	 * @param time			Time to vibrate in ms.
	 */
	public void vibrate(long time){
        // Start the vibration, 0 defaults to half a second.
		if (time == 0) {
			time = 500;
		}
        Vibrator vibrator = (Vibrator) this.ctx.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(time);
	}

}
