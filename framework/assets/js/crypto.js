
var Crypto = function() {
};

Crypto.prototype.encrypt = function(seed, string, callback) {
    this.encryptWin = callback;
    PhoneGap.execAsync(null, null, "Crypto", "encrypt", [seed, string]);
};

Crypto.prototype.decrypt = function(seed, string, callback) {
    this.decryptWin = callback;
    PhoneGap.execAsync(null, null, "Crypto", "decrypt", [seed, string]);
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

