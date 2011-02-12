/*
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
(function() {
	
	// window object
	var global = window;
	
	// WebSocket Object. All listener methods are cleaned up!
	var WebSocket = global.WebSocket = function(url) {
		// must be overloaded
		this.onopen = null;
		this.onmessage = null;
		this.onclose = null;
		this.onerror = null;
		
		// get a new websocket object from factory (check com.strumsoft.websocket.WebSocketFactory.java)
		this.socket = WebSocketFactory.getInstance(url);
		// store in registry
		WebSocket.store[this.socket.getId()] = this;
	};
	
	// storage to hold websocket object for later invokation of event methods
	WebSocket.store = {};
	
	// static event methods to call event methods on target websocket objects
	WebSocket.onmessage = function (evt) {
		WebSocket.store[evt._target]['onmessage'].call(global, evt._data);
	}	
	
	WebSocket.onopen = function (evt) {
		WebSocket.store[evt._target]['onopen'].call(global, evt._data);
	}
	
	WebSocket.onclose = function (evt) {
		WebSocket.store[evt._target]['onclose'].call(global, evt._data);
	}
	
	WebSocket.onerror = function (evt) {
		WebSocket.store[evt._target]['onerror'].call(global, evt._data);
	}

	WebSocket.prototype.send = function(data) {
		this.socket.send(data);
	}

	WebSocket.prototype.close = function() {
		this.socket.close();
	}
})();