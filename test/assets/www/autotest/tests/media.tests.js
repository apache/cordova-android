//
// @TODO Update to Latest HTML5 Audio Element Spec
// @see http://www.whatwg.org/specs/web-apps/current-work/multipage/video.html#audio
//
Tests.prototype.MediaTests = function() {	
	module('Media (Audio)');
	test("should exist", function() {
  		expect(1);
		ok(typeof Audio === "function" || typeof Audio === "object", "'Audio' should be defined as a function in global scope.");
	});
	test("should define constants for Media errors", function() {
		expect(5);
		ok(MediaError != null && typeof MediaError != 'undefined', "MediaError object exists in global scope.");
		equals(MediaError.MEDIA_ERR_ABORTED, 1, "MediaError.MEDIA_ERR_ABORTED is equal to 1.");
		equals(MediaError.MEDIA_ERR_NETWORK, 2, "MediaError.MEDIA_ERR_NETWORK is equal to 2.");
		equals(MediaError.MEDIA_ERR_DECODE, 3, "MediaError.MEDIA_ERR_DECODE is equal to 3.");
		equals(MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED, 4, "MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED is equal to 4.");
	});
	test("should contain 'src', 'loop' and 'error' properties", function() {
  		expect(7);
		var audioSrc = '/test.mp3';
		var audio = new Audio(audioSrc);
  		ok(typeof audio == "object", "Instantiated 'Audio' object instance should be of type 'object.'");
		ok(audio.src != null && typeof audio.src != 'undefined', "Instantiated 'Audio' object's 'src' property should not be null or undefined.");
		ok(audio.src.indexOf(audioSrc) >= 0, "Instantiated 'Audio' object's 'src' property should match constructor parameter.");
		ok(audio.loop != null && typeof audio.loop != 'undefined', "Instantiated 'Audio' object's 'loop' property should not be null or undefined.");
		ok(audio.loop == false, "Instantiated 'Audio' object's 'loop' property should initially be false.");
		ok(typeof audio.error != 'undefined', "Instantiated 'Audio' object's 'error' property should not undefined.");
		ok(audio.error == null, "Instantiated 'Audio' object's 'error' should initially be null.");
	});
};