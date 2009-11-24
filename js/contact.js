/**
 * This class provides access to the device contacts.
 * @constructor
 */

function Contact(jsonObject) {
	  this.firstName = "";
	  this.lastName = "";
    this.name = "";
    this.phones = {};
    this.emails = {};
  	this.address = "";
}

Contact.prototype.displayName = function()
{
    // TODO: can be tuned according to prefs
	return this.name;
}

function ContactManager() {
	// Dummy object to hold array of contacts
	this.contacts = [];
	this.timestamp = new Date().getTime();
}

ContactManager.prototype.getAllContacts = function(successCallback, errorCallback, options) {
	// Interface
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.ContactManager == "undefined") navigator.ContactManager = new ContactManager();
});
ContactManager.prototype.getAllContacts = function(successCallback, errorCallback, options) {
  this.win = successCallback;
  this.fail = errorCallback;
	ContactHook.getContactsAndSendBack();
}

ContactManager.prototype.droidAddContact = function(name, phone, email)
{
  var contact = new Contact();
  contact.name = name;
  contact.phones.primary = phone;
  contact.emails.primary = email;
  this.contacts.push(contact);
}

ContactManager.prototype.droidDone = function()
{
  this.win(this.contacts);
}
