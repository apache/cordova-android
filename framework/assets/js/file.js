/**
 * This class provides generic read and write access to the mobile device file system.
 */
function File() {
	/**
	 * The data of a file.
	 */
	this.data = "";
	/**
	 * The name of the file.
	 */
	this.name = "";
}

/**
 * Reads a file from the mobile device. This function is asyncronous.
 * @param {String} fileName The name (including the path) to the file on the mobile device. 
 * The file name will likely be device dependent.
 * @param {Function} successCallback The function to call when the file is successfully read.
 * @param {Function} errorCallback The function to call when there is an error reading the file from the device.
 */
File.prototype.read = function(fileName, successCallback, errorCallback) {
	
}

/**
 * Writes a file to the mobile device.
 * @param {File} file The file to write to the device.
 */
File.prototype.write = function(file) {
	
}

PhoneGap.addConstructor(function() {
    if (typeof navigator.file == "undefined") navigator.file = new File();
});

File.prototype.read = function(fileName, successCallback, errorCallback) {
  this.failCallback = errorCallback; 
  this.winCallback = successCallback;

  return FileUtil.read(fileName);
}

File.prototype.hasRead = function(data)
{
  if(data.substr("FAIL"))
    this.failCallback(data);
  else
    this.winCallback(data);
}

/**
 * Writes a file to the mobile device.
 * @param {File} file The file to write to the device.
 */
File.prototype.write = function(file, str, mode, successCallback, failCallback) {
  this.winCallback = successCallback;
  this.failCallback = failCallback;
  var call = FileUtil.write(file, str, mode);
}

File.prototype.testFileExists = function(file, successCallback, failCallback)
{
  var exists = FileUtil.testFileExists(file);
  if(exists)
    successCallback();
  else
    failCallback();
  return exists;
}

File.prototype.testDirectoryExists = function(file, successCallback, failCallback)
{
  var exists = FileUtil.testDirectoryExists(file);
  if(exists)
    successCallback();
  else
    failCallback();
  return exists;
}

File.prototype.createDirectory = function(dir, successCallback, failCallback)
{
  var good = FileUtils.createDirectory(dir);
  good ? successCallback() : failCallback();
}

File.prototype.deleteDirectory = function(dir, successCallback, failCallback)
{
  var good = FileUtils.deleteDirectory(dir);
  good ? successCallback() : failCallback();
}

File.prototype.deleteFile = function(dir, successCallback, failCallback)
{
  var good = FileUtils.deleteFile(dir);
  good ? successCallback() : failCallback();
}

File.prototype.getFreeDiskSpace = function(successCallback, failCallback)
{
  var diskSpace =  FileUtils.getFreeDiskSpace();
  if(diskSpace > 0)
    successCallback();
  else
    failCallback();
  return diskSpace;
}
