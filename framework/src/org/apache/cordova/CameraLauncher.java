/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
//import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.LOG;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentValues;
//import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * This class launches the camera view, allows the user to take a picture, closes the camera view,
 * and returns the captured image.  When the camera view is closed, the screen displayed before
 * the camera view was shown is redisplayed.
 */
public class CameraLauncher extends Plugin implements MediaScannerConnectionClient {

    private static final int DATA_URL = 0;              // Return base64 encoded string
    private static final int FILE_URI = 1;              // Return file uri (content://media/external/images/media/2 for Android)

    private static final int PHOTOLIBRARY = 0;          // Choose image from picture library (same as SAVEDPHOTOALBUM for Android)
    private static final int CAMERA = 1;                // Take picture from camera
    private static final int SAVEDPHOTOALBUM = 2;       // Choose image from picture library (same as PHOTOLIBRARY for Android)

    private static final int PICTURE = 0;               // allow selection of still pictures only. DEFAULT. Will return format specified via DestinationType
    private static final int VIDEO = 1;                 // allow selection of video only, ONLY RETURNS URL
    private static final int ALLMEDIA = 2;              // allow selection from all media types

    private static final int JPEG = 0;                  // Take a picture of type JPEG
    private static final int PNG = 1;                   // Take a picture of type PNG
    private static final String GET_PICTURE = "Get Picture";
    private static final String GET_VIDEO = "Get Video";
    private static final String GET_All = "Get All";

    private static final String LOG_TAG = "CameraLauncher";

    private int mQuality;                   // Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
    private int targetWidth;                // desired width of the image
    private int targetHeight;               // desired height of the image
    private Uri imageUri;                   // Uri of captured image
    private int encodingType;               // Type of encoding to use
    private int mediaType;                  // What type of media to retrieve
    private boolean saveToPhotoAlbum;       // Should the picture be saved to the device's photo album

    public String callbackId;
    private int numPics;
    
    private MediaScannerConnection conn;    // Used to update gallery app with newly-written files

    //This should never be null!
    //private CordovaInterface cordova;

    /**
     * Constructor.
     */
    public CameraLauncher() {
    }

//    public void setContext(CordovaInterface mCtx) {
//        super.setContext(mCtx);
//        if (CordovaInterface.class.isInstance(mCtx))
//            cordova = (CordovaInterface) mCtx;
//        else
//            LOG.d(LOG_TAG, "ERROR: You must use the CordovaInterface for this to work correctly. Please implement it in your activity");
//    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
        this.callbackId = callbackId;

        try {
            if (action.equals("takePicture")) {
                int srcType = CAMERA;
                int destType = FILE_URI;
                this.saveToPhotoAlbum = false;
                this.targetHeight = 0;
                this.targetWidth = 0;
                this.encodingType = JPEG;
                this.mediaType = PICTURE;
                this.mQuality = 80;

                this.mQuality = args.getInt(0);
                destType = args.getInt(1);
                srcType = args.getInt(2);
                this.targetWidth = args.getInt(3);
                this.targetHeight = args.getInt(4);
                this.encodingType = args.getInt(5);
                this.mediaType = args.getInt(6);
                this.saveToPhotoAlbum = args.getBoolean(9);

                if (srcType == CAMERA) {
                    this.takePicture(destType, encodingType);
                }
                else if ((srcType == PHOTOLIBRARY) || (srcType == SAVEDPHOTOALBUM)) {
                    this.getImage(srcType, destType);
                }
                PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
                r.setKeepCallback(true);
                return r;
            }
            return new PluginResult(status, result);
        } catch (JSONException e) {
            e.printStackTrace();
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        }
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Take a picture with the camera.
     * When an image is captured or the camera view is cancelled, the result is returned
     * in CordovaActivity.onActivityResult, which forwards the result to this.onActivityResult.
     *
     * The image can either be returned as a base64 string or a URI that points to the file.
     * To display base64 string in an img tag, set the source to:
     *      img.src="data:image/jpeg;base64,"+result;
     * or to display URI in an img tag
     *      img.src=result;
     *
     * @param quality           Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
     * @param returnType        Set the type of image to return.
     */
    public void takePicture(int returnType, int encodingType) {
        // Save the number of images currently on disk for later
        this.numPics = queryImgDB(whichContentStore()).getCount();

        // Display camera
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        // Specify file so that large image is captured and returned
        // TODO: What if there isn't any external storage?
        File photo = createCaptureFile(encodingType);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        this.imageUri = Uri.fromFile(photo);

        if (this.cordova != null) {
            this.cordova.startActivityForResult((Plugin) this, intent, (CAMERA + 1) * 16 + returnType + 1);
        }
//        else
//            LOG.d(LOG_TAG, "ERROR: You must use the CordovaInterface for this to work correctly. Please implement it in your activity");
    }

    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @return a File object pointing to the temporary picture
     */
    private File createCaptureFile(int encodingType) {
        File photo = null;
        if (encodingType == JPEG) {
            photo = new File(DirectoryManager.getTempDirectoryPath(this.cordova.getActivity()), "Pic.jpg");
        } else if (encodingType == PNG) {
            photo = new File(DirectoryManager.getTempDirectoryPath(this.cordova.getActivity()), "Pic.png");
        } else {
            throw new IllegalArgumentException("Invalid Encoding Type: " + encodingType);
        }
        return photo;
    }

    /**
     * Get image from photo library.
     *
     * @param quality           Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
     * @param srcType           The album to get image from.
     * @param returnType        Set the type of image to return.
     */
    // TODO: Images selected from SDCARD don't display correctly, but from CAMERA ALBUM do!
    public void getImage(int srcType, int returnType) {
        Intent intent = new Intent();
        String title = GET_PICTURE;
        if (this.mediaType == PICTURE) {
            intent.setType("image/*");
        }
        else if (this.mediaType == VIDEO) {
            intent.setType("video/*");
            title = GET_VIDEO;
        }
        else if (this.mediaType == ALLMEDIA) {
            // I wanted to make the type 'image/*, video/*' but this does not work on all versions
            // of android so I had to go with the wildcard search.
            intent.setType("*/*");
            title = GET_All;
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (this.cordova != null) {
            this.cordova.startActivityForResult((Plugin) this, Intent.createChooser(intent,
                    new String(title)), (srcType + 1) * 16 + returnType + 1);
        }
    }

    /**
     * Scales the bitmap according to the requested size.
     *
     * @param bitmap        The bitmap to scale.
     * @return Bitmap       A new Bitmap object of the same bitmap after scaling.
     */
    public Bitmap scaleBitmap(Bitmap bitmap) {
        int newWidth = this.targetWidth;
        int newHeight = this.targetHeight;
        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();

        // If no new width or height were specified return the original bitmap
        if (newWidth <= 0 && newHeight <= 0) {
            return bitmap;
        }
        // Only the width was specified
        else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * origHeight) / origWidth;
        }
        // only the height was specified
        else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * origWidth) / origHeight;
        }
        // If the user specified both a positive width and height
        // (potentially different aspect ratio) then the width or height is
        // scaled so that the image fits while maintaining aspect ratio.
        // Alternatively, the specified width and height could have been
        // kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
        // would result in whitespace in the new image.
        else {
            double newRatio = newWidth / (double) newHeight;
            double origRatio = origWidth / (double) origHeight;

            if (origRatio > newRatio) {
                newHeight = (newWidth * origHeight) / origWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * origWidth) / origHeight;
            }
        }

        Bitmap retval = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        bitmap.recycle();
        System.gc();
        return retval;
    }

    /**
     * Called when the camera view exits.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Get src and dest types from request code
        int srcType = (requestCode / 16) - 1;
        int destType = (requestCode % 16) - 1;
        int rotate = 0;

        // Create an ExifHelper to save the exif data that is lost during compression
        ExifHelper exif = new ExifHelper();
        try {
            if (this.encodingType == JPEG) {
                exif.createInFile(DirectoryManager.getTempDirectoryPath(this.cordova.getActivity()) + "/Pic.jpg");
                exif.readExifData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // If CAMERA
        if (srcType == CAMERA) {
            // If image available
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap bitmap = null;

                    // If sending base64 image back
                    if (destType == DATA_URL) {
                        bitmap = scaleBitmap(getBitmapFromResult(intent));

                        this.processPicture(bitmap);
                        checkForDuplicateImage(DATA_URL);
                    }

                    // If sending filename back
                    else if (destType == FILE_URI) {
                        // Create entry in media store for image
                        // (Don't use insertImage() because it uses default compression setting of 50 - no way to change it)
                        ContentValues values = new ContentValues();
                        values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                       
                        try {
                            this.imageUri = this.cordova.getActivity().getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        } catch (UnsupportedOperationException e) {
                            LOG.d(LOG_TAG, "Can't write to external media storage.");
                            try {
                                this.imageUri = this.cordova.getActivity().getContentResolver().insert(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
                            } catch (UnsupportedOperationException ex) {
                                LOG.d(LOG_TAG, "Can't write to internal media storage.");
                                this.failPicture("Error capturing image - no media storage found.");
                                return;
                            }
                        }
                        if (!this.saveToPhotoAlbum) {
                            File tempFile = new File(this.imageUri.toString());
                            Uri jailURI = Uri.fromFile(new File("/data/data/" + this.cordova.getActivity().getPackageName() + "/", tempFile.getName()));
                            
                            // Clean up initial URI before writing out safe URI
                            boolean didWeDeleteIt = tempFile.delete();
                            if (!didWeDeleteIt) {
                                int result = this.cordova.getActivity().getContentResolver().delete(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    MediaStore.Images.Media.DATA + " = ?",
                                    new String[] { this.imageUri.toString() }
                                );
                                LOG.d("TAG!","result is " + result);
                            }
                            this.imageUri = jailURI;
                        }

                        // If all this is true we shouldn't compress the image.
                        if (this.targetHeight == -1 && this.targetWidth == -1 && this.mQuality == 100) {
                            FileInputStream fis = new FileInputStream(FileUtils.stripFileProtocol(imageUri.toString()));
                            OutputStream os = this.cordova.getActivity().getContentResolver().openOutputStream(uri);
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = fis.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                            os.flush();
                            os.close();
                            fis.close();

                            checkForDuplicateImage(FILE_URI);

                            this.success(new PluginResult(PluginResult.Status.OK, uri.toString()), this.callbackId);
                            return;
                        }

                        bitmap = scaleBitmap(getBitmapFromResult(intent));

                        // Add compressed version of captured image to returned media store Uri
                        OutputStream os = this.cordova.getActivity().getContentResolver().openOutputStream(this.imageUri);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, this.mQuality, os);
                        os.close();

                        // Restore exif data to file
                        
                        if (this.encodingType == JPEG) {
                            String exifPath;
                            if (this.saveToPhotoAlbum) {
                                exifPath = FileUtils.getRealPathFromURI(this.imageUri, this.cordova);
                            } else {
                                exifPath = this.imageUri.toString();
                            }
                            exif.createOutFile(exifPath);
                            exif.writeExifData();
                        }
                        

                        // Scan for the gallery to update pic refs in gallery
                        this.scanForGallery();

                        // Send Uri back to JavaScript for viewing image
                        this.success(new PluginResult(PluginResult.Status.OK, this.imageUri.toString()), this.callbackId);
                    }
                    bitmap.recycle();
                    bitmap = null;
                    System.gc();

                    checkForDuplicateImage(FILE_URI);
                    
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
                android.content.ContentResolver resolver = this.cordova.getActivity().getContentResolver();

                // If you ask for video or all media type you will automatically get back a file URI
                // and there will be no attempt to resize any returned data
                if (this.mediaType != PICTURE) {
                    this.success(new PluginResult(PluginResult.Status.OK, uri.toString()), this.callbackId);
                }
                else {
                    // If sending base64 image back
                    if (destType == DATA_URL) {
                        try {
                            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(resolver.openInputStream(uri));
                            String[] cols = { MediaStore.Images.Media.ORIENTATION };
                            Cursor cursor = this.cordova.getActivity().getContentResolver().query(intent.getData(),
                                    cols,
                                    null, null, null);
                            if (cursor != null) {
                                cursor.moveToPosition(0);
                                rotate = cursor.getInt(0);
                                cursor.close();
                            }
                            if (rotate != 0) {
                                Matrix matrix = new Matrix();
                                matrix.setRotate(rotate);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            }
                           bitmap = scaleBitmap(bitmap);
                            this.processPicture(bitmap);
                            bitmap.recycle();
                            bitmap = null;
                            System.gc();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            this.failPicture("Error retrieving image.");
                        }
                    }

                    // If sending filename back
                    else if (destType == FILE_URI) {
                        // Do we need to scale the returned file
                        if (this.targetHeight > 0 && this.targetWidth > 0) {
                            try {
                                Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(resolver.openInputStream(uri));
                                bitmap = scaleBitmap(bitmap);

                                String fileName = DirectoryManager.getTempDirectoryPath(this.cordova.getActivity()) + "/resize.jpg";
                                OutputStream os = new FileOutputStream(fileName);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, this.mQuality, os);
                                os.close();

                                // Restore exif data to file
                                if (this.encodingType == JPEG) {
                                    exif.createOutFile(FileUtils.getRealPathFromURI(uri, this.cordova));
                                    exif.writeExifData();
                                }

                                bitmap.recycle();
                                bitmap = null;

                                // The resized image is cached by the app in order to get around this and not have to delete you
                                // application cache I'm adding the current system time to the end of the file url.
                                this.success(new PluginResult(PluginResult.Status.OK, ("file://" + fileName + "?" + System.currentTimeMillis())), this.callbackId);
                                System.gc();
                            } catch (Exception e) {
                                e.printStackTrace();
                                this.failPicture("Error retrieving image.");
                            }
                        }
                        else {
                            this.success(new PluginResult(PluginResult.Status.OK, uri.toString()), this.callbackId);
                        }
                    }
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

    private Bitmap getBitmapFromResult(Intent intent)
            throws IOException, FileNotFoundException {
        Bitmap bitmap = null;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.ctx.getActivity().getContentResolver(), imageUri);
        } catch (FileNotFoundException e) {
            Uri uri = intent.getData();
            android.content.ContentResolver resolver = this.ctx.getActivity().getContentResolver();
            bitmap = android.graphics.BitmapFactory.decodeStream(resolver.openInputStream(uri));
        }
        return bitmap;
    }

    /**
     * Creates a cursor that can be used to determine how many images we have.
     *
     * @return a cursor
     */
    private Cursor queryImgDB(Uri contentStore) {
        return this.cordova.getActivity().getContentResolver().query(
                contentStore,
                new String[] { MediaStore.Images.Media._ID },
                null,
                null,
                null);
    }

    /**
     * Used to find out if we are in a situation where the Camera Intent adds to images
     * to the content store. If we are using a FILE_URI and the number of images in the DB
     * increases by 2 we have a duplicate, when using a DATA_URL the number is 1.
     *
     * @param type FILE_URI or DATA_URL
     */
    private void checkForDuplicateImage(int type) {
        int diff = 1;
        Uri contentStore = whichContentStore();
        Cursor cursor = queryImgDB(contentStore);
        int currentNumOfImages = cursor.getCount();

        if (type == FILE_URI) {
            diff = 2;
        }

        // delete the duplicate file if the difference is 2 for file URI or 1 for Data URL
        if ((currentNumOfImages - numPics) == diff) {
            cursor.moveToLast();
            int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID))) - 1;
            Uri uri = Uri.parse(contentStore + "/" + id);
            this.cordova.getActivity().getContentResolver().delete(uri, null, null);
        }
    }

    /**
     * Determine if we are storing the images in internal or external storage
     * @return Uri
     */
    private Uri whichContentStore() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            return android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
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
                byte[] code = jpeg_data.toByteArray();
                byte[] output = Base64.encodeBase64(code);
                String js_out = new String(output);
                this.success(new PluginResult(PluginResult.Status.OK, js_out), this.callbackId);
                js_out = null;
                output = null;
                code = null;
            }
        } catch (Exception e) {
            this.failPicture("Error compressing image.");
        }
        jpeg_data = null;
    }

    /**
     * Send error message to JavaScript.
     *
     * @param err
     */
    public void failPicture(String err) {
        this.error(new PluginResult(PluginResult.Status.ERROR, err), this.callbackId);
    }
    
    private void scanForGallery() { 
        if(this.conn!=null) this.conn.disconnect();  
        this.conn = new MediaScannerConnection(this.ctx.getActivity().getApplicationContext(), this); 
        conn.connect(); 
    } 

    @Override
    public void onMediaScannerConnected() {
        try{
            this.conn.scanFile(this.imageUri.toString(), "image/*");
        } catch (java.lang.IllegalStateException e){
            e.printStackTrace();
            LOG.d(LOG_TAG, "Can;t scan file in MediaScanner aftering taking picture");
        }
        
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        this.conn.disconnect();   
    }
}
