var gradle_builder = require('../../../bin/templates/cordova/lib/builders/GradleBuilder.js');
var fs = require('fs');
var superspawn = require('cordova-common').superspawn;
var builder;

describe('Gradle Builder', function() {
    beforeEach(function() {
        spyOn(fs, 'existsSync').and.returnValue(true);
        builder = new gradle_builder('/root');
        spyOn(superspawn, 'spawn');
    });

    describe('runGradleWrapper method', function() {
        it('should run the provided gradle command if a gradle wrapper does not already exist', function() {
            fs.existsSync.and.returnValue(false);
            builder.runGradleWrapper('/my/sweet/gradle');
            expect(superspawn.spawn).toHaveBeenCalledWith('/my/sweet/gradle', jasmine.any(Array), jasmine.any(Object));
        });
        it('should do nothing if a gradle wrapper exists in the project directory', function() {
            fs.existsSync.and.returnValue(true);
            builder.runGradleWrapper('/my/sweet/gradle');
            expect(superspawn.spawn).not.toHaveBeenCalledWith('/my/sweet/gradle', jasmine.any(Array), jasmine.any(Object));
        });
    });
});
