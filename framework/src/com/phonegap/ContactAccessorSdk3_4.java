// Taken from Android tutorials
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonegap;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.util.Log;
import android.webkit.WebView;

/**
 * An implementation of {@link ContactAccessor} that uses legacy Contacts API.
 * These APIs are deprecated and should not be used unless we are running on a
 * pre-Eclair SDK.
 * <p>
 * There are several reasons why we wouldn't want to use this class on an Eclair device:
 * <ul>
 * <li>It would see at most one account, namely the first Google account created on the device.
 * <li>It would work through a compatibility layer, which would make it inherently less efficient.
 * <li>Not relevant to this particular example, but it would not have access to new kinds
 * of data available through current APIs.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class ContactAccessorSdk3_4 extends ContactAccessor {
	
	private Uri mPeople = android.provider.Contacts.People.CONTENT_URI;
	private Uri mPhone = android.provider.Contacts.Phones.CONTENT_URI;	
	private Uri mEmail = android.provider.Contacts.ContactMethods.CONTENT_URI;
	
	public ContactAccessorSdk3_4(WebView view, Activity app)
	{
		mApp = app;
		mView = view;
	}
	
	@Override
	public void search(String name, String npa, String email) {
		if (email.length() > 0)
			searchByEmail(email);		
		else
			searchPeople(name, npa);
	}
	
	private void searchByEmail(String email)
	{						
		String[] projection = new String[] {
								ContactMethods._ID,
								ContactMethods.DATA,
								ContactMethods.KIND,
								ContactMethods.PERSON_ID
							};
		String[] variables = new String[] {
				 email
		};
		
		try{
			Cursor myCursor = mApp.managedQuery(mEmail, projection, 
						"contact_methods." + ContactMethods.DATA + " = ?" + "AND contact_methods.kind = 1", variables , ContactMethods.DATA + " ASC");
			getMethodData(myCursor);
						
		}
		catch (SQLiteException ex)
		{
			Log.d(this.LOG_TAG, ex.getMessage());
		}
				
	}
	
	private void searchPeople(String name, String number)
	{
			String conditions = "";
		
			if (name.length() == 0)
			{
				name = "%";
				conditions += People.NAME + " LIKE ? AND ";
			}
			else
			{
				conditions += People.NAME + " = ? AND ";
			}
		
			if (number.length() == 0)
				number = "%";
			else
			{
				number = number.replace('+', '%');
				number = number.replace('.', '%');
				number = number.replace('-', '%');
			}
			
			conditions += People.NUMBER + " LIKE ? ";
			
			String[] projection = new String[] {
								People._ID,
								People.NAME,
								People.NUMBER,
								People.PRIMARY_EMAIL_ID
			};
			
			String[] variables = new String[] {
					name, number
			};
			
			try{
				Cursor myCursor = mApp.managedQuery(mPeople, projection, 
								conditions, variables , People.NAME + " ASC");
				processResults(myCursor, false);
			}
			catch (SQLiteException ex)
			{
					Log.d(this.LOG_TAG, ex.getMessage());
			}		
	
	}

	private void processResults(Cursor cur, boolean all){
		
	    if (cur.moveToFirst()) {

	        String name; 
	        String phoneNumber;	        
	        String email_id;
	        String email;
	        
	        int nameColumn = cur.getColumnIndex(People.NAME);
	        int phoneColumn = cur.getColumnIndex(People.NUMBER);
	        int emailIdColumn = cur.getColumnIndex(People.PRIMARY_EMAIL_ID);
	        
	        do {
	            // Get the field values
	            name = cur.getString(nameColumn);	            
	            phoneNumber = cur.getString(phoneColumn);
	            email_id = cur.getString(emailIdColumn);
	            if (email_id != null && email_id.length() > 0)
	            	email = getEmail(email_id);
	            else
	            	email = "";
	            
	            // Code for backwards compatibility with the OLD Contacts API
	            if (all)
	            	mView.loadUrl("javascript:navigator.ContactManager.droidAddContact('" + name + "','" + phoneNumber + "','" + email +"')");	            	
	            else
	            	mView.loadUrl("javascript:navigator.contacts.droidFoundContact('" + name + "','" + phoneNumber + "','" + email +"')");
	            	            
	        } while (cur.moveToNext());
	        if (all)
	        	mView.loadUrl("javascript:navigator.ContactManager.droidDone()");
	        else
	        	mView.loadUrl("javascript:navigator.contacts.droidDone();");
	    }
	    else
	    {
	    	if(all)
	    		mView.loadUrl("javascript:navigator.ContactManager.fail()");
	    	else
	    		mView.loadUrl("javascript:navigator.contacts.fail('None found!')");
	    }
	}	
	
	private void getMethodData(Cursor cur)
	{        
        ContactTriplet data = new ContactTriplet();
        String id;    
        String email;
        
	    if (cur.moveToFirst()) {

	        int idColumn = cur.getColumnIndex(ContactMethods._ID);
	        int emailColumn = cur.getColumnIndex(ContactMethods.DATA);	        
	        do {
	            // Get the field values	            
	            id = cur.getString(idColumn);
	            email = cur.getString(emailColumn);
	            
	            data = getContactData(id);
	            if(data != null)
	            {
	            	data.email = email;	            
	            	mView.loadUrl("javascript:navigator.Contacts.droidFoundContact('" + data.name + "','" + data.phone + "','" + data.email +"')");
	            }	           
	        } while (cur.moveToNext());
	        mView.loadUrl("javascript:navigator.contacts.droidDoneContacts();");	        
	    }	 
	}		
	
	private ContactTriplet getContactData(String id) {
		ContactTriplet data = null;
		String[] projection = new String[] {
				People._ID,
				People.NAME,
				People.NUMBER,
				People.PRIMARY_EMAIL_ID
		};

		String[] variables = new String[] {
				id
		};

		try{
			Cursor myCursor = mApp.managedQuery(mPeople, projection, 
				People.PRIMARY_EMAIL_ID + " = ?", variables , People.NAME + " ASC");
			data = getTriplet(myCursor);
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}		
		
		return data;
	}

	private ContactTriplet getTriplet(Cursor cur) {
		 ContactTriplet data = new ContactTriplet();         
		 if (cur.moveToFirst()) {

			 int nameColumn = cur.getColumnIndex(People.NAME); 
		     int numberColumn = cur.getColumnIndex(People.NUMBER);	        
		     do {
		         	            
		    	 data.name = cur.getString(nameColumn);
		    	 data.phone = cur.getString(numberColumn);
		    	 
		     } while (cur.moveToNext());	       
		 }
		return data;
	}

	private String getEmailColumnData(Cursor cur)
	{					        
        String email = "";         
	    if (cur != null && cur.moveToFirst()) {
	        int emailColumn = cur.getColumnIndex(ContactMethods.DATA);	        
	        do {
	            // Get the field values	            
	            email = cur.getString(emailColumn);	            	            
	        } while (cur.moveToNext());	       
	    }
	    return email;
	}
	
	private String getEmail(String id)
	{		
		String email = "";		
		String[] projection = new String[] {
								ContactMethods._ID,
								ContactMethods.DATA,
								ContactMethods.KIND
							};
		String[] variables = new String[] {
				 id
		};
		
		try
		{
			Cursor myCursor = mApp.managedQuery(mEmail, projection, 
						"contact_methods." + ContactMethods._ID + " = ?" + " AND contact_methods.kind = 1", variables , ContactMethods.DATA + " ASC");
			email = getEmailColumnData(myCursor);
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}
		
		return email;		
	}
}