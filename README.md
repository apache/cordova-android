PhoneGap Android
=============================================================
PhoneGap Android is an Android application library that allows for PhoneGap-based projects to be built for the Android Platform. PhoneGap-based applications are, at the core, an application written in web-based languages, generally HTML, CSS and JavaScript. 

Pre-requisites
-------------------------------------------------------------
  * Java JDK 1.5
  * Android SDK Package [http://developer.android.com](http://developer.android.com)
  * Apache ANT (For build script)
  * Ruby, Rubygems, nokogiri (for build.rb)

Recommended:
----------------------------------------------------------------
  * Eclipse (Recommended for back-end debugging, not required)

Getting Started with PhoneGap Android
--------------------------------------
1. Make sure you have Ruby and the nokogiri gem installed. Run 'gem list' to see a list of installed Ruby gems, and 'gem install nokogiri' from command-line if you don't have it.
2. Clone the repository using git, from command line: git clone git://github.com/phonegap/phonegap-android.git
3. Run 'cd phonegap-android/framework'
4. Create a local.properties file with the following line in it:

    sdk-location=/path/to/your/androidsdk
    
5. Run 'ant jar' - this will create the phonegap.jar file and build a fresh phonegap.js for use in your HTML/JS/CSS-based application (for accessing native functionality).
6. cd back to the root directory (cd ..)
7. Run 'ruby build.rb <app-name> <package_name> <wwwdir> <path>', where:
 - app-name: Name of application without spaces
 - package name: Java namespace of package ( i.e. com.nitobi.demo). This must be unique otherwise it won't load properly on your phone
 - www-dir: www directory which includes an icon.png file for the icon
 - path: The path to copy the final project assets into, i.e. the final build path
8. Now you can use the generated Android code that is located in the 'path' directory from the above step and do what you want with it: import into Eclipse to test with the simulator or otherwise.

Using Eclipse with PhoneGap
-------------------------------------------------------------
The wiki article to get started with Eclipse can be found at [http://phonegap.pbworks.com/Getting-started-with-Android-PhoneGap-in-Eclipse](http://phonegap.pbworks.com/Getting-started-with-Android-PhoneGap-in-Eclipse)