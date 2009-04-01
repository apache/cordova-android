package com.phonegap.demo;
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

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.SensorListener;
import android.webkit.WebView;

public class Orientation implements SensorListener{

	private WebView mAppView;
    private SensorManager sensorManager;
	private Context mCtx;
    
	Orientation(WebView kit, Context ctx) {
		mAppView = kit;
		mCtx = ctx;
        sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
        this.resumeAccel();
	}
	
	public void onSensorChanged(int sensor, final float[] values) {
		if (sensor != SensorManager.SENSOR_ACCELEROMETER || values.length < 3)
			return;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        mAppView.loadUrl("javascript:gotAcceleration(" + x + ", " + y + "," + z + ")");
	}

	public void onAccuracyChanged(int arg0, int arg1) {
		// This is a stub method.
		
	}

	public void pauseAccel()
	{
        sensorManager.unregisterListener(this);	
	}
	
	public void resumeAccel()
	{
		sensorManager.registerListener(this, 
				   SensorManager.SENSOR_ACCELEROMETER,
				   SensorManager.SENSOR_DELAY_GAME);
	}
	
}
