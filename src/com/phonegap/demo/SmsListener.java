package com.phonegap.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.webkit.WebView;

public class SmsListener extends BroadcastReceiver 
{
	private WebView mAppView;
	
	public SmsListener(Context ctx, WebView mAppView) 
	{
        this.mAppView = mAppView;
	}

    String ACTION = "android.provider.Telephony.SMS_RECEIVED"; 
  	
    public void onReceive(Context ctx, Intent intent) 
    {
    	SmsMessage[] msg;
    	if (intent.getAction().equals(ACTION))         	
    	{
    		msg = getMessagesFromIntent(intent);
    		String smsContent = null;
    		String sendersNumber = null;
    		for(int i=0; i < msg.length; i++)
    		{
    			sendersNumber = msg[i].getDisplayOriginatingAddress();
    			smsContent = msg[i].getDisplayMessageBody();
    		}
                onReceiveSMS(sendersNumber, smsContent);
        }
    }
        
    protected void onReceiveSMS(String sendersNumber, String smsContent) 
    /**
     * Call back to Java Script
     */
    {
    	mAppView.loadUrl("javascript:onReceiveSms('"+sendersNumber+"',"+"'"+ smsContent +"'"+")");
    }
        
    private SmsMessage[] getMessagesFromIntent(Intent intent)
    {
    	SmsMessage retMsgs[] = null;
    	Bundle bdl = intent.getExtras();
    	try
    	{
    		Object pdus[] = (Object [])bdl.get("pdus");
    		retMsgs = new SmsMessage[pdus.length];
    		for(int n=0; n < pdus.length; n++)
    		{
    			byte[] byteData = (byte[])pdus[n];
    			retMsgs[n] = SmsMessage.createFromPdu(byteData);
    		}                              
        } catch(Exception e)
            {
                Log.e("SMS_getMessagesFromIntent", "fail", e);
            }
            return retMsgs;
    }

}

