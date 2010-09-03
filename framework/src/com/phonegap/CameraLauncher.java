package com.phonegap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.webkit.WebView;

/**
 * This class launches the camera view, allows the user to take a picture, closes the camera view,
 * and returns the captured image.  When the camera view is closed, the screen displayed before 
 * the camera view was shown is redisplayed.
 */
public class CameraLauncher extends ActivityResultModule {
		
	private int mQuality;				// Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
	private Uri imageUri;				// Uri of captured image 
	private boolean base64 = false;
	
    /**
     * Constructor.
     * 
     * @param view
     * @param gap
     */
	public CameraLauncher(WebView view, DroidGap gap) {
		super(view, gap);
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
        
        // Specify file so that large image is captured and returned
        // TODO: What if there isn't any external storage?
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        this.imageUri = Uri.fromFile(photo);

        this.startActivityForResult(intent);
	}

    /**
     * Called when the camera view exits. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		// If image available
		if (resultCode == Activity.RESULT_OK) {
			try {
				// Read in bitmap of captured image
				Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.gap.getContentResolver(), imageUri);
				
				// If sending base64 image back
				if (this.base64) {
					this.processPicture(bitmap);
				}
				
				// If sending filename back
				else {
					// Create entry in media store for image
					// (Don't use insertImage() because it uses default compression setting of 50 - no way to change it)
					ContentValues values = new ContentValues();
					values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
					Uri uri = null;
					try {
						uri = this.gap.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
					} catch (UnsupportedOperationException e) {
						System.out.println("Can't write to external media storage.");
						try {
							uri = this.gap.getContentResolver().insert(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
						} catch (UnsupportedOperationException ex) {
							System.out.println("Can't write to internal media storage.");							
				        	this.failPicture("Error capturing image - no media storage found.");
				        	return;
						}
					}
	            
					// Add compressed version of captured image to returned media store Uri
					OutputStream os = this.gap.getContentResolver().openOutputStream(uri);
					bitmap.compress(Bitmap.CompressFormat.JPEG, this.mQuality, os);
					os.close();
            	
					// Send Uri back to JavaScript for viewing image
					this.sendJavascript("navigator.camera.success('" + uri.toString() + "');");
				}
			} catch (IOException e) {
				e.printStackTrace();
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
				this.sendJavascript("navigator.camera.success('" + js_out + "');");
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
		this.sendJavascript("navigator.camera.error('" + err + "');");
	}
}
