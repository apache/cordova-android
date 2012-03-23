## Mobile Spec Suite ##

These specs are designed to run inside the mobile device that implements it - _it will fail in the DESKTOP browser_.

These set of tests is designed to be used with Cordova. You should initialize a fresh Cordova repository for a target platform and then toss these files into the www folder, replacing the
contents. 

Make sure you include Cordova-\*.js in the www folder.  You also need to edit Cordova.js to reference the Cordova-\*.js file you are testing.
For example, to test with Cordova-0.9.6.1, the Cordova.js file would be:

    document.write('<script type="text/javascript" charset="utf-8" src="../Cordova-0.9.6.1.js"></script>');
    document.write('<script type="text/javascript" charset="utf-8" src="Cordova-0.9.6.1.js"></script>');

This is done so that you don't have to modify every HTML file when you want to test a new version of Cordova.

The goal is to test mobile device functionality inside a mobile browser.
Where possible, the Cordova API lines up with HTML 5 spec. Maybe down
the road we could use this spec for parts of HTML 5, too :)
