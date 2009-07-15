package com.phonegap.demo;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class CameraPreview extends Activity implements SurfaceHolder.Callback{

    private static final String TAG = "CameraApiTest";
    Camera mCamera;
    boolean mPreviewRunning = false;

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
    }

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
     * Take JPEG Data and do what now?
     * 
     */
    
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            Log.e(TAG, "PICTURE CALLBACK: data.length = " + data.length);
            mCamera.startPreview();
        }
    };

    
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

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
	
}
