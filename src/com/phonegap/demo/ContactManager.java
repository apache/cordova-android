package com.phonegap.demo;

import android.provider.Contacts.People;
import android.content.ContentUris;
import android.net.Uri;
import android.database.Cursor;

public class ContactManager {
		
	private void getColumnData(Cursor cur){ 
	    if (cur.moveToFirst()) {

	        String name; 
	        String phoneNumber; 
	        int nameColumn = cur.getColumnIndex(People.NAME); 
	        int phoneColumn = cur.getColumnIndex(People.NUMBER);
	        String imagePath; 
	    
	        do {
	            // Get the field values
	            name = cur.getString(nameColumn);
	            phoneNumber = cur.getString(phoneColumn);
	           	
	        } while (cur.moveToNext());

	    }
	}

	
}
