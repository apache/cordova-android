package com.phonegap;

import java.io.*;

import android.webkit.WebView;

public class FileUtils {


	WebView mView;
	FileReader f_in;
	FileWriter f_out;
	
	public FileUtils(WebView view)
	{
		mView = view;
	}
	
    public int testSaveLocationExists(){
        if (DirectoryManager.testSaveLocationExists())
            return 0;
        else
            return 1;
    }
    
    public long getFreeDiskSpace(){
        long freeDiskSpace=DirectoryManager.getFreeDiskSpace();
        return freeDiskSpace;
    }

    public int testFileExists(String file){
        if (DirectoryManager.testFileExists(file))
            return 0;
        else
            return 1;
    }
    
    public int testDirectoryExists(String file){
        if (DirectoryManager.testFileExists(file))
            return 0;
        else
            return 1;
    } 

    /**
	 * Delete a specific directory. 
	 * Everyting in side the directory would be gone.
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int deleteDirectory (String dir){
        if (DirectoryManager.deleteDirectory(dir))
            return 0;
        else
            return 1;
    }
    

    /**
	 * Delete a specific file. 
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int deleteFile (String file){
        if (DirectoryManager.deleteFile(file))
            return 0;
        else
            return 1;
    }
    

    /**
	 * Create a new directory. 
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int createDirectory(String dir){
    	if (DirectoryManager.createDirectory(dir))
            return 0;
        else
            return 1;
    } 
	
    public String read(String filename)
    {
    	String data = "";
    	String output = "";
    	try {
    		FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			  while (in.available() !=0)
				{                 
					data += in.readLine();
				}
			  
		} catch (FileNotFoundException e) {
			data = "FAIL: File not found";
		} catch (IOException e) {
			data = "FAIL: IO ERROR";		
		}
		
		//mView.loadUrl("javascript:navigator.FileReader.hasRead('" + data + "')");
    	return data;
    }
    
    public int write(String filename, String data, boolean append)
    {
    		String FilePath= filename;
    		try {
				byte [] rawData = data.getBytes();
    			ByteArrayInputStream in = new ByteArrayInputStream(rawData);    			    			
    			FileOutputStream out= new FileOutputStream(FilePath, append);
    			byte buff[] = new byte[rawData.length];
    			in.read(buff, 0, buff.length);
    			out.write(buff, 0, rawData.length);
    			out.flush();
    			out.close();    			
    			//mView.loadUrl("javascript:navigator.FileReader.onsuccess('File written')");
    		} catch (Exception e) { 
    			//mView.loadUrl("javascript:navigator.FileReader.onerror('Fail')");
    			// So, do we just return -1 at this point!
    			return -1;
    		}
		return 0;
    }
    

}
