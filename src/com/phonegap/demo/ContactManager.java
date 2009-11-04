package com.phonegap.demo;

import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.util.Log;
import android.webkit.WebView;
import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

public class ContactManager {
	
	private static final String LOG_TAG = "Contact Query";
	Activity mApp;
	WebView mView;
	Uri mPeople = android.provider.Contacts.People.CONTENT_URI;
	Uri mPhone = android.provider.Contacts.Phones.CONTENT_URI;	
	Uri mEmail = android.provider.Contacts.ContactMethods.CONTENT_URI;
	
	ContactManager(Activity app, WebView view)
	{
		mApp = app;
		mView = view;
	}
			
	private void processResults(Cursor cur){
				
	    if (cur.moveToFirst()) {

	        String name; 
	        String phoneNumber;	        
	        String email;
	        
	        int nameColumn = cur.getColumnIndex(People.NAME);
	        int phoneColumn = cur.getColumnIndex(People.NUMBER);
	        int emailColumn = cur.getColumnIndex(ContactMethods.DATA);
	        do {
	            // Get the field values
	            name = cur.getString(nameColumn);	            
	            phoneNumber = cur.getString(phoneColumn);
	            email = cur.getString(emailColumn);
	            
	            mView.loadUrl("javascript:navigator.addressBook.droidFoundContact('" + name + "','" + phoneNumber + "','" + email +")");
	            	            
	        } while (cur.moveToNext());

	    }
	}
	
	public void search(String rawdata)
	{
		String conditions = "";						
		String name = "";
		String phone = "";
		String email = "";
		
		try {
			JSONObject data = new JSONObject(rawdata);

			if (data.has("givenName"))
				name += data.getString("givenName");			
			if (data.has("familyName"))
				name += data.getString("familyName");
			
			if (name.length() > 0)
			{
				conditions += "people.name = ?";
			}
				
			if (data.has("phone"))
			{
				phone = data.getString("phone");
				if(conditions.length() > 0)
					conditions += "AND ";
				conditions += "people.number LIKE ?";
			}
			if (data.has("email"))
			{
				email = data.getString("email");
				if(conditions.length() > 0)
					conditions += "AND ";
				conditions += "contact_methods.data = ?";
			}

			conditions += "AND contact_methods.kind = 1";			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] projection = new String[] {
				ContactMethods._ID,
				People.NAME,
				People.NUMBER,
				ContactMethods.DATA				
		};
		
		String[] params = new String[] { name, phone, email };
		
		Cursor myCursor = mApp.managedQuery(mEmail, projection, 
				conditions, params , ContactMethods.DATA + " ASC");
		
		processResults(myCursor);
	}
	
	
	private String getPhoneColumnData(Cursor cur){
		
		ContentResolver cr = mApp.getContentResolver();
			        
        String email = "";
        String kind; 
	    if (cur.moveToFirst()) {

	        int emailColumn = cur.getColumnIndex(ContactMethods.DATA);
	        int kindColumn = cur.getColumnIndex(ContactMethods.KIND);	        
	        do {
	            // Get the field values	            
	            email = cur.getString(emailColumn);
	            kind = cur.getString(kindColumn);
	            	            
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
		
		try{
		Cursor myCursor = mApp.managedQuery(mEmail, projection, 
						"contact_methods." + ContactMethods._ID + " = ?" + "AND contact_methods.kind = 1", variables , ContactMethods.DATA + " ASC");
		email = getPhoneColumnData(myCursor);
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}
		
		return email;		
	}
	
	
}
