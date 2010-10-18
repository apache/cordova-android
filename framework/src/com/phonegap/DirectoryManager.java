/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * This class provides file directory utilities.  
 * All file operations are performed on the SD card.
 *   
 * It is used by the FileUtils class.
 */
public class DirectoryManager {
	
	/**
	 * Determine if a file or directory exists.
	 * 
	 * @param name				The name of the file to check.
	 * @return					T=exists, F=not found
	 */
	protected static boolean testFileExists(String name) {
		boolean status;
		
		// If SD card exists
		if ((testSaveLocationExists()) && (!name.equals(""))) {
    		File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), name);
            status = newPath.exists();
    	}
		
		// If no SD card
		else{
    		status = false;
    	}
		return status;
	}
	
	/**
	 * Get the free disk space on the SD card
	 * 
	 * @return 		Size in KB or -1 if not available
	 */
	protected static long getFreeDiskSpace() {
		String status = Environment.getExternalStorageState();
		long freeSpace = 0;
		
		// If SD card exists
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				freeSpace = availableBlocks*blockSize/1024;
			} catch (Exception e) {e.printStackTrace(); }
		} 
		
		// If no SD card, then return -1
		else { 
			return -1; 
		}
		
		return (freeSpace);
	}	
	
	/**
	 * Create directory on SD card.
	 * 
	 * @param directoryName		The name of the directory to create.
	 * @return 					T=successful, F=failed
	 */
	protected static boolean createDirectory(String directoryName) {
		boolean status;
		
		// Make sure SD card exists
		if ((testSaveLocationExists()) && (!directoryName.equals(""))) {
			File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), directoryName);
			status = newPath.mkdir();
			status = true;
		}
		
		// If no SD card or invalid dir name
		else {
			status = false;
		}
		return status;
	}
	
	/**
	 * Determine if SD card exists.
	 * 
	 * @return				T=exists, F=not found
	 */
	protected static boolean testSaveLocationExists() {
		String sDCardStatus = Environment.getExternalStorageState();
		boolean status;
		
		// If SD card is mounted
		if (sDCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			status = true;
		}
		
		// If no SD card
		else {
			status = false;
		}
		return status;
	}
	
	/**
	 * Delete directory.
	 * 
	 * @param fileName		The name of the directory to delete
	 * @return				T=deleted, F=could not delete
	 */
	protected static boolean deleteDirectory(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();
			
		// Make sure SD card exists
		if ((testSaveLocationExists()) && (!fileName.equals(""))) {	
			File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), fileName);
			checker.checkDelete(newPath.toString());
			
			// If dir to delete is really a directory
			if (newPath.isDirectory()) {
				String[] listfile = newPath.list();
				
				// Delete all files within the specified directory and then delete the directory
				try{
					for (int i=0; i < listfile.length; i++){
						File deletedFile = new File (newPath.toString()+"/"+listfile[i].toString());
						deletedFile.delete();
					}
					newPath.delete();
					Log.i("DirectoryManager deleteDirectory", fileName);
					status = true;
				}
				catch (Exception e){
					e.printStackTrace();
					status = false;
				}
			}
			
			// If dir not a directory, then error
			else {
				status = false;
			}
		}
		
		// If no SD card 
		else {
			status = false;
		}
		return status;
	}
	
	/**
	 * Delete file.
	 * 
	 * @param fileName				The name of the file to delete
	 * @return						T=deleted, F=not deleted
	 */
	protected static boolean deleteFile(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();
			
		// Make sure SD card exists
		if ((testSaveLocationExists()) && (!fileName.equals(""))) {
			File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), fileName);
			checker.checkDelete(newPath.toString());
			
			// If file to delete is really a file
			if (newPath.isFile()){
				try {
					Log.i("DirectoryManager deleteFile", fileName);
					newPath.delete();
					status = true;
				}catch (SecurityException se){
					se.printStackTrace();
					status = false;
				}
			}
			// If not a file, then error
			else {
				status = false;
			}
		}
		
		// If no SD card
		else {
			status = false;
		}
		return status;
	}
	
	/**
	 * Create a new file object from two file paths.
	 * 
	 * @param file1			Base file path
	 * @param file2			Remaining file path
	 * @return				File object
	 */
	private static File constructFilePaths (String file1, String file2) {
		File newPath;
		newPath = new File(file1+"/"+file2);
		return newPath;
	}

}