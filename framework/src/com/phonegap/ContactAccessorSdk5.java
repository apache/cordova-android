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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.webkit.WebView;

/**
 * An implementation of {@link ContactAccessor} that uses current Contacts API.
 * This class should be used on Eclair or beyond, but would not work on any earlier
 * release of Android.  As a matter of fact, it could not even be loaded.
 * <p>
 * This implementation has several advantages:
 * <ul>
 * <li>It sees contacts from multiple accounts.
 * <li>It works with aggregated contacts. So for example, if the contact is the result
 * of aggregation of two raw contacts from different accounts, it may return the name from
 * one and the phone number from the other.
 * <li>It is efficient because it uses the more efficient current API.
 * <li>Not obvious in this particular example, but it has access to new kinds
 * of data available exclusively through the new APIs. Exercise for the reader: add support
 * for nickname (see {@link android.provider.ContactsContract.CommonDataKinds.Nickname}) or
 * social status updates (see {@link android.provider.ContactsContract.StatusUpdates}).
 * </ul>
 */
public class ContactAccessorSdk5 extends ContactAccessor {
	
	/**
	 * A static map that converts the JavaScript property name to Android database column name.
	 */
    private static final Map<String, String> dbMap = new HashMap<String, String>();
    static {
    	dbMap.put("id", ContactsContract.Contacts._ID);
    	dbMap.put("displayName", ContactsContract.Contacts.DISPLAY_NAME);
    	dbMap.put("name", ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
    	dbMap.put("name.formatted", ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
    	dbMap.put("name.familyName", ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
    	dbMap.put("name.givenName", ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
    	dbMap.put("name.middleName", ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
    	dbMap.put("name.honorificPrefix", ContactsContract.CommonDataKinds.StructuredName.PREFIX);
    	dbMap.put("name.honorificSuffix", ContactsContract.CommonDataKinds.StructuredName.SUFFIX);
    	dbMap.put("nickname", ContactsContract.CommonDataKinds.Nickname.NAME);
    	dbMap.put("phoneNumbers", ContactsContract.CommonDataKinds.Phone.NUMBER);
    	dbMap.put("phoneNumbers.value", ContactsContract.CommonDataKinds.Phone.NUMBER);
    	dbMap.put("emails", ContactsContract.CommonDataKinds.Email.DATA);
    	dbMap.put("emails.value", ContactsContract.CommonDataKinds.Email.DATA);
    	dbMap.put("addresses", ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
    	dbMap.put("addresses.formatted", ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
    	dbMap.put("addresses.streetAddress", ContactsContract.CommonDataKinds.StructuredPostal.STREET);
    	dbMap.put("addresses.locality", ContactsContract.CommonDataKinds.StructuredPostal.CITY);
    	dbMap.put("addresses.region", ContactsContract.CommonDataKinds.StructuredPostal.REGION);
    	dbMap.put("addresses.postalCode", ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
    	dbMap.put("addresses.country", ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
    	dbMap.put("ims", ContactsContract.CommonDataKinds.Im.DATA);
    	dbMap.put("ims.value", ContactsContract.CommonDataKinds.Im.DATA);
    	dbMap.put("organizations", ContactsContract.CommonDataKinds.Organization.COMPANY);
    	dbMap.put("organizations.name", ContactsContract.CommonDataKinds.Organization.COMPANY);
    	dbMap.put("organizations.department", ContactsContract.CommonDataKinds.Organization.DEPARTMENT);
    	dbMap.put("organizations.title", ContactsContract.CommonDataKinds.Organization.TITLE);
    	dbMap.put("organizations.location", ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION);
    	dbMap.put("organizations.description", ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION);
    	//dbMap.put("published", null);
    	//dbMap.put("updated", null);
    	dbMap.put("birthday", ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
    	dbMap.put("anniversary", ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
    	//dbMap.put("gender", null);
    	dbMap.put("note", ContactsContract.CommonDataKinds.Note.NOTE);
    	//dbMap.put("preferredUsername", null);
    	//dbMap.put("photos.value", null);
    	//dbMap.put("tags.value", null);
    	dbMap.put("relationships", ContactsContract.CommonDataKinds.Relation.NAME);
    	dbMap.put("relationships.value", ContactsContract.CommonDataKinds.Relation.NAME);
    	dbMap.put("urls", ContactsContract.CommonDataKinds.Website.URL);
    	dbMap.put("urls.value", ContactsContract.CommonDataKinds.Website.URL);
    	//dbMap.put("accounts.domain", null);
    	//dbMap.put("accounts.username", null);
    	//dbMap.put("accounts.userid", null);
    	//dbMap.put("utcOffset", null);
    	//dbMap.put("connected", null);
    }

    /**
     * Create an contact accessor.
     */
    public ContactAccessorSdk5(WebView view, Activity app) {
		mApp = app;
		mView = view;
	}
	
	/** 
	 * This method takes the fields required and search options in order to produce an 
	 * array of contacts that matches the criteria provided.
	 * @param fields an array of items to be used as search criteria
	 * @param options that can be applied to contact searching
	 * @return an array of contacts 
	 */
	@Override
	public JSONArray search(JSONArray fields, JSONObject options) {
		long totalEnd;
		long totalStart = System.currentTimeMillis();

		// Get the find options
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

		// Loop through the fields the user provided to see what data should be returned.
		HashMap<String,Boolean> populate = buildPopulationSet(fields);
		
		// Build the ugly where clause and where arguments for one big query.
		WhereOptions whereOptions = buildWhereClause(fields, searchTerm);
			
		// Get all the rows where the search term matches the fields passed in.
		Cursor c = mApp.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
				null,
				whereOptions.getWhere(),
				whereOptions.getWhereArgs(),
				ContactsContract.Data.CONTACT_ID + " ASC");				

		String contactId = "";
		String oldContactId = "";
		boolean newContact = true;
		String mimetype = "";

		JSONArray contacts = new JSONArray();
		JSONObject contact = new JSONObject();
		JSONArray organizations = new JSONArray();
		JSONArray addresses = new JSONArray();
		JSONArray phones = new JSONArray();
		JSONArray emails = new JSONArray();
		JSONArray ims = new JSONArray();
		JSONArray websites = new JSONArray();
		JSONArray relationships = new JSONArray();			
		
		while (c.moveToNext() && (contacts.length() < (limit-1))) {					
			try {
				contactId = c.getString(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
				
				// If we are in the first row set the oldContactId
				if (c.getPosition() == 0) {
					oldContactId = contactId;
				}
				
				// When the contact ID changes we need to push the Contact object 
				// to the array of contacts and create new objects.
				if (!oldContactId.equals(contactId)) {
					// Populate the Contact object with it's arrays
					// and push the contact into the contacts array
					contacts.put(populateContact(contact, organizations, addresses, phones,
							emails, ims, websites, relationships));
					
					// Clean up the objects
					contact = new JSONObject();
					organizations = new JSONArray();
					addresses = new JSONArray();
					phones = new JSONArray();
					emails = new JSONArray();
					ims = new JSONArray();
					websites = new JSONArray();
					relationships = new JSONArray();
					
					// Set newContact to true as we are starting to populate a new contact
					newContact = true;
				}
				
				// When we detect a new contact set the ID and display name.
				// These fields are available in every row in the result set returned.
				if (newContact) {
					newContact = false;
					contact.put("id", contactId);
					contact.put("displayName", c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
				}
				
				// Grab the mimetype of the current row as it will be used in a lot of comparisons
				mimetype = c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE));
				
				if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) 
						&& isRequired("name",populate)) {
					contact.put("name", nameQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) 
						&& isRequired("phoneNumbers",populate)) {
					phones.put(phoneQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) 
						&& isRequired("emails",populate)) {
					emails.put(emailQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE) 
						&& isRequired("addresses",populate)) {
					addresses.put(addressQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE) 
						&& isRequired("organizations",populate)) {
					organizations.put(organizationQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE) 
						&& isRequired("ims",populate)) {
					ims.put(imQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE) 
						&& isRequired("note",populate)) {
					contact.put("note",c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE) 
						&& isRequired("nickname",populate)) {
					contact.put("nickname",c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE) 
						&& isRequired("urls",populate)) {
					websites.put(websiteQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE) 
						&& isRequired("relationships",populate)) {
					relationships.put(relationshipQuery(c));
				}
				else if (mimetype.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
					if (ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY == c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)) 
							&& isRequired("anniversary",populate)) {
						contact.put("anniversary", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)));
					}
					else if (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY == c.getInt(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.TYPE)) 
							&& isRequired("birthday",populate)) {
						contact.put("birthday", c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)));
					}
				}
			}
			catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(),e);
			}
			
			// Set the old contact ID 
			oldContactId = contactId;			
		}
		c.close();
		
		// Push the last contact into the contacts array
		contacts.put(populateContact(contact, organizations, addresses, phones,
				emails, ims, websites, relationships));
		
		totalEnd = System.currentTimeMillis();
		Log.d(LOG_TAG,"Total time = " + (totalEnd-totalStart));
		return contacts;
	}

	/**
	 * Create a new contact using a JSONObject to hold all the data. 
	 * @param contact 
	 * @param organizations array of organizations
	 * @param addresses array of addresses
	 * @param phones array of phones
	 * @param emails array of emails
	 * @param ims array of instant messenger addresses
	 * @param websites array of websites
	 * @param relationships array of relationships
	 * @return
	 */
	private JSONObject populateContact(JSONObject contact, JSONArray organizations,
			JSONArray addresses, JSONArray phones, JSONArray emails,
			JSONArray ims, JSONArray websites, JSONArray relationships) {
		try {
			contact.put("organizations", organizations);
			contact.put("addresses", addresses);
			contact.put("phoneNumbers", phones);
			contact.put("emails", emails);
			contact.put("ims", ims);
			contact.put("websites", websites);
			contact.put("relationships", relationships);
		}
		catch (JSONException e) {
			Log.e(LOG_TAG,e.getMessage(),e);
		}
		return contact;
	}

	/**
	 * Take the search criteria passed into the method and create a SQL WHERE clause.
	 * @param fields the properties to search against
	 * @param searchTerm the string to search for
	 * @return an object containing the selection and selection args
	 */
	private WhereOptions buildWhereClause(JSONArray fields, String searchTerm) {

		ArrayList<String> where = new ArrayList<String>();
		ArrayList<String> whereArgs = new ArrayList<String>();
		
		WhereOptions options = new WhereOptions();

		/*
		 * Special case for when the user wants all the contacts
		 */
		if ("%".equals(searchTerm)) {
			options.setWhere("(" + ContactsContract.Contacts.DISPLAY_NAME + " LIKE ? )");
			options.setWhereArgs(new String[] {searchTerm});
			return options;
		}		
		
		String key;
		try {
			for (int i=0; i<fields.length(); i++) {
				key = fields.getString(i);

				if (key.startsWith("displayName")) {
					where.add("(" + dbMap.get(key) + " LIKE ? )");
					whereArgs.add(searchTerm);
				}
				else if (key.startsWith("name")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("nickname")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("phoneNumbers")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("emails")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("addresses")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("ims")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("organizations")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
				}
//				else if (key.startsWith("birthday")) {
//					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
//							+ ContactsContract.Data.MIMETYPE + " = ? )");									
//				}
//				else if (key.startsWith("anniversary")) {
//					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
//							+ ContactsContract.Data.MIMETYPE + " = ? )");				
//					whereArgs.add(searchTerm);
//					whereArgs.add(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
//				}
				else if (key.startsWith("note")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("relationships")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE);
				}
				else if (key.startsWith("urls")) {
					where.add("(" + dbMap.get(key) + " LIKE ? AND " 
							+ ContactsContract.Data.MIMETYPE + " = ? )");				
					whereArgs.add(searchTerm);
					whereArgs.add(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
				}
			}
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}

		// Creating the where string
		StringBuffer selection = new StringBuffer();
		for (int i=0; i<where.size(); i++) {
			selection.append(where.get(i));
			if (i != (where.size()-1)) {
				selection.append(" OR ");
			}
		}
		options.setWhere(selection.toString());

		// Creating the where args array
		String[] selectionArgs = new String[whereArgs.size()];
		for (int i=0; i<whereArgs.size(); i++) {
			selectionArgs[i] = whereArgs.get(i);
		}
		options.setWhereArgs(selectionArgs);
		
		return options;
	}

	/**
	 * Create a ContactOrganization JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactOrganization
	 */
	private JSONObject organizationQuery(Cursor cursor) {
		JSONObject organization = new JSONObject();
		try {
			organization.put("department", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT)));
			organization.put("description", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION)));
			// TODO No endDate
			// organization.put("endDate", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization)));
			organization.put("location", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION)));
			organization.put("name", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)));
			// TODO no startDate
			// organization.put("startDate", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization)));
			organization.put("title", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return organization;
	}

	/**
	 * Create a ContactAddress JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactAddress
	 */
	private JSONObject addressQuery(Cursor cursor) {
		JSONObject address = new JSONObject();
		try {
			address.put("formatted", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)));
			address.put("streetAddress", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
			address.put("locality", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
			address.put("region", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
			address.put("postalCode", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
			address.put("country", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return address;
	}

	/**
	 * Create a ContactName JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactName
	 */
	private JSONObject nameQuery(Cursor cursor) {
		JSONObject contactName = new JSONObject();
		try {
			String familyName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
			String givenName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
			String middleName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
			String honorificPrefix = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
			String honorificSuffix = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));

			// Create the formatted name
			StringBuffer formatted = new StringBuffer("");
			if (honorificPrefix != null) { formatted.append(honorificPrefix + " "); }
			if (givenName != null) { formatted.append(givenName + " "); }
			if (middleName != null) { formatted.append(middleName + " "); }
			if (familyName != null) { formatted.append(familyName + " "); }
			if (honorificSuffix != null) { formatted.append(honorificSuffix + " "); }
			
			contactName.put("familyName", familyName);
			contactName.put("givenName", givenName);
			contactName.put("middleName", middleName);
			contactName.put("honorificPrefix", honorificPrefix);
			contactName.put("honorificSuffix", honorificSuffix);
			contactName.put("formatted", formatted);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return contactName;
	}

	/**
	 * Create a ContactField JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactField
	 */
	private JSONObject phoneQuery(Cursor cursor) {
		JSONObject phoneNumber = new JSONObject();
		try {
			phoneNumber.put("primary", false); // Android does not store primary attribute
			phoneNumber.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
			phoneNumber.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		catch (Exception excp) {
			Log.e(LOG_TAG, excp.getMessage(), excp);
		} 
		return phoneNumber;
	}

	/**
	 * Create a ContactField JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactField
	 */
	private JSONObject emailQuery(Cursor cursor) {
		JSONObject email = new JSONObject();
		try {
			email.put("primary", false); // Android does not store primary attribute
			email.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
			email.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return email;
	}

	/**
	 * Create a ContactField JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactField
	 */
	private JSONObject imQuery(Cursor cursor) {
		JSONObject im = new JSONObject();
		try {
			im.put("primary", false); // Android does not store primary attribute
			im.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA)));
			im.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return im;
	}	

	/**
	 * Create a ContactField JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactField
	 */
	private JSONObject websiteQuery(Cursor cursor) {
		JSONObject website = new JSONObject();
		try {
			website.put("primary", false); // Android does not store primary attribute
			website.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL)));
			website.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return website;
	}	

	/**
	 * Create a ContactField JSONObject
	 * @param cursor the current database row
	 * @return a JSONObject representing a ContactField
	 */
	private JSONObject relationshipQuery(Cursor cursor) {
		JSONObject relationship = new JSONObject();
		try {
			relationship.put("primary", false); // Android does not store primary attribute
			relationship.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.NAME)));
			relationship.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE)));
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return relationship;
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
    	int result = mApp.getContentResolver().delete(ContactsContract.Data.CONTENT_URI, 
    			ContactsContract.Data.CONTACT_ID + " = ?", 
    			new String[] {id});    	
    	return (result > 0) ? true : false;
	}	
}