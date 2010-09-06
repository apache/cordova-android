com.phonegap.AudioHandlerProxy = function() {
    this.className = "com.phonegap.AudioHandler";
};
com.phonegap.AudioHandlerProxy.prototype.startRecordingAudio = function(id, file) {
    return PhoneGap.exec(this.className, "startRecordingAudio", [id, file]);
};
com.phonegap.AudioHandlerProxy.prototype.stopRecordingAudio = function(id) {
    return PhoneGap.exec(this.className, "stopRecordingAudio", [id]);
};
com.phonegap.AudioHandlerProxy.prototype.startPlayingAudio = function(id, file) {
    return PhoneGap.exec(this.className, "startPlayingAudio", [id, file]);
};
com.phonegap.AudioHandlerProxy.prototype.pausePlayingAudio = function(id) {
    return PhoneGap.exec(this.className, "pausePlayingAudio", [id]);
};
com.phonegap.AudioHandlerProxy.prototype.stopPlayingAudio = function(id) {
    return PhoneGap.exec(this.className, "stopPlayingAudio", [id]);
};
com.phonegap.AudioHandlerProxy.prototype.getCurrentPositionAudio = function(id) {
    return PhoneGap.exec(this.className, "getCurrentPositionAudio", [id]);
};
com.phonegap.AudioHandlerProxy.prototype.getDurationAudio = function(id, file) {
    return PhoneGap.exec(this.className, "getDurationAudio", [id, file]);
};
com.phonegap.AudioHandler = new com.phonegap.AudioHandlerProxy();

/**
 * List of media objects.
 * PRIVATE
 */
PhoneGap.mediaObjects = {};

/**
 * Object that receives native callbacks.
 * PRIVATE
 */
PhoneGap.Media = function() {};

/**
 * Get the media object.
 * PRIVATE
 *
 * @param id            The media object id (string)
 */
PhoneGap.Media.getMediaObject = function(id) {
    return PhoneGap.mediaObjects[id];
};

/**
 * Audio has status update.
 * PRIVATE
 *
 * @param id            The media object id (string)
 * @param status        The status code (int)
 * @param msg           The status message (string)
 */
PhoneGap.Media.onStatus = function(id, msg, value) {
    var media = PhoneGap.mediaObjects[id];

    // If state update
    if (msg == Media.MEDIA_STATE) {
        if (value == Media.MEDIA_STOPPED) {
            if (media.successCallback) {
                media.successCallback();
            }
        }
        if (media.statusCallback) {
            media.statusCallback(value);
        }
    }
    else if (msg == Media.MEDIA_DURATION) {
        media._duration = value;
    }
    else if (msg == Media.MEDIA_ERROR) {
        if (media.errorCallback) {
            media.errorCallback(value);
        }
    }
};

/**
 * This class provides access to the device media, interfaces to both sound and video
 *
 * @param src                   The file name or url to play
 * @param successCallback       The callback to be called when the file is done playing or recording.
 *                                  successCallback() - OPTIONAL
 * @param errorCallback         The callback to be called if there is an error.
 *                                  errorCallback(int errorCode) - OPTIONAL
 * @param statusCallback        The callback to be called when media status has changed.
 *                                  statusCallback(int statusCode) - OPTIONAL
 */
Media = function(src, successCallback, errorCallback, statusCallback) {

    // successCallback optional
    if (successCallback && (typeof successCallback != "function")) {
        console.log("Media Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback != "function")) {
        console.log("Media Error: errorCallback is not a function");
        return;
    }

    // statusCallback optional
    if (statusCallback && (typeof statusCallback != "function")) {
        console.log("Media Error: statusCallback is not a function");
        return;
    }

    this.id = PhoneGap.createUUID();
    PhoneGap.mediaObjects[this.id] = this;
    this.src = src;
    this.successCallback = successCallback;
    this.errorCallback = errorCallback;
    this.statusCallback = statusCallback;
    this._duration = -1;
};

// Media messages
Media.MEDIA_STATE = 1;
Media.MEDIA_DURATION = 2;
Media.MEDIA_ERROR = 3;

// Media states
Media.MEDIA_NONE = 0;
Media.MEDIA_STARTING = 1;
Media.MEDIA_RUNNING = 2;
Media.MEDIA_PAUSED = 3;
Media.MEDIA_STOPPED = 4;
Media.MEDIA_MSG = ["None", "Starting", "Running", "Paused", "Stopped"];

// TODO: Will MediaError be used?
/**
 * This class contains information about any Media errors.
 * @constructor
 */
function MediaError() {
    this.code = null,
    this.message = "";
};

MediaError.MEDIA_ERR_ABORTED        = 1;
MediaError.MEDIA_ERR_NETWORK        = 2;
MediaError.MEDIA_ERR_DECODE         = 3;
MediaError.MEDIA_ERR_NONE_SUPPORTED = 4;

/**
 * Start or resume playing audio file.
 */
Media.prototype.play = function() {
    com.phonegap.AudioHandler.startPlayingAudio(this.id, this.src);
};

/**
 * Stop playing audio file.
 */
Media.prototype.stop = function() {
    com.phonegap.AudioHandler.stopPlayingAudio(this.id);
};

/**
 * Pause playing audio file.
 */
Media.prototype.pause = function() {
    com.phonegap.AudioHandler.pausePlayingAudio(this.id);
};

/**
 * Get duration of an audio file.
 * The duration is only set for audio that is playing, paused or stopped.
 *
 * @return      duration or -1 if not known.
 */
Media.prototype.getDuration = function() {
    return this._duration;
};

/**
 * Get position of audio.
 *
 * @return
 */
Media.prototype.getCurrentPosition = function() {
    return com.phonegap.AudioHandler.getCurrentPositionAudio(this.id);
};

/**
 * Start recording audio file.
 */
Media.prototype.startRecord = function() {
    com.phonegap.AudioHandler.startRecordingAudio(this.id, this.src);
};

/**
 * Stop recording audio file.
 */
Media.prototype.stopRecord = function() {
    com.phonegap.AudioHandler.stopRecordingAudio(this.id);
};

