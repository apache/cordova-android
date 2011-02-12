/*
 * Copyright (c) 2010 Nathan Rajlich (https://github.com/TooTallNate)
 * Copyright (c) 2010 Animesh Kumar  (https://github.com/anismiles)
 *  
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *  
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *  
 */
package com.phonegap.websocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.webkit.WebView;

/**
 * The <tt>WebSocket</tt> is an implementation of WebSocket Client API, and
 * expects a valid "ws://" URI to connect to. When connected, an instance
 * recieves important events related to the life of the connection, like
 * <var>onOpen</var>, <var>onClose</var>, <var>onError</var> and
 * <var>onMessage</var>. An instance can send messages to the server via the
 * <var>send</var> method.
 * 
 * @author Animesh Kumar
 */
public class WebSocket implements Runnable {

	/**
	 * Enum for WebSocket Draft
	 */
	public enum Draft {
		DRAFT75, DRAFT76
	}

	////////////////// CONSTANT
	/**
	 * An empty string
	 */
	private static String BLANK_MESSAGE = "";
	/**
	 * The javascript method name for onOpen event.
	 */
	private static String EVENT_ON_OPEN = "onopen";
	/**
	 * The javascript method name for onMessage event.
	 */
	private static String EVENT_ON_MESSAGE = "onmessage";
	/**
	 * The javascript method name for onClose event.
	 */
	private static String EVENT_ON_CLOSE = "onclose";
	/**
	 * The javascript method name for onError event.
	 */
	private static String EVENT_ON_ERROR = "onerror";
	/**
	 * The default port of WebSockets, as defined in the spec.
	 */
	public static final int DEFAULT_PORT = 80;
	/**
	 * The WebSocket protocol expects UTF-8 encoded bytes.
	 */
	public static final String UTF8_CHARSET = "UTF-8";
	/**
	 * The byte representing Carriage Return, or \r
	 */
	public static final byte DATA_CR = (byte) 0x0D;
	/**
	 * The byte representing Line Feed, or \n
	 */
	public static final byte DATA_LF = (byte) 0x0A;
	/**
	 * The byte representing the beginning of a WebSocket text frame.
	 */
	public static final byte DATA_START_OF_FRAME = (byte) 0x00;
	/**
	 * The byte representing the end of a WebSocket text frame.
	 */
	public static final byte DATA_END_OF_FRAME = (byte) 0xFF;

	////////////////// INSTANCE Variables
	/**
	 * The WebView instance from Phonegap DroidGap
	 */
	private WebView appView;
	/**
	 * The unique id for this instance (helps to bind this to javascript events)
	 */
	private String id;
	/**
	 * The URI this client is supposed to connect to.
	 */
	private URI uri;
	/**
	 * The port of the websocket server
	 */
	private int port;
	/**
	 * The Draft of the WebSocket protocol the Client is adhering to.
	 */
	private Draft draft;
	/**
	 * The <tt>SocketChannel</tt> instance to use for this server connection.
	 * This is used to read and write data to.
	 */
	private SocketChannel socketChannel;
	/**
	 * The 'Selector' used to get event keys from the underlying socket.
	 */
	private Selector selector;
	/**
	 * Keeps track of whether or not the client thread should continue running.
	 */
	private boolean running;
	/**
	 * Internally used to determine whether to recieve data as part of the
	 * remote handshake, or as part of a text frame.
	 */
	private boolean handshakeComplete;
	/**
	 * The 1-byte buffer reused throughout the WebSocket connection to read
	 * data.
	 */
	private ByteBuffer buffer;
	/**
	 * The bytes that make up the remote handshake.
	 */
	private ByteBuffer remoteHandshake;
	/**
	 * The bytes that make up the current text frame being read.
	 */
	private ByteBuffer currentFrame;
	/**
	 * Queue of buffers that need to be sent to the client.
	 */
	private BlockingQueue<ByteBuffer> bufferQueue;
	/**
	 * Lock object to ensure that data is sent from the bufferQueue in the
	 * proper order
	 */
	private Object bufferQueueMutex = new Object();
	/**
	 * Number 1 used in handshake
	 */
	private int number1 = 0;
	/**
	 * Number 2 used in handshake
	 */
	private int number2 = 0;
	/**
	 * Key3 used in handshake
	 */
	private byte[] key3 = null;

	/**
	 * Constructor.
	 * 
	 * Note: this is protected because it's supposed to be instantiated from
	 * {@link WebSocketFactory} only.
	 * 
	 * @param appView
	 *            {@link android.webkit.WebView}
	 * @param uri
	 *            websocket server {@link URI}
	 * @param draft
	 *            websocket server {@link Draft} implementation (75/76)
	 * @param id
	 *            unique id for this instance
	 */
	protected WebSocket(WebView appView, URI uri, Draft draft, String id) {
		this.appView = appView;
		this.uri = uri;
		this.draft = draft;

		// port
		port = uri.getPort();
		if (port == -1) {
			port = DEFAULT_PORT;
		}

		// Id
		this.id = id;

		this.bufferQueue = new LinkedBlockingQueue<ByteBuffer>();
		this.handshakeComplete = false;
		this.remoteHandshake = this.currentFrame = null;
		this.buffer = ByteBuffer.allocate(1);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////// WEB SOCKET API Methods
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Starts a new Thread and connects to server
	 */
	public void connect() {
		this.running = true;
		(new Thread(this)).start();
	}

	public void run() {
		try {
			_connect();
		} catch (IOException e) {
			this.onError(e);
		}
	}

	/**
	 * Closes connection with server
	 */
	public void close() {
		// close socket channel
		try {
			this.socketChannel.close();
		} catch (IOException e) {
			this.onError(e);
		}
		this.running = false;
		selector.wakeup();

		// fire onClose method
		this.onClose();
	}

	/**
	 * Sends <var>text</var> to server
	 * 
	 * @param text
	 *            String to send to server
	 */
	public void send(String text) {
		try {
			_send(text);
		} catch (IOException e) {
			this.onError(e);
		}
	}

	/**
	 * Called when an entire text frame has been recieved.
	 * 
	 * @param msg
	 *            Message from websocket server
	 */
	public void onMessage(String msg) {
		appView.loadUrl(buildJavaScriptData(EVENT_ON_MESSAGE, msg));
	}

	public void onOpen() {
		appView.loadUrl(buildJavaScriptData(EVENT_ON_OPEN, BLANK_MESSAGE));
	}

	public void onClose() {
		appView.loadUrl(buildJavaScriptData(EVENT_ON_CLOSE, BLANK_MESSAGE));
	}

	public void onError(Throwable t) {
		String msg = t.getMessage();
		appView.loadUrl(buildJavaScriptData(EVENT_ON_ERROR, msg));
	}

	public String getId() {
		return id;
	}

	/**
	 * Builds text for javascript engine to invoke proper event method with proper data.
	 * 
	 * @param event	websocket event (onOpen, onMessage etc.)
	 * @param msg	Text message received from websocket server
	 * @return
	 */
	private String buildJavaScriptData(String event, String msg) {
		String _d = "javascript:WebSocket." + event + "(" + "{" + "\"_target\":\"" + id + "\","
				+ "\"_data\":'" + msg + "'" + "}" + ")";
		return _d;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////// WEB SOCKET Internal Methods
	// //////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////

	private boolean _send(String text) throws IOException {
		if (!this.handshakeComplete) {
			throw new NotYetConnectedException();
		}
		if (text == null) {
			throw new NullPointerException("Cannot send 'null' data to a WebSocket.");
		}

		// Get 'text' into a WebSocket "frame" of bytes
		byte[] textBytes = text.getBytes(UTF8_CHARSET.toString());
		ByteBuffer b = ByteBuffer.allocate(textBytes.length + 2);
		b.put(DATA_START_OF_FRAME);
		b.put(textBytes);
		b.put(DATA_END_OF_FRAME);
		b.rewind();

		// See if we have any backlog that needs to be sent first
		if (_write()) {
			// Write the ByteBuffer to the socket
			this.socketChannel.write(b);
		}

		// If we didn't get it all sent, add it to the buffer of buffers
		if (b.remaining() > 0) {
			if (!this.bufferQueue.offer(b)) {
				throw new IOException("Buffers are full, message could not be sent to"
						+ this.socketChannel.socket().getRemoteSocketAddress());
			}
			return false;
		}
		return true;
	}

	// actual connection logic
	private void _connect() throws IOException {

		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress(uri.getHost(), port));

		// More info:
		// http://groups.google.com/group/android-developers/browse_thread/thread/45a8b53e9bf60d82
		// http://stackoverflow.com/questions/2879455/android-2-2-and-bad-address-family-on-socket-connect
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.net.preferIPv6Addresses", "false");

		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_CONNECT);

		// Continuous loop that is only supposed to end when "close" is called.
		while (this.running) {
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> i = keys.iterator();

			while (i.hasNext()) {
				SelectionKey key = i.next();
				i.remove();
				if (key.isConnectable()) {
					if (socketChannel.isConnectionPending()) {
						socketChannel.finishConnect();
					}
					socketChannel.register(selector, SelectionKey.OP_READ);
					_writeHandshake();
				}
				if (key.isReadable()) {
					try {
						_read();
					} catch (NoSuchAlgorithmException nsa) {
						this.onError(nsa);
					}
				}
			}
		}
	}

	private void _writeHandshake() throws IOException {
		String path = this.uri.getPath();
		if (path.indexOf("/") != 0) {
			path = "/" + path;
		}

		String host = uri.getHost() + (port != DEFAULT_PORT ? ":" + port : "");
		String origin = "*"; // TODO: Make 'origin' configurable
		String request = "GET " + path + " HTTP/1.1\r\n" + "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n" + "Host: " + host + "\r\n" + "Origin: " + origin
				+ "\r\n";

		// Add randon keys for Draft76
		if (this.draft == Draft.DRAFT76) {
			request += "Sec-WebSocket-Key1: " + this._randomKey() + "\r\n";
			request += "Sec-WebSocket-Key2: " + this._randomKey() + "\r\n";
			this.key3 = new byte[8];
			(new Random()).nextBytes(this.key3);
		}

		request += "\r\n";
		_write(request.getBytes(UTF8_CHARSET));
		if (this.key3 != null) {
			_write(this.key3);
		}

	}

	private boolean _write() throws IOException {
		synchronized (this.bufferQueueMutex) {
			ByteBuffer buffer = this.bufferQueue.peek();
			while (buffer != null) {
				this.socketChannel.write(buffer);
				if (buffer.remaining() > 0) {
					return false; // Didn't finish this buffer. There's more to
					// send.
				} else {
					this.bufferQueue.poll(); // Buffer finished. Remove it.
					buffer = this.bufferQueue.peek();
				}
			}
			return true;
		}
	}

	private void _write(byte[] bytes) throws IOException {
		this.socketChannel.write(ByteBuffer.wrap(bytes));
	}

	private void _read() throws IOException, NoSuchAlgorithmException {
		this.buffer.rewind();

		int bytesRead = -1;
		try {
			bytesRead = this.socketChannel.read(this.buffer);
		} catch (Exception ex) {
		}

		if (bytesRead == -1) {
			close();
		} else if (bytesRead > 0) {
			this.buffer.rewind();

			if (!this.handshakeComplete) {
				_readHandshake();
			} else {
				_readFrame();
			}
		}
	}

	private void _readFrame() throws UnsupportedEncodingException {
		byte newestByte = this.buffer.get();

		if (newestByte == DATA_START_OF_FRAME) { // Beginning of Frame
			this.currentFrame = null;

		} else if (newestByte == DATA_END_OF_FRAME) { // End of Frame
			String textFrame = null;
			// currentFrame will be null if END_OF_FRAME was send directly after
			// START_OF_FRAME, thus we will send 'null' as the sent message.
			if (this.currentFrame != null) {
				textFrame = new String(this.currentFrame.array(), UTF8_CHARSET.toString());
			}
			// fire onMessage method
			this.onMessage(textFrame);

		} else { // Regular frame data, add to current frame buffer
			ByteBuffer frame = ByteBuffer.allocate((this.currentFrame != null ? this.currentFrame
					.capacity() : 0)
					+ this.buffer.capacity());
			if (this.currentFrame != null) {
				this.currentFrame.rewind();
				frame.put(this.currentFrame);
			}
			frame.put(newestByte);
			this.currentFrame = frame;
		}
	}

	private void _readHandshake() throws IOException, NoSuchAlgorithmException {
		ByteBuffer ch = ByteBuffer.allocate((this.remoteHandshake != null ? this.remoteHandshake
				.capacity() : 0)
				+ this.buffer.capacity());
		if (this.remoteHandshake != null) {
			this.remoteHandshake.rewind();
			ch.put(this.remoteHandshake);
		}
		ch.put(this.buffer);
		this.remoteHandshake = ch;
		byte[] h = this.remoteHandshake.array();
		// If the ByteBuffer contains 16 random bytes, and ends with
		// 0x0D 0x0A 0x0D 0x0A (or two CRLFs), then the client
		// handshake is complete for Draft 76 Client.
		if ((h.length >= 20 && h[h.length - 20] == DATA_CR && h[h.length - 19] == DATA_LF
				&& h[h.length - 18] == DATA_CR && h[h.length - 17] == DATA_LF)) {
			_readHandshake(new byte[] { h[h.length - 16], h[h.length - 15], h[h.length - 14],
					h[h.length - 13], h[h.length - 12], h[h.length - 11], h[h.length - 10],
					h[h.length - 9], h[h.length - 8], h[h.length - 7], h[h.length - 6],
					h[h.length - 5], h[h.length - 4], h[h.length - 3], h[h.length - 2],
					h[h.length - 1] });

			// If the ByteBuffer contains 8 random bytes,ends with
			// 0x0D 0x0A 0x0D 0x0A (or two CRLFs), and the response
			// contains Sec-WebSocket-Key1 then the client
			// handshake is complete for Draft 76 Server.
		} else if ((h.length >= 12 && h[h.length - 12] == DATA_CR && h[h.length - 11] == DATA_LF
				&& h[h.length - 10] == DATA_CR && h[h.length - 9] == DATA_LF)
				&& new String(this.remoteHandshake.array(), UTF8_CHARSET)
						.contains("Sec-WebSocket-Key1")) {// ************************
			_readHandshake(new byte[] { h[h.length - 8], h[h.length - 7], h[h.length - 6],
					h[h.length - 5], h[h.length - 4], h[h.length - 3], h[h.length - 2],
					h[h.length - 1] });

			// Consider Draft 75, and the Flash Security Policy
			// Request edge-case.
		} else if ((h.length >= 4 && h[h.length - 4] == DATA_CR && h[h.length - 3] == DATA_LF
				&& h[h.length - 2] == DATA_CR && h[h.length - 1] == DATA_LF)
				&& !(new String(this.remoteHandshake.array(), UTF8_CHARSET).contains("Sec"))
				|| (h.length == 23 && h[h.length - 1] == 0)) {
			_readHandshake(null);
		}
	}

	private void _readHandshake(byte[] handShakeBody) throws IOException, NoSuchAlgorithmException {
		// byte[] handshakeBytes = this.remoteHandshake.array();
		// String handshake = new String(handshakeBytes, UTF8_CHARSET);
		// TODO: Do some parsing of the returned handshake, and close connection
		// in received anything unexpected!

		this.handshakeComplete = true;
		boolean isConnectionReady = true;

		if (this.draft == WebSocket.Draft.DRAFT76) {
			if (handShakeBody == null) {
				isConnectionReady = true;
			}
			byte[] challenge = new byte[] { (byte) (this.number1 >> 24),
					(byte) ((this.number1 << 8) >> 24), (byte) ((this.number1 << 16) >> 24),
					(byte) ((this.number1 << 24) >> 24), (byte) (this.number2 >> 24),
					(byte) ((this.number2 << 8) >> 24), (byte) ((this.number2 << 16) >> 24),
					(byte) ((this.number2 << 24) >> 24), this.key3[0], this.key3[1], this.key3[2],
					this.key3[3], this.key3[4], this.key3[5], this.key3[6], this.key3[7] };
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] expected = md5.digest(challenge);
			for (int i = 0; i < handShakeBody.length; i++) {
				if (expected[i] != handShakeBody[i]) {
					isConnectionReady = true;
				}
			}
		}

		if (isConnectionReady) {
			// fire onOpen method
			this.onOpen();
		} else {
			close();
		}
	}

	private String _randomKey() {
		Random r = new Random();
		long maxNumber = 4294967295L;
		long spaces = r.nextInt(12) + 1;
		int max = new Long(maxNumber / spaces).intValue();
		max = Math.abs(max);
		int number = r.nextInt(max) + 1;
		if (this.number1 == 0) {
			this.number1 = number;
		} else {
			this.number2 = number;
		}
		long product = number * spaces;
		String key = Long.toString(product);
		int numChars = r.nextInt(12);
		for (int i = 0; i < numChars; i++) {
			int position = r.nextInt(key.length());
			position = Math.abs(position);
			char randChar = (char) (r.nextInt(95) + 33);
			// exclude numbers here
			if (randChar >= 48 && randChar <= 57) {
				randChar -= 15;
			}
			key = new StringBuilder(key).insert(position, randChar).toString();
		}
		for (int i = 0; i < spaces; i++) {
			int position = r.nextInt(key.length() - 1) + 1;
			position = Math.abs(position);
			key = new StringBuilder(key).insert(position, "\u0020").toString();
		}
		return key;
	}
}
