package com.nitobi.phonegap;

import java.io.OutputStream;
import java.text.SimpleDateFormat;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CameraHandler implements PictureCallback{
	
	
	private OutputStream oStream;
	BitmapFactory photoLab;

	CameraHandler(OutputStream output)
	{
		oStream = output;
	}
	
	public void onPictureTaken(byte[] graphic, Camera arg1) {	
		try {
			oStream.write(graphic);
			oStream.flush();
			oStream.close();
		}
		catch (Exception ex)
		{
			//TO-DO: Put some logging here saying that this epic failed
		}
		
		// Do some other things, like post it to a service!
	}
	
	
}
