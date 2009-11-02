package com.phonegap.demo;

import android.provider.Contacts.People;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.database.Cursor;

public class ContactManager {
	
	Activity mApp;
	Uri mPeople = android.provider.Contacts.People.CONTENT_URI;
	Uri mPhone = android.provider.Contacts.Phones.CONTENT_URI;
	Uri mEmail = android.provider.Contacts.ContactMethods.CONTENT_EMAIL_URI;
	
	ContactManager(Activity app)
	{
		mApp = app;
	}
		
	private void getColumnData(Cursor cur){
		
		ContentResolver cr = mApp.getContentResolver();
			
	    if (cur.moveToFirst()) {

	        String name; 
	        String email;
	        String phoneNumber; 	        
	        int nameColumn = cur.getColumnIndex(People.NAME);
	        int phoneColumn = cur.getColumnIndex(People.NUMBER);
	        do {
	            // Get the field values
	            name = cur.getString(nameColumn);	            
	            phoneNumber = cur.getString(phoneColumn);
	            	            
	        } while (cur.moveToNext());

	    }
	}

	
	public void findContacts()
	{
		
		// Form an array specifying which columns to return. 
		String[] projection = new String[] {
		                             People._ID,
		                             People.NAME,
		                             People.NUMBER
		                             };
		
		// Make the query. 
		Cursor managedCursor = mApp.managedQuery(mPeople,
		                         projection, // Which columns to return 
		                         null,       // Which rows to return (all rows)
		                         null,       // Selection arguments (none)
		                         // Put the results in ascending order by name
		                         People.DISPLAY_NAME + " ASC");

		this.getColumnData(managedCursor);		
	}
	
}
