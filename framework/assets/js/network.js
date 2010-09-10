
/**
 * This class contains information about any NetworkStatus.
 * @constructor
 */
function NetworkStatus() {
    this.code = null;
    this.message = "";
};

NetworkStatus.NOT_REACHABLE = 0;
NetworkStatus.REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
NetworkStatus.REACHABLE_VIA_WIFI_NETWORK = 2;

/**
 * This class provides access to device Network data (reachability).
 * @constructor
 */
function Network() {
    /**
     * The last known Network status.
	 * { hostName: string, ipAddress: string, 
		remoteHostStatus: int(0/1/2), internetConnectionStatus: int(0/1/2), localWiFiConnectionStatus: int (0/2) }
     */
	this.lastReachability = null;
};

/**
 * Called by the geolocation framework when the reachability status has changed.
 * @param {Reachibility} reachability The current reachability status.
 */
// TODO: Callback from native code not implemented for Android
Network.prototype.updateReachability = function(reachability) {
    this.lastReachability = reachability;
};

/**
 * Determine if a URI is reachable over the network.

 * @param {Object} uri
 * @param {Function} callback
 * @param {Object} options  (isIpAddress:boolean)
 */
Network.prototype.isReachable = function(uri, callback, options) {

    // callback required
    if (typeof callback != "function") {
        console.log("Network Error: callback is not a function");
        return;
    }

    PhoneGap.execAsync(
        function(status) {

            // If reachable, the check for wifi vs carrier
            if (status) {
                PhoneGap.execAsync(
                    function(wifi) {
                        var s = new NetworkStatus();
                        if (wifi) {
                            s.code = NetworkStatus.REACHABLE_VIA_WIFI_NETWORK;
                        }
                        else {
                            s.code = NetworkStatus.REACHABLE_VIA_CARRIER_DATA_NETWORK;
                        }
                        callback(s);
                    }, null, "Network Status", "isWifiActive", []);
            }

            // If not
            else {
                var s = new NetworkStatus();
                s.code = NetworkStatus.NOT_REACHABLE;
                callback(s);
            }
        }, null, "Network Status", "isReachable", [uri]);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.network == "undefined") navigator.network = new Network();
});

