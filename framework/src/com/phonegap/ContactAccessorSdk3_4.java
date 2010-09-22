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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.ContactMethodsColumns;
import android.provider.Contacts.Organizations;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
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
	public void search(JSONArray filter, JSONObject options) {
		Log.d(LOG_TAG, "in 1.5+ search");
		String searchTerm = "";
		int limit = Integer.MAX_VALUE;
		boolean multiple = true;
		try {
			searchTerm = options.getString("filter");
			if (searchTerm.length()==0) searchTerm = "%";
			multiple = options.getBoolean("multiple");
			if (multiple) {
				limit = options.getInt("limit");
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		
		
		JSONArray contacts = new JSONArray();
		JSONObject contact;

        ContentResolver cr = mApp.getContentResolver();
        Cursor cur = cr.query(People.CONTENT_URI, 
			null, null, null, null);

        int pos = 0;
        while (cur.moveToNext() && pos < limit) {
	    	contact = new JSONObject();
	    	try {
	    		String contactId = cur.getString(cur.getColumnIndex(People._ID));
	    		// name
		    	contact.put("id", contactId);
		    	contact.put("displayName", cur.getString(cur.getColumnIndex(People.DISPLAY_NAME)));
		    	
				// phone number
		    	if (Integer.parseInt(cur.getString(cur.getColumnIndex(People.PRIMARY_PHONE_ID))) > 0) {
		    		contact.put("phoneNumbers", phoneQuery(cr, contactId));
		    	}
				// email
		    	//contact.put("emails", emailQuery(cr, contactId));
				// addresses
		    	contact.put("addresses", addressQuery(cr, contactId));
				// organizations
		    	contact.put("organizations", organizationQuery(cr, contactId));
				// ims
		    	contact.put("ims", imQuery(cr, contactId));
				// note
		    	cur.getString(cur.getColumnIndex(People.NOTES));
				// nickname
				// urls
				// relationship
				// birthdays
				// anniversary
		    	
		    	pos++;
	    	} catch (JSONException e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
	    	}
	    	contacts.put(contact);
        }
		cur.close();
		Log.d(LOG_TAG, "returning contacts string to javascript");
		mView.loadUrl("javascript:navigator.service.contacts.droidDone('" + contacts.toString() + "');");
	}
	
	private JSONArray imQuery(ContentResolver cr, String contactId) {
		String imWhere = ContactMethods.PERSON_ID 
        	+ " = ? AND " + ContactMethods.KIND + " = ?"; 
		String[] imWhereParams = new String[]{contactId, ContactMethods.CONTENT_IM_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactMethods.CONTENT_URI, 
		         null, imWhere, imWhereParams, null); 
		JSONArray ims = new JSONArray();
		JSONObject im;
		while (cursor.moveToNext()) {
			im = new JSONObject();
			try{
			im.put("value", cursor.getString(
					cursor.getColumnIndex(ContactMethodsColumns.DATA)));
			im.put("type", cursor.getString(
					cursor.getColumnIndex(ContactMethodsColumns.TYPE)));
			ims.put(im);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		cursor.close();
		return null;
	}

	private JSONArray organizationQuery(ContentResolver cr, String contactId) {
		String orgWhere = ContactMethods.PERSON_ID + " = ?"; 
		String[] orgWhereParams = new String[]{contactId}; 
		Cursor cursor = cr.query(Organizations.CONTENT_URI, 
	              null, orgWhere, orgWhereParams, null);
		JSONArray organizations = new JSONArray();
		JSONObject organization;
		while (cursor.moveToNext()) {
			organization = new JSONObject();
			try{
				organization.put("name", cursor.getString(cursor.getColumnIndex(Organizations.COMPANY)));
				organization.put("title", cursor.getString(cursor.getColumnIndex(Organizations.TITLE)));
				// organization.put("department", cursor.getString(cursor.getColumnIndex(Organizations)));
				// organization.put("description", cursor.getString(cursor.getColumnIndex(Organizations)));
				// organization.put("endDate", cursor.getString(cursor.getColumnIndex(Organizations)));
				// organization.put("location", cursor.getString(cursor.getColumnIndex(Organizations)));
				// organization.put("startDate", cursor.getString(cursor.getColumnIndex(Organizations)));
				organizations.put(organization);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		return organizations;
	}
	
	private JSONArray addressQuery(ContentResolver cr, String contactId) {
		String addrWhere = ContactMethods.PERSON_ID 
			+ " = ? AND " + ContactMethods.KIND + " = ?"; 
		String[] addrWhereParams = new String[]{contactId, 
		ContactMethods.CONTENT_POSTAL_ITEM_TYPE}; 		
		Cursor cursor = cr.query(ContactMethods.CONTENT_URI, 
				null, addrWhere, addrWhereParams, null); 
		JSONArray addresses = new JSONArray();
		JSONObject address;
		while (cursor.moveToNext()) {
			address = new JSONObject();
			try{
				address.put("formatted", cursor.getString(cursor.getColumnIndex(ContactMethodsColumns.DATA)));
				addresses.put(address);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		return addresses;
	}
	
	private JSONArray phoneQuery(ContentResolver cr, String contactId) {
		Cursor cursor = cr.query(
				Phones.CONTENT_URI, 
				null, 
				Phones.PERSON_ID +" = ?", 
				new String[]{contactId}, null);
		JSONArray phones = new JSONArray();
		JSONObject phone;
		while (cursor.moveToNext()) {
			phone = new JSONObject();
			try{
				phone.put("value", cursor.getString(cursor.getColumnIndex(Phones.NUMBER)));
				phone.put("type", cursor.getString(cursor.getColumnIndex(Phones.TYPE)));
				phones.put(phone);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		return phones;
	}
	
	private JSONArray emailQuery(ContentResolver cr, String contactId) {
		Cursor cursor = cr.query(
				ContactMethods.CONTENT_EMAIL_URI, 
				null, 
				ContactMethods.PERSON_ID +" = ?", 
				new String[]{contactId}, null);
		JSONArray emails = new JSONArray();
		JSONObject email;
		while (cursor.moveToNext()) {
			email = new JSONObject();
			try{
				email.put("value", cursor.getString(cursor.getColumnIndex(ContactMethods.DATA)));
				email.put("type", cursor.getString(cursor.getColumnIndex(ContactMethods.TYPE)));
				emails.put(email);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		return emails;
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
	            	mView.loadUrl("javascript:navigator.service.ContactManager.droidAddContact('" + name + "','" + phoneNumber + "','" + email +"')");	            	
	            else
	            	mView.loadUrl("javascript:navigator.service.contacts.droidFoundContact('" + name + "','" + phoneNumber + "','" + email +"')");
	            	            
	        } while (cur.moveToNext());
	        if (all)
	        	mView.loadUrl("javascript:navigator.service.ContactManager.droidDone()");
	        else
	        	mView.loadUrl("javascript:navigator.service.contacts.droidDone();");
	    }
	    else
	    {
	    	if(all)
	    		mView.loadUrl("javascript:navigator.service.ContactManager.fail()");
	    	else
	    		mView.loadUrl("javascript:navigator.service.contacts.fail('None found!')");
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
	            	mView.loadUrl("javascript:navigator.service.Contacts.droidFoundContact('" + data.name + "','" + data.phone + "','" + data.email +"')");
	            }	           
	        } while (cur.moveToNext());
	        mView.loadUrl("javascript:navigator.service.contacts.droidDoneContacts();");	        
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