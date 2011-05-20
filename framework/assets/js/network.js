/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010-2011, IBM Corporation
 */

if (!PhoneGap.hasResource("network")) {
PhoneGap.addResource("network");

/**
 * This class contains information about any NetworkStatus.
 * @constructor
 */
var NetworkStatus = function() {
    //this.code = null;
    //this.message = "";
};

NetworkStatus.NOT_REACHABLE = 0;
NetworkStatus.REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
NetworkStatus.REACHABLE_VIA_WIFI_NETWORK = 2;

/**
 * This class provides access to device Network data (reachability).
 * @constructor
 */
var Network = function() {
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
    var isIpAddress = false;
    if (options && options.isIpAddress) {
        isIpAddress = options.isIpAddress;
    }
    PhoneGap.exec(callback, null, "Network Status", "isReachable", [uri, isIpAddress]);
};

/**
 * This class contains information about the current network Connection.
 * @constructor
 */
var Connection = function() {
    this.type = null;
    this.homeNW = null;
    this.currentNW = null;

    var me = this;
    this.getInfo(
        function(info) {
            me.type = info.type;
            me.homeNW = info.homeNW;
            me.currentNW = info.currentNW;
            PhoneGap.onPhoneGapConnectionReady.fire();
        },
        function(e) {
            console.log("Error initializing Network Connection: " + e);
        });
};

Connection.UNKNOWN = 0;
Connection.ETHERNET = 1;
Connection.WIFI = 2;
Connection.CELL_2G = 3;
Connection.CELL_3G = 4;
Connection.CELL_4G = 5;
Connection.NONE = 20;

/**
 * Get connection info
 *
 * @param {Function} successCallback The function to call when the Connection data is available
 * @param {Function} errorCallback The function to call when there is an error getting the Connection data. (OPTIONAL)
 */
Connection.prototype.getInfo = function(successCallback, errorCallback) {
    // Get info
    PhoneGap.exec(successCallback, errorCallback, "Network Status", "getConnectionInfo", []);
};


PhoneGap.addConstructor(function() {
    if (typeof navigator.network === "undefined") {
        navigator.network = new Network();
    }
    if (typeof navigator.network.connection === "undefined") {
        navigator.network.connection = new Connection();
    }
});
}
