/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

/**
* Contains information about a single contact.
* @param {DOMString} id unique identifier
* @param {DOMString} displayName
* @param {ContactName} name
* @param {DOMString} nickname
* @param {ContactField[]} phoneNumbers array of phone numbers
* @param {ContactField[]} emails array of email addresses
* @param {ContactAddress[]} addresses array of addresses
* @param {ContactField[]} ims instant messaging user ids
* @param {ContactOrganization[]} organizations
* @param {DOMString} published date contact was first created
* @param {DOMString} updated date contact was last updated
* @param {DOMString} birthday contact's birthday
* @param (DOMString} anniversary contact's anniversary
* @param {DOMString} gender contact's gender
* @param {DOMString} note user notes about contact
* @param {DOMString} preferredUsername
* @param {ContactField[]} photos
* @param {ContactField[]} tags
* @param {ContactField[]} relationships
* @param {ContactField[]} urls contact's web sites
* @param {ContactAccounts[]} accounts contact's online accounts
* @param {DOMString} utcOffset UTC time zone offset
* @param {DOMString} connected
*/
var Contact = function(id, displayName, name, nickname, phoneNumbers, emails, addresses,
    ims, organizations, published, updated, birthday, anniversary, gender, note,
    preferredUsername, photos, tags, relationships, urls, accounts, utcOffset, connected) {
    this.id = id || null;
    this.displayName = displayName || null;
    this.name = name || null; // ContactName
    this.nickname = nickname || null;
    this.phoneNumbers = phoneNumbers || null; // ContactField[]
    this.emails = emails || null; // ContactField[]
    this.addresses = addresses || null; // ContactAddress[]
    this.ims = ims || null; // ContactField[]
    this.organizations = organizations || null; // ContactOrganization[]
    this.published = published || null;
    this.updated = updated || null;
    this.birthday = birthday || null;
    this.anniversary = anniversary || null;
    this.gender = gender || null;
    this.note = note || null;
    this.preferredUsername = preferredUsername || null;
    this.photos = photos || null; // ContactField[]
    this.tags = tags || null; // ContactField[]
    this.relationships = relationships || null; // ContactField[]
    this.urls = urls || null; // ContactField[]
    this.accounts = accounts || null; // ContactAccount[]
    this.utcOffset = utcOffset || null;
    this.connected = connected || null;
};

/**
* Removes contact from device storage.
* @param successCB success callback
* @param errorCB error callback
*/
Contact.prototype.remove = function(successCB, errorCB) {
    if (this.id == null) {
        var errorObj = new ContactError();
        errorObj.code = ContactError.NOT_FOUND_ERROR;
        errorCB(errorObj);
    }

    PhoneGap.exec(successCB, errorCB, "Contacts", "remove", [this.id]);
};

/**
* Creates a deep copy of this Contact.
* With the contact ID set to null.
* @return copy of this Contact
*/
Contact.prototype.clone = function() {
    var clonedContact = PhoneGap.clone(this);
    clonedContact.id = null;
    return clonedContact;
};

/**
* Persists contact to device storage.
* @param successCB success callback
* @param errorCB error callback
*/
Contact.prototype.save = function(successCB, errorCB) {
};

/**
* Contact name.
* @param formatted
* @param familyName
* @param givenName
* @param middle
* @param prefix
* @param suffix
*/
var ContactName = function(formatted, familyName, givenName, middle, prefix, suffix) {
    this.formatted = formatted || null;
    this.familyName = familyName || null;
    this.givenName = givenName || null;
    this.middleName = middle || null;
    this.honorificPrefix = prefix || null;
    this.honorificSuffix = suffix || null;
};

/**
* Generic contact field.
* @param type
* @param value
* @param primary
*/
var ContactField = function(type, value, primary) {
    this.type = type || null;
    this.value = value || null;
    this.primary = primary || null;
};

/**
* Contact address.
* @param formatted
* @param streetAddress
* @param locality
* @param region
* @param postalCode
* @param country
*/
var ContactAddress = function(formatted, streetAddress, locality, region, postalCode, country) {
    this.formatted = formatted || null;
    this.streetAddress = streetAddress || null;
    this.locality = locality || null;
    this.region = region || null;
    this.postalCode = postalCode || null;
    this.country = country || null;
};

/**
* Contact organization.
* @param name
* @param dept
* @param title
* @param startDate
* @param endDate
* @param location
* @param desc
*/
var ContactOrganization = function(name, dept, title, startDate, endDate, location, desc) {
    this.name = name || null;
    this.department = dept || null;
    this.title = title || null;
    this.startDate = startDate || null;
    this.endDate = endDate || null;
    this.location = location || null;
    this.description = desc || null;
};

/**
* Contact account.
* @param domain
* @param username
* @param userid
*/
var ContactAccount = function(domain, username, userid) {
    this.domain = domain || null;
    this.username = username || null;
    this.userid = userid || null;
}

/**
* Represents a group of Contacts.
*/
var Contacts = function() {
    this.inProgress = false;
    this.records = new Array();
}
/**
* Returns an array of Contacts matching the search criteria.
* @param fields that should be searched
* @param successCB success callback
* @param errorCB error callback
* @param {ContactFindOptions} options that can be applied to contact searching
* @return array of Contacts matching search criteria
*/
Contacts.prototype.find = function(fields, successCB, errorCB, options) {
    PhoneGap.exec(successCB, errorCB, "Contacts", "search", [fields, options]);
};

/**
* This function creates a new contact, but it does not persist the contact
* to device storage. To persist the contact to device storage, invoke
* contact.save().
* @param properties an object who's properties will be examined to create a new Contact
* @returns new Contact object
*/
Contacts.prototype.create = function(properties) {
    var contact = new Contact();
    for (i in properties) {
        if (contact[i]!='undefined') {
            contact[i]=properties[i];
        }
    }
    return contact;
};

/**
 * ContactFindOptions.
 * @param filter used to match contacts against
 * @param multiple boolean used to determine if more than one contact should be returned
 * @param limit maximum number of results to return from the contacts search
 * @param updatedSince return only contact records that have been updated on or after the given time
 */
var ContactFindOptions = function(filter, multiple, limit, updatedSince) {
    this.filter = filter || '';
    this.multiple = multiple || false;
    this.limit = limit || 1;
    this.updatedSince = updatedSince || '';
};

/**
 *  ContactError.
 *  An error code assigned by an implementation when an error has occurred
 */
var ContactError = function() {
    this.code=null;
};

/**
 * Error codes
 */
ContactError.UNKNOWN_ERROR = 0;
ContactError.INVALID_ARGUMENT_ERROR = 1;
ContactError.NOT_FOUND_ERROR = 2;
ContactError.TIMEOUT_ERROR = 3;
ContactError.PENDING_OPERATION_ERROR = 4;
ContactError.IO_ERROR = 5;
ContactError.NOT_SUPPORTED_ERROR = 6;
ContactError.PERMISSION_DENIED_ERROR = 20;

/**
 * Add the contact interface into the browser.
 */
PhoneGap.addConstructor(function() {
    if(typeof navigator.service == "undefined") navigator.service = new Object();
    if(typeof navigator.service.contacts == "undefined") navigator.service.contacts = new Contacts();
});
