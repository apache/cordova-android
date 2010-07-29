PhoneGap.addPlugin = function(name, obj) {
	if ( !window.plugins ) {
		window.plugins = {};
	}

	if ( !window.plugins[name] ) {
		window.plugins[name] = obj;
	}
}

function Cache() {
}

Cache.prototype.getCachedPathForURI = function(uri, success, fail) {
	PhoneGap.execAsync(success, fail, 'com.phonegap.api.impl.Cache', 'getCachedPathForURI', [uri]);
};

PhoneGap.addPlugin('cache', new Cache());