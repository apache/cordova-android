
PhoneGap Android
=============================================================
PhoneGap Android is an Android application library that allows
for PhoneGap based projects to be built for the Android Platform.

Pre-requisites
-------------------------------------------------------------
  * Java JDK 1.5
  * Android SDK Package [http://developer.android.com](http://developer.android.com)
  * Apache ANT (For build script)

Recommended:
----------------------------------------------------------------
  * Ruby, Rubygems (for build.rb)
  * Eclipse (Recommended for back-end debugging, not required)

Getting Started from Command Line
--------------------------------------
Step 1: cd framework && ant jar
Step 2: In the root directory, run:

ruby build.rb <app-name> <package_name> <wwwdir> <path>

app-name: Name of application without spaces
package name: Java namespace of package ( i.e. com.nitobi.demo)
www-dir: www directory which includes an icon.png file for the icon
path: The path you wish to put your PhoneGap Application

Using Eclipse with PhoneGap
-------------------------------------------------------------
The wiki article to get started with Eclipse can be found at [http://phonegap.pbworks.com/Getting-started-with-Android-PhoneGap-in-Eclipse](http://phonegap.pbworks.com/Getting-started-with-Android-PhoneGap-in-Eclipse)

