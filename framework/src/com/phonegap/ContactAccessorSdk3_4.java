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
import android.provider.ContactsContract;
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
	
	public ContactAccessorSdk3_4(WebView view, Activity app)
	{
		mApp = app;
		mView = view;
	}
	
	@Override
	public void search(JSONArray filter, JSONObject options) {
		String searchTerm = "";
		int limit = Integer.MAX_VALUE;
		boolean multiple = true;
		try {
			searchTerm = options.getString("filter");
			if (searchTerm.length()==0) {
				searchTerm = "%";
			}
			else {
				searchTerm = "%" + searchTerm + "%";
			}
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

        // Right now we are just querying the displayName
        Cursor cur = cr.query(People.CONTENT_URI, 
			null,
			People.DISPLAY_NAME + " LIKE ?",
			new String[] {searchTerm},
			People.DISPLAY_NAME + " ASC");

		
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
		    	contact.put("emails", emailQuery(cr, contactId));
				// addresses
		    	contact.put("addresses", addressQuery(cr, contactId));
				// organizations
		    	contact.put("organizations", organizationQuery(cr, contactId));
				// ims
		    	contact.put("ims", imQuery(cr, contactId));
				// note
		    	contact.put("note", cur.getString(cur.getColumnIndex(People.NOTES)));
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
			im.put("primary", false);
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
				phone.put("primary", false);
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
				email.put("primary", false);
				email.put("value", cursor.getString(cursor.getColumnIndex(ContactMethods.DATA)));
				// TODO Find out why adding an email type throws and exception
				//email.put("type", cursor.getString(cursor.getColumnIndex(ContactMethods.TYPE)));
				emails.put(email);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		return emails;
	}
}