Tests.prototype.ContactsTests = function() {
	module("Contacts (navigator.contacts)");
	test("should exist", function() {
  		expect(1);
  		ok(navigator.contacts != null, "navigator.contacts should not be null.");
	});	
	test("should contain a find function", function() {
		expect(2);
		ok(typeof navigator.contacts.find != 'undefined' && navigator.contacts.find != null, "navigator.contacts.find should not be null.");
		ok(typeof navigator.contacts.find == 'function', "navigator.contacts.find should be a function.");
	});
    test("contacts.find success callback should be called with an array", function() {
        expect(2);
        QUnit.stop(Tests.TEST_TIMEOUT);
        var win = function(result) {
            ok(typeof result == 'object', "Object returned in contacts.find success callback is of type 'object' (actually array).");
            ok(typeof result.length == 'number', "Object returned in contacts.find success callback has a length property which is numerical.");
            QUnit.start();
        };
        var fail = function() { QUnit.start(); };
        var obj = new ContactFindOptions();
        obj.filter="";
        obj.multiple=true;
        navigator.contacts.find(["displayName", "name", "phoneNumbers", "emails"], win, fail, obj);
    }); 
    test("contacts.find success callback should not be null", function() {
        expect(1);
        var fail = function() {};
        var obj = new ContactFindOptions();
        obj.filter="";
        obj.multiple=true;
        try {
            navigator.contacts.find(["displayName", "name", "emails", "phoneNumbers"], null, fail, obj);
        } catch(e) {
            ok(true, "Trying to find with a null success call back should throw TypeError.");
        }
    }); 
    test("contacts.find error callback should be called when no fields are specified", function() {
        expect(2);
        QUnit.stop(Tests.TEST_TIMEOUT);
        var win = function(result) {
            QUnit.start();
        };
        var fail = function(result) { 
            ok(typeof result == 'object', "Object returned in contact.find failure callback is of type 'object' (actually ContactError).");
            ok(result.code == ContactError.INVALID_ARGUMENT_ERROR, "Object returned in contacts.find failure callback has a code property which equal to ContactError.INVALID_ARGUMENT_ERROR.");
            QUnit.start(); 
        };
        var obj = new ContactFindOptions();
        obj.filter="";
        obj.multiple=true;
        navigator.contacts.find([], win, fail, obj);
    }); 
	test("should contain a create function", function() {
		expect(2);
		ok(typeof navigator.contacts.create != 'undefined' && navigator.contacts.create != null, "navigator.contacts.create should not be null.");
		ok(typeof navigator.contacts.create == 'function', "navigator.contacts.create should be a function.");
	});
	test("contacts.create should return a Contact object", function() {
		expect(9);
		var obj = navigator.contacts.create({"displayName": "test name", "gender": "male", "note": "my note", "name": {"formatted": "Mr. Test Name"}, "emails": [{"value": "here@there.com"}, {"value": "there@here.com"}]});		
		ok(obj != 'undefined' && obj != null, "navigator.contacts.create should return a Contact object.");
		ok(obj.displayName == 'test name', "Contact should contain a displayName property.");
		ok(obj.gender == 'male', "Contact should contain a gender property.");
		ok(obj.note == 'my note', "Contact should contain a note property.");
		ok(obj.name.formatted == 'Mr. Test Name', "Contact should contain a name.formatted property.");
		ok(obj.emails.length == 2, "Contact should contain and array of emails with 2 entries");
		ok(obj.emails[0].value == 'here@there.com', "Contact.emails[1] should contain a value.");
		ok(obj.emails[1].value == 'there@here.com', "Contact.emails[2] should contain a value.");	
		ok(obj.birthday == null, "Contact object should not contain a birthday property.");
	});
	module("Contact model");
	test("should be able to define a Contact object", function() {
		expect(15);
		var contact = new Contact("a", "b", new ContactName("a", "b", "c", "d", "e", "f"), "c", [], [], [], [], [], "f", "i",  
				[], [], []);
		ok(contact != null, "new Contact() should not be null.");
		ok(typeof contact.id != 'undefined' && contact.id != null && contact.id == "a", "new Contact() should include a 'id' property.");
		ok(typeof contact.displayName != 'undefined' && contact.displayName != null && contact.displayName == "b", "new Contact() should include a 'displayName' property.");
		ok(typeof contact.name != 'undefined' && contact.name != null && contact.name.formatted == "a", "new Contact() should include a 'name' property.");
		ok(typeof contact.nickname != 'undefined' && contact.nickname != null && contact.nickname == "c", "new Contact() should include a 'nickname' property.");
		ok(typeof contact.phoneNumbers != 'undefined' && contact.phoneNumbers != null, "new Contact() should include a 'phoneNumbers' property.");
		ok(typeof contact.emails != 'undefined' && contact.emails != null, "new Contact() should include a 'emails' property.");
		ok(typeof contact.addresses != 'undefined' && contact.addresses != null, "new Contact() should include a 'addresses' property.");
		ok(typeof contact.ims != 'undefined' && contact.ims != null, "new Contact() should include a 'ims' property.");
		ok(typeof contact.organizations != 'undefined' && contact.organizations != null, "new Contact() should include a 'organizations' property.");
		ok(typeof contact.birthday != 'undefined' && contact.birthday != null && contact.birthday == "f", "new Contact() should include a 'birthday' property.");
		ok(typeof contact.note != 'undefined' && contact.note != null && contact.note == "i", "new Contact() should include a 'note' property.");
		ok(typeof contact.photos != 'undefined' && contact.photos != null, "new Contact() should include a 'photos' property.");
		ok(typeof contact.categories != 'undefined' && contact.categories != null, "new Contact() should include a 'categories' property.");
		ok(typeof contact.urls != 'undefined' && contact.urls != null, "new Contact() should include a 'urls' property.");
	});	
	test("should be able to define a ContactName object", function() {
		expect(7);
		var contactName = new ContactName("Dr. First Last Jr.", "Last", "First", "Middle", "Dr.", "Jr.");
		ok(contactName != null, "new ContactName() should not be null.");
		ok(typeof contactName.formatted != 'undefined' && contactName.formatted != null && contactName.formatted == "Dr. First Last Jr.", "new ContactName() should include a 'formatted' property.");
		ok(typeof contactName.familyName != 'undefined' && contactName.familyName != null && contactName.familyName == "Last", "new ContactName() should include a 'familyName' property.");
		ok(typeof contactName.givenName != 'undefined' && contactName.givenName != null && contactName.givenName == "First", "new ContactName() should include a 'givenName' property.");
		ok(typeof contactName.middleName != 'undefined' && contactName.middleName != null && contactName.middleName == "Middle", "new ContactName() should include a 'middleName' property.");
		ok(typeof contactName.honorificPrefix != 'undefined' && contactName.honorificPrefix != null && contactName.honorificPrefix == "Dr.", "new ContactName() should include a 'honorificPrefix' property.");
		ok(typeof contactName.honorificSuffix != 'undefined' && contactName.honorificSuffix != null && contactName.honorificSuffix == "Jr.", "new ContactName() should include a 'honorificSuffix' property.");
	});	
	test("should be able to define a ContactField object", function() {
		expect(4);
		var contactField = new ContactField("home", "8005551212", true);
		ok(contactField != null, "new ContactField() should not be null.");
		ok(typeof contactField.type != 'undefined' && contactField.type != null && contactField.type == "home", "new ContactField() should include a 'type' property.");
		ok(typeof contactField.value != 'undefined' && contactField.value != null && contactField.value == "8005551212", "new ContactField() should include a 'value' property.");
		ok(typeof contactField.pref != 'undefined' && contactField.pref != null && contactField.pref == true, "new ContactField() should include a 'pref' property.");
	});	
	test("should be able to define a ContactAddress object", function() {
		expect(9);
		var contactAddress = new ContactAddress(true, "home", "a","b","c","d","e","f");
		ok(contactAddress != null, "new ContactAddress() should not be null.");
        ok(typeof contactAddress.pref != 'undefined' && contactAddress.pref != null && contactAddress.pref == true, "new ContactAddress() should include a 'pref' property.");
        ok(typeof contactAddress.type != 'undefined' && contactAddress.type != null && contactAddress.type == "home", "new ContactAddress() should include a 'type' property.");
		ok(typeof contactAddress.formatted != 'undefined' && contactAddress.formatted != null && contactAddress.formatted == "a", "new ContactAddress() should include a 'formatted' property.");
		ok(typeof contactAddress.streetAddress != 'undefined' && contactAddress.streetAddress != null && contactAddress.streetAddress == "b", "new ContactAddress() should include a 'streetAddress' property.");
		ok(typeof contactAddress.locality != 'undefined' && contactAddress.locality != null && contactAddress.locality == "c", "new ContactAddress() should include a 'locality' property.");
		ok(typeof contactAddress.region != 'undefined' && contactAddress.region != null && contactAddress.region == "d", "new ContactAddress() should include a 'region' property.");
		ok(typeof contactAddress.postalCode != 'undefined' && contactAddress.postalCode != null && contactAddress.postalCode == "e", "new ContactAddress() should include a 'postalCode' property.");
		ok(typeof contactAddress.country != 'undefined' && contactAddress.country != null && contactAddress.country == "f", "new ContactAddress() should include a 'country' property.");
	});	
	test("should be able to define a ContactOrganization object", function() {
		expect(6);
		var contactOrg = new ContactOrganization(true, "home", "a","b","c","d","e","f","g");
		ok(contactOrg != null, "new ContactOrganization() should not be null.");
        ok(typeof contactOrg.pref != 'undefined' && contactOrg.pref != null && contactOrg.pref == true, "new ContactOrganization() should include a 'pref' property.");
        ok(typeof contactOrg.type != 'undefined' && contactOrg.type != null && contactOrg.type == "home", "new ContactOrganization() should include a 'type' property.");
		ok(typeof contactOrg.name != 'undefined' && contactOrg.name != null && contactOrg.name == "a", "new ContactOrganization() should include a 'name' property.");
		ok(typeof contactOrg.department != 'undefined' && contactOrg.department != null && contactOrg.department == "b", "new ContactOrganization() should include a 'department' property.");
		ok(typeof contactOrg.title != 'undefined' && contactOrg.title != null && contactOrg.title == "c", "new ContactOrganization() should include a 'title' property.");
	});	
	test("should be able to define a ContactFindOptions object", function() {
		expect(3);
		var contactFindOptions = new ContactFindOptions("a", true, "b");
		ok(contactFindOptions != null, "new ContactFindOptions() should not be null.");
		ok(typeof contactFindOptions.filter != 'undefined' && contactFindOptions.filter != null && contactFindOptions.filter == "a", "new ContactFindOptions() should include a 'filter' property.");
		ok(typeof contactFindOptions.multiple != 'undefined' && contactFindOptions.multiple != null && contactFindOptions.multiple == true, "new ContactFindOptions() should include a 'multiple' property.");
	});	
	module("Contact Object");
	test("should contain a clone function", function() {
		expect(2);
		var contact = new Contact();
		ok(typeof contact.clone != 'undefined' && contact.clone != null, "contact.clone should not be null.");
		ok(typeof contact.clone == 'function', "contact.clone should be a function.");
	});
	test("clone function should make deep copy of Contact Object", function() {
		expect(8);
		var contact = new Contact();
		contact.id=1;
		contact.displayName="Test Name";
		contact.nickname="Testy";
		contact.gender="male";
		contact.note="note to be cloned";
		contact.name = new ContactName("Mr. Test Name");
		
		var clonedContact = contact.clone();
		
		ok(contact.id == 1, "contact.id should be 1.");
		ok(clonedContact.id == null, "clonedContact.id should be null.");
		ok(clonedContact.displayName == contact.displayName, "displayName's should be equal");
		ok(clonedContact.nickname == contact.nickname, "nickname's should be equal");
		ok(clonedContact.gender == contact.gender, "gender's should be equal");
		ok(clonedContact.note == contact.note, "note's should be equal");
		ok(clonedContact.name.formatted == contact.name.formatted, "name.formatted's should be equal");
		ok(clonedContact.connected == contact.connected, "connected's should be equal (null)");
	});
	test("should contain a remove function", function() {
		expect(2);
		var contact = new Contact();
		ok(typeof contact.remove != 'undefined' && contact.remove != null, "contact.remove should not be null.");
		ok(typeof contact.remove == 'function', "contact.remove should be a function.");
	});
	test("calling remove on a contact has an id of null should return ContactError.UNKNOWN_ERROR", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
		expect(2);
		var win = function(result) {
		};
		var fail = function(result) {
			ok(typeof result == 'object', "Object returned in contact.remove failure callback is of type 'object' (actually ContactError).");
			ok(result.code == ContactError.UNKNOWN_ERROR, "Object returned in contacts.remove failure callback has a code property which equal to ContactError.UNKNOWN_ERROR.");
			QUnit.start();
		};
		var rmContact = new Contact();
		rmContact.remove(win, fail);
	});
	test("calling remove on a contact that does not exist should return ContactError.UNKNOWN_ERROR", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
		expect(2);
		var win = function(result) {
		};
		var fail = function(result) {
			ok(typeof result == 'object', "Object returned in contact.remove failure callback is of type 'object' (actually ContactError).");
			ok(result.code == ContactError.UNKNOWN_ERROR, "Object returned in contacts.remove failure callback has a code property which equal to ContactError.UNKNOWN_ERROR.");
			QUnit.start();
		};
		var contact = new Contact(99);
		contact.remove(win, fail);		
	});
	test("should contain a save function", function() {
		expect(2);
		var contact = new Contact();
		ok(typeof contact.save != 'undefined' && contact.save != null, "contact.save should not be null.");
		ok(typeof contact.save == 'function', "contact.save should be a function.");
	});
    module('ContactError interface');
    test("ContactError constants should be defined", function() {
        expect(7);
        equal(ContactError.UNKNOWN_ERROR, 0, "ContactError.UNKNOWN_ERROR should be defined");
        equal(ContactError.INVALID_ARGUMENT_ERROR, 1, "ContactError.INVALID_ARGUMENT_ERROR should be defined");
        equal(ContactError.TIMEOUT_ERROR, 2, "ContactError.TIMEOUT_ERROR should be defined");
        equal(ContactError.PENDING_OPERATION_ERROR, 3, "ContactError.PENDING_OPERATION_ERROR should be defined");
        equal(ContactError.IO_ERROR, 4, "ContactError.IO_ERROR should be defined");
        equal(ContactError.NOT_SUPPORTED_ERROR, 5, "ContactError.NOT_SUPPORTED_ERROR should be defined");
        equal(ContactError.PERMISSION_DENIED_ERROR, 20, "ContactError.PERMISSION_DENIED_ERROR should be defined");
    });
};
