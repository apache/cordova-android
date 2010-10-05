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

    public ContactAccessorSdk5(WebView view, Activity app) {
		mApp = app;
		mView = view;
	}
	
	@Override
	public JSONArray search(JSONArray filter, JSONObject options) {
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
		
		Set<String> contactIds = buildSetOfContactIds(filter, searchTerm);
		
		Iterator<String> it = contactIds.iterator();
		
		JSONArray contacts = new JSONArray();
		JSONObject contact;
		String contactId;
		int pos = 0;
		while (it.hasNext() && (pos < limit)) {
			contact = new JSONObject();
			contactId = it.next();
			
			try {
				contact.put("id", contactId);
				contact.put("displayName", displayNameQuery(cr, contactId));
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
			Log.d(LOG_TAG, "putting in contact ID = " + contactId);

			contacts.put(contact);
			pos++;
		}
		return contacts;
	}
	
	private Set<String> buildSetOfContactIds(JSONArray filter, String searchTerm) {
		Set<String> contactIds = new HashSet<String>();	
		
		/*
		 * Special case for when the user wants all the contacts
		 */
		if ("%".equals(searchTerm)) {
			doQuery(searchTerm, contactIds,
					ContactsContract.Contacts.CONTENT_URI,
					ContactsContract.Contacts._ID,
					ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
					new String[] {searchTerm});
			return contactIds;
		}
		
		String key;
		try {
			for (int i=0; i<filter.length(); i++) {
				key = filter.getString(i);
				if (key.startsWith("displayName")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Contacts.CONTENT_URI,
							ContactsContract.Contacts._ID,
							dbMap.get(key) + " LIKE ?",
							new String[] {searchTerm});
				}
				else if (key.startsWith("name")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
				}
				else if (key.startsWith("nickname")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE});					
				}
				else if (key.startsWith("phoneNumbers")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
							dbMap.get(key) + " LIKE ?",
							new String[] {searchTerm});
				}
				else if (key.startsWith("emails")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID,
							dbMap.get(key) + " LIKE ?", 
							new String[] {searchTerm});
				}
				else if (key.startsWith("addresses")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE});					
				}
				else if (key.startsWith("ims")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE});					
				}
				else if (key.startsWith("organizations")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE});					
				}
				else if (key.startsWith("birthday")) {
					
				}
				else if (key.startsWith("anniversary")) {
					
				}
				else if (key.startsWith("note")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE});					
				}
				else if (key.startsWith("relationships")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE});					
				}
				else if (key.startsWith("urls")) {
					doQuery(searchTerm, contactIds,
							ContactsContract.Data.CONTENT_URI,
							ContactsContract.Data.CONTACT_ID,
							dbMap.get(key) + " LIKE ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
							new String[] {searchTerm, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE});					
				}
			}
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		
		return contactIds;
	}

	private void doQuery(String searchTerm, Set<String> contactIds, 
			Uri uri, String projection, String selection, String[] selectionArgs) {
		// Get a cursor by creating the query.
		ContentResolver cr = mApp.getContentResolver();

		Cursor cursor = cr.query(
				uri, 
				new String[] {projection},
				selection,
				selectionArgs,
				null);
		
		while (cursor.moveToNext()) {
			contactIds.add(cursor.getString(cursor.getColumnIndex(projection)));
		}
		cursor.close();
	}

	private String displayNameQuery(ContentResolver cr, String contactId) {
		Cursor cursor = cr.query(
				ContactsContract.Contacts.CONTENT_URI, 
				new String[] {ContactsContract.Contacts.DISPLAY_NAME},
				ContactsContract.Contacts._ID + " = ?",
				new String[] {contactId},
				null);
		cursor.moveToFirst();
		String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		cursor.close();
		return displayName;
	}

	private JSONArray organizationQuery(ContentResolver cr, String contactId) {
	 	String[] orgWhereParams = new String[]{contactId, 
	 		ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}; 
	 	Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, 
	                new String[] {ContactsContract.CommonDataKinds.Organization.DEPARTMENT,
	 					ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
	 					ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
	 					ContactsContract.CommonDataKinds.Organization.COMPANY,
	 					ContactsContract.CommonDataKinds.Organization.TITLE},
	                WHERE_STRING, orgWhereParams, null);
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
	                new String[] {ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
						ContactsContract.CommonDataKinds.StructuredPostal.STREET,
						ContactsContract.CommonDataKinds.StructuredPostal.CITY,
						ContactsContract.CommonDataKinds.StructuredPostal.REGION,
						ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
						ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY}, 
	                WHERE_STRING, addrWhereParams, null); 
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
	                new String[] {ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
						ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
						ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
						ContactsContract.CommonDataKinds.StructuredName.PREFIX,
						ContactsContract.CommonDataKinds.StructuredName.SUFFIX}, 
	                WHERE_STRING, addrWhereParams, null); 
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
			new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.TYPE}, 
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
			new String[] {ContactsContract.CommonDataKinds.Email.DATA,ContactsContract.CommonDataKinds.Email.TYPE}, 
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
					new String[] {ContactsContract.CommonDataKinds.Im.DATA,ContactsContract.CommonDataKinds.Im.TYPE}, 
	                WHERE_STRING, addrWhereParams, null); 
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
				new String[] {ContactsContract.CommonDataKinds.Note.NOTE}, WHERE_STRING, noteWhereParams, null); 
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
				new String[] {ContactsContract.CommonDataKinds.Nickname.NAME}, WHERE_STRING, nicknameWhereParams, null); 
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
				new String[] {ContactsContract.CommonDataKinds.Website.URL,ContactsContract.CommonDataKinds.Website.TYPE}, 
				WHERE_STRING, websiteWhereParams, null); 
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
				new String[] {ContactsContract.CommonDataKinds.Relation.NAME,ContactsContract.CommonDataKinds.Relation.TYPE}, 
				WHERE_STRING, relationshipWhereParams, null); 
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

	@Override
	public void save(JSONObject contact) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean remove(String id) {
    	int result = mApp.getContentResolver().delete(ContactsContract.Data.CONTENT_URI, 
    			ContactsContract.Data.CONTACT_ID + " = ?", 
    			new String[] {id});    	
    	return (result > 0) ? true : false;
	}	
}