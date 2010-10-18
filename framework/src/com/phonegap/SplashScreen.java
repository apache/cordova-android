/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 */
package com.phonegap;

public class SplashScreen {
	private final DroidGap gap;
	public SplashScreen(DroidGap gap) {
		this.gap = gap;
	}
	public void hide() {
		gap.runOnUiThread(new Runnable() {
			public void run() {
				gap.hideSplashScreen();
			}
		});
	}
}
