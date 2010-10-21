/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

/**
 * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function Device() {
    this.available = PhoneGap.available;
    this.platform = null;
    this.version = null;
    this.name = null;
    this.uuid = null;
    this.phonegap = null;

    var me = this;
    this.getInfo(
        function(info) {
            me.available = true;
            me.platform = info.platform;
            me.version = info.version;
            me.uuid = info.uuid;
            me.phonegap = info.phonegap;
            PhoneGap.onPhoneGapInfoReady.fire();
        },
        function(e) {
            me.available = false;
            console.log("Error initializing PhoneGap: " + e);
            alert("Error initializing PhoneGap: "+e);
        });
}

/**
 * Get device info
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
Device.prototype.getInfo = function(successCallback, errorCallback) {

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Device Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Device Error: errorCallback is not a function");
        return;
    }

    // Get info
    PhoneGap.exec(successCallback, errorCallback, "Device", "getDeviceInfo", []);
};

/*
 * This is only for Android.
 *
 * You must explicitly override the back button.
 */
Device.prototype.overrideBackButton = function() {
    BackButton.override();
}

/*
 * This is only for Android.
 *
 * This resets the back button to the default behaviour
 */
Device.prototype.resetBackButton = function() {
    BackButton.reset();
}

/*
 * This is only for Android.
 *
 * This terminates the activity!
 */
Device.prototype.exitApp = function() {
    BackButton.exitApp();
}

PhoneGap.addConstructor(function() {
    navigator.device = window.device = new Device();
});
