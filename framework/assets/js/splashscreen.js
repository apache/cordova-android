PhoneGap.addConstructor(function() {
    if (typeof navigator.splashScreen == "undefined") {
    	navigator.splashScreen = SplashScreen;  // SplashScreen object come from native side through addJavaScriptInterface
    }
});