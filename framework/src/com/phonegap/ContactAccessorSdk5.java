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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
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
			if (searchTerm.length()==0) searchTerm = "%";
			multiple = options.getBoolean("multiple");
			if (multiple) {
				limit = options.getInt("limit");
			}
			
			System.out.println("Limit = " + limit);
			System.out.println("Multiple = " + multiple);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Get a cursor by creating the query.
		// TODO: parse name/number/email and dispatch to different query types.
		// Right now assumption is only name search. Lame but I'm on time constraints.
		ContentResolver cr = mApp.getContentResolver();
		Cursor cursor = cr.query(
				ContactsContract.Contacts.CONTENT_URI, 
				new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME},
				ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
				new String[] {searchTerm},
				ContactsContract.Contacts.DISPLAY_NAME + " ASC");
		JSONArray contacts = new JSONArray();
		int pos = 0;
		while (cursor.moveToNext() && (pos < limit)) {
			JSONObject contact = new JSONObject();
			
			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			if (contactName.trim().length() == 0) continue;
			
			try {
				contact.put("displayName", contactName);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
			//String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
			//if (Boolean.parseBoolean(hasPhone)) { 
			//}
			try {
				contact.put("name", nameQuery(cr, contactId));
				contact.put("phoneNumbers", phoneQuery(cr, contactId));
				contact.put("emails", emailQuery(cr, contactId));
				contact.put("addresses", addressQuery(cr, contactId));
				contact.put("organizations", organizationQuery(cr, contactId));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			contacts.put(contact);
			pos++;
		} 
		cursor.close();
		mView.loadUrl("javascript:navigator.service.contacts.droidDone('" + contacts.toString() + "');");
	}

	private JSONArray organizationQuery(ContentResolver cr, String contactId) {
		// TODO Fix the query URI
		Cursor cursor = cr.query( 
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				null, 
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
				null, null); 
		JSONArray organizations = new JSONArray();
		JSONObject organization = new JSONObject();
		while (cursor.moveToNext()) {
			Log.d(LOG_TAG, "We found a phone!");
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		cursor.close();
		return organizations;
	}

	private JSONArray addressQuery(ContentResolver cr, String contactId) {
		// TODO Fix the query URI
		Cursor cursor = cr.query( 
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				null, 
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
				null, null); 
		JSONArray addresses = new JSONArray();
		JSONObject address = new JSONObject();
		while (cursor.moveToNext()) {
			Log.d(LOG_TAG, "We found a phone!");
			try {
				address.put("formatted", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)));
				address.put("streetAddress", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
				address.put("locality", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
				address.put("region", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
				address.put("postalCode", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
				address.put("country", cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));
				addresses.put(address);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		cursor.close();
		return addresses;
	}

	private JSONObject nameQuery(ContentResolver cr, String contactId) {
		// TODO Fix the query URI
		Cursor name = cr.query( 
			ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
			null, 
			ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
			null, null); 
		JSONObject contactName = new JSONObject();
		if (name.moveToFirst()) {
			Log.d(LOG_TAG, "We found a name!");
			try {
				contactName.put("familyName", name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));
				contactName.put("givenName", name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
				contactName.put("middleName", name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)));
				contactName.put("honorificPrefix", name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX)));
				contactName.put("honorificSuffix", name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)));
				contactName.put("formatted", name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX))
						+ " " + name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
						+ " " + name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
						+ " " + name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
						+ " " + name.getString(name.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			Log.d(LOG_TAG, "We found a phone!");
			try {
				phoneNumber.put("primary", false); // Android does not store primary attribute
				phoneNumber.put("value", phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace('\'', '`'));
				phoneNumber.put("type", phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
				phoneNumbers.put(phoneNumber);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			Log.d(LOG_TAG, "We found an email!");
			try {
				email.put("primary", false);
				email.put("value", emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).replace('\'', '`'));
				email.put("type", emails.getInt(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
				emailAddresses.put(email);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		emails.close();
		return emailAddresses;
	}
	
}