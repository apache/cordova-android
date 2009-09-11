package com.phonegap.demo;

import java.io.*;

public class FileUtils {

	DirectoryManager fileManager;
	FileReader f_in;
	FileWriter f_out;
	
    public int testSaveLocationExists(){
        if (fileManager.testSaveLocationExists())
            return 0;
        else
            return 1;
    }
    
    public long getFreeDiskSpace(){
        long freeDiskSpace=fileManager.getFreeDiskSpace();
        return freeDiskSpace;
    }

    public int testFileExists(String file){
        if (fileManager.testFileExists(file))
            return 0;
        else
            return 1;
    }
    
    public int testDirectoryExists(String file){
        if (fileManager.testFileExists(file))
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
        if (fileManager.deleteDirectory(dir))
            return 0;
        else
            return 1;
    }
    

    /**
	 * Delete a specific file. 
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int deleteFile (String file){
        if (fileManager.deleteFile(file))
            return 0;
        else
            return 1;
    }
    

    /**
	 * Create a new directory. 
	 * TODO: JavaScript Call backs for success and error handling 
	 */
    public int createDirectory(String dir){
    	if (fileManager.createDirectory(dir))
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

    	return data;
    }
    
    public int write(String filename, String data)
    {
    	try {
			FileOutputStream out = new FileOutputStream(filename);
			PrintStream p = new PrintStream(out);
			p.print(data);
		} catch (FileNotFoundException e) {
			return -1;
		}    	
    	return 0;
    }
}
