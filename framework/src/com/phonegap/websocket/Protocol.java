package com.phonegap.websocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * @author http://github.com/anismiles
 *
 */
public final class Protocol {

	public enum Draft {
		DRAFT75,
		DRAFT76
	}

	// Constants
	public static final int DEFAULT_PORT = 80;
	public static final String UTF8_CHARSET = "UTF-8";
	public static final byte CR = (byte) 0x0D;
	public static final byte LF = (byte) 0x0A;
	public static final byte START_OF_FRAME = (byte) 0x00;
	public static final byte END_OF_FRAME = (byte) 0xFF;

	// Instance Varables
	private final SocketChannel socketChannel;
	private boolean handshakeComplete;
	private WebSocket webSocket;
	private ByteBuffer buffer;
	private ByteBuffer remoteHandshake;
	private ByteBuffer currentFrame;
	private BlockingQueue<ByteBuffer> bufferQueue;
	private Object bufferQueueMutex = new Object();

	private int number1 = 0;
	private int number2 = 0;
	private byte[] key3 = null;

	protected Protocol(SocketChannel socketChannel, BlockingQueue<ByteBuffer> bufferQueue,
			WebSocket webSocket) {
		this.socketChannel = socketChannel;
		this.bufferQueue = bufferQueue;
		this.handshakeComplete = false;
		this.remoteHandshake = this.currentFrame = null;
		this.buffer = ByteBuffer.allocate(1);
		this.webSocket = webSocket;
	}

	protected void writeHandshake() throws IOException {
		URI uri = this.webSocket.getUri();
		String path = uri.getPath();
		if (path.indexOf("/") != 0) {
			path = "/" + path;
		}
		
		int port = uri.getPort();
		if (port == -1) {
			port = Protocol.DEFAULT_PORT;
		}

		String host = uri.getHost() + (port != Protocol.DEFAULT_PORT ? ":" + port : "");
		String origin = "*"; // TODO: Make 'origin' configurable
		String request = "GET " + path + " HTTP/1.1\r\n" + "Upgrade: WebSocket\r\n"
				+ "Connection: Upgrade\r\n" + "Host: " + host + "\r\n" + "Origin: "
				+ origin + "\r\n";
		
		// Add randon keys for Draft76
		if (this.webSocket.getDraft() == Protocol.Draft.DRAFT76) {
			request += "Sec-WebSocket-Key1: " + this.generateRandomKey() + "\r\n";
			request += "Sec-WebSocket-Key2: " + this.generateRandomKey() + "\r\n";
			this.key3 = new byte[8];
			(new Random()).nextBytes(this.key3);
		}
		
		request += "\r\n";
		write(request.getBytes(Protocol.UTF8_CHARSET));
		if (this.key3 != null) {
			write(this.key3);
		}

	}
	
	protected void read() throws IOException, NoSuchAlgorithmException {
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
				readHandshake();
			} else {
				readFrame();
			}
		}
	}

	protected void close() throws IOException {
		this.socketChannel.close();
		// fire onClose method
		this.webSocket.onClose();
	}

	protected boolean send(String text) throws IOException {
		if (!this.handshakeComplete)
			throw new NotYetConnectedException();
		if (text == null)
			throw new NullPointerException("Cannot send 'null' data to a WebSocket.");

		// Get 'text' into a WebSocket "frame" of bytes
		byte[] textBytes = text.getBytes(UTF8_CHARSET.toString());
		ByteBuffer b = ByteBuffer.allocate(textBytes.length + 2);
		b.put(START_OF_FRAME);
		b.put(textBytes);
		b.put(END_OF_FRAME);
		b.rewind();

		// See if we have any backlog that needs to be sent first
		if (write()) {
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

	private	boolean write() throws IOException {
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

	protected void write(byte[] bytes) throws IOException {
		this.socketChannel.write(ByteBuffer.wrap(bytes));
	}

	private void readFrame() throws UnsupportedEncodingException {
		byte newestByte = this.buffer.get();

		if (newestByte == START_OF_FRAME) { // Beginning of Frame
			this.currentFrame = null;

		} else if (newestByte == END_OF_FRAME) { // End of Frame
			String textFrame = null;
			// currentFrame will be null if END_OF_FRAME was send directly after
			// START_OF_FRAME, thus we will send 'null' as the sent message.
			if (this.currentFrame != null) {
				textFrame = new String(this.currentFrame.array(), UTF8_CHARSET.toString());
			}
			// fire onMessage method
			this.webSocket.onMessage(textFrame);

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

	private void readHandshake() throws IOException, NoSuchAlgorithmException {
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
		if ((h.length >= 20 && h[h.length - 20] == CR && h[h.length - 19] == LF
				&& h[h.length - 18] == CR && h[h.length - 17] == LF)) {
			readHandshake(new byte[] { h[h.length - 16], h[h.length - 15], h[h.length - 14],
					h[h.length - 13], h[h.length - 12], h[h.length - 11], h[h.length - 10],
					h[h.length - 9], h[h.length - 8], h[h.length - 7], h[h.length - 6],
					h[h.length - 5], h[h.length - 4], h[h.length - 3], h[h.length - 2],
					h[h.length - 1] });

			// If the ByteBuffer contains 8 random bytes,ends with
			// 0x0D 0x0A 0x0D 0x0A (or two CRLFs), and the response
			// contains Sec-WebSocket-Key1 then the client
			// handshake is complete for Draft 76 Server.
		} else if ((h.length >= 12 && h[h.length - 12] == CR && h[h.length - 11] == LF
				&& h[h.length - 10] == CR && h[h.length - 9] == LF)
				&& new String(this.remoteHandshake.array(), UTF8_CHARSET)
						.contains("Sec-WebSocket-Key1")) {// ************************
			readHandshake(new byte[] { h[h.length - 8], h[h.length - 7], h[h.length - 6],
					h[h.length - 5], h[h.length - 4], h[h.length - 3], h[h.length - 2],
					h[h.length - 1] });

			// Consider Draft 75, and the Flash Security Policy
			// Request edge-case.
		} else if ((h.length >= 4 && h[h.length - 4] == CR && h[h.length - 3] == LF
				&& h[h.length - 2] == CR && h[h.length - 1] == LF)
				&& !(new String(this.remoteHandshake.array(), UTF8_CHARSET).contains("Sec"))
				|| (h.length == 23 && h[h.length - 1] == 0)) {
			readHandshake(null);
		}
	}

	private void readHandshake(byte[] handShakeBody) throws IOException,
			NoSuchAlgorithmException {
		//byte[] handshakeBytes = this.remoteHandshake.array();
		//String handshake = new String(handshakeBytes, UTF8_CHARSET);
		this.handshakeComplete = true;
		
		boolean isConnectionReady = true;
		if (this.webSocket.getDraft() == Protocol.Draft.DRAFT76) {
			// TODO: Draft76 specific stuffs
			// store result in isConnectionReady
		}
		
		if (isConnectionReady) {
			// fire onOpen method
			this.webSocket.onOpen();
		} else {
			close();
		}
	}
	
	private String generateRandomKey() {
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
