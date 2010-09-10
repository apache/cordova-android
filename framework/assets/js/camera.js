
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
    var capturetype = "base64";
    var quality = 80;
    if (this.options.capturetype) {
        capturetype = this.options.capturetype;
    }
    if (options.quality) {
        quality = this.options.quality;
    }
    PhoneGap.execAsync(null, null, "Camera", "takePicture", [quality, capturetype]);
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
