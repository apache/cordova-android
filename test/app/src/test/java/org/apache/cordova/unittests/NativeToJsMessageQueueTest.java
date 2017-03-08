package org.apache.cordova.unittests;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.webkit.ValueCallback;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.ICordovaCookieManager;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

public class NativeToJsMessageQueueTest {

    NativeToJsMessageQueue queue = new NativeToJsMessageQueue();
    private String TEST_CALLBACK_ID = "MessageQueueTest";

    //A queue with no bridges should not work
    @Test
    public void testEmptyBridge()
    {
        assertFalse(queue.isBridgeEnabled());
    }


    //A queue with at least one bridge should work, using Eval Bridge
    @Test
    public void testEnabledBridge()
    {
        NativeToJsMessageQueue.BridgeMode bridge;
        bridge = new NativeToJsMessageQueue.NoOpBridgeMode();
        queue.addBridgeMode(bridge);
        queue.setBridgeMode(0);
        assertTrue(queue.isBridgeEnabled());
    }

    //This test is for the undocumented encoding system setup for the bridge
    //TODO: Document how the non-Javascript bridges are supposed to work
    @Test
    public void testPopAndEncode()
    {
        NativeToJsMessageQueue.BridgeMode bridge;
        bridge = new NativeToJsMessageQueue.NoOpBridgeMode();
        queue.addBridgeMode(bridge);
        queue.setBridgeMode(0);

        PluginResult result = new PluginResult(PluginResult.Status.OK);
        queue.addPluginResult(result, TEST_CALLBACK_ID);
        assertFalse(queue.isEmpty());
        String resultString = queue.popAndEncode(false);
        String [] results = resultString.split(" ");
        assertEquals(TEST_CALLBACK_ID, results[2]);
    }

    //This test is for the evalBridge, which directly calls cordova.callbackFromNative, skipping
    //platform specific NativeToJs code
    @Test
    public void testBasicPopAndEncodeAsJs()
    {
        NativeToJsMessageQueue.BridgeMode bridge;
        bridge = new NativeToJsMessageQueue.NoOpBridgeMode();
        queue.addBridgeMode(bridge);
        queue.setBridgeMode(0);

        PluginResult result = new PluginResult(PluginResult.Status.OK);
        queue.addPluginResult(result, TEST_CALLBACK_ID);
        assertFalse(queue.isEmpty());
        String resultString = queue.popAndEncodeAsJs();
        assertTrue(resultString.startsWith("cordova.callbackFromNative"));
    }

    //This test is for the evalBridge, which directly calls cordova.callbackFromNative, skipping
    //platform specific NativeToJs code
    @Test
    public void testStringPopAndEncodeAsJs()
    {
        NativeToJsMessageQueue.BridgeMode bridge;
        bridge = new NativeToJsMessageQueue.NoOpBridgeMode();
        queue.addBridgeMode(bridge);
        queue.setBridgeMode(0);

        PluginResult result = new PluginResult(PluginResult.Status.OK, "String Plugin Result");
        queue.addPluginResult(result, TEST_CALLBACK_ID);
        assertFalse(queue.isEmpty());
        String resultString = queue.popAndEncodeAsJs();
        assertTrue(resultString.startsWith("cordova.callbackFromNative"));
    }

    //This test is for the evalBridge, which directly calls cordova.callbackFromNative, skipping
    //platform specific NativeToJs code
    @Test
    public void testJsonPopAndEncodeAsJs()
    {
        NativeToJsMessageQueue.BridgeMode bridge;
        bridge = new NativeToJsMessageQueue.NoOpBridgeMode();
        queue.addBridgeMode(bridge);
        queue.setBridgeMode(0);

        JSONObject object = new JSONObject();
        try {
            object.put("test", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, object);
        queue.addPluginResult(result, TEST_CALLBACK_ID);
        assertFalse(queue.isEmpty());
        String resultString = queue.popAndEncodeAsJs();
        assertTrue(resultString.startsWith("cordova.callbackFromNative"));
    }

    //This test is for the evalBridge, which directly calls cordova.callbackFromNative, skipping
    //platform specific NativeToJs code
    @Test
    public void testMultipartPopAndEncodeAsJs()
    {
        ArrayList<PluginResult> multiparts = new ArrayList<PluginResult>();
        for (int i=0; i<5; i++) {
            multiparts.add(new PluginResult(PluginResult.Status.OK, i));
        }
        PluginResult multipartresult = new PluginResult(PluginResult.Status.OK, multiparts);
        NativeToJsMessageQueue queue = new NativeToJsMessageQueue();
        queue.addBridgeMode(new NativeToJsMessageQueue.NoOpBridgeMode());
        queue.setBridgeMode(0);
        queue.addPluginResult(multipartresult, "37");
        String result = queue.popAndEncodeAsJs();
        assertEquals(result, "cordova.callbackFromNative('37',true,1,[0,1,2,3,4],false);");
    }

}
