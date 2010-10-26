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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

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
	private boolean usePolling;
	
	/**
	 * Constructor.
	 */
	public CallbackServer() {
		//System.out.println("CallbackServer()");
		this.active = false;
		this.empty = true;
		this.port = 0;
		this.javascript = new LinkedList<String>();
		
		if (android.net.Proxy.getDefaultHost() != null) {
			this.usePolling = true;
		}
		else {
			this.usePolling = false;
			this.startServer();
		}
	}
	
	/**
	 * Determine if polling should be used instead of XHR.
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
			//System.out.println(" -- using port " +this.port);

			 while (this.active) {
				 //System.out.println("CallbackServer: Waiting for data on socket");
				 Socket connection = waitSocket.accept();
				 BufferedReader xhrReader = new BufferedReader(new InputStreamReader(connection.getInputStream()),40);
				 DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				 request = xhrReader.readLine();
				 //System.out.println("Request="+request);
				 if(request.contains("GET"))
				 {
					 //System.out.println(" -- Processing GET request");
					 
					 // Wait until there is some data to send, or send empty data every 30 sec 
					 // to prevent XHR timeout on the client 
					 synchronized (this) { 
						 while (this.empty) { 
							 try { 
								 this.wait(30000); // prevent timeout from happening
								 //System.out.println(">>> break <<<");
								 break;
							 } 
							 catch (Exception e) { }
						 } 
					 }
					 
					 // If server is still running
					 if (this.active) {
					
						 // If no data, then send 404 back to client before it times out
						 if (this.empty) {
							 //System.out.println(" -- sending data 0");
							 output.writeBytes("HTTP/1.1 404 NO DATA\r\n\r\n");
						 }
						 else {
							 //System.out.println(" -- sending item");
							 output.writeBytes("HTTP/1.1 200 OK\r\n\r\n"+this.getJavascript());
						 }
					 }					 
				 }
				 //System.out.println("CallbackServer: closing output");
				 output.close();				 
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
		this.active = false;

		// Break out of server wait
		synchronized (this) { 
			this.notify();
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
	
}
