Tests.prototype.NetworkTests = function() {
	module('Network (navigator.network)');
	test("should exist", function() {
  		expect(1);
  		ok(navigator.network != null, "navigator.network should not be null.");
	});
    module('Network Information API');
    test("connection should exist", function() {
        expect(1);
        ok(navigator.network.connection != null, "navigator.network.connection should not be null.");
    });
    test("should contain connection properties", function() {
        expect(1);
        ok(typeof navigator.network.connection.type != 'undefined', "navigator.network.connection.type is defined.");
    });
    test("should define constants for connection status", function() {
        expect(7);
        equals(Connection.UNKNOWN, "unknown", "Connection.UNKNOWN is equal to 'unknown'.");
        equals(Connection.ETHERNET, "ethernet", "Connection.ETHERNET is equal to 'ethernet'.");
        equals(Connection.WIFI, "wifi", "Connection.WIFI is equal to 'wifi'.");
        equals(Connection.CELL_2G, "2g", "Connection.CELL_2G is equal to '2g'.");
        equals(Connection.CELL_3G, "3g", "Connection.CELL_3G is equal to '3g'.");
        equals(Connection.CELL_4G, "4g", "Connection.CELL_4G is equal to '4g'.");
        equals(Connection.NONE, "none", "Connection.NONE is equal to 'none'.");
    });
};