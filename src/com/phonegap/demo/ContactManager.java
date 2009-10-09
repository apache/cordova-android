package com.phonegap.demo;

import android.provider.Contacts.People;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.database.Cursor;

public class ContactManager {
		
	public void grabContacts()
	{
		Uri people = android.provider.Contacts.People.CONTENT_URI;
		Uri phone = android.provider.Contacts.Phones.CONTENT_URI;		
	}
	
	private void getColumnData(Cursor cur){
		
		ContentResolver cr = getContentResolver();
		
		
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
