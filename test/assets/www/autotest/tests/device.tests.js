Tests.prototype.DeviceTests = function() {
	module('Device Information (window.device)');
	test("should exist", function() {
  		expect(1);
  		ok(window.device != null, "window.device should not be null.");
	});
	test("should contain a platform specification that is a string", function() {
		expect(2);
		ok(typeof window.device.platform != 'undefined' && window.device.platform != null, "window.device.platform should not be null.")
		ok((new String(window.device.platform)).length > 0, "window.device.platform should contain some sort of description.")
	});
	test("should contain a version specification that is a string", function() {
		expect(2);
		ok(typeof window.device.version != 'undefined' && window.device.version != null, "window.device.version should not be null.")
		ok((new String(window.device.version)).length > 0, "window.device.version should contain some kind of description.")
	});
	test("should contain a name specification that is a string", function() {
		expect(2);
		ok(typeof window.device.name != 'undefined' && window.device.name != null, "window.device.name should not be null.")
		ok((new String(window.device.name)).length > 0, "window.device.name should contain some kind of description.")
	});
	test("should contain a UUID specification that is a string or a number", function() {
		expect(2);
		ok(typeof window.device.uuid != 'undefined' && window.device.uuid != null, "window.device.uuid should not be null.")
		if (typeof window.device.uuid == 'string' || typeof window.device.uuid == 'object') {
			ok((new String(window.device.uuid)).length > 0, "window.device.uuid, as a string, should have at least one character.")
		} else {
			ok(window.device.uuid > 0, "window.device.uuid, as a number, should be greater than 0. (should it, even?)")
		}
	});
	test("should contain a phonegap specification that is a string", function() {
		expect(2);
		ok(typeof window.device.phonegap != 'undefined' && window.device.phonegap != null, "window.device.phonegap should not be null.")
		ok((new String(window.device.phonegap)).length > 0, "window.device.phonegap should contain some kind of description.")
	});
};