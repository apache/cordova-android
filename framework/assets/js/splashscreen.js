/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

PhoneGap.addConstructor(function() {
    if (typeof navigator.splashScreen == "undefined") {
    	navigator.splashScreen = SplashScreen;  // SplashScreen object come from native side through addJavaScriptInterface
    }
});