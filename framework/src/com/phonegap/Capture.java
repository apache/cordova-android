/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2011, IBM Corporation
 */
package com.phonegap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class Capture extends Plugin {

    private static final int CAPTURE_AUDIO = 0;     // Constant for capture audio
    private static final int CAPTURE_IMAGE = 1;     // Constant for capture image
    private static final int CAPTURE_VIDEO = 2;     // Constant for capture video
    private static final String LOG_TAG = "Capture";
    private String callbackId;                      // The ID of the callback to be invoked with our result
    private long limit;                             // the number of pics/vids/clips to take
    private double duration;                        // optional duration parameter for video recording
    private JSONArray results;                      // The array of results to be returned to the user
    private Uri imageUri;                           // Uri of captured image 

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        this.callbackId = callbackId;
        this.limit = 1;
        this.duration = 0.0f;
        this.results = new JSONArray();
        
        JSONObject options = args.optJSONObject(0);
        if (options != null) {
            limit = options.optLong("limit", 1);
            duration = options.optDouble("duration", 0.0f);
        }

        if (action.equals("getFormatData")) {
            try {
                JSONObject obj = getFormatData(args.getString(0), args.getString(1));
                return new PluginResult(PluginResult.Status.OK, obj);
            } catch (JSONException e) {
                return new PluginResult(PluginResult.Status.ERROR);
            }
        }
        else if (action.equals("captureAudio")) {
            this.captureAudio();
        }
        else if (action.equals("captureImage")) {
            this.captureImage();
        }
        else if (action.equals("captureVideo")) {
            this.captureVideo(duration);    
        }
        
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
    }

    /**
     * Provides the media data file data depending on it's mime type
     * 
     * @param filePath path to the file
     * @param mimeType of the file
     * @return a MediaFileData object
     */
    private JSONObject getFormatData(String filePath, String mimeType) {
        JSONObject obj = new JSONObject();
        try {
            // setup defaults
            obj.put("height", 0);
            obj.put("width", 0);
            obj.put("bitrate", 0);
            obj.put("duration", 0);
            obj.put("codecs", "");

            // If the mimeType isn't set the rest will fail
            // so let's see if we can determine it.
            if (mimeType == null || mimeType.equals("")) {
                mimeType = FileUtils.getMimeType(filePath);
            }
            
            if (mimeType.equals("image/jpeg") || filePath.endsWith(".jpg")) {
                obj = getImageData(filePath, obj);
            }
            else if (filePath.endsWith("audio/3gpp")) {
                obj = getAudioVideoData(filePath, obj, false);
            }
            else if (mimeType.equals("video/3gpp")) {
                obj = getAudioVideoData(filePath, obj, true);
            }
        }
        catch (JSONException e) {
            Log.d(LOG_TAG, "Error: setting media file data object");
        }
        return obj;
    }

    /**
     * Get the Image specific attributes
     * 
     * @param filePath path to the file
     * @param obj represents the Media File Data
     * @return a JSONObject that represents the Media File Data
     * @throws JSONException
     */
    private JSONObject getImageData(String filePath, JSONObject obj) throws JSONException {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        obj.put("height", bitmap.getHeight());
        obj.put("width", bitmap.getWidth());
        return obj;
    }

    /**
     * Get the Image specific attributes
     * 
     * @param filePath path to the file
     * @param obj represents the Media File Data
     * @param video if true get video attributes as well
     * @return a JSONObject that represents the Media File Data
     * @throws JSONException
     */
    private JSONObject getAudioVideoData(String filePath, JSONObject obj, boolean video) throws JSONException {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.prepare();
            obj.put("duration", player.getDuration());
            if (video) {
                obj.put("height", player.getVideoHeight());
                obj.put("width", player.getVideoWidth());
            }
        }
        catch (IOException e) {
            Log.d(LOG_TAG, "Error: loading video file");
        } 
        return obj;
    }

    /**
     * Sets up an intent to capture audio.  Result handled by onActivityResult()
     */
    private void captureAudio() {
        Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);

        this.ctx.startActivityForResult((Plugin) this, intent, CAPTURE_AUDIO);
    }

    /**
     * Sets up an intent to capture images.  Result handled by onActivityResult()
     */
    private void captureImage() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        // Specify file so that large image is captured and returned
        File photo = new File(DirectoryManager.getTempDirectoryPath(ctx),  "Capture.jpg");
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        this.imageUri = Uri.fromFile(photo);

        this.ctx.startActivityForResult((Plugin) this, intent, CAPTURE_IMAGE);
    }

    /**
     * Sets up an intent to capture video.  Result handled by onActivityResult()
     */
    private void captureVideo(double duration) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        // Introduced in API 8
        //intent.putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, duration);
        
        this.ctx.startActivityForResult((Plugin) this, intent, CAPTURE_VIDEO);
    }
    
    /**
     * Called when the video view exits. 
     * 
     * @param requestCode       The request code originally supplied to startActivityForResult(), 
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     * @throws JSONException 
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Result received okay
        if (resultCode == Activity.RESULT_OK) {
            // An audio clip was requested
            if (requestCode == CAPTURE_AUDIO) {
                // Get the uri of the audio clip
                Uri data = intent.getData();
                // create a file object from the uri
                results.put(createMediaFile(data));

                if (results.length() >= limit) {
                    // Send Uri back to JavaScript for listening to audio
                    this.success(new PluginResult(PluginResult.Status.OK, results, "navigator.device.capture._castMediaFile"), this.callbackId);
                } else {
                    // still need to capture more audio clips
                    captureAudio();
                }
            } else if (requestCode == CAPTURE_IMAGE) {
                // For some reason if I try to do:
                // Uri data = intent.getData();
                // It crashes in the emulator and on my phone with a null pointer exception
                // To work around it I had to grab the code from CameraLauncher.java
                try {
                    // Create an ExifHelper to save the exif data that is lost during compression
                    ExifHelper exif = new ExifHelper();
                    exif.createInFile(DirectoryManager.getTempDirectoryPath(ctx) + "/Capture.jpg");
                    exif.readExifData();
                    
                    // Read in bitmap of captured image
                    Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.ctx.getContentResolver(), imageUri);

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
                            this.fail("Error capturing image - no media storage found.");
                            return;
                        }
                    }

                    // Add compressed version of captured image to returned media store Uri
                    OutputStream os  = this.ctx.getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    os.close();

                    bitmap.recycle();
                    bitmap = null;
                    System.gc();
                    
                    // Restore exif data to file
                    exif.createOutFile(FileUtils.getRealPathFromURI(uri, this.ctx));
                    exif.writeExifData();
                    
                    // Add image to results
                    results.put(createMediaFile(uri));
                    
                    if (results.length() >= limit) {
                        // Send Uri back to JavaScript for viewing image
                        this.success(new PluginResult(PluginResult.Status.OK, results, "navigator.device.capture._castMediaFile"), this.callbackId);
                    } else {
                        // still need to capture more images
                        captureImage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    this.fail("Error capturing image.");
                }
            } else if (requestCode == CAPTURE_VIDEO) {
                // Get the uri of the video clip
                Uri data = intent.getData();
                // create a file object from the uri
                results.put(createMediaFile(data));

                if (results.length() >= limit) {
                    // Send Uri back to JavaScript for viewing video
                    this.success(new PluginResult(PluginResult.Status.OK, results, "navigator.device.capture._castMediaFile"), this.callbackId);
                } else {
                    // still need to capture more video clips
                    captureVideo(duration);
                }
            }
        }
        // If canceled
        else if (resultCode == Activity.RESULT_CANCELED) {
            // If we have partial results send them back to the user
            if (results.length() > 0) {
                this.success(new PluginResult(PluginResult.Status.OK, results, "navigator.device.capture._castMediaFile"), this.callbackId);                
            }
            // user canceled the action
            else {
                this.fail("Canceled.");
            }
        }
        // If something else
        else {
            // If we have partial results send them back to the user
            if (results.length() > 0) {
                this.success(new PluginResult(PluginResult.Status.OK, results, "navigator.device.capture._castMediaFile"), this.callbackId);                
            }
            // something bad happened
            else {
                this.fail("Did not complete!");
            }
        }
    }

    /**
     * Creates a JSONObject that represents a File from the Uri
     *  
     * @param data the Uri of the audio/image/video
     * @return a JSONObject that represents a File
     * @throws IOException 
     */
    private JSONObject createMediaFile(Uri data){
        File fp = new File(FileUtils.getRealPathFromURI(data, this.ctx));
        JSONObject obj = new JSONObject();

        try {       
            // File properties
            obj.put("name", fp.getName());
            obj.put("fullPath", fp.getAbsolutePath());
            obj.put("type", FileUtils.getMimeType(fp.getAbsolutePath()));
            obj.put("lastModifiedDate", fp.lastModified());
            obj.put("size", fp.length());
        } catch (JSONException e) {
            // this will never happen
            e.printStackTrace();
        }
        
        return obj;
    }

    /**
     * Send error message to JavaScript.
     * 
     * @param err
     */
    public void fail(String err) {
        this.error(new PluginResult(PluginResult.Status.ERROR, err), this.callbackId);
    }
}