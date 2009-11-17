/**
 * This class provides access to the device media, interfaces to both sound and video
 * @constructor
 */
function Media(src, successCallback, errorCallback) {
	this.src = src;
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;												
}

Media.prototype.record = function() {
}

Media.prototype.play = function() {
}

Media.prototype.pause = function() {
}

Media.prototype.stop = function() {
}


/**
 * This class contains information about any Media errors.
 * @constructor
 */
function MediaError() {
	this.code = null,
	this.message = "";
}

MediaError.MEDIA_ERR_ABORTED 		= 1;
MediaError.MEDIA_ERR_NETWORK 		= 2;
MediaError.MEDIA_ERR_DECODE 		= 3;
MediaError.MEDIA_ERR_NONE_SUPPORTED = 4;


//if (typeof navigator.audio == "undefined") navigator.audio = new Media(src);

/**
 * This class provides access to the device media, interfaces to both sound and video
 * @constructor
 */

Media.prototype.play = function() {
  DroidGap.startPlayingAudio(this.src);  
}

Media.prototype.stop = function() {
  DroidGap.stopPlayingAudio();
}

Media.prototype.startRecord = function() {
  DroidGap.startRecordingAudio(this.src);
}

Media.prototype.stopRecordingAudio = function() {
  DroidGap.stopRecordingAudio();
}


