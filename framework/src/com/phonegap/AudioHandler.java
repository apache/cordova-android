package com.phonegap;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginManager;
import com.phonegap.api.PluginResult;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.webkit.WebView;

/**
 * This class called by DroidGap to play and record audio.  
 * The file can be local or over a network using http.
 * 
 * Audio formats supported (tested):
 * 	.mp3, .wav
 * 
 * Local audio files must reside in one of two places:
 * 		android_asset: 		file name must start with /android_asset/sound.mp3
 * 		sdcard:				file name is just sound.mp3
 */
public class AudioHandler implements Plugin {

    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object

	HashMap<String,AudioPlayer> players;	// Audio player object
	
	/**
	 * Constructor.
	 */
	public AudioHandler() {
		this.players = new HashMap<String,AudioPlayer>();
	}

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(DroidGap ctx) {
		this.ctx = ctx;
	}

	/**
	 * Sets the main View of the application, this is the WebView within which 
	 * a PhoneGap app runs.
	 * 
	 * @param webView The PhoneGap WebView
	 */
	public void setView(WebView webView) {
		this.webView = webView;
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
			if (action.equals("startRecordingAudio")) {
				this.startRecordingAudio(args.getString(0), args.getString(1));
			}
			else if (action.equals("stopRecordingAudio")) {
				this.stopRecordingAudio(args.getString(0));
			}
			else if (action.equals("startPlayingAudio")) {
				this.startPlayingAudio(args.getString(0), args.getString(1));
			}
			else if (action.equals("pausePlayingAudio")) {
				this.pausePlayingAudio(args.getString(0));
			}
			else if (action.equals("stopPlayingAudio")) {
				this.stopPlayingAudio(args.getString(0));
			}
			else if (action.equals("getCurrentPositionAudio")) {
				long l = this.getCurrentPositionAudio(args.getString(0));
				return new PluginResult(status, l);
			}
			else if (action.equals("getDurationAudio")) {
				long l = this.getDurationAudio(args.getString(0), args.getString(1));
				return new PluginResult(status, l);
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			e.printStackTrace();
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
		if (action.equals("getCurrentPositionAudio")) {
			return true;
		}
		else if (action.equals("getDurationAudio")) {
			return true;
		}
		return false;
	}

	/**
     * Called when the system is about to start resuming a previous activity. 
     */
    public void onPause() {
    }

    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    }

	/**
	 * Stop all audio players and recorders.
	 */
	public void onDestroy() {
		java.util.Set<Entry<String,AudioPlayer>> s = this.players.entrySet();
        java.util.Iterator<Entry<String,AudioPlayer>> it = s.iterator();
        while(it.hasNext()) {
            Entry<String,AudioPlayer> entry = it.next();
            AudioPlayer audio = entry.getValue();
            audio.destroy();
		}
        this.players.clear();
	}

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	/**
	 * Start recording and save the specified file.
	 * 
	 * @param id				The id of the audio player
	 * @param file				The name of the file
	 */
    public void startRecordingAudio(String id, String file) {
    	// If already recording, then just return;
    	if (this.players.containsKey(id)) {
    		return;
    	}
    	AudioPlayer audio = new AudioPlayer(this, id);
    	this.players.put(id, audio);
    	audio.startRecording(file);
    }

    /**
     * Stop recording and save to the file specified when recording started.
     * 
	 * @param id				The id of the audio player
     */
    public void stopRecordingAudio(String id) {
    	AudioPlayer audio = this.players.get(id);
    	if (audio != null) {
    		audio.stopRecording();
    		this.players.remove(id);
    	}
    }
    
    /**
     * Start or resume playing audio file.
     * 
	 * @param id				The id of the audio player
     * @param file				The name of the audio file.
     */
    public void startPlayingAudio(String id, String file) {
    	AudioPlayer audio = this.players.get(id);
    	if (audio == null) {
    		audio = new AudioPlayer(this, id);
    		this.players.put(id, audio);
    	}
    	audio.startPlaying(file);
    }

    /**
     * Pause playing.
     * 
	 * @param id				The id of the audio player
     */
    public void pausePlayingAudio(String id) {
    	AudioPlayer audio = this.players.get(id);
    	if (audio != null) {
    		audio.pausePlaying();
    	}
    }

    /**
     * Stop playing the audio file.
     * 
	 * @param id				The id of the audio player
     */
    public void stopPlayingAudio(String id) {
    	AudioPlayer audio = this.players.get(id);
    	if (audio != null) {
    		audio.stopPlaying();
    		//audio.destroy();
    		//this.players.remove(id);
    	}
    }
    
    /**
     * Get current position of playback.
     * 
	 * @param id				The id of the audio player
     * @return 					position in msec
     */
    public long getCurrentPositionAudio(String id) {
    	AudioPlayer audio = this.players.get(id);
    	if (audio != null) {
    		return(audio.getCurrentPosition());
    	}
    	return -1;
    }
    
    /**
     * Get the duration of the audio file.
     * 
	 * @param id				The id of the audio player
     * @param file				The name of the audio file.
     * @return					The duration in msec.
     */
    public long getDurationAudio(String id, String file) {
    	
    	// Get audio file
    	AudioPlayer audio = this.players.get(id);
    	if (audio != null) {
    		return(audio.getDuration(file));
    	}
    	
    	// If not already open, then open the file
    	else {
    		audio = new AudioPlayer(this, id);
    		this.players.put(id, audio);
    		return(audio.getDuration(file));
    	}
    }  
    
    /**
     * Set the audio device to be used for playback.
     * 
     * @param output			1=earpiece, 2=speaker
     */
    public void setAudioOutputDevice(int output) {
		AudioManager audiMgr = (AudioManager) this.ctx.getSystemService(Context.AUDIO_SERVICE);
		if (output == 2) {
			audiMgr.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_SPEAKER, AudioManager.ROUTE_ALL);
		}
		else if (output == 1) {
			audiMgr.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
		}
		else {
			System.out.println("AudioHandler.setAudioOutputDevice() Error: Unknown output device.");
		}
    }
    
    /**
     * Get the audio device to be used for playback.
     * 
     * @return					1=earpiece, 2=speaker
     */
    public int getAudioOutputDevice() {
		AudioManager audiMgr = (AudioManager) this.ctx.getSystemService(Context.AUDIO_SERVICE);
		if (audiMgr.getRouting(AudioManager.MODE_NORMAL) == AudioManager.ROUTE_EARPIECE) {
			return 1;
		}
		else if (audiMgr.getRouting(AudioManager.MODE_NORMAL) == AudioManager.ROUTE_SPEAKER) {
			return 2;
		}
		else {
			return -1;
		}
    }       
}
