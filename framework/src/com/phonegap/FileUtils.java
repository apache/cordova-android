/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import java.io.*;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

/**
 * This class provides SD card file and directory services to JavaScript.
 * Only files on the SD card can be accessed.
 */
public class FileUtils extends Plugin {

	public static int NOT_FOUND_ERR = 8;
	public static int SECURITY_ERR = 18;
	public static int ABORT_ERR = 20;

	public static int NOT_READABLE_ERR = 24;
	public static int ENCODING_ERR = 26;

	FileReader f_in;
	FileWriter f_out;
	
	/**
	 * Constructor.
	 */
	public FileUtils() {
		System.out.println("FileUtils()");
	}

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action 		The action to execute.
	 * @param args 			JSONArry of arguments for the plugin.
	 * @param callbackId	The callback id used when calling back into JavaScript.
	 * @return 				A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		//System.out.println("FileUtils.execute("+action+")");
		
		try {
			if (action.equals("testSaveLocationExists")) {
		        boolean b = DirectoryManager.testSaveLocationExists();
		        return new PluginResult(status, b);
		    }  
			else if (action.equals("getFreeDiskSpace")) {
		        long l = DirectoryManager.getFreeDiskSpace();
		        return new PluginResult(status, l);
		    }
			else if (action.equals("testFileExists")) {
		        boolean b = DirectoryManager.testFileExists(args.getString(0));
		        return new PluginResult(status, b);
		    }
			else if (action.equals("testDirectoryExists")) {
		        boolean b = DirectoryManager.testFileExists(args.getString(0));
		        return new PluginResult(status, b);
		    }
			else if (action.equals("deleteDirectory")) {
		        boolean b = DirectoryManager.deleteDirectory(args.getString(0));
		        return new PluginResult(status, b);
		    }
			else if (action.equals("deleteFile")) {
		        boolean b = DirectoryManager.deleteFile(args.getString(0));
		        return new PluginResult(status, b);
		    }	
			else if (action.equals("createDirectory")) {
		        boolean b = DirectoryManager.createDirectory(args.getString(0));
		        return new PluginResult(status, b);
		    }
			else if (action.equals("readAsText")) {
				try {
					String s = this.readAsText(args.getString(0), args.getString(1));
			        return new PluginResult(status, s);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return new PluginResult(PluginResult.Status.ERROR, FileUtils.NOT_FOUND_ERR);
				} catch (IOException e) {
					e.printStackTrace();
					return new PluginResult(PluginResult.Status.ERROR, FileUtils.NOT_READABLE_ERR);
				}
		    }
			else if (action.equals("readAsDataURL")) {
				try {
					String s = this.readAsDataURL(args.getString(0));
			        return new PluginResult(status, s);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return new PluginResult(PluginResult.Status.ERROR, FileUtils.NOT_FOUND_ERR);
				} catch (IOException e) {
					e.printStackTrace();
					return new PluginResult(PluginResult.Status.ERROR, FileUtils.NOT_READABLE_ERR);
				}
		    }
			else if (action.equals("writeAsText")) {
				try {
					this.writeAsText(args.getString(0), args.getString(1), args.getBoolean(2));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return new PluginResult(PluginResult.Status.ERROR, FileUtils.NOT_FOUND_ERR);
				} catch (IOException e) {
					e.printStackTrace();
					return new PluginResult(PluginResult.Status.ERROR, FileUtils.NOT_READABLE_ERR);
				}
		    }
			return new PluginResult(status, result);
		} catch (JSONException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		if (action.equals("readAsText")) {
			return false;
		}
		else if (action.equals("readAsDataURL")) {
			return false;
		}
		else if (action.equals("writeAsText")) {
			return false;
		}
		return true;
	}

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------
	
    /**
     * Read content of text file.
     * 
     * @param filename			The name of the file.
     * @param encoding			The encoding to return contents as.  Typical value is UTF-8.
     * 							(see http://www.iana.org/assignments/character-sets)
     * @return					Contents of file.
     * @throws FileNotFoundException, IOException
     */
    public String readAsText(String filename, String encoding) throws FileNotFoundException, IOException {
    	System.out.println("FileUtils.readAsText("+filename+", "+encoding+")");
    	StringBuilder data = new StringBuilder();
   		FileInputStream fis = new FileInputStream(filename);
   		BufferedReader reader = new BufferedReader(new InputStreamReader(fis, encoding), 1024);
   		String line;
   		while ((line = reader.readLine()) != null) {
   			data.append(line);
   		}
    	return data.toString();
    }
    
    /**
     * Read content of text file and return as base64 encoded data url.
     * 
     * @param filename			The name of the file.
     * @return					Contents of file = data:<media type>;base64,<data>
     * @throws FileNotFoundException, IOException
     */
    public String readAsDataURL(String filename) throws FileNotFoundException, IOException {
    	byte[] bytes = new byte[1000];
   		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename), 1024);
   		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	int numRead = 0;
    	while ((numRead = bis.read(bytes, 0, 1000)) >= 0) {
    	    bos.write(bytes, 0, numRead);
    	}
    	
    	// Determine content type from file name
    	// TODO
    	String contentType = "";

		byte[] base64 = Base64.encodeBase64(bos.toByteArray());
		String data = "data:" + contentType + ";base64," + new String(base64);
    	return data;
    }
    
    /**
     * Write contents of file.
     * 
     * @param filename			The name of the file.
     * @param data				The contents of the file.
     * @param append			T=append, F=overwrite
     * @throws FileNotFoundException, IOException
     */
    public void writeAsText(String filename, String data, boolean append) throws FileNotFoundException, IOException {
    	String FilePath= filename;
   		byte [] rawData = data.getBytes();
   		ByteArrayInputStream in = new ByteArrayInputStream(rawData);    			    			
   		FileOutputStream out= new FileOutputStream(FilePath, append);
   		byte buff[] = new byte[rawData.length];
   		in.read(buff, 0, buff.length);
   		out.write(buff, 0, rawData.length);
   		out.flush();
   		out.close();    			
    }
    

}
