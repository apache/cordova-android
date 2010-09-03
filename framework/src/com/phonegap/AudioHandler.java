package com.phonegap;

import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
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
public class AudioHandler extends Module {
	
	HashMap<String,AudioPlayer> players;	// Audio player object
	WebView mAppView;						// Webview object
	DroidGap mCtx;							// DroidGap object
	
	/**
	 * Constructor.
	 * 
	 * @param view
	 * @param ctx
	 */
	public AudioHandler(WebView view, DroidGap ctx) {
		super(view, ctx);
		this.mAppView = view;
		this.mCtx = ctx;
		this.players = new HashMap<String,AudioPlayer>();
	}

	/**
	 * Stop all audio players and recorders.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
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
		AudioManager audiMgr = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
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
		AudioManager audiMgr = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
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
