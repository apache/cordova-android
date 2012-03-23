Tests.prototype.OrientationTests = function() {	
	module('Orientation (navigator.orientation)');
	test("should exist", function() {
  		expect(1);
  		ok(navigator.orientation != null, "navigator.orientation should not be null.");
	});
	test("should have an initially null lastPosition property", function() {
  		expect(1);
  		ok(typeof navigator.orientation.currentOrientation != 'undefined' && navigator.orientation.currentOrientation == null, "navigator.orientation.currentOrientation should be initially null.");
	});
	test("should contain a getCurrentOrientation function", function() {
		expect(2);
		ok(typeof navigator.orientation.getCurrentOrientation != 'undefined' && navigator.orientation.getCurrentOrientation != null, "navigator.orientation.getCurrentOrientation should not be null.");
		ok(typeof navigator.orientation.getCurrentOrientation == 'function', "navigator.orientation.getCurrentOrientation should be a function.");
	});
	test("should contain a watchOrientation function", function() {
		expect(2);
		ok(typeof navigator.orientation.watchOrientation != 'undefined' && navigator.orientation.watchOrientation != null, "navigator.orientation.watchOrientation should not be null.");
		ok(typeof navigator.orientation.watchOrientation == 'function', "navigator.orientation.watchOrientation should be a function.");
	});
	// TODO: add tests for DisplayOrientation constants?
	test("getCurrentOrientation success callback should be called with an Orientation enumeration", function() {
		expect(2);
		QUnit.stop(Tests.TEST_TIMEOUT);
		var win = function(orient) {
			ok(0 <= orient <= 6, "Position object returned in getCurrentPosition success callback is a valid DisplayOrientation value.");
			equals(orient, navigator.orientation.currentOrientation, "Orientation value returned in getCurrentOrientation success callback equals navigator.orientation.currentOrientation.");
			start();
		};
		var fail = function() { start(); };
		navigator.orientation.getCurrentOrientation(win, fail);
	});
	// TODO: Need to test watchPosition success callback, test that makes sure clearPosition works (how to test that a timer is getting cleared?)
};