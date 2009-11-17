/**
 * This overrides existing contact code, and builds proper contacts
 * @constructor
 */

var Contact = function() {
  this.givenNames = [];
  this.familyName = "";
  this.phones = [];
  this.category = "";
  this.companyName = "";
  this.isCompany = false;
  this.email = [];
  this.addresses = [];
  this.uri = [];
  this.prefix = "";
  this.jobTitle = "";
  this.birthday = "";
  this.phoneticName = "";
}

var Address = function() {
  this.street = "";
  this.postalCode = "";
  this.city = "";
  this.region = "";
  this.countryCode = "";
  this.country = "";
  this.building = "";
  this.floor = "";
  this.accessCode = "";
}

var PhoneNumber = function() {
  this.number = "";
  this.type = ""; 
}

var Email = function() {
  this.address = "";
  this.type = "";
}

var ImHandle = function()
{
  this.address = "";
  this.type = "";
  this.network = "";
}

var Uri = function() {
  this.addr = "";
  this.rel = "";
}


var AddressBook = function() {
  this.name = "";
  this.resultSet = [];
}

AddressBook.prototype.addContact = function(newContact, win, fail)
{
    
}

AddressBook.prototype.removeContact = function(target, win, fail)
{
  
}

AddressBook.prototype.findContacts = function(filter,win, fail)
{
}

PhoneGap.addConstructor(function() {
  if (typeof navigator.AddressBook == "undefined")
    navigator.AddressBook = new AddressBook(); });
AddressBook.prototype.addContact = function(newContact, win, fail)
{
    
}

AddressBook.prototype.removeContact = function(target, win, fail)
{
  
}

AddressBook.prototype.findContacts = function(filter,win, fail)
{
  this.win = win;
  this.fail = fail;
  // Zero out the result set for the query
  this.resultSet = [];
  var name = "";
  var phone = "";
  var email = "";

  if (filter.givenName)
    name = filter.givenName;
  if (filter.familyName)
    name += filter.familyName;
  if (filter.phone)
    var phone = filter.phone;
  if (filter.email)
    var email = filter.email;
  ContactHook.search(name, phone, email);
}

AddressBook.prototype.droidFoundContact = function(name, npa, email)
{
  names = name.split(' ');
  personContact = new Contact();
  personContact.givenNames.push(names[0]);
  // This is technically wrong, but we can't distinguish right now
  if(names.length > 1)
  {
    personContact.familyName = name[name.length -1];
  }

  telNumber = new PhoneNumber();
  telNumber.number = npa;
  personContact.phones.push(telNumber);

  email_addr = new Email();
  email_addr.address = email;
  
  personContact.email.push(email_addr);

  this.resultSet.push(personContact);

}

AddressBook.prototype.droidDoneContacts = function()
{
  if(resultSet.length > 0)
    this.win(resultSet);
  else
    this.fail();
}
