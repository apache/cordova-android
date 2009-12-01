var Contact = function(){
  this.name = null;
  this.emails = [];
  this.phones = [];
}

var ContactName = function()
{
  this.formatted = "";
  this.familyName = "";
  this.givenName = "";
  this.additionalNames = [];
  this.prefixes = [];
  this.suffixes = [];
}


var ContactEmail = function()
{
  this.types = [];
  this.address = "";
}


var Contacts = function()
{
  this.records = [];  
}

var Contacts.prototype.find = function(obj, win, fail)
{
  if(obj.name)
  {
   ContactHook.search(name, "", ""); 
  }
  this.win = win;
  this.fail = fail;
}

var Contacts.prototype.droidFoundContact = function(name, npa, email)
{
  var contact = new Contact();
  contact.name = new ContactName();
  contact.name.formatted = name;
  contact.name.givenName = name;
  var mail = new ContactEmail();
  mail.types.push("home");
  mail.address = email;
  contact.emails.push(mail);
  phone = new ContactPhoneNumber();
  phone.types.push("home");
  phone.number = npa;
  contact.phones.push(phone);
  this.records.push(contact);
}

var Contacts.prototype.droidDone = function()
{
  this.win(this.records);
}
