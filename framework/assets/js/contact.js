
var Contact = function(id, displayName, name, nickname, phoneNumbers, emails, addresses,
    ims, organizations, published, updated, birthday, anniversary, gender, note,
    preferredUsername, photos, tags, relationships, urls, accounts, utcOffset, connected) {
    this.id = id || '';
    this.displayName = displayName || '';
    this.name = name || null; // ContactName
    this.nickname = nickname || '';
    this.phoneNumbers = phoneNumbers || null; // ContactField[]
    this.emails = emails || null; // ContactField[]
    this.addresses = addresses || null; // ContactAddress[]
    this.ims = ims || null; // ContactField[]
    this.organizations = organizations || null; // ContactOrganization[]
    this.published = published || '';
    this.updated = updated || '';
    this.birthday = birthday || '';
    this.anniversary = anniversary || '';
    this.gender = gender || '';
    this.note = note || '';
    this.preferredUsername = preferredUsername || '';
    this.photos = photos || null; // ContactField[]
    this.tags = tags || null; // ContactField[]
    this.relationships = relationships || null; // ContactField[]
    this.urls = urls || null; // ContactField[]
    this.accounts = accounts || null; // ContactAccount[]
    this.utcOffset = utcOffset || '';
    this.connected = connected || '';
};

var ContactName = function(formatted, familyName, givenName, middle, prefix, suffix) {
    this.formatted = formatted || '';
    this.familyName = familyName || '';
    this.givenName = givenName || '';
    this.middleName = middle || '';
    this.honorificPrefix = prefix || '';
    this.honorificSuffix = suffix || '';
};

var ContactField = function(type, value, primary) {
    this.type = type || '';
    this.value = value || '';
    this.primary = primary || '';
};

var ContactAddress = function(formatted, streetAddress, locality, region, postalCode, country) {
    this.formatted = formatted || '';
    this.streetAddress = streetAddress || '';
    this.locality = locality || '';
    this.region = region || '';
    this.postalCode = postalCode || '';
    this.country = country || '';
};

var ContactOrganization = function(name, dept, title, startDate, endDate, location, desc) {
    this.name = name || '';
    this.department = dept || '';
    this.title = title || '';
    this.startDate = startDate || '';
    this.endDate = endDate || '';
    this.location = location || '';
    this.description = desc || '';
};

var ContactAccount = function(domain, username, userid) {
    this.domain = domain || '';
    this.username = username || '';
    this.userid = userid || '';
}

var Contacts = function() {
    this.inProgress = false;
    this.records = new Array();
}

// Contacts.prototype.find = function(obj, win, fail) {
Contacts.prototype.find = function(fields, win, fail, options) {
    this.win = win;
    this.fail = fail;
    
    PhoneGap.execAsync(null, null, "Contacts", "search", [fields, options]);
};

Contacts.prototype.droidDone = function(contacts) {
    this.win(eval('(' + contacts + ')'));
};

Contacts.prototype.remove = function(contact) {
    
};

Contacts.prototype.save = function(contact) {
    
};

Contacts.prototype.create = function(contact) {
    
};

Contacts.prototype.m_foundContacts = function(win, contacts) {
    this.inProgress = false;
    win(contacts);
};

var ContactFindOptions = function(filter, multiple, limit, updatedSince) {
    this.filter = filter || '';
    this.multiple = multiple || true;
    this.limit = limit || 0;
    this.updatedSince = updatedSince || '';
};

var ContactError = function() {
};

ContactError.INVALID_ARGUMENT_ERROR = 0;
ContactError.IO_ERROR = 1;
ContactError.NOT_FOUND_ERROR = 2;
ContactError.NOT_SUPPORTED_ERROR = 3;
ContactError.PENDING_OPERATION_ERROR = 4;
ContactError.PERMISSION_DENIED_ERROR = 5;
ContactError.TIMEOUT_ERROR = 6;
ContactError.UNKNOWN_ERROR = 7;

PhoneGap.addConstructor(function() {
    if(typeof navigator.service == "undefined") navigator.service = new Object();
    if(typeof navigator.service.contacts == "undefined") navigator.service.contacts = new Contacts();
});
