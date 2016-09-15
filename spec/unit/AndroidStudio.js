var AndroidStudio = require('../../bin/templates/cordova/lib/AndroidStudio');

describe('AndroidStudio module', function () {
    it('should detect Android Studio project', function() {
      var root = './fixtures/android_studio_project';
      spyOn(AndroidStudio, 'isAndroidStudioProject').andReturn(true);
      AndroidStudio.isAndroidStudioProject(root);
    });
    it('should detect non Android Studio project', function() {
      var root = './fixtures/android_project';
      spyOn(AndroidStudio, 'isAndroidStudioProject').andReturn(false);
      AndroidStudio.isAndroidStudioProject(root);
    });
});
