/*
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 */

if (!Cordova.hasResource("camera")) {
Cordova.addResource("camera");

/**
 * This class provides access to the device camera.
 *
 * @constructor
 */
var Camera = function() {
    this.successCallback = null;
    this.errorCallback = null;
    this.options = null;
};

/**
 * Format of image that returned from getPicture.
 *
 * Example: navigator.camera.getPicture(success, fail,
 *              { quality: 80,
 *                destinationType: Camera.DestinationType.DATA_URL,
 *                sourceType: Camera.PictureSourceType.PHOTOLIBRARY})
 */
Camera.DestinationType = {
    DATA_URL: 0,                // Return base64 encoded string
    FILE_URI: 1                 // Return file uri (content://media/external/images/media/2 for Android)
};
Camera.prototype.DestinationType = Camera.DestinationType;

/**
 * Encoding of image returned from getPicture.
 *
 * Example: navigator.camera.getPicture(success, fail,
 *              { quality: 80,
 *                destinationType: Camera.DestinationType.DATA_URL,
 *                sourceType: Camera.PictureSourceType.CAMERA,
 *                encodingType: Camera.EncodingType.PNG})
*/
Camera.EncodingType = {
    JPEG: 0,                    // Return JPEG encoded image
    PNG: 1                      // Return PNG encoded image
};
Camera.prototype.EncodingType = Camera.EncodingType;

/**
 * Type of pictures to select from.  Only applicable when
 *      PictureSourceType is PHOTOLIBRARY or SAVEDPHOTOALBUM
 *
 * Example: navigator.camera.getPicture(success, fail,
 *              { quality: 80,
 *                destinationType: Camera.DestinationType.DATA_URL,
 *                sourceType: Camera.PictureSourceType.PHOTOLIBRARY,
 *                mediaType: Camera.MediaType.PICTURE})
 */
Camera.MediaType = {
       PICTURE: 0,      // allow selection of still pictures only. DEFAULT. Will return format specified via DestinationType
       VIDEO: 1,        // allow selection of video only, ONLY RETURNS URL
       ALLMEDIA : 2     // allow selection from all media types
};
Camera.prototype.MediaType = Camera.MediaType;


/**
 * Source to getPicture from.
 *
 * Example: navigator.camera.getPicture(success, fail,
 *              { quality: 80,
 *                destinationType: Camera.DestinationType.DATA_URL,
 *                sourceType: Camera.PictureSourceType.PHOTOLIBRARY})
 */
Camera.PictureSourceType = {
    PHOTOLIBRARY : 0,           // Choose image from picture library (same as SAVEDPHOTOALBUM for Android)
    CAMERA : 1,                 // Take picture from camera
    SAVEDPHOTOALBUM : 2         // Choose image from picture library (same as PHOTOLIBRARY for Android)
};
Camera.prototype.PictureSourceType = Camera.PictureSourceType;

/**
 * Gets a picture from source defined by "options.sourceType", and returns the
 * image as defined by the "options.destinationType" option.

 * The defaults are sourceType=CAMERA and destinationType=DATA_URL.
 *
 * @param {Function} successCallback
 * @param {Function} errorCallback
 * @param {Object} options
 */
Camera.prototype.getPicture = function(successCallback, errorCallback, options) {

    // successCallback required
    if (typeof successCallback !== "function") {
        console.log("Camera Error: successCallback is not a function");
        return;
    }

    // errorCallback optional
    if (errorCallback && (typeof errorCallback !== "function")) {
        console.log("Camera Error: errorCallback is not a function");
        return;
    }
    
    if (options === null || typeof options === "undefined") {
        options = {};
    }
    if (options.quality === null || typeof options.quality === "undefined") {
        options.quality = 80;
    }
    if (options.maxResolution === null || typeof options.maxResolution === "undefined") {
        options.maxResolution = 0;
    }
    if (options.destinationType === null || typeof options.destinationType === "undefined") {
        options.destinationType = Camera.DestinationType.FILE_URI;
    }
    if (options.sourceType === null || typeof options.sourceType === "undefined") {
        options.sourceType = Camera.PictureSourceType.CAMERA;
    }
    if (options.encodingType === null || typeof options.encodingType === "undefined") {
        options.encodingType = Camera.EncodingType.JPEG;
    }
    if (options.mediaType === null || typeof options.mediaType === "undefined") {
        options.mediaType = Camera.MediaType.PICTURE;
    }
    if (options.targetWidth === null || typeof options.targetWidth === "undefined") {
        options.targetWidth = -1;
    } 
    else if (typeof options.targetWidth === "string") {
        var width = new Number(options.targetWidth);
        if (isNaN(width) === false) {
            options.targetWidth = width.valueOf();
        }
    }
    if (options.targetHeight === null || typeof options.targetHeight === "undefined") {
        options.targetHeight = -1;
    } 
    else if (typeof options.targetHeight === "string") {
        var height = new Number(options.targetHeight);
        if (isNaN(height) === false) {
            options.targetHeight = height.valueOf();
        }
    }
    
    Cordova.exec(successCallback, errorCallback, "Camera", "takePicture", [options]);
};

Cordova.addConstructor(function() {
    if (typeof navigator.camera === "undefined") {
        navigator.camera = new Camera();
    }
});
}
