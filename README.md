PhoneGap/Android
===

PhoneGap/Android is an Android application library that allows for PhoneGap based projects to be built for the Android Platform. PhoneGap based applications are, at the core, an application written with web technology: HTML, CSS and JavaScript. 

Requires
---

- Java JDK 1.5
- Apache ANT
- Android SDK [http://developer.android.com](http://developer.android.com)

PhoneGap/Android Developer Tools
---

The PhoneGap developer tooling is split between general tooling and project level tooling. 

Commands

    ./bin/create [path package activity] ... create the ./exmaple app or a phonegap/android project
    ./bin/bench ............................ generate a bench proj
    ./bin/autotest ......................... test the cli tools
    ./bin/test ............................. run mobile-spec

Project Commands

These commands live in a generated PhoneGap/Android project.

    ./phonegap/debug [path] ..................... install to first device
    ./phonegap/emulate .......................... start avd (emulator) named default
    ./phonegap/log .............................. starts logcat

Running the Example Project
---

Start avd (emulator) named `default`:

    ./bin/emulate

Create the exmaple project and build it to the first device:

    ./bin/create
    cd example
    ./phonegap/debug

Start adb logcat (console.log calls output here):

    ./phonegap/log

Running the [phonegap/mobile-spec](http://github.com/phonegap/mobile-spec) tests:

    ./bin/test

Create a new PhoneGap/Android Project

    ./bin/create ~/Desktop/myapp com.phonegap.special MyApp

Importing a PhoneGap/Android Project into Eclipse
----

1. File > New > Project...
2. Android > Android Project
3. Create project from existing source (point to the generated app found in tmp/android)
4. Right click on libs/phonegap.jar and add to build path
5. Right click on the project root: Run as > Run Configurations
6. Click on the Target tab and select Manual (this way you can choose the emulator or device to build to)


Further Reading
---

- [http://developer.android.com](http://developer.android.com)
- [http://docs.phonegap.com](http://docs.phonegap.com)
- [http://wiki.phonegap.com](http://wiki.phonegap.com)
