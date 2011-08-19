PhoneGap/Android
===

PhoneGap/Android is an Android application library that allows for PhoneGap based projects to be built for the Android Platform. PhoneGap based applications are, at the core, an application written with web technology: HTML, CSS and JavaScript. 

Pre Requisites
---

- Java JDK 1.5
- Android SDK [http://developer.android.com](http://developer.android.com)
- Apache ANT

PhoneGap/Android Developer Tools
---

Tools for developers building mobile apps using PhoneGap for Android.

Commands

    ./bin/create [path package activity] ... create a phonegap/android project
    ./bin/debug ............................ install to first device
    ./bin/emulate .......................... start emulator named default
    ./bin/log .............................. starts logging to stdout
    ./bin/test ............................. run mobile-spec

Usage

    # start avd (emulator) named 'default'
    ./bin/emulate

    # create the exmaple project and build it to the first device
    ./bin/create && cd example && ./../bin/debug

    # start logging to stdout
    ./bin/log

Importing a PhoneGap/Android app into Eclipse
---------------------------------------------

1. File > New > Project...
2. Android > Android Project
3. Create project from existing source (point to the generated app found in tmp/android)
4. Right click on libs/phonegap.jar and add to build path
5. Right click on the project root: Run as > Run Configurations
6. Click on the Target tab and select Manual (this way you can choose the emulator or device to build to)


Common Commandline Tools
===

List devices attached
---

	adb devices 

Install APK onto device
---

	apk -s 0123456789012 install phonegap.apk
    
Logging 
---

Via console.log calls from your apps javascript.

	adb logcat
    
Debugging
---
    
Attach it to a process on the device

    $ adb jdwp
    adb forward tcp:8000 jdwp: jdb -attach localhost:8000
    
    
For more info see
-----------------
- [http://docs.phonegap.com](http://docs.phonegap.com)
- [http://wiki.phonegap.com](http://wiki.phonegap.com)
