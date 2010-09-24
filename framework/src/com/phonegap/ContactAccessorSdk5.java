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
	
	private static final String WHERE_STRING = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

	public ContactAccessorSdk5(WebView view, Activity app)
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
		// Get a cursor by creating the query.
		ContentResolver cr = mApp.getContentResolver();
		
		// Right now we are just querying the displayName
		Cursor cursor = cr.query(
				ContactsContract.Contacts.CONTENT_URI, 
				new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME},
				ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
				new String[] {searchTerm},
				ContactsContract.Contacts.DISPLAY_NAME + " ASC");
		JSONArray contacts = new JSONArray();
		JSONObject contact;
		int pos = 0;
		while (cursor.moveToNext() && (pos < limit)) {
			contact = new JSONObject();
			
			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			if (contactName.trim().length() == 0) continue;
						
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
			try {
				contact.put("id", contactId);
				contact.put("displayName", contactName);
				contact.put("name", nameQuery(cr, contactId));
				contact.put("phoneNumbers", phoneQuery(cr, contactId));
				contact.put("emails", emailQuery(cr, contactId));
				contact.put("addresses", addressQuery(cr, contactId));
				contact.put("organizations", organizationQuery(cr, contactId));
				contact.put("ims",imQuery(cr, contactId));
				contact.put("note",noteQuery(cr, contactId));
				contact.put("nickname",nicknameQuery(cr, contactId));
				contact.put("urls",websiteQuery(cr, contactId));
				contact.put("relationships",relationshipQuery(cr, contactId));
				contact.put("birthday",birthdayQuery(cr, contactId));
				contact.put("anniversary",anniversaryQuery(cr, contactId));
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}

			contacts.put(contact);
			pos++;
		} 
		cursor.close();
		mView.loadUrl("javascript:navigator.service.contacts.droidDone('" + contacts.toString() + "');");
	}

	private JSONArray organizationQuery(ContentResolver cr, String contactId) {
	 	String[] orgWhereParams = new String[]{contactId, 
	 		ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}; 
	 	Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, orgWhereParams, null);
		JSONArray organizations = new JSONArray();
		JSONObject organization = new JSONObject();
		while (cursor.moveToNext()) {
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
				organizations.put(organization);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		cursor.close();
		return organizations;
	}

	private JSONArray addressQuery(ContentResolver cr, String contactId) {
		String[] addrWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, addrWhereParams, null); 
		JSONArray addresses = new JSONArray();
		JSONObject address = new JSONObject();
		while (cursor.moveToNext()) {
			try {
				address.put("formatted", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)));
				address.put("streetAddress", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
				address.put("locality", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
				address.put("region", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
				address.put("postalCode", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
				address.put("country", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
				addresses.put(address);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		cursor.close();
		return addresses;
	}

	private JSONObject nameQuery(ContentResolver cr, String contactId) {
		String[] addrWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}; 
		Cursor name = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, addrWhereParams, null); 
		JSONObject contactName = new JSONObject();
		if (name.moveToFirst()) {
			try {
				String familyName = name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
				String givenName = name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
				String middleName = name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
				String honorificPrefix = name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
				String honorificSuffix = name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));

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
		} 
		name.close();
		return contactName;
	}

	private JSONArray phoneQuery(ContentResolver cr, String contactId) {
		Cursor phones = cr.query( 
			ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
			null, 
			ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
			null, null); 
		JSONArray phoneNumbers = new JSONArray();
		JSONObject phoneNumber = new JSONObject();
		while (phones.moveToNext()) {
			try {
				phoneNumber.put("primary", false); // Android does not store primary attribute
				phoneNumber.put("value", phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				phoneNumber.put("type", phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
				phoneNumbers.put(phoneNumber);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		phones.close();
		return phoneNumbers;
	}

	private JSONArray emailQuery(ContentResolver cr, String contactId) {
		Cursor emails = cr.query( 
			ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
			null, 
			ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, 
			null, null); 
		JSONArray emailAddresses = new JSONArray();
		JSONObject email = new JSONObject();
		while (emails.moveToNext()) { 
			try {
				email.put("primary", false); // Android does not store primary attribute
				email.put("value", emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
				email.put("type", emails.getInt(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
				emailAddresses.put(email);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		emails.close();
		return emailAddresses;
	}

	private JSONArray imQuery(ContentResolver cr, String contactId) {
		String[] addrWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, addrWhereParams, null); 
		JSONArray ims = new JSONArray();
		JSONObject im = new JSONObject();
		while (cursor.moveToNext()) { 
			try {
				im.put("primary", false); // Android does not store primary attribute
				im.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA)));
				im.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE)));
				ims.put(im);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		cursor.close();
		return ims;
	}	

	private String noteQuery(ContentResolver cr, String contactId) {
		String[] noteWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, noteWhereParams, null); 
		String note = new String("");
		if (cursor.moveToFirst()) { 
			note = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
		} 
		cursor.close();
		return note;
	}	

	private String nicknameQuery(ContentResolver cr, String contactId) {
		String[] nicknameWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, nicknameWhereParams, null); 
		String nickname = new String("");
		if (cursor.moveToFirst()) { 
			nickname = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
		} 
		cursor.close();
		return nickname;
	}	

	private JSONArray websiteQuery(ContentResolver cr, String contactId) {
		String[] websiteWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, websiteWhereParams, null); 
		JSONArray websites = new JSONArray();
		JSONObject website = new JSONObject();
		while (cursor.moveToNext()) { 
			try {
				website.put("primary", false); // Android does not store primary attribute
				website.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL)));
				website.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE)));
				websites.put(website);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		cursor.close();
		return websites;
	}	

	private JSONArray relationshipQuery(ContentResolver cr, String contactId) {
		String[] relationshipWhereParams = new String[]{contactId, 
			ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, relationshipWhereParams, null); 
		JSONArray relationships = new JSONArray();
		JSONObject relationship = new JSONObject();
		while (cursor.moveToNext()) { 
			try {
				relationship.put("primary", false); // Android does not store primary attribute
				relationship.put("value", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.NAME)));
				relationship.put("type", cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.TYPE)));
				relationships.put(relationship);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage(), e);
			}
		} 
		cursor.close();
		return relationships;
	}	

	private String birthdayQuery(ContentResolver cr, String contactId) {
		String birthday = conditionalStringQuery(cr, contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, 
				ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY, ContactsContract.CommonDataKinds.Event.TYPE, 
				ContactsContract.CommonDataKinds.Event.START_DATE);
		return birthday;
	}	

	private String anniversaryQuery(ContentResolver cr, String contactId) {
		String anniversary = conditionalStringQuery(cr, contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, 
				ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY, ContactsContract.CommonDataKinds.Event.TYPE, 
				ContactsContract.CommonDataKinds.Event.START_DATE);
		return anniversary;
	}	

	private String conditionalStringQuery(ContentResolver cr, String contactId, String dataType, int type, String label, String data) {
		String[] whereParams = new String[]{contactId, dataType}; 
		Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                null, WHERE_STRING, whereParams, null); 
		String retVal = new String("");
		while (cursor.moveToNext()) { 
			if (type == cursor.getInt(cursor.getColumnIndex(label))) {
				retVal = cursor.getString(cursor.getColumnIndex(data));
			}
		} 
		cursor.close();
		return retVal;
	}	
}