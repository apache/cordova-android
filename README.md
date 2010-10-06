PhoneGap/Android
================
PhoneGap/Android is an Android application library that allows for PhoneGap based projects to be built for the Android Platform. PhoneGap based applications are, at the core, an application written with web technology: HTML, CSS and JavaScript. 

Pre Requisites
--------------
- Java JDK 1.5
- Android SDK [http://developer.android.com](http://developer.android.com)
- Apache ANT
- Ruby (Optional, see section: DroidGap with JRuby)

Install
-------

On any POSIX machine add PhoneGap/Android to your PATH variable like so:

    export PATH=$PATH:~/phonegap-android/bin

On Windows add the phonegap-android/bin to your PATH as normal.

DroidGap: PhoneGap/Android Dev Script
-------------------------------------

Tools for developers building mobile apps using PhoneGap for Android.

Usage:

<pre>droidgap [command] [parameters]</pre>

Commands:    

<pre>
	help ...... See this message. Type help [command name] to see specific help topics.
	gen ....... Generate an example PhoneGap application to current directory.
	create .... Creates an Android compatible project from a WWW folder. 
	classic ... Backwards support for droidgap script. Run "droidgap help classic" for more info.
	update .... Copy a fresh phonegap.jar and phonegap.js into a valid PhoneGap/Android project.
	test ...... Gets edge copy of mobile-spec and runs in first device or emulator attached.
</pre>

Quickstart:

<pre>
  	$ droidgap gen example 
  	$ cd example
	$ ant debug install && adb logcat
</pre>

DroidGap with JRuby
-------------------

If you want to use the droidgap command but do not want to install Ruby then you can call it using jruby jar included in the lib folder. All the options are the same and a call looks like this:

    java -jar jruby-complete-1.4.0RC1.jar ../bin/droidgap help run
    
Keep in mind this will be slower due to JVM warmup.

Importing a PhoneGap/Android app into Eclipse
---------------------------------------------

1. File > New > Project...
2. Android > Android Project
3. Create project from existing source (point to the generated app found in tmp/android)
4. Right click on libs/phonegap.jar and add to build path
5. Right click on the project root: Run as > Run Configurations
6. Click on the Target tab and select Manual (this way you can choose the emulator or device to build to)


Common Command Line Tasks
=========================

Running Mobile Spec
---

droidgap test
    
Compile an APK
---

Make sure you have a device plugged in (with debugging enabled) or a running emulator. Then:

	ant debug install
	
or

	droidgap run

Converting a W3C Widget into a an APK
---

Given a Widget called FooBar with an index.html file in it. You navigate to its folder and run:

	droidgap create
	cd ../FooBar_android
	ant debug install

List devices attached
---

	adb devices 
    List of devices attached 
    0123456789012	device

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
