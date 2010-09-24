package com.phonegap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.webkit.WebView;
import android.provider.MediaStore;

/**
 * This class launches the camera view, allows the user to take a picture, closes the camera view,
 * and returns the captured image.  When the camera view is closed, the screen displayed before 
 * the camera view was shown is redisplayed.
 */
public class CameraLauncher implements Plugin {

	private static final int DATA_URL = 0;				// Return base64 encoded string
	private static final int FILE_URI = 1;				// Return file uri (content://media/external/images/media/2 for Android)
	
	private static final int PHOTOLIBRARY = 0;			// Choose image from picture library (same as SAVEDPHOTOALBUM for Android)
	private static final int CAMERA = 1;				// Take picture from camera
	private static final int SAVEDPHOTOALBUM = 2;		// Choose image from picture library (same as PHOTOLIBRARY for Android)
	
    WebView webView;						// WebView object
    DroidGap ctx;							// DroidGap object

	private int mQuality;					// Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
	private Uri imageUri;					// Uri of captured image 
	
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
			if (action.equals("takePicture")) {
				int destType = DATA_URL;
				if (args.length() > 1) {
					destType = args.getInt(1);
				}
				int srcType = CAMERA;
				if (args.length() > 2) {
					srcType = args.getInt(2);
				}
				if (srcType == CAMERA) {
					this.takePicture(args.getInt(0), destType);
				}
				else if ((srcType == PHOTOLIBRARY) || (srcType == SAVEDPHOTOALBUM)) {
					this.getImage(srcType, destType);
				}
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
	 * Take a picture with the camera.
	 * When an image is captured or the camera view is cancelled, the result is returned
	 * in DroidGap.onActivityResult, which forwards the result to this.onActivityResult.
	 * 
	 * The image can either be returned as a base64 string or a URI that points to the file.
	 * To display base64 string in an img tag, set the source to:
	 * 		img.src="data:image/jpeg;base64,"+result;
	 * or to display URI in an img tag
	 * 		img.src=result;
	 * 
	 * @param quality			Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
	 * @param returnType		Set the type of image to return. 
	 */
	public void takePicture(int quality, int returnType) {
		this.mQuality = quality;
		
		// Display camera
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        
        // Specify file so that large image is captured and returned
        // TODO: What if there isn't any external storage?
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        this.imageUri = Uri.fromFile(photo);

        this.ctx.startActivityForResult((Plugin) this, intent, (CAMERA+1)*16 + returnType+1);
	}
	
	/**
	 * Get image from photo library.
	 * 
	 * @param returnType
	 */
	// TODO: Images selected from SDCARD don't display correctly, but from CAMERA ALBUM do!
	public void getImage(int srcType, int returnType) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		this.ctx.startActivityForResult((Plugin) this, Intent.createChooser(intent,
				new String("Get Picture")), (srcType+1)*16 + returnType + 1);
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
		
		// Get src and dest types from request code
		int srcType = (requestCode/16) - 1;
		int destType = (requestCode % 16) - 1;

		// If CAMERA
		if (srcType == CAMERA) {
			
			// If image available
			if (resultCode == Activity.RESULT_OK) {
				try {
					// Read in bitmap of captured image
					Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.ctx.getContentResolver(), imageUri);

					// If sending base64 image back
					if (destType == DATA_URL) {
						this.processPicture(bitmap);
					}

					// If sending filename back
					else if (destType == FILE_URI){
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
		
		// If retrieving photo from library
		else if ((srcType == PHOTOLIBRARY) || (srcType == SAVEDPHOTOALBUM)) {
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = intent.getData();
				android.content.ContentResolver resolver = this.ctx.getContentResolver();
				// If sending base64 image back
				if (destType == DATA_URL) {
					try {
						Bitmap bitmap =	android.graphics.BitmapFactory.decodeStream(resolver.openInputStream(uri));
						this.processPicture(bitmap);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						this.failPicture("Error retrieving image.");
					}
				}
				
				// If sending filename back
				else if (destType == FILE_URI) {
					this.ctx.sendJavascript("navigator.camera.success('" + uri + "');");
				}
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				this.failPicture("Selection cancelled.");				
			}
			else {
				this.failPicture("Selection did not complete!");				
			}
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
