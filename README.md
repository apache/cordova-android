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

Commands

    ./bin/create [path package activity] ... create the ./exmaple app or a phonegap/android project
    ./bin/debug [path] ..................... install to first device
    ./bin/emulate .......................... start avd (emulator) named default
    ./bin/log .............................. starts logcat
    ./bin/test ............................. run mobile-spec
    ./bin/autotest ......................... run the cli unit tests (requires nodejs)

Running the Example Project
---

Start avd (emulator) named `default`:

    ./bin/emulate

Create the exmaple project and build it to the first device:

    ./bin/create
    ./bin/debug ./example

Start adb logcat (console.log calls output here):

    ./bin/log

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
