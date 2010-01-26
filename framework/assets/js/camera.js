/**
 * This class provides access to the device camera.
 * @constructor
 */
function Camera() {
	
}

/**
 * 
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
Camera.prototype.getPicture = function(successCallback, errorCallback, options) {

  this.winCallback = successCallback;
  this.failCallback = errorCallback;
  if (options.quality)
  {
    GapCam.takePicture(options.quality);
  }
  else 
  {
    GapCam.takePicture(80);
  }
}

Camera.prototype.win = function(picture)
{
  this.winCallback(picture);
}

Camera.prototype.fail = function(err)
{
  this.failCallback(err);
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.camera == "undefined") navigator.camera = new Camera();
});
