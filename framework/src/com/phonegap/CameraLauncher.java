package com.phonegap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginManager;
import com.phonegap.api.PluginResult;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
public class CameraLauncher implements Plugin {

    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object

	private int mQuality;				// Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
	private Uri imageUri;				// Uri of captured image 
	private boolean base64 = true;
	
    /**
     * Constructor.
     */
	public CameraLauncher() {
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
			if (action.equals("setBase64")) {
				this.setBase64(args.getBoolean(0));
			}
			else if (action.equals("takePicture")) {
				this.takePicture(args.getInt(0));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean hasReturnValue(String action) {
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
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {  	
    }
	
    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------
    
	/**
	 * Set the type of data to return.  The data can either be returned
	 * as a base64 string or a URI that points to the file.
	 * To display base64 string in an img tag, set the source to:
	 * 		img.src="data:image/jpeg;base64,"+result;
	 * or to display URI in an img tag
	 * 		img.src=result;
	 * 
	 * @param b			T=return base64 string (default), F=return URI
	 */
	public void setBase64(boolean b) {
		this.base64 = b;
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

        this.ctx.startActivityForResult((Plugin) this, intent);
	}

    /**
     * Called when the camera view exits. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param intent			An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		// If image available
		if (resultCode == Activity.RESULT_OK) {
			try {
				// Read in bitmap of captured image
				Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.ctx.getContentResolver(), imageUri);
				
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
						uri = this.ctx.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
					} catch (UnsupportedOperationException e) {
						System.out.println("Can't write to external media storage.");
						try {
							uri = this.ctx.getContentResolver().insert(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
						} catch (UnsupportedOperationException ex) {
							System.out.println("Can't write to internal media storage.");							
				        	this.failPicture("Error capturing image - no media storage found.");
				        	return;
						}
					}
	            
					// Add compressed version of captured image to returned media store Uri
					OutputStream os = this.ctx.getContentResolver().openOutputStream(uri);
					bitmap.compress(Bitmap.CompressFormat.JPEG, this.mQuality, os);
					os.close();
            	
					// Send Uri back to JavaScript for viewing image
					this.ctx.sendJavascript("navigator.camera.success('" + uri.toString() + "');");
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
				this.ctx.sendJavascript("navigator.camera.success('" + js_out + "');");
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
		this.ctx.sendJavascript("navigator.camera.error('" + err + "');");
	}
}
