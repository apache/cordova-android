// Taken from Android tutorials
/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
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
	/**
	 * A static map that converts the JavaScript property name to Android database column name.
	 */
    private static final Map<String, String> dbMap = new HashMap<String, String>();
    static {
    	dbMap.put("id", People._ID);
    	dbMap.put("displayName", People.DISPLAY_NAME);
    	dbMap.put("phoneNumbers", Phones.NUMBER);
    	dbMap.put("phoneNumbers.value", Phones.NUMBER);
    	dbMap.put("emails", ContactMethods.DATA);
    	dbMap.put("emails.value", ContactMethods.DATA);
    	dbMap.put("addresses", ContactMethodsColumns.DATA);
    	dbMap.put("addresses.formatted", ContactMethodsColumns.DATA);
    	dbMap.put("ims", ContactMethodsColumns.DATA);
    	dbMap.put("ims.value", ContactMethodsColumns.DATA);
    	dbMap.put("organizations", Organizations.COMPANY);
    	dbMap.put("organizations.name", Organizations.COMPANY);
    	dbMap.put("organizations.title", Organizations.TITLE);
    	dbMap.put("note", People.NOTES);
    }
	
    /**
     * Create an contact accessor.
     */
	public ContactAccessorSdk3_4(WebView view, Activity app)
	{
		mApp = app;
		mView = view;
	}
	
	@Override
	/** 
	 * This method takes the fields required and search options in order to produce an 
	 * array of contacts that matches the criteria provided.
	 * @param fields an array of items to be used as search criteria
	 * @param options that can be applied to contact searching
	 * @return an array of contacts 
	 */
	public JSONArray search(JSONArray fields, JSONObject options) {
		String searchTerm = "";
		int limit = 1;
		boolean multiple = false;
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
		
    	ContentResolver cr = mApp.getContentResolver();
    	
    	Set<String> contactIds = buildSetOfContactIds(fields, searchTerm);
		HashMap<String,Boolean> populate = buildPopulationSet(fields);

    	Iterator<String> it = contactIds.iterator();
    		
    	JSONArray contacts = new JSONArray();
    	JSONObject contact;
    	String contactId;
    	int pos = 0;
    	while (it.hasNext() && (pos < limit)) {
    		contact = new JSONObject();
	    	try {	    		
	    		contactId = it.next();
		    	contact.put("id", contactId);
		    	
		    	// Do query for name and note
		        Cursor cur = cr.query(People.CONTENT_URI, 
					new String[] {People.DISPLAY_NAME, People.NOTES},
					"people._id = ?",
					new String[] {contactId},
					null);
		        cur.moveToFirst();
		    	
				if (isRequired("displayName",populate)) {
					contact.put("displayName", cur.getString(cur.getColumnIndex(People.DISPLAY_NAME)));		    	
				}
				if (isRequired("phoneNumbers",populate)) {
					contact.put("phoneNumbers", phoneQuery(cr, contactId));
				}
				if (isRequired("emails",populate)) {
					contact.put("emails", emailQuery(cr, contactId));
				}
				if (isRequired("addresses",populate)) {
					contact.put("addresses", addressQuery(cr, contactId));
				}
				if (isRequired("organizations",populate)) {
					contact.put("organizations", organizationQuery(cr, contactId));
				}
				if (isRequired("ims",populate)) {
					contact.put("ims", imQuery(cr, contactId));
				}
				if (isRequired("note",populate)) {
					contact.put("note", cur.getString(cur.getColumnIndex(People.NOTES)));
				}
				// nickname
				// urls
				// relationship
				// birthdays
				// anniversary
		    	
		    	pos++;
				cur.close();
	    	} catch (JSONException e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
	    	}
	    	contacts.put(contact);
        }
    	return contacts;
	}
	
	/**
	 * Query the database using the search term to build up a list of contact ID's 
	 * matching the search term
	 * @param fields
	 * @param searchTerm
	 * @return a set of contact ID's
	 */
	private Set<String> buildSetOfContactIds(JSONArray fields, String searchTerm) {
		Set<String> contactIds = new HashSet<String>();	
		
		String key;
		try {
			for (int i=0; i<fields.length(); i++) {
				key = fields.getString(i);
				if (key.startsWith("displayName")) {
					doQuery(searchTerm, contactIds,
							People.CONTENT_URI,
							People._ID,
							dbMap.get(key) + " LIKE ?",
							new String[] {searchTerm});
				}
//				else if (key.startsWith("name")) {
//					Log.d(LOG_TAG, "Doing " + key + " query");
//					doQuery(searchTerm, contactIds,
//							ContactsContract.Data.CONTENT_URI,
//							ContactsContract.Data.CONTACT_ID,
//							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
//							new String[] {searchTerm, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
//				}
				else if (key.startsWith("phoneNumbers")) {
					doQuery(searchTerm, contactIds,
							Phones.CONTENT_URI,
							Phones.PERSON_ID,
							dbMap.get(key) + " LIKE ?",
							new String[] {searchTerm});
				}
				else if (key.startsWith("emails")) {
					doQuery(searchTerm, contactIds,
							ContactMethods.CONTENT_EMAIL_URI,
							ContactMethods.PERSON_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactMethods.KIND + " = ?", 
							new String[] {searchTerm, ContactMethods.CONTENT_EMAIL_ITEM_TYPE});
				}
				else if (key.startsWith("addresses")) {
					doQuery(searchTerm, contactIds,
							ContactMethods.CONTENT_URI,
							ContactMethods.PERSON_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactMethods.KIND + " = ?",
							new String[] {searchTerm, ContactMethods.CONTENT_POSTAL_ITEM_TYPE});					
				}
				else if (key.startsWith("ims")) {
					doQuery(searchTerm, contactIds,
							ContactMethods.CONTENT_URI,
							ContactMethods.PERSON_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactMethods.KIND + " = ?",
							new String[] {searchTerm, ContactMethods.CONTENT_IM_ITEM_TYPE});					
				}
				else if (key.startsWith("organizations")) {
					doQuery(searchTerm, contactIds,
							Organizations.CONTENT_URI,
							ContactMethods.PERSON_ID,
							dbMap.get(key) + " LIKE ?",
							new String[] {searchTerm});					
				}
				else if (key.startsWith("note")) {
					doQuery(searchTerm, contactIds,
							People.CONTENT_URI,
							People._ID,
							dbMap.get(key) + " LIKE ?",
							new String[] {searchTerm});					
				}
			}
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		
		return contactIds;
	}

	/**
	 * A convenience method so we don't duplicate code in doQuery
	 * @param searchTerm
	 * @param contactIds
	 * @param uri
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 */
	private void doQuery(String searchTerm, Set<String> contactIds, 
			Uri uri, String projection, String selection, String[] selectionArgs) {
		ContentResolver cr = mApp.getContentResolver();

		Cursor cursor = cr.query(
				uri, 
				null,
				selection,
				selectionArgs,
				null);
		
		while (cursor.moveToNext()) {
			contactIds.add(cursor.getString(cursor.getColumnIndex(projection)));
		}
		cursor.close();
	}
	
	/**
	 * Create a ContactField JSONArray
	 * @param cr database access object
	 * @param contactId the ID to search the database for
	 * @return a JSONArray representing a set of ContactFields
	 */
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

	/**
	 * Create a ContactOrganization JSONArray
	 * @param cr database access object
	 * @param contactId the ID to search the database for
	 * @return a JSONArray representing a set of ContactOrganization
	 */
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
	
	/**
	 * Create a ContactAddress JSONArray
	 * @param cr database access object
	 * @param contactId the ID to search the database for
	 * @return a JSONArray representing a set of ContactAddress
	 */
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
	
	/**
	 * Create a ContactField JSONArray
	 * @param cr database access object
	 * @param contactId the ID to search the database for
	 * @return a JSONArray representing a set of ContactFields
	 */
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
	
	/**
	 * Create a ContactField JSONArray
	 * @param cr database access object
	 * @param contactId the ID to search the database for
	 * @return a JSONArray representing a set of ContactFields
	 */
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

	@Override
	public void save(JSONObject contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	/** 
	 * This method will remove a Contact from the database based on ID.
	 * @param id the unique ID of the contact to remove
	 */
	public boolean remove(String id) {
    	int result = mApp.getContentResolver().delete(People.CONTENT_URI, 
    			"people._id = ?", 
    			new String[] {id});
    	
    	return (result > 0) ? true : false;
	}
}