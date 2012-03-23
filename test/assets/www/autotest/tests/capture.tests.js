Tests.prototype.CaptureTests = function() {  
    module('Capture (navigator.device.capture)');
    test("should exist", function() {
        expect(2);
        ok(navigator.device !== null, "navigator.device should not be null.");
        ok(navigator.device.capture !== null, "navigator.device.capture should not be null.");
    });
    test("should have the correct properties ", function() {
        expect(3);
        ok(typeof navigator.device.capture.supportedAudioModes != 'undefined' && navigator.device.capture.supportedAudioModes !== null , "there should be a supported audio modes property");
        ok(typeof navigator.device.capture.supportedImageModes != 'undefined' && navigator.device.capture.supportedImageModes !== null , "there should be a supported image modes property");
        ok(typeof navigator.device.capture.supportedVideoModes != 'undefined' && navigator.device.capture.supportedVideoModes !== null , "there should be a supported video modes property");
    });
    test("should contain a captureAudio function", function() {
        expect(2);
        ok(typeof navigator.device.capture.captureAudio != 'undefined' && navigator.device.capture.captureAudio !== null, "navigator.device.capture.captureAudio should not be null.");
        ok(typeof navigator.device.capture.captureAudio == 'function', "navigator.device.capture.captureAudio should be a function.");
    });
    test("should contain a captureImage function", function() {
        expect(2);
        ok(typeof navigator.device.capture.captureImage != 'undefined' && navigator.device.capture.captureImage !== null, "navigator.device.capture.captureImage should not be null.");
        ok(typeof navigator.device.capture.captureImage == 'function', "navigator.device.capture.captureImage should be a function.");
    });
    test("should contain a captureVideo function", function() {
        expect(2);
        ok(typeof navigator.device.capture.captureVideo != 'undefined' && navigator.device.capture.captureVideo !== null, "navigator.device.capture.captureVideo should not be null.");
        ok(typeof navigator.device.capture.captureVideo == 'function', "navigator.device.capture.captureVideo should be a function.");
    });

    module('CaptureAudioOptions');
    test("CaptureAudioOptions constructor should exist", function() {
        expect(4);
        var options = new CaptureAudioOptions();
        ok(options !== null, "CaptureAudioOptions object should not be null.");
        ok(typeof options.limit !== 'undefined', "CaptureAudioOptions object should have a 'limit' property.");
        ok(typeof options.duration !== 'undefined', "CaptureAudioOptions object should have a 'duration' property.");
        ok(typeof options.mode !== 'undefined', "CaptureAudioOptions object should have a 'mode' property.");
    });

    module('CaptureImageOptions');
    test("CaptureImageOptions constructor should exist", function() {
        expect(3);
        var options = new CaptureImageOptions();
        ok(options !== null, "CaptureImageOptions object should not be null.");
        ok(typeof options.limit !== 'undefined', "CaptureImageOptions object should have a 'limit' property.");
        ok(typeof options.mode !== 'undefined', "CaptureImageOptions object should have a 'mode' property.");
    });

    module('CaptureVideoOptions');
    test("CaptureVideoOptions constructor should exist", function() {
        expect(4);
        var options = new CaptureVideoOptions();
        ok(options !== null, "CaptureVideoOptions object should not be null.");
        ok(typeof options.limit !== 'undefined', "CaptureVideoOptions object should have a 'limit' property.");
        ok(typeof options.duration !== 'undefined', "CaptureVideoOptions object should have a 'duration' property.");
        ok(typeof options.mode !== 'undefined', "CaptureVideoOptions object should have a 'mode' property.");
    });

    module('CaptureError interface');
    test("CaptureError constants should be defined", function() {
        expect(4);
        equal(CaptureError.CAPTURE_INTERNAL_ERR, 0, "CaptureError.CAPTURE_INTERNAL_ERR should be defined");
        equal(CaptureError.CAPTURE_APPLICATION_BUSY, 1, "CaptureError.CAPTURE_APPLICATION_BUSY should be defined");
        equal(CaptureError.CAPTURE_INVALID_ARGUMENT, 2, "CaptureError.CAPTURE_INVALID_ARGUMENT should be defined");
        equal(CaptureError.CAPTURE_NO_MEDIA_FILES, 3, "CaptureError.CAPTURE_NO_MEDIA_FILES should be defined");
    });
    test("CaptureError properties should exist", function() {
        expect(2);
        var error = new CaptureError();
        ok(error !== null, "CaptureError object should not be null.");
        ok(typeof error.code !== 'undefined', "CaptureError object should have a 'code' property.");
    });

    module('MediaFileData');
    test("MediaFileData constructor should exist", function() {
        expect(6);
        var fileData = new MediaFileData();
        ok(fileData !== null, "MediaFileData object should not be null.");
        ok(typeof fileData.bitrate !== 'undefined', "MediaFileData object should have a 'bitrate' property.");
        ok(typeof fileData.codecs !== 'undefined', "MediaFileData object should have a 'codecs' property.");
        ok(typeof fileData.duration !== 'undefined', "MediaFileData object should have a 'duration' property.");
        ok(typeof fileData.height !== 'undefined', "MediaFileData object should have a 'height' property.");
        ok(typeof fileData.width !== 'undefined', "MediaFileData object should have a 'width' property.");
    });

    module('MediaFile');
    test("MediaFile constructor should exist", function() {
        expect(6);
        var fileData = new MediaFileData();
        ok(fileData !== null, "MediaFileData object should not be null.");
        ok(typeof fileData.name !== 'undefined', "MediaFile object should have a 'name' property.");
        ok(typeof fileData.fullPath !== 'undefined', "MediaFile object should have a 'fullPath' property.");
        ok(typeof fileData.type !== 'undefined', "MediaFile object should have a 'type' property.");
        ok(typeof fileData.lastModifiedDate !== 'undefined', "MediaFile object should have a 'lastModifiedDate' property.");
        ok(typeof fileData.size !== 'undefined', "MediaFile object should have a 'size' property.");
    });
};
