com.phonegap.CryptoHandlerProxy = function() {
    this.className = "com.phonegap.CryptoHandler";
};
com.phonegap.CryptoHandlerProxy.prototype.encrypt = function(pass, text) {
    return PhoneGap.exec(this.className, "encrypt", [pass, text]);
};
com.phonegap.CryptoHandlerProxy.prototype.decrypt = function(pass, text) {
    return PhoneGap.exec(this.className, "decrypt", [pass, text]);
};
com.phonegap.CryptoHandler = new com.phonegap.CryptoHandlerProxy();

var Crypto = function() {
};

Crypto.prototype.encrypt = function(seed, string, callback) {
    com.phonegap.CryptoHandler.encrypt(seed, string);
    this.encryptWin = callback;
};

Crypto.prototype.decrypt = function(seed, string, callback) {
    com.phonegap.CryptoHandler.decrypt(seed, string);
    this.decryptWin = callback;
};

Crypto.prototype.gotCryptedString = function(string) {
    this.encryptWin(string);
};

Crypto.prototype.getPlainString = function(string) {
    this.decryptWin(string);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.Crypto == "undefined") navigator.Crypto = new Crypto();
});

