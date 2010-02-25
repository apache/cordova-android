var Crypto = function()
{
}

Crypto.prototype.encrypt = function(seed, string, callback)
{
	GapCrypto.encrypt(seed, string);
	this.encryptWin = callback;
}

Crypto.prototype.decrypt = function(seed, string, callback)
{
	GapCrypto.decrypt(seed, string);
	this.decryptWin = callback;
}

Crypto.prototype.gotCryptedString = function(string)
{
	this.encryptWin(string);
}

Crypto.prototype.getPlainString = function(string)
{
	this.decryptWin(string);
}

PhoneGap.addConstructor(function() {
  if (typeof navigator.Crypto == "undefined")
  {
    navigator.Crypto = new Crypto();
  }
});

