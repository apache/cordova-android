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
		HashMap<String,Boolean> populate = buildPopulationSet(filter);
				
		Iterator<String> it = contactIds.iterator();
		
		JSONArray contacts = new JSONArray();

		String contactId;
		int pos = 0;
		boolean firstRow = true;
		while (it.hasNext() && (pos < limit)) {
			JSONObject contact = new JSONObject();
			JSONArray organizations = new JSONArray();
			JSONArray addresses = new JSONArray();
			JSONArray phones = new JSONArray();
			JSONArray emails = new JSONArray();
			JSONArray ims = new JSONArray();
			JSONArray websites = new JSONArray();
			JSONArray relationships = new JSONArray();			
			
			contactId = it.next();
			
			Cursor c = cr.query(ContactsContract.Data.CONTENT_URI,
					null,             
					ContactsContract.Data.CONTACT_ID + " = ?",
					new String[] {contactId},
					null);
			
			String mimetype = "";
			while (c.moveToNext()) {
				try {
					if (firstRow) {
						firstRow = false;
						contact.put("id", contactId);
						contact.put("displayName", c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
					}
					mimetype = c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE));
					if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) 
							&& isRequired("name",populate)) {
						contact.put("name", nameQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) 
							&& isRequired("phoneNumbers",populate)) {
						phones.put(phoneQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) 
							&& isRequired("emails",populate)) {
						emails.put(emailQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE) 
							&& isRequired("addresses",populate)) {
						addresses.put(addressQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE) 
							&& isRequired("organizations",populate)) {
						organizations.put(organizationQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE) 
							&& isRequired("ims",populate)) {
						ims.put(imQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE) 
							&& isRequired("note",populate)) {
						contact.put("note",c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE) 
							&& isRequired("nickname",populate)) {
						contact.put("nickname",c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME)));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE) 
							&& isRequired("urls",populate)) {
						websites.put(websiteQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE) 
							&& isRequired("relationships",populate)) {
						relationships.put(relationshipQuery(c));
					}
					if (mimetype.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
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
			}
			c.close();
			
			firstRow = true;
			
			// Populate the Contact object with it's arrays
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
	public boolean remove(String id) {
    	int result = mApp.getContentResolver().delete(ContactsContract.Data.CONTENT_URI, 
    			ContactsContract.Data.CONTACT_ID + " = ?", 
    			new String[] {id});    	
    	return (result > 0) ? true : false;
	}	
}