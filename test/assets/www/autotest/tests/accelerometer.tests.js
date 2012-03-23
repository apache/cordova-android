Tests.prototype.AccelerometerTests = function() {
	module('Accelerometer (navigator.accelerometer)');
	test("should exist", function() {
  		expect(1);
  		ok(navigator.accelerometer != null, "navigator.accelerometer should not be null.");
	});
	test("should contain a getCurrentAcceleration function", function() {
		expect(2);
		ok(typeof navigator.accelerometer.getCurrentAcceleration != 'undefined' && navigator.accelerometer.getCurrentAcceleration != null, "navigator.accelerometer.getCurrentAcceleration should not be null.");
		ok(typeof navigator.accelerometer.getCurrentAcceleration == 'function', "navigator.accelerometer.getCurrentAcceleration should be a function.");
	});
	test("getCurrentAcceleration success callback should be called with an Acceleration object", function() {
		expect(7);
		QUnit.stop(Tests.TEST_TIMEOUT);
		var win = function(a) {
			ok(typeof a == 'object', "Acceleration object returned in getCurrentAcceleration success callback should be of type 'object'.");
			ok(a.x != null, "Acceleration object returned in getCurrentAcceleration success callback should have an 'x' property.");
			ok(typeof a.x == 'number', "Acceleration object's 'x' property returned in getCurrentAcceleration success callback should be of type 'number'.");
			ok(a.y != null, "Acceleration object returned in getCurrentAcceleration success callback should have a 'y' property.");
			ok(typeof a.y == 'number', "Acceleration object's 'y' property returned in getCurrentAcceleration success callback should be of type 'number'.");
			ok(a.z != null, "Acceleration object returned in getCurrentAcceleration success callback should have a 'z' property.");
			ok(typeof a.z == 'number', "Acceleration object's 'z' property returned in getCurrentAcceleration success callback should be of type 'number'.");
			start();
		};
		var fail = function() { start(); };
		navigator.accelerometer.getCurrentAcceleration(win, fail);
	});
	test("should contain a watchAcceleration function", function() {
		expect(2);
		ok(typeof navigator.accelerometer.watchAcceleration != 'undefined' && navigator.accelerometer.watchAcceleration != null, "navigator.accelerometer.watchAcceleration should not be null.");
		ok(typeof navigator.accelerometer.watchAcceleration == 'function', "navigator.accelerometer.watchAcceleration should be a function.");
	});
	test("should contain a clearWatch function", function() {
		expect(2);
		ok(typeof navigator.accelerometer.clearWatch != 'undefined' && navigator.accelerometer.clearWatch != null, "navigator.accelerometer.clearWatch should not be null.");
		ok(typeof navigator.accelerometer.clearWatch == 'function', "navigator.accelerometer.clearWatch should be a function!");
	});
	module('Acceleration model');
	test("should be able to define a new Acceleration object with x, y, z and timestamp properties.", function () {
		expect(9);
		var x = 1;
		var y = 2;
		var z = 3;
		var a = new Acceleration(x, y, z);
		ok(a != null, "new Acceleration object should not be null.");
		ok(typeof a == 'object', "new Acceleration object should be of type 'object'.");
		ok(a.x != null, "new Acceleration object should have an 'x' property.");
		equals(a.x, x, "new Acceleration object should have 'x' property equal to first parameter passed in Acceleration constructor.");
		ok(a.y != null, "new Acceleration object should have a 'y' property.");
		equals(a.y, y, "new Acceleration object should have 'y' property equal to second parameter passed in Acceleration constructor.");
		ok(a.z != null, "new Acceleration object should have a 'z' property.");
		equals(a.z, z, "new Acceleration object should have 'z' property equal to third parameter passed in Acceleration constructor.");
		ok(a.timestamp != null, "new Acceleration object should have a 'timestamp' property.");
	});
};