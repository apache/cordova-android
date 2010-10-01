
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


Contact.prototype.remove = function(successCB, errorCB) {
	if (this.id == null) {
		var errorObj = new ContactError();
		errorObj.code = ContactError.NOT_FOUND_ERROR;
		errorCB(errorObj);
	}
	
    PhoneGap.execAsync(successCB, errorCB, "Contacts", "remove", [this.id]);	
};

Contact.prototype.clone = function() {
	var clonedContact = PhoneGap.clone(this);
	clonedContact.id = null;
    return clonedContact;
};

Contact.prototype.save = function(win, fail) {
};


var ContactName = function(formatted, familyName, givenName, middle, prefix, suffix) {
    this.formatted = formatted || null;
    this.familyName = familyName || null;
    this.givenName = givenName || null;
    this.middleName = middle || null;
    this.honorificPrefix = prefix || null;
    this.honorificSuffix = suffix || null;
};

var ContactField = function(type, value, primary) {
    this.type = type || null;
    this.value = value || null;
    this.primary = primary || null;
};

var ContactAddress = function(formatted, streetAddress, locality, region, postalCode, country) {
    this.formatted = formatted || null;
    this.streetAddress = streetAddress || null;
    this.locality = locality || null;
    this.region = region || null;
    this.postalCode = postalCode || null;
    this.country = country || null;
};

var ContactOrganization = function(name, dept, title, startDate, endDate, location, desc) {
    this.name = name || null;
    this.department = dept || null;
    this.title = title || null;
    this.startDate = startDate || null;
    this.endDate = endDate || null;
    this.location = location || null;
    this.description = desc || null;
};

var ContactAccount = function(domain, username, userid) {
    this.domain = domain || null;
    this.username = username || null;
    this.userid = userid || null;
}

var Contacts = function() {
    this.inProgress = false;
    this.records = new Array();
}

Contacts.prototype.find = function(fields, win, fail, options) {
    this.win = win;
    this.fail = fail;
    
    PhoneGap.execAsync(null, null, "Contacts", "search", [fields, options]);
};

//This function does not create a new contact in the db.  
//Must call contact.save() for it to be persisted in the db.
Contacts.prototype.create = function(properties) {
	var contact = new Contact();
	for (i in properties) {
		if (contact[i]!='undefined') {
			contact[i]=properties[i];
		}
	}
	return contact;
};

Contacts.prototype.droidDone = function(contacts) {
    this.win(eval('(' + contacts + ')'));
};

Contacts.prototype.m_foundContacts = function(win, contacts) {
    this.inProgress = false;
    win(contacts);
};

var ContactFindOptions = function(filter, multiple, limit, updatedSince) {
    this.filter = filter || '';
    this.multiple = multiple || true;
    this.limit = limit || Number.MAX_VALUE;
    this.updatedSince = updatedSince || '';
};

var ContactError = function() {
    this.code=null;
};

ContactError.UNKNOWN_ERROR = 0;
ContactError.INVALID_ARGUMENT_ERROR = 1;
ContactError.NOT_FOUND_ERROR = 2;
ContactError.TIMEOUT_ERROR = 3;
ContactError.PENDING_OPERATION_ERROR = 4;
ContactError.IO_ERROR = 5;
ContactError.NOT_SUPPORTED_ERROR = 6;
ContactError.PERMISSION_DENIED_ERROR = 20;

PhoneGap.addConstructor(function() {
    if(typeof navigator.service == "undefined") navigator.service = new Object();
    if(typeof navigator.service.contacts == "undefined") navigator.service.contacts = new Contacts();
});
