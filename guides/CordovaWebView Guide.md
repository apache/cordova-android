# How to use Cordova as a component #

Beginning in Cordova 1.8, with the assistance of the CordovaActivity, you can use Cordova as a component in your Android applications.  This component is known in Android
as the CordovaWebView, and new Cordova-based applications from 1.8 and greater will be using the CordovaWebView as its main view, whether the legacy DroidGap approach is 
used or not.


The pre-requisites are the same as the pre-requisites for Android application development.  It is assumed that you are familiar with Android Development.  If not, please
look at the Getting Started guide to developing an Cordova Application and start there before continuing with this approach.  Since this is not the main method that people use
to run applications, the instructions are currently manual.  In the future, we may try to further automate the project generation.

## Pre-requisites ##

1. **Cordova 1.8** or greater downloaded
2. Android SDK updated with 15

## Guide to using CordovaWebView in an Android Project ##

1. Use bin/create to fetch the commons-codec-1.6.jar
2. Go into framework and run ant jar to build the cordova jar (currently cordova-1.8.jar at the time of writing)
3. Copy the cordova jar into your Android project libs directory
4. Edit your main.xml to look similar the following. The layout_height, layout_width and ID can be modified to suit your application:

` <org.apache.cordova.CordovaWebView$
        android:id="@+id/tutorialView"$
        android:layout_width="match_parent"$
        android:layout_height="match_parent" />$
`

5. Modify your activity so that it implements the CordovaInterface.  It is recommended that you implement the methods that are included.  You may wish to copy the methods from framework/src/org/apache/cordova/DroidGap.java, or you may wish to implement your own methods.  Below is a fragment of code from a basic application that uses the interface:

`
public class CordovaViewTestActivity extends Activity implements CordovaInterface {$
    CordovaWebView phoneGap;$
    $
    /** Called when the activity is first created. */$
    @Override$
    public void onCreate(Bundle savedInstanceState) {$
        super.onCreate(savedInstanceState);$
        setContentView(R.layout.main);$
        $
        phoneGap = (CordovaWebView) findViewById(R.id.phoneGapView);$
        $
        phoneGap.loadUrl("file:///android_asset/www/index.html");$
    }$
`

6. Copy the HTML and Javascript used to the assets directory of your Android project
7. Copy cordova.xml and plugins.xml from framework/res/xml to the framework/res/xml in your project




