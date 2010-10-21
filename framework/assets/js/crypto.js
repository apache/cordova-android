/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

// TODO: Needs to be commented

var Crypto = function() {
};

Crypto.prototype.encrypt = function(seed, string, callback) {
    this.encryptWin = callback;
    PhoneGap.exec(null, null, "Crypto", "encrypt", [seed, string]);
};

Crypto.prototype.decrypt = function(seed, string, callback) {
    this.decryptWin = callback;
    PhoneGap.exec(null, null, "Crypto", "decrypt", [seed, string]);
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

