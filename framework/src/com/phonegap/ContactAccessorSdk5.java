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
		try {
			searchTerm = options.getString("filter");
			if (searchTerm.length()==0) searchTerm = "%";
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
		while (cursor.moveToNext()) {
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
				Cursor phones = cr.query( 
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
					null, 
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
					null, null); 
				if (phones.moveToFirst()) {
					Log.d(LOG_TAG, "We found a phone!");
					JSONArray phoneNumbers = new JSONArray();
					JSONObject phoneNumber = new JSONObject();
					try {
						phoneNumber.put("primary", true);
						phoneNumber.put("value", phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace('\'', '`'));
						phoneNumbers.put(phoneNumber);
						contact.put("phoneNumbers", phoneNumbers);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
				phones.close(); 
			//}
			Cursor emails = cr.query( 
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				null, 
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, 
				null, null); 
			if (emails.moveToFirst()) { 
				Log.d(LOG_TAG, "We found an email!");
				JSONArray emailAddresses = new JSONArray();
				JSONObject email = new JSONObject();
				try {
					email.put("primary", true);
					email.put("value", emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).replace('\'', '`'));
					emailAddresses.put(email);
					contact.put("emails", emailAddresses);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
			emails.close();
			contacts.put(contact);
		} 
		cursor.close();
		mView.loadUrl("javascript:navigator.service.contacts.droidDone('" + contacts.toString() + "');");
	}
	
}