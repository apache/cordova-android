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
	public void search(String name, String npa, String email) {
		if (name.length()==0) name = "%";
		// Get a cursor by creating the query.
		// TODO: parse name/number/email and dispatch to different query types.
		// Right now assumption is only name search. Lame but I'm on time constraints.
		ContentResolver cr = mApp.getContentResolver();
		Cursor cursor = cr.query(
				ContactsContract.Contacts.CONTENT_URI, 
				new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.DISPLAY_NAME},
				ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
				new String[] {name},
				ContactsContract.Contacts.DISPLAY_NAME + " ASC");		
		while (cursor.moveToNext()) {
			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			if (contactName.trim().length() == 0) continue;
			String phoneNumber = "null";
			String emailAddress = "null";
			
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
			if (Boolean.parseBoolean(hasPhone)) { 
				Cursor phones = cr.query( 
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
					null, 
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
					null, null); 
				if (phones.moveToFirst()) { 
					phoneNumber = "'" + phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace('\'', '`') + "'";                 
				} 
				phones.close(); 
			}
			Cursor emails = cr.query( 
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				null, 
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, 
				null, null); 
			if (emails.moveToFirst()) { 
				// This would allow you get several email addresses 
				emailAddress = "'" + emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).replace('\'', '`') + "'"; 
			} 
			emails.close();
			String contactAddJS = "javascript:navigator.contacts.droidFoundContact('" + contactName.replace('\'', '`') + "'," + phoneNumber + "," + emailAddress +")";
			mView.loadUrl(contactAddJS);
		} 
		cursor.close();
		mView.loadUrl("javascript:navigator.contacts.droidDone();");
	}
	
}