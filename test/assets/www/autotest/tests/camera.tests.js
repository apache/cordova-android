Tests.prototype.CameraTests = function() {	
	module('Camera (navigator.camera)');
	test("should exist", function() {
      expect(1);
      ok(navigator.camera !== null, "navigator.camera should not be null.");
	});
	test("should contain a getPicture function", function() {
		expect(2);
		ok(typeof navigator.camera.getPicture != 'undefined' && navigator.camera.getPicture !== null, "navigator.camera.getPicture should not be null.");
		ok(typeof navigator.camera.getPicture == 'function', "navigator.camera.getPicture should be a function.");
	});

  module('Camera Constants (window.Camera)');
  test("should exist", function() {
    expect(1);
    ok(window.Camera !== null, "window.Camera should not be null.");
  });
  test("should contain two DestinationType constants", function() {
    expect(2);
    equals(Camera.DestinationType.DATA_URL, 0, "DestinationType.DATA_URL should equal to 0");
    equals(Camera.DestinationType.FILE_URI, 1, "DestinationType.DATA_URL should equal to 1");
  });
  test("should contain two EncodingType constants", function() {
    expect(2);
    equals(Camera.EncodingType.JPEG, 0, "EncodingType.JPEG should equal to 0");
    equals(Camera.EncodingType.PNG, 1, "EncodingType.PNG should equal to 1");
  });
  test("should contain three MediaType constants", function() {
    expect(3);
    equals(Camera.MediaType.PICTURE, 0, 'MediaType.PICTURE should equal to 0');
    equals(Camera.MediaType.VIDEO, 1, 'MediaType.VIDEO should equal to 1');
    equals(Camera.MediaType.ALLMEDIA, 2, 'MediaType.ALLMEDIA should equal to 2');
  });
  test("should contain three PictureSourceType constants", function() {
    expect(3);
    equals(Camera.PictureSourceType.PHOTOLIBRARY, 0, 'PictureSourceType.PHOTOLIBRARY should equal to 0');
    equals(Camera.PictureSourceType.CAMERA, 1, 'PictureSourceType.CAMERA should equal to 1');
    equals(Camera.PictureSourceType.SAVEDPHOTOALBUM, 2, 'PictureSourceType.SAVEDPHOTOALBUM should equal to 2');
  });
};
