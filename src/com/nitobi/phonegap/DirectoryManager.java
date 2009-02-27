package com.nitobi.phonegap;

import java.io.File;

import android.os.Environment;

public class DirectoryManager {
	
	protected boolean isDirtoryOrFileExists (String name){
		boolean status;
		if ((testSaveLocationExists())&&(!name.equals(""))){
    		File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), name);
            status = newPath.exists();
    		
    	}else{
    		status = false;
    	}
		return status;
	}
	
	protected boolean createDirectory(String directoryName){
		boolean status;
		if ((testSaveLocationExists())&&(!directoryName.equals(""))){
			File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), directoryName);
			status = newPath.mkdir();
			status = true;
		}else
			status = false;
		return status;
	}
	
	protected boolean testSaveLocationExists(){
		String sDCardStatus = Environment.getExternalStorageState();
		boolean status;
		if (sDCardStatus.equals(Environment.MEDIA_MOUNTED)){
			status = true;
		}else
			status = false;
		return status;
	}
	
	protected boolean deleteDir (String fileName){
		boolean status;
		SecurityManager checker = new SecurityManager();
			
		if ((testSaveLocationExists())&&(!fileName.equals(""))){
		
			File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), fileName);
			checker.checkDelete(newPath.toString());
			if(newPath.isDirectory()){
				System.out.println("Dir = "+ fileName);
				String[] listfile = newPath.list();
				
				try{
					for (int i=0; i < listfile.length; i++){
						System.out.println(listfile[i].toString()+" length = "+listfile.length);
						File deletedFile = new File (newPath.toString()+"/"+listfile[i].toString());
						deletedFile.delete();
					}
				
					newPath.delete();
					status = true;
				}catch (Exception e){
					e.printStackTrace();
					status = false;
				}
				
			}else
				status = false;
		}else
			status = false;
		return status;
	
	}
	protected boolean deleteFile (String fileName){
		boolean status;
		SecurityManager checker = new SecurityManager();
			
		if ((testSaveLocationExists())&&(!fileName.equals(""))){
		
			File path = Environment.getExternalStorageDirectory();
            File newPath = constructFilePaths(path.toString(), fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isFile()){
				try {
					System.out.println("deleting the file");
					newPath.delete();
					status = true;
				}catch (SecurityException se){
					se.printStackTrace();
					status = false;
				}
			}else
				status = false;
		}else
			status = false;
		return status;
	
	}
	private File constructFilePaths (String file1, String file2){
		File newPath;
		newPath = new File(file1+"/"+file2);
		return newPath;
	}

}