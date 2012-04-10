Bryce Curtis (5):
      [CB-352] Support initializing DroidGap with existing WebView, WebViewClient and webViewChrome.     [CB-353] Create PluginEntry object to use by PluginManager.
      [CB-367] Back button event should fire on key up not key down     Also changed menu key and search key to be consistent.
      Tests to verify Android native features.
      [CB-423] Problem displaying patch-9 splash screen.
      Update project template cordova.js reference and title.

Fil Maj (6):
      switched from "require" syntax to "cordova.require"
      cordova.require("cordova") is pretty funny. wish i didnt write it
      updates to JS: removing require+define from global scope, tweaking geolocation code, online/offline events fire on document now
      removed old javascript files and removed unused target + commented out lines in build.xml
      spacing fixes, null check in getPhoneType in contacts, returning error integers instead of objects in contacts
      updating network status plugin label and updating cordova-js to latest

Joe Bowser (11):
      We show the default 404 on non-resolved domains
      Fixing CB-210 with patch and adding fix for CB-210
      Tweaked File Transfer to fix CB-74
      Changing to the modern icon
      Added temporary Cordova splash for now
      Checking for the callback server before we call sendJavascript for the Kindle Fire, CB-247
      Fixing CB-343: We need to respect the whitelist
      Fixing a bug with File Upload on Android where Chunked mode isn't used by default
      First stab at CB-21, I really need more info before I can close this
      Tagged 1.6rc1
      Fixing the template, since this doesn't have to be unit tested. :)

macdonst (12):
      CB-383: Fixes issue with misspelled destinationType for Camera.getPicture()
      Fix for CB-389: resolveLocalFileSystemURI does not work on a resized image captured from Camera.getPicture()
      Fixing license header in com.phonegap.api.PluginManager
      CB-321: Media API: 'mediaSuccess' callback param to new Media() is called soon after new obj created
      CB-163: contactFindOptions.filter does not work as expected on Android
      CB-426: camera.getPicture ignores mediaType in 1.5
      Updating cordova.android.js for CB-421 and CB-426
      CB-438: File metadata.modificationTime returns an invalid date
      Return MediaError object and not error code from native side of Media API
      CB-446: Enhance setting data source for local files in AudioPlayer
      CB-453: FileWriter.append - Chinese characters are not appended to the file correctly
      CB-446: Enhance setting data source for local files in AudioPlayer

