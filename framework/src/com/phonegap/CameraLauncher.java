package com.phonegap;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.WebView;

/**
 * This class launches the camera view, allows the user to take a picture, closes the camera view,
 * and returns the captured image.  When the camera view is closed, the screen displayed before 
 * the camera view was shown is redisplayed.
 */
public class CameraLauncher {
		
	private WebView mAppView;			// Webview object
	private DroidGap mGap;				// DroidGap object
	private int mQuality;				// Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
	private Uri imageUri;				// Uri of captured image 
	
    /**
     * Constructor.
     * 
     * @param view
     * @param gap
     */
	CameraLauncher(WebView view, DroidGap gap) {
		mAppView = view;
		mGap = gap;
	}
		
	/**
	 * Take a picture with the camera.
	 * When an image is captured or the camera view is cancelled, the result is returned
	 * in DroidGap.onActivityResult, which forwards the result to this.onActivityResult.
	 * 
	 * @param quality			Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
	 */
	public void takePicture(int quality) {
		this.mQuality = quality;
		
		// Display camera
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        this.imageUri = Uri.fromFile(photo);
        mGap.startActivityForResult(intent, DroidGap.CAMERA_ACTIVIY_RESULT);
	}

    /**
     * Called when the camera view exits. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		// If image available
		if (resultCode == Activity.RESULT_OK) {
			Uri selectedImage = this.imageUri;
			mGap.getContentResolver().notifyChange(selectedImage, null);
	        ContentResolver cr = mGap.getContentResolver();
	        Bitmap bitmap;
	        try {
	        	bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
	            this.processPicture(bitmap);
	        } catch (Exception e) {
	        	this.failPicture("Error capturing image.");
	        }
		}
		
		// If cancelled
		else if (resultCode == Activity.RESULT_CANCELED) {
			this.failPicture("Camera cancelled.");
		}
		
		// If something else
	    else {
	    	this.failPicture("Did not complete!");
	    }
	}
	
	/**
	 * Compress bitmap using jpeg, convert to Base64 encoded string, and return to JavaScript.
	 *
	 * @param bitmap
	 */
	public void processPicture(Bitmap bitmap) {		
		ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
		try {
			if (bitmap.compress(CompressFormat.JPEG, mQuality, jpeg_data)) {
				byte[] code  = jpeg_data.toByteArray();
				byte[] output = Base64.encodeBase64(code);
				String js_out = new String(output);
				mGap.sendJavascript("navigator.camera.success('" + js_out + "');");
			}	
		}
		catch(Exception e) {
			this.failPicture("Error compressing image.");
		}		
	}
	
	/**
	 * Send error message to JavaScript.
	 * 
	 * @param err
	 */
	public void failPicture(String err) {
		mGap.sendJavascript("navigator.camera.error('" + err + "');");
	}
}
