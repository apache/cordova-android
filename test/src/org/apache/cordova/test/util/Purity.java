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

/*
 * Purity is a small set of Android utility methods that allows us to simulate touch events on 
 * Android applications.  This is important for simulating some of the most annoying tests.
 */

package org.apache.cordova.test.util;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.webkit.WebView;


public class Purity {

    Instrumentation inst;
    int width, height;
    float density;
    Bitmap state;
    boolean fingerDown = false;
   
    public Purity(Context ctx, Instrumentation i)
    {
        inst = i;
        DisplayMetrics display = ctx.getResources().getDisplayMetrics();
        density = display.density;
        width = display.widthPixels;
        height = display.heightPixels;
        
    }
    
    /*
     * WebKit doesn't give you real pixels anymore, this is done for subpixel fonts to appear on
     * iOS and Android.  However, Android automation requires real pixels
     */
    private int getRealCoord(int coord)
    {
        return (int) (coord * density);
    }

    public int getViewportWidth()
    {
        return (int) (width/density);
    }
    
    public int getViewportHeight()
    {
        return (int) (height/density);
    }
    
    public void touch(int x, int y)
    {
        int realX = getRealCoord(x);
        int realY = getRealCoord(y);
        long downTime = SystemClock.uptimeMillis();
        // event time MUST be retrieved only by this way!
        long eventTime = SystemClock.uptimeMillis();
        if(!fingerDown)
        {
            MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, realX, realY, 0);
            inst.sendPointerSync(downEvent);
        }
        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, realX, realY, 0);
        inst.sendPointerSync(upEvent);
    }
    
    public void touchStart(int x, int y)
    {
        int realX = getRealCoord(x);
        int realY = getRealCoord(y);
        long downTime = SystemClock.uptimeMillis();
        // event time MUST be retrieved only by this way!
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, realX, realY, 0);
        inst.sendPointerSync(event);
        fingerDown = true;
    }
    
    //Move from the touch start
    public void touchMove(int x, int y)
    {
        if(!fingerDown)
            touchStart(x,y);
        else
        {
            int realX = getRealCoord(x);
            int realY = getRealCoord(y);
            long downTime = SystemClock.uptimeMillis();
            // event time MUST be retrieved only by this way!
            long eventTime = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, realX, realY, 0);
            inst.sendPointerSync(event);
        }
    }
    
    public void touchEnd(int x, int y)
    {
        if(!fingerDown)
        {
            touch(x, y);
        }
        else
        {
            int realX = getRealCoord(x);
            int realY = getRealCoord(y);
            long downTime = SystemClock.uptimeMillis();
            // event time MUST be retrieved only by this way!
            long eventTime = SystemClock.uptimeMillis();
            MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, realX, realY, 0);
            inst.sendPointerSync(event);
            fingerDown = false;
        }
    }
    
    public void setBitmap(WebView view)
    {
        Picture p = view.capturePicture();
        state = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
    }
    
    public boolean checkRenderView(WebView view)
    {
        if(state == null)
        {
            setBitmap(view);
            return false;
        }
        else
        {
            Picture p = view.capturePicture();
            Bitmap newState = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
            boolean result = newState.equals(state);
            newState.recycle();
            return result;
        }
    }
    
    public void clearBitmap()
    {
        if(state != null)
            state.recycle();
    }
    
    protected void finalize()
    {
            clearBitmap();
    }
}
