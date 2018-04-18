
var path = require('path');
var AndroidStudio = require('../../bin/templates/cordova/lib/AndroidStudio');

describe('AndroidStudio module', function () {
    it('should return true for Android Studio project', function () {
        var root = path.join(__dirname, '../fixtures/android_studio_project/');
        var isAndStud = AndroidStudio.isAndroidStudioProject(root);
        expect(isAndStud).toBe(true);
    });
});
