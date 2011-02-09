package com.phonegap.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;


import android.webkit.WebView;

/**
 * @author http://github.com/anismiles
 *
 */
public class WebSocketFactory {

	/** The app view. */
	WebView appView;

	/**
	 * Instantiates a new web socket factory.
	 *
	 * @param appView the app view
	 */
	public WebSocketFactory(WebView appView) {
		this.appView = appView;
	}

	/**
	 * Gets the web socket.
	 *
	 * @param url the url
	 * @return the web socket
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public WebSocket getInstance(String url) throws URISyntaxException {
		// random id
		String id = "WebSocket." + new Random().nextInt(100);
		WebSocket socket =  new WebSocket(appView, new URI(url), Protocol.Draft.DRAFT75, id);
		socket.connect();
		return socket;
	}
}
