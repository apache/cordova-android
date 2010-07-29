package com.phonegap.api.impl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;

import com.phonegap.api.Command;
import com.phonegap.api.CommandResult;

public final class Cache implements Command {
	
	private Context ctx;
	
	public boolean accept(String action) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setContext(Context ctx) {
		this.ctx = ctx;
	}

	public CommandResult execute(String action, String[] args) {

		CommandResult.Status status = CommandResult.Status.OK;
		String result = "";

		String uri = args[0];
		String fileName = md5(uri);
		
		if (action.equals("getCachedPathForURI") && args.length == 1)
		{
			// First check if the file exists already
			String fileDir = ctx.getFilesDir().getAbsolutePath();
			String filePath = fileDir + "/" + fileName;
			
			File f = new File(filePath);
			if (f.exists()) {
				result = "{ file: '"+filePath+"', status: 0 }";
			} else {

				URL u;
				InputStream is = null;
				DataInputStream dis;
				FileOutputStream out = null;
				byte[] buffer = new byte[1024];
				int length = -1;
				
				try {
					u = new URL(uri);
					is = u.openStream();         // throws an IOException
					dis = new DataInputStream(new BufferedInputStream(is));
					out = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
					while ((length = dis.read(buffer)) != -1) {
						out.write(buffer, 0, length);
					}
					out.flush();
					result = "{ file: '"+fileName+"', status: 0 }";
				} catch (MalformedURLException e) {
					status = CommandResult.Status.MALFORMEDURLEXCEPTION;
					result = "{ message: 'MalformedURLException', status: "+status.ordinal()+" }";
				} catch (IOException e) {
					status = CommandResult.Status.IOEXCEPTION;
					result = "{ message: 'IOException', status: "+status.ordinal()+" }";
				} finally {
					try {
						is.close();
						out.close();
					} catch (IOException e) {
						status = CommandResult.Status.IOEXCEPTION;
						result = "{ message: 'IOException', status: "+status.ordinal()+" }";
					}
				}
			}
		} else {
			status = CommandResult.Status.INVALIDACTION;
			result = "{ message: 'InvalidAction', status: "+status.ordinal()+" }";
		}
		return new CommandResult(status, result);
	}
	
	public String md5(String s) {  
	    try {  
	        // Create MD5 Hash  
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");  
	        digest.update(s.getBytes());  
	        byte messageDigest[] = digest.digest();  

	        // Create Hex String  
	        StringBuffer hexString = new StringBuffer();  
	        for (int i=0; i<messageDigest.length; i++)  
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));  
	        return hexString.toString();  
	          
	    } catch (NoSuchAlgorithmException e) {  
	        e.printStackTrace();  
	    }  
	    return "";  
	}
}
