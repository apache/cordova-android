package com.nitobi.phonegap;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;

public class CameraListener implements ShutterCallback{

	private Camera mCam;
	private CameraHandler camHand;
	private SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");  
	private Context mCtx;
	private Uri target = Media.EXTERNAL_CONTENT_URI;
	
	CameraListener(Context ctx){
		mCam = Camera.open();
		mCtx = ctx;
	}
	
	public void snap()
	{
		String filename = timeStampFormat.format(new Date());
		ContentValues values = new ContentValues();
		values.put(Media.TITLE, filename);
		values.put(Media.DESCRIPTION, "PhoneGap");
		Uri uri = mCtx.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		try
		{
			OutputStream output = (OutputStream) mCtx.getContentResolver().openOutputStream(uri);
			camHand = new CameraHandler(output);
			mCam.takePicture(this, null, camHand);
		}
		catch (Exception ex)
		{
			/*TODO:  Do some logging here */
		}
	}
	
	public void onShutter() {
		/* This is logged */
		
	}

}
