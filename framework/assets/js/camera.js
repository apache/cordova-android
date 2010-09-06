com.phonegap.CameraLauncherProxy = function() {
    this.className = "com.phonegap.CameraLauncher";
};
com.phonegap.CameraLauncherProxy.prototype.setBase64 = function(b) {
    return PhoneGap.exec(this.className, "setBase64", [b]);
};
com.phonegap.CameraLauncherProxy.prototype.takePicture = function(quality) {
    return PhoneGap.exec(this.className, "takePicture", [quality]);
};
com.phonegap.CameraLauncher = new com.phonegap.CameraLauncherProxy();

/**
 * This class provides access to the device camera.
 *
 * @constructor
 */
function Camera() {
    this.successCallback = null;
    this.errorCallback = null;
    this.options = null;
};

/**
 * Takes a photo and returns the image as a base64 encoded `String`.
 *
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
Camera.prototype.getPicture = function(successCallback, errorCallback, options) {

    // successCallback required
    if (typeof successCallback != "function") {
        console.log("Camera Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Camera Error: errorCallback is not a function");
        return;
    }

    this.successCallback = successCallback;
    this.errorCallback = errorCallback;
    this.options = options;
    if (options.quality) {
        com.phonegap.CameraLauncher.takePicture(options.quality);
    }
    else {
        com.phonegap.CameraLauncher.takePicture(80);
    }
};

/**
 * Callback function from native code that is called when image has been captured.
 *
 * @param picture           The base64 encoded string of the image
 */
Camera.prototype.success = function(picture) {
    if (this.successCallback) {
        this.successCallback(picture);
    }
};

/**
 * Callback function from native code that is called when there is an error
 * capturing an image, or the capture is cancelled.
 *
 * @param err               The error message
 */
Camera.prototype.error = function(err) {
    if (this.errorCallback) {
        this.errorCallback(err);
    }
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.camera == "undefined") navigator.camera = new Camera();
});
