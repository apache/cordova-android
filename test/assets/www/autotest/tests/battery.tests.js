Tests.prototype.BatteryTests = function() {  
    module('Battery (navigator.battery)');
    test("should exist", function() {
        expect(1);
        ok(navigator.battery != null, "navigator.battery should not be null.");
    });
    /**
     * Tests add for Battery Status API spec
     * http://www.w3.org/TR/2011/WD-battery-status-20111129/
     */
    test("should have properties", function() {
        expect(4);
        ok(typeof navigator.battery.charging != 'undefined' && navigator.battery.charging != null, "navigator.battery.charging should be a boolean value.");
        ok(typeof navigator.battery.chargingTime != 'undefined' && navigator.battery.chargingTime != null, "navigator.battery.chargingTime should be a double value.");
        ok(typeof navigator.battery.level != 'undefined' && navigator.battery.level != null, "navigator.battery.level should be a double value.");
        ok(typeof navigator.battery.dischargingTime != 'undefined' && navigator.battery.dischargingTime != null, "navigator.battery.dischargingTime should be a double value.");
    });
};