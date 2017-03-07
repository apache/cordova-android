package org.apache.cordova.test;

import android.util.Log;
import org.apache.cordova.NativeToJsMessageQueue;
import org.apache.cordova.PluginResult;
import java.util.ArrayList;

public class MultipartMessageTest extends BaseCordovaIntegrationTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpWithStartUrl(null);
    }

    public void testMultipartMessages() throws Throwable {
        ArrayList<PluginResult> multiparts = new ArrayList<PluginResult>();
        for (int i=0; i<5; i++) {
            multiparts.add(new PluginResult(PluginResult.Status.OK, i));
        }
        PluginResult multipartresult = new PluginResult(PluginResult.Status.OK, multiparts);
        NativeToJsMessageQueue q = new NativeToJsMessageQueue();
        q.addBridgeMode(new NativeToJsMessageQueue.NoOpBridgeMode());
        q.setBridgeMode(0);
        q.addPluginResult(multipartresult, "37");
        String result = q.popAndEncodeAsJs();
        assertEquals(result, "cordova.callbackFromNative('37',true,1,[0,1,2,3,4],false);");
        Log.v("MultiPartMessageTest", result);
    }

}
