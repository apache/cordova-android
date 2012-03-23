// Prevent QUnit from running when the DOM load event fires
QUnit.config.autostart = false;
sessionStorage.clear();

// Timeout is 2 seconds to allow physical devices enough
// time to query the response. This is important for some
// Android devices.
var Tests = function() {};
Tests.TEST_TIMEOUT = 2000;
