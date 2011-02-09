/*
 *	In DroidGap class, attach WebSocketFactoy like this,
 *  appView.addJavascriptInterface(new WebSocketFactory(appView), "WebSocketFactory");
 *  
 *  Now, in your html file, 
 *  1. Include websocket.js 
 *  	<script type="text/javascript" charset="utf-8" src="js/websocket.js"></script>
 *  2. Create WebSocket object, and override event methods, 
 *  
 *	// new socket
 *	var socket = new WebSocket('ws://122.168.196.27:8082/');
 *			
 *	// push a message after the connection is established.
 *	socket.onopen = function() {
 *		socket.send('--message--')
 *	};
 *			
 *	// alerts message pushed from server
 *	socket.onmessage = function(msg) {
 *		alert(JSON.stringify(msg));
 *	};
 *			
 *	// alert close event
 *	socket.onclose = function() {
 *		alert('closed');
 *	};
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
	
	WebSocket.prototype.send = function(data) {
		this.socket.send(data);
	}

	WebSocket.prototype.close = function() {
		this.socket.close();
	}
})();