package com.phonegap;

import org.json.JSONArray;
import org.json.JSONException;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.util.Log;
import android.webkit.WebView;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

@SuppressWarnings("deprecation")
public class ContactManager implements Plugin {
	
	public class ContactTriplet
	{
		public String name = "";
		public String email = "";
		public String phone = "";
	}

    WebView webView;					// WebView object
    DroidGap ctx;						// DroidGap object

	private static final String LOG_TAG = "Contact Query";
	Uri mPeople = android.provider.Contacts.People.CONTENT_URI;
	Uri mPhone = android.provider.Contacts.Phones.CONTENT_URI;	
	Uri mEmail = android.provider.Contacts.ContactMethods.CONTENT_URI;

	/**
	 * Constructor.
	 */
	public ContactManager()	{
	}
	
	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(DroidGap ctx) {
		this.ctx = ctx;
	}

	/**
	 * Sets the main View of the application, this is the WebView within which 
	 * a PhoneGap app runs.
	 * 
	 * @param webView The PhoneGap WebView
	 */
	public void setView(WebView webView) {
		this.webView = webView;
	}

	/**
	 * Executes the request and returns CommandResult.
	 * 
	 * @param action The command to execute.
	 * @param args JSONArry of arguments for the command.
	 * @return A CommandResult object with a status and message.
	 */
	public PluginResult execute(String action, JSONArray args) {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";		
		
		try {
			if (action.equals("getContactsAndSendBack")) {
				this.getContactsAndSendBack();
			}
			else if (action.equals("search")) {
				this.search(args.getString(0), args.getString(1), args.getString(2));
			}
			return new PluginResult(status, result);
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	/**
	 * Identifies if action to be executed returns a value and should be run synchronously.
	 * 
	 * @param action	The action to execute
	 * @return			T=returns value
	 */
	public boolean isSynch(String action) {
		return false;
	}

	/**
     * Called when the system is about to start resuming a previous activity. 
     */
    public void onPause() {
    }

    /**
     * Called when the activity will start interacting with the user. 
     */
    public void onResume() {
    }
    
    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {   	
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it. 
     * 
     * @param requestCode		The request code originally supplied to startActivityForResult(), 
     * 							allowing you to identify who this result came from.
     * @param resultCode		The integer result code returned by the child activity through its setResult().
     * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

	// This is to add backwards compatibility to the OLD Contacts API\
	public void getContactsAndSendBack()
	{
		String[] projection = new String[] {
				People._ID,
				People.NAME,
				People.NUMBER,
				People.PRIMARY_EMAIL_ID
			};

		try{
			Cursor myCursor = this.ctx.managedQuery(mPeople, projection, 
				null, null , People.NAME + " ASC");
			processResults(myCursor, true);
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}		
	}
	
	public void search(String name, String npa, String email)
	{
		
		if (email.length() > 0)
			searchByEmail(email);		
		else
			searchPeople(name, npa);
	}
	
	private void searchByEmail(String email)
	{						
		String[] projection = new String[] {
								ContactMethods._ID,
								ContactMethods.DATA,
								ContactMethods.KIND,
								ContactMethods.PERSON_ID
							};
		String[] variables = new String[] {
				 email
		};
		
		try{
			Cursor myCursor = this.ctx.managedQuery(mEmail, projection, 
						"contact_methods." + ContactMethods.DATA + " = ?" + "AND contact_methods.kind = 1", variables , ContactMethods.DATA + " ASC");
			getMethodData(myCursor);
						
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}
				
	}
	
	private void searchPeople(String name, String number)
	{
			String conditions = "";
		
			if (name.length() == 0)
			{
				name = "%";
				conditions += People.NAME + " LIKE ? AND ";
			}
			else
			{
				conditions += People.NAME + " = ? AND ";
			}
		
			if (number.length() == 0)
				number = "%";
			else
			{
				number = number.replace('+', '%');
				number = number.replace('.', '%');
				number = number.replace('-', '%');
			}
			
			conditions += People.NUMBER + " LIKE ? ";
			
			String[] projection = new String[] {
								People._ID,
								People.NAME,
								People.NUMBER,
								People.PRIMARY_EMAIL_ID
			};
			
			String[] variables = new String[] {
					name, number
			};
			
			try{
				Cursor myCursor = this.ctx.managedQuery(mPeople, projection, 
								conditions, variables , People.NAME + " ASC");
				processResults(myCursor, false);
			}
			catch (SQLiteException ex)
			{
					Log.d(LOG_TAG, ex.getMessage());
			}		
	
	}

	private void processResults(Cursor cur, boolean all){
		
	    if (cur.moveToFirst()) {

	        String name; 
	        String phoneNumber;	        
	        String email_id;
	        String email;
	        
	        int nameColumn = cur.getColumnIndex(People.NAME);
	        int phoneColumn = cur.getColumnIndex(People.NUMBER);
	        int emailIdColumn = cur.getColumnIndex(People.PRIMARY_EMAIL_ID);
	        
	        do {
	            // Get the field values
	            name = cur.getString(nameColumn);	            
	            phoneNumber = cur.getString(phoneColumn);
	            email_id = cur.getString(emailIdColumn);
	            if (email_id != null && email_id.length() > 0)
	            	email = getEmail(email_id);
	            else
	            	email = "";
	            
	            // Code for backwards compatibility with the OLD Contacts API
	            if (all) {
	            	this.ctx.sendJavascript("navigator.contacts.droidFoundContact('" + name + "','" + phoneNumber + "','" + email +"');");	            	
	            }
	            else {
	            	this.ctx.sendJavascript("navigator.contacts.droidFoundContact('" + name + "','" + phoneNumber + "','" + email +"');");
	            }           
	        } while (cur.moveToNext());
	        if (all) {
	        	this.ctx.sendJavascript("navigator.contacts.droidDone();");
	        }
	        else {
	        	this.ctx.sendJavascript("navigator.contacts.droidDone();");
	        }
	    }
	    else
	    {
	    	if (all) {
	    		this.ctx.sendJavascript("navigator.contacts.fail('Error');");
	    	}
	    	else {
	    		this.ctx.sendJavascript("navigator.contacts.fail('None found!');");
	    	}
	    }
	}	
	
	private void getMethodData(Cursor cur)
	{        
        ContactTriplet data = new ContactTriplet();
        String id;    
        String email;
        
	    if (cur.moveToFirst()) {

	        int idColumn = cur.getColumnIndex(ContactMethods._ID);
	        int emailColumn = cur.getColumnIndex(ContactMethods.DATA);	        
	        do {
	            // Get the field values	            
	            id = cur.getString(idColumn);
	            email = cur.getString(emailColumn);
	            
	            data = getContactData(id);
	            if(data != null)
	            {
	            	data.email = email;	            
	            	this.ctx.sendJavascript("navigator.contacts.droidFoundContact('" + data.name + "','" + data.phone + "','" + data.email +"');");
	            }	           
	        } while (cur.moveToNext());
	        this.ctx.sendJavascript("navigator.contacts.droidDoneContacts();");	        
	    }	 
	}		
	
	private ContactTriplet getContactData(String id) {
		ContactTriplet data = null;
		String[] projection = new String[] {
				People._ID,
				People.NAME,
				People.NUMBER,
				People.PRIMARY_EMAIL_ID
		};

		String[] variables = new String[] {
				id
		};

		try{
			Cursor myCursor = this.ctx.managedQuery(mPeople, projection, 
				People.PRIMARY_EMAIL_ID + " = ?", variables , People.NAME + " ASC");
			data = getTriplet(myCursor);
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}		
		
		return data;
	}

	private ContactTriplet getTriplet(Cursor cur) {
		 ContactTriplet data = new ContactTriplet();         
		 if (cur.moveToFirst()) {

			 int nameColumn = cur.getColumnIndex(People.NAME); 
		     int numberColumn = cur.getColumnIndex(People.NUMBER);	        
		     do {
		         	            
		    	 data.name = cur.getString(nameColumn);
		    	 data.phone = cur.getString(numberColumn);
		    	 
		     } while (cur.moveToNext());	       
		 }
		return data;
	}

	private String getEmailColumnData(Cursor cur)
	{					        
        String email = "";         
	    if (cur != null && cur.moveToFirst()) {
	        int emailColumn = cur.getColumnIndex(ContactMethods.DATA);	        
	        do {
	            // Get the field values	            
	            email = cur.getString(emailColumn);	            	            
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
		
		try
		{
			Cursor myCursor = this.ctx.managedQuery(mEmail, projection, 
						"contact_methods." + ContactMethods._ID + " = ?" + " AND contact_methods.kind = 1", variables , ContactMethods.DATA + " ASC");
			email = getEmailColumnData(myCursor);
		}
		catch (SQLiteException ex)
		{
			Log.d(LOG_TAG, ex.getMessage());
		}
		
		return email;		
	}
	
	
}
