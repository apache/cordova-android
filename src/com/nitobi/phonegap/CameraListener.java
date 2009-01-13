package com.nitobi.phonegap;
/* License (MIT)
 * Copyright (c) 2008 Nitobi
 * website: http://phonegap.com
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
