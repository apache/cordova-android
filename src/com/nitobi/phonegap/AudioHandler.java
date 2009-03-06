package com.nitobi.phonegap;

import java.io.File;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

public class AudioHandler implements OnCompletionListener, OnPreparedListener, OnErrorListener {
	private MediaRecorder recorder;
	private boolean isRecording = false;
	MediaPlayer mPlayer;
	private boolean isPlaying = false;
	private String recording;
	private String saveFile;
	private Context mCtx;
	
	public AudioHandler(String file, Context ctx) {
		this.recording = file;
		this.mCtx = ctx;
	}
	
	public void startRecording(String file){
		if (!isRecording){
			saveFile=file;
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(this.recording);
			recorder.prepare();
			isRecording = true;
			recorder.start();
		}
	}
	
	private void moveFile(String file) {
		/* this is a hack to save the file as the specified name */
		File f = new File (this.recording);
		f.renameTo(new File("/sdcard" + file));
	}
	
	public void stopRecording(){
		try{
			if((recorder != null)&&(isRecording))
			{
				isRecording = false;
				recorder.stop();
		        recorder.release(); 
			}
			moveFile(saveFile);
		}catch (Exception e){e.printStackTrace();}
	}	
	
	public void startPlaying(String file) {
		if (isPlaying==false) {
			try {
				mPlayer = new MediaPlayer();
				isPlaying=true;
				Log.d("Audio startPlaying", "audio: " + file);
				if (isStreaming(file))
				{
					Log.d("AudioStartPlaying", "Streaming");
					// Streaming prepare async
					mPlayer.setDataSource(file);
					mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  
					mPlayer.prepareAsync();
				} else {
					Log.d("AudioStartPlaying", "File");
					// Not streaming prepare synchronous, abstract base directory
					mPlayer.setDataSource("/sdcard/" + file);
					mPlayer.prepare();
				}
				mPlayer.setOnPreparedListener(this);
			} catch (Exception e) { e.printStackTrace(); }
		}
	} 

	public void stopPlaying() {
		if (isPlaying) {
			mPlayer.stop();
			mPlayer.release();
			isPlaying=false;
		}
	}
	
	public void onCompletion(MediaPlayer mPlayer) {
		mPlayer.stop();
		mPlayer.release();
		isPlaying=false;
    } 
	
	public long getCurrentPosition() {
		if (isPlaying) 
		{
			return(mPlayer.getCurrentPosition());
		} else { return(-1); }
	}
	
	private boolean isStreaming(String file) 
	{
		if (file.contains("http://")) {
			return true;
		} else {
			return false;
		}
	}
	
	public long getDuration(String file) {
		long duration = -2;
		if (!isPlaying & !isStreaming(file)) {
			try {
				mPlayer = new MediaPlayer();
				mPlayer.setDataSource("/sdcard/" + file);
				mPlayer.prepare();
				duration = mPlayer.getDuration();
				mPlayer.release();
			} catch (Exception e) { e.printStackTrace(); return(-3); }
		} else
		if (isPlaying & !isStreaming(file)) {
			duration = mPlayer.getDuration();
		} else 
		if (isPlaying & isStreaming(file)) {
			try {
				duration = mPlayer.getDuration();
			} catch (Exception e) { e.printStackTrace(); return(-4); }
		}else { return -1; }
		return duration;
	}

	@Override
	public void onPrepared(MediaPlayer mPlayer) {
		if (isPlaying) {
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener()
			{
				@Override
				public void onBufferingUpdate(MediaPlayer mPlayer, int percent)
				{
					/* TODO: call back, e.g. update outer progress bar */
					Log.d("AudioOnBufferingUpdate", "percent: " + percent); 
				}
			});
			mPlayer.start();
		}
	}

	@Override
	public boolean onError(MediaPlayer mPlayer, int arg1, int arg2) {
		Log.e("AUDIO onError", "error " + arg1 + " " + arg2);
		return false;
	}
	
	protected void setAudioOutputDevice(String output){
		System.out.println ("Change audio setting to be "+output);
		AudioManager audiMgr = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
		if (output.contains("Speaker"))
			audiMgr.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_SPEAKER, AudioManager.ROUTE_ALL);
		else if (output.contains("Earpiece")){
			audiMgr.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
		}else
			System.out.println("input error");
			
	}
	protected int getAudioOutputDevice(){
		AudioManager audiMgr = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
		if (audiMgr.getRouting(AudioManager.MODE_NORMAL) == AudioManager.ROUTE_EARPIECE)
			return 1;
		else if (audiMgr.getRouting(AudioManager.MODE_NORMAL) == AudioManager.ROUTE_SPEAKER)
			return 2;
		else
			return -1;
	}
}
