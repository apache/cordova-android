package com.phonegap.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CameraPreview extends Activity implements SurfaceHolder.Callback{

    private static final String TAG = "PhoneGapCamera";
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    
    Camera mCamera;
    boolean mPreviewRunning = false;
    
    int quality;
    Intent mIntent;
    
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        Log.e(TAG, "onCreate");

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        setContentView(R.layout.preview);
        mSurfaceView = (SurfaceView)findViewById(R.id.surface);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);                    
        mIntent = this.getIntent();
        
        quality = mIntent.getIntExtra("quality", 100);
        
        Button stopButton = (Button) findViewById(R.id.go);
        stopButton.setOnClickListener(mSnapListener);
    }
    
    private OnClickListener mSnapListener = new OnClickListener() {
        public void onClick(View v) {
        	mCamera.takePicture(null, null, mPictureCallback);
        }
    };

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuItem item = menu.add(0, 0, 0, "goto gallery");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Uri target = Uri.parse("content://media/external/images/media");
                Intent intent = new Intent(Intent.ACTION_VIEW, target);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /*
     * We got the data, send it back to PhoneGap to be handled and processed.
     * 
     */
    
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            Log.e(TAG, "PICTURE CALLBACK: data.length = " + data.length);
            storeAndExit(data);
        }
    };

    /*
     * We can't just store and exit, because Android freezes up when we try to cram a picture across a process in a Bundle.
     * We HAVE to compress this data and send back the compressed data  
     */
    public void storeAndExit(byte[] data)
    {
    	ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
		Bitmap myMap = BitmapFactory.decodeByteArray(data, 0, data.length);
		try {
			if (myMap.compress(CompressFormat.JPEG, quality, jpeg_data))
			{
				byte[] code  = jpeg_data.toByteArray();
				byte[] output = Base64.encodeBase64(code);
				String js_out = new String(output);
				mIntent.putExtra("picture", js_out);
				setResult(RESULT_OK, mIntent);
			}	
		}
		catch(Exception e)
		{
			//Do shit here
		}
        finish();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {        	
            return super.onKeyDown(keyCode, event);
        }
 
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_SEARCH) {
            mCamera.takePicture(null, null, mPictureCallback);
            return true;
        }

        return false;
    }

    protected void onResume()
    {
        Log.e(TAG, "onResume");
        super.onResume();
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    protected void onStop()
    {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.e(TAG, "surfaceCreated");
        mCamera = Camera.open();
        //mCamera.startPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        Log.e(TAG, "surfaceChanged");

        // XXX stopPreview() will crash if preview is not running
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(w, h);
        mCamera.setParameters(p);
        try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.e(TAG, "surfaceDestroyed");
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();
    }

}
