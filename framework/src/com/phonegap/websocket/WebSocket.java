package com.phonegap.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import android.webkit.WebView;

/**
 * @author http://github.com/anismiles
 * 
 */
public class WebSocket implements Runnable {

	// Constants
	private static String BLANK_MESSAGE = "";
	private static String EVENT_ON_OPEN = "onopen";
	private static String EVENT_ON_MESSAGE = "onmessage";
	private static String EVENT_ON_CLOSE = "onclose";

	// Instance Variables
	private WebView appView;
	private String id;
	private URI uri;
	private int port;
	private Protocol protocol;
	private Protocol.Draft draft;
	private SocketChannel channel;
	private Selector selector;
	private boolean running;

	// Constructor (only used from Factory)
	protected WebSocket(WebView appView, URI uri, Protocol.Draft draft, String id) {
		this.appView = appView;
		this.uri = uri;
		this.draft = draft;
		// port
		port = uri.getPort();
		if (port == -1) {
			port = Protocol.DEFAULT_PORT;
		}
		
		// Id
		this.id =  id;
	}

	// start a thread and connect to server
	public void connect() {
		this.running = true;
		(new Thread(this)).start();
	}

	// close connection
	public void close() throws IOException {
		this.running = false;
		selector.wakeup();
		protocol.close();
	}

	// send message
	public void send(String msg) throws IOException {
		protocol.send(msg);
	}

	public void run() {
		// TODO: Reconnection Logic
		try {
			_connect();
		} catch (IOException e) {
		}
	}

	// actual connection logic
	private void _connect() throws IOException {

		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(uri.getHost(), port));

		// More info:
		// http://groups.google.com/group/android-developers/browse_thread/thread/45a8b53e9bf60d82
		// http://stackoverflow.com/questions/2879455/android-2-2-and-bad-address-family-on-socket-connect
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");

		selector = Selector.open();
		this.protocol = new Protocol(channel, new LinkedBlockingQueue<ByteBuffer>(), this);
		channel.register(selector, SelectionKey.OP_CONNECT);

		// Continuous loop that is only supposed to end when "close" is called.
		while (this.running) {
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> i = keys.iterator();

			while (i.hasNext()) {
				SelectionKey key = i.next();
				i.remove();
				if (key.isConnectable()) {
					if (channel.isConnectionPending()) {
						channel.finishConnect();
					}
					channel.register(selector, SelectionKey.OP_READ);
					protocol.writeHandshake();
				}
				if (key.isReadable()) {
					try {
						protocol.read();
					} catch (NoSuchAlgorithmException e) {
					}
				}
			}
		}
	}

	public void onMessage(String message) {
		appView.loadUrl(buildJavaScriptData(EVENT_ON_MESSAGE, message));
	}

	public void onOpen() {
		appView.loadUrl(buildJavaScriptData(EVENT_ON_OPEN, BLANK_MESSAGE));
	}

	public void onClose() {
		appView.loadUrl(buildJavaScriptData(EVENT_ON_CLOSE, BLANK_MESSAGE));
	}

	public String getId() {
		return id;
	}

	protected Protocol.Draft getDraft() {
		return draft;
	}

	protected URI getUri() {
		return uri;
	}

	private String buildJavaScriptData(String event, String msg) {
		String _d = "javascript:WebSocket." + event + "(" + "{" + "\"_target\":\"" + id + "\","
				+ "\"_data\":'" + msg + "'" + "}" + ")";
		return _d;
	}
}
