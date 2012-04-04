## Android Native Tests ##

These tests are designed to verify Android native features and other Android specific features.

Before running the tests, they need to be set up.

1. Copy the version of cordova-x.y.z.js into assets/www directory
2. Edit assets/www/cordova.js to reference the correct version
3. Copy cordova-x.y.z.jar into libs directory

To run from command line:

4. Build by entering "ant debug install"
5. Run tests by clicking on "CordovaTest" icon on device

To run from Eclipse:

4. Import Android project into Eclipse
5. Ensure Project properties "Java Build Path" includes the lib/cordova-x.y.z.jar
6. Create run configuration if not already created
7. Run tests 


