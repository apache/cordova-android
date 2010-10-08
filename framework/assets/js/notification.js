/**
 * This class provides access to notifications on the device.
 */
function Notification() {
}

/**
 * Open a native alert dialog, with a customizable title and button text.
 *
 * @param {String} message      Message to print in the body of the alert
 * @param {String} title        Title of the alert dialog (default: Alert)
 * @param {String} buttonLabel  Label of the close button (default: OK)
 */
Notification.prototype.alert = function(message, title, buttonLabel) {
    var _title = (title || "Alert");
    var _buttonLabel = (buttonLabel || "OK");
    PhoneGap.execAsync(null, null, "Notification", "alert", [message,_title,_buttonLabel]);
};

/**
 * Open a native confirm dialog, with a customizable title and button text.
 *
 * @param {String} message      Message to print in the body of the alert
 * @param {String} title        Title of the alert dialog (default: Confirm)
 * @param {String} buttonLabels Comma separated list of the labels of the buttons (default: 'OK,Cancel')
 * @return {Number}             The index of the button clicked
 */
Notification.prototype.confirm = function(message, title, buttonLabels) {
    var _title = (title || "Confirm");
    var _buttonLabels = (buttonLabels || "OK,Cancel");
    return PhoneGap.execAsync(null, null, "Notification", "confirm", [message,_title,_buttonLabels]);
};

/**
 * Start spinning the activity indicator on the statusbar
 */
Notification.prototype.activityStart = function() {
    PhoneGap.execAsync(null, null, "Notification", "activityStart", ["Busy","Please wait..."]);
};

/**
 * Stop spinning the activity indicator on the statusbar, if it's currently spinning
 */
Notification.prototype.activityStop = function() {
    PhoneGap.execAsync(null, null, "Notification", "activityStop", []);
};

/**
 * Display a progress dialog with progress bar that goes from 0 to 100.
 *
 * @param {String} title        Title of the progress dialog.
 * @param {String} message      Message to display in the dialog.
 */
Notification.prototype.progressStart = function(title, message) {
    PhoneGap.execAsync(null, null, "Notification", "progressStart", [title, message]);
};

/**
 * Set the progress dialog value.
 *
 * @param {Number} value         0-100
 */
Notification.prototype.progressValue = function(value) {
    PhoneGap.execAsync(null, null, "Notification", "progressValue", [value]);
};

/**
 * Close the progress dialog.
 */
Notification.prototype.progressStop = function() {
    PhoneGap.execAsync(null, null, "Notification", "progressStop", []);
};

/**
 * Causes the device to blink a status LED.
 *
 * @param {Integer} count       The number of blinks.
 * @param {String} colour       The colour of the light.
 */
Notification.prototype.blink = function(count, colour) {
    // NOT IMPLEMENTED
};

/**
 * Causes the device to vibrate.
 *
 * @param {Integer} mills       The number of milliseconds to vibrate for.
 */
Notification.prototype.vibrate = function(mills) {
    PhoneGap.execAsync(null, null, "Notification", "vibrate", [mills]);
};

/**
 * Causes the device to beep.
 * On Android, the default notification ringtone is played "count" times.
 *
 * @param {Integer} count       The number of beeps.
 */
Notification.prototype.beep = function(count) {
    PhoneGap.execAsync(null, null, "Notification", "beep", [count]);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.notification == "undefined") navigator.notification = new Notification();
});

