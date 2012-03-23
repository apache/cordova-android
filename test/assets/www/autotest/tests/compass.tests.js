Tests.prototype.CompassTests = function() {
  module('Compass (navigator.compass)');
	test("should exist", function() {
      expect(1);
      ok(navigator.compass !== null, "navigator.compass should not be null.");
	});
	test("should contain a getCurrentHeading function", function() {
		expect(2);
		ok(typeof navigator.compass.getCurrentHeading != 'undefined' && navigator.compass.getCurrentHeading !== null, "navigator.compass.getCurrentHeading should not be null.");
		ok(typeof navigator.compass.getCurrentHeading == 'function', "navigator.compass.getCurrentHeading should be a function.");
	});
	test("getCurrentHeading success callback should be called with a Heading object", function() {
		expect(9);
		QUnit.stop(Tests.TEST_TIMEOUT);
		var win = function(a) {
			ok(typeof a == 'object', "Heading object returned in getCurrentHeading success callback should be of type 'object'.");
			ok(a.magneticHeading !== null, "Heading object returned in getCurrentHeading success callback should have an 'magneticHeading' property.");
			ok(typeof a.magneticHeading == 'number', "Heading object's 'magneticHeading' property returned in getCurrentHeading success callback should be of type 'number'.");
			ok(a.trueHeading !== null, "Heading object returned in getCurrentHeading success callback should have a 'trueHeading' property.");
			ok(typeof a.trueHeading == 'number', "Heading object's 'trueHeading' property returned in getCurrentHeading success callback should be of type 'number'.");
			ok(a.headingAccuracy !== null, "Heading object returned in getCurrentHeading success callback should have a 'headingAccuracy' property.");
			ok(typeof a.headingAccuracy == 'number', "Heading object's 'headingAccuracy' property returned in getCurrentHeading success callback should be of type 'number'.");
			ok(a.timestamp !== null, "Heading object returned in getCurrentHeading success callback should have a 'timestamp' property.");
			ok(a.timestamp instanceof Date, "Heading object's 'timestamp' property returned in getCurrentHeading success callback should be an instance of Date.");
			QUnit.start();
		};
		var fail = function() { QUnit.start(); };
		navigator.compass.getCurrentHeading(win, fail);
	});
	test("should contain a watchHeading function", function() {
		expect(2);
		ok(typeof navigator.compass.watchHeading != 'undefined' && navigator.compass.watchHeading !== null, "navigator.compass.watchHeading should not be null.");
		ok(typeof navigator.compass.watchHeading == 'function', "navigator.compass.watchHeading should be a function.");
	});
	test("should contain a clearWatch function", function() {
		expect(2);
		ok(typeof navigator.compass.clearWatch != 'undefined' && navigator.compass.clearWatch !== null, "navigator.compass.clearWatch should not be null.");
		ok(typeof navigator.compass.clearWatch == 'function', "navigator.compass.clearWatch should be a function!");
	});

  module('Compass Constants (window.CompassError)');
  test("CompassError globals should exist", function() {
    expect(3);
    ok(window.CompassError !== null, 'window.CompassError should not be null');
    equals(window.CompassError.COMPASS_INTERNAL_ERR, 0, 'window.CompassError.COMPASS_INTERNAL_ERR should be 0');
    equals(window.CompassError.COMPASS_NOT_SUPPORTED, 20, 'window.CompassError.COMPASS_NOT_SUPPORTED should be 20');
  });

  module('Compass Heading model (CompassHeading)');
  test("CompassHeading function should exist", function() {
    expect(1);
    ok(typeof CompassHeading != 'undefined' && CompassHeading !== null, 'CompassHeading should not be null');
  });
  test("Creating a new CompassHeading instance", function() {
    expect(4);
    var h = new CompassHeading();
    equals(h.magneticHeading, null, "CompassHeading instance should have null magneticHeading property by default");
    equals(h.trueHeading, null, "CompassHeading instance should have null trueHeading property by default");
    equals(h.headingAccuracy, null, "CompassHeading instance should have null headingAccuracy property by default");
    ok(h.timestamp !== null, "CompassHeading instance should have timestamp that is not null by default");
  });
};
