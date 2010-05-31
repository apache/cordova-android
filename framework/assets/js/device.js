/**
 * this represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function Device() {
    this.available = PhoneGap.available;
    this.platform = null;
    this.version  = null;
    this.name     = null;
    this.gap      = null;
    this.uuid     = null;
    try {
        if (window.DroidGap) {
            this.available = true;
            this.uuid = window.DroidGap.getUuid();
            this.version = window.DroidGap.getOSVersion();
            this.gapVersion = window.DroidGap.getVersion();
            this.platform = window.DroidGap.getPlatform();
            this.name = window.DroidGap.getProductName();
            this.line1Number = window.DroidGap.getLine1Number();
            this.deviceId = window.DroidGap.getDeviceId();
            this.simSerialNumber = window.DroidGap.getSimSerialNumber();
            this.subscriberId = window.DroidGap.getSubscriberId();
        } 
    } catch(e) {
        this.available = false;
    }
}

PhoneGap.addConstructor(function() {
    navigator.device = window.device = new Device();
});
