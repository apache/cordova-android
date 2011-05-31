/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.LinkedList;

import android.util.Log;

/**
 * This class provides a way for Java to run JavaScript in the web page that has loaded PhoneGap.
 * The CallbackServer class implements an XHR server and a polling server with a list of JavaScript
 * statements that are to be executed on the web page.
 * 
 * The process flow for XHR is:
 * 1. JavaScript makes an async XHR call. 
 * 2. The server holds the connection open until data is available. 
 * 3. The server writes the data to the client and closes the connection. 
 * 4. The server immediately starts listening for the next XHR call. 
 * 5. The client receives this XHR response, processes it.
 * 6. The client sends a new async XHR request.
 *
 * The CallbackServer class requires the following permission in Android manifest file
 * 		<uses-permission android:name="android.permission.INTERNET" />
 * 
 * If the device has a proxy set, then XHR cannot be used, so polling must be used instead.
 * This can be determined by the client by calling CallbackServer.usePolling().  
 * 
 * The process flow for polling is:
 * 1. The client calls CallbackServer.getJavascript() to retrieve next statement.
 * 2. If statement available, then client processes it.
 * 3. The client repeats #1 in loop. 
 */
public class CallbackServer implements Runnable {
	
	private static final String LOG_TAG = "CallbackServer";

	/**
	 * The list of JavaScript statements to be sent to JavaScript.
	 */
	private LinkedList<String> javascript;
	
	/**
	 * The port to listen on.
	 */
	private int port;
	
	/**
	 * The server thread.
	 */
	private Thread serverThread;
	
	/**
	 * Indicates the server is running.
	 */
	private boolean active;
	
	/**
	 * Indicates that the JavaScript statements list is empty
	 */
	private boolean empty;
	
	/**
	 * Indicates that polling should be used instead of XHR.
	 */
	private boolean usePolling = true;
	
	/**
	 * Security token to prevent other apps from accessing this callback server via XHR
	 */
	private String token;
	
	/**
	 * Constructor.
	 */
	public CallbackServer() {
		//System.out.println("CallbackServer()");
		this.active = false;
		this.empty = true;
		this.port = 0;
		this.javascript = new LinkedList<String>();		
	}
	
	/**
	 * Init callback server and start XHR if running local app.
	 * 
	 * If PhoneGap app is loaded from file://, then we can use XHR
	 * otherwise we have to use polling due to cross-domain security restrictions.
	 * 
	 * @param url			The URL of the PhoneGap app being loaded
	 */
	public void init(String url) {
		//System.out.println("CallbackServer.start("+url+")");

		// Determine if XHR or polling is to be used
		if ((url != null) && !url.startsWith("file://")) {
			this.usePolling = true;
			this.stopServer();
		}
		else if (android.net.Proxy.getDefaultHost() != null) {
			this.usePolling = true;
			this.stopServer();
		}
		else {
			this.usePolling = false;
			this.startServer();
		}
	}
	
	/**
	 * Return if polling is being used instead of XHR.
	 * 
	 * @return
	 */
	public boolean usePolling() {
		return this.usePolling;
	}
	
	/**
	 * Get the port that this server is running on.
	 * 
	 * @return
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Get the security token that this server requires when calling getJavascript().
	 * 
	 * @return
	 */
	public String getToken() {
		return this.token;
	}
	
	/**
	 * Start the server on a new thread.
	 */
	public void startServer() {
		//System.out.println("CallbackServer.startServer()");
		this.active = false;
		
		// Start server on new thread
		this.serverThread = new Thread(this);
		this.serverThread.start();
	}

	/**
	 * Restart the server on a new thread.
	 */
	public void restartServer() {
		
		// Stop server
		this.stopServer();
				
		// Start server again
		this.startServer();
	}

	/**
	 * Start running the server.  
	 * This is called automatically when the server thread is started.
	 */
	public void run() {
		
		// Start server
		try {
			this.active = true;
			String request;
			ServerSocket waitSocket = new ServerSocket(0);
			this.port = waitSocket.getLocalPort();
			//System.out.println("CallbackServer -- using port " +this.port);
			this.token = java.util.UUID.randomUUID().toString();
			//System.out.println("CallbackServer -- using token "+this.token);

			 while (this.active) {
				 //System.out.println("CallbackServer: Waiting for data on socket");
				 Socket connection = waitSocket.accept();
				 BufferedReader xhrReader = new BufferedReader(new InputStreamReader(connection.getInputStream()),40);
				 DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				 request = xhrReader.readLine();
				 String response = "";
				 //System.out.println("CallbackServerRequest="+request);
				 if (this.active && (request != null)) {
					 if (request.contains("GET")) {
						 
						 // Get requested file
						 String[] requestParts = request.split(" "); 
						 
						 // Must have security token
						 if ((requestParts.length == 3) && (requestParts[1].substring(1).equals(this.token))) {
							 //System.out.println("CallbackServer -- Processing GET request");

							 // Wait until there is some data to send, or send empty data every 10 sec 
							 // to prevent XHR timeout on the client 
							 synchronized (this) { 
								 while (this.empty) { 
									 try { 
										 this.wait(10000); // prevent timeout from happening
										 //System.out.println("CallbackServer>>> break <<<");
										 break;
									 } 
									 catch (Exception e) { }
								 } 
							 }

							 // If server is still running
							 if (this.active) {

								 // If no data, then send 404 back to client before it times out
								 if (this.empty) {
									 //System.out.println("CallbackServer -- sending data 0");
									 response = "HTTP/1.1 404 NO DATA\r\n\r\n "; // need to send content otherwise some Android devices fail, so send space
								 }
								 else {
									 //System.out.println("CallbackServer -- sending item");
									 response = "HTTP/1.1 200 OK\r\n\r\n";
									 String js = this.getJavascript();
									 if (js != null) {
										 response += encode(js);
									 }
								 }
							 }
							 else {
								 response = "HTTP/1.1 503 Service Unavailable\r\n\r\n ";							 
							 }
						 }
						 else {
							 response = "HTTP/1.1 403 Forbidden\r\n\r\n ";						 
						 }
					 }
					 else {
						 response = "HTTP/1.1 400 Bad Request\r\n\r\n ";
					 }
					 //System.out.println("CallbackServer: response="+response);
					 //System.out.println("CallbackServer: closing output");
					 output.writeBytes(response);
					 output.flush();
				 }
				 output.close();
				 xhrReader.close();
			 }
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 this.active = false;
		 //System.out.println("CallbackServer.startServer() - EXIT");
	}
		
	/**
	 * Stop server.  
	 * This stops the thread that the server is running on.
	 */
	public void stopServer() {
		//System.out.println("CallbackServer.stopServer()");
		if (this.active) {
			this.active = false;

			// Break out of server wait
			synchronized (this) { 
				this.notify();
			}
		}		
	}

    /**
     * Destroy
     */
    public void destroy() {
    	this.stopServer();
    }

	/**
	 * Get the number of JavaScript statements.
	 * 
	 * @return int
	 */
	public int getSize() {
		int size = this.javascript.size();
		//System.out.println("getSize() = " + size);
		return size;
	}
	
	/**
	 * Get the next JavaScript statement and remove from list.
	 *  
	 * @return String
	 */
	public String getJavascript() {
		if (this.javascript.size() == 0) {
			return null;
		}
		String statement = this.javascript.remove(0);
		//System.out.println("CallbackServer.getJavascript() = " + statement);
		if (this.javascript.size() == 0) {
			synchronized (this) { 
				this.empty = true;
			}
		}
		return statement;
	}
	
	/**
	 * Add a JavaScript statement to the list.
	 * 
	 * @param statement
	 */
	public void sendJavascript(String statement) {
		//System.out.println("CallbackServer.sendJavascript("+statement+")");
		this.javascript.add(statement);
		synchronized (this) { 
			this.empty = false;
			this.notify();
		}
	}
	
	/**
	 * This will encode the return value to JavaScript.  We revert the encoding for 
	 * common characters that don't require encoding to reduce the size of the string 
	 * being passed to JavaScript.
	 * 
	 * @param value to be encoded
	 * @return encoded string
	 */
	public static String encode(String value) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(value, "UTF-8")
				.replaceAll("\\+", " ")
				.replaceAll("\\%21", "!")
				.replaceAll("\\%22", "\"")
				.replaceAll("\\%27", "'")
				.replaceAll("\\%28", "(")
				.replaceAll("\\%29", ")")
				.replaceAll("\\%2C", ",")
				.replaceAll("\\%3C", "<")
				.replaceAll("\\%3D", "=")
				.replaceAll("\\%3E", ">")
				.replaceAll("\\%3F", "?")
				.replaceAll("\\%40", "@")
				.replaceAll("\\%5B", "[")
				.replaceAll("\\%5D", "]")
				.replaceAll("\\%7B", "{")
				.replaceAll("\\%7D", "}")
				.replaceAll("\\%3A", ":")
				.replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return encoded;
	}
	
}
