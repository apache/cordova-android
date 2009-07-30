package com.phonegap.demo;


import java.io.ByteArrayOutputStream;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.webkit.WebView;
import org.apache.commons.codec.binary.Base64;

public class CameraLauncher {
		
	private WebView mAppView;
	private DroidGap mGap;
	int quality;	
	
	CameraLauncher(WebView view, DroidGap gap)
	{
		mAppView = view;
		mGap = gap;
	}
	
	public void takePicture(int quality)
	{
		mGap.startCamera();
	}
	
	/* Return Base64 Encoded String to Javascript */
	public void processPicture( byte[] data )
	{
		ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
		Bitmap myMap = BitmapFactory.decodeByteArray(data, 0, data.length);
		if (myMap.compress(CompressFormat.JPEG, quality, jpeg_data))
		{
			byte[] code  = jpeg_data.toByteArray();
			byte[] output = Base64.encodeBase64(code);
			String js_out = output.toString();
			mAppView.loadUrl("javascript:Camera.win('" + js_out + "');");
		}
		else
		{
			mAppView.loadUrl("javascript:Camera.fail();");
		}		
	}
	
}
