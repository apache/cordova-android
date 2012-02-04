Cordova Android
===

Cordova Android is an Android application library that allows for Cordova based projects to be built for the Android Platform. Cordova based applications are, at the core, an application written with web technology: HTML, CSS and JavaScript. 

Apache Cordova is an effort undergoing incubation at The Apache
Software Foundation (ASF), sponsored by the Apache Incubator project.
Incubation is required of all newly accepted projects until a further
review indicates that the infrastructure, communications, and decision
making process have stabilized in a manner consistent with other
successful ASF projects. While incubation status is not necessarily
a reflection of the completeness or stability of the code, it does
indicate that the project has yet to be fully endorsed by the ASF.

Requires
---

- Java JDK 1.5
- Apache ANT
- Android SDK [http://developer.android.com](http://developer.android.com)


Building
---

To create your cordova.jar, run in the framework directory:

    android update project -p . -t android-15
    ant jar


Cordova Android Developer Tools
---

The Cordova developer tooling is split between general tooling and project level tooling. 

General Commands

    ./bin/create [path package activity] ... create the ./example app or a cordova android project
    ./bin/bench ............................ generate a bench proj
    ./bin/autotest ......................... test the cli tools
    ./bin/test ............................. run mobile-spec

Project Commands

These commands live in a generated Cordova Android project.

    ./cordovap/debug [path] ..................... install to first device
    ./cordova/emulate .......................... start avd (emulator) named default
    ./cordova/log .............................. starts logcat

Running the Example Project
---

Start avd (emulator) named `default`:

    ./bin/emulate

Create the example project and build it to the first device:

    ./bin/create
    cd example
    ./cordova/debug

Start adb logcat (console.log calls output here):

    ./cordova/log

Running the [callback/callback-test](http://github.com/callback/callback-test) tests:
---

    ./bin/test

Creating a new Cordova Android Project
---

    ./bin/create ~/Desktop/myapp com.phonegap.special MyApp

Importing a Cordova Android Project into Eclipse
----

1. File > New > Project...
2. Android > Android Project
3. Create project from existing source (point to the generated app found in tmp/android)
4. Right click on libs/cordova.jar and add to build path
5. Right click on the project root: Run as > Run Configurations
6. Click on the Target tab and select Manual (this way you can choose the emulator or device to build to)


Further Reading
---

- [http://developer.android.com](http://developer.android.com)
- [http://docs.phonegap.com](http://docs.phonegap.com)
- [http://wiki.phonegap.com](http://wiki.phonegap.com)
