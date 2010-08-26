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
