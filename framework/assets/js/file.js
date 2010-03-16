


PhoneGap.addConstructor(function() { if (typeof navigator.fileMgr == "undefined") navigator.fileMgr = new FileMgr();});


/**
 * This class provides iPhone read and write access to the mobile device file system.
 * Based loosely on http://www.w3.org/TR/2009/WD-FileAPI-20091117/#dfn-empty
 */
function FileMgr() 
{
	this.fileWriters = {}; // empty maps
	this.fileReaders = {};

	this.docsFolderPath = "../../Documents";
	this.tempFolderPath = "../../tmp";
	this.freeDiskSpace = -1;
	this.getFileBasePaths();
}

// private, called from Native Code
FileMgr.prototype._setPaths = function(docs,temp)
{
	this.docsFolderPath = docs;
	this.tempFolderPath = temp;
}

// private, called from Native Code
FileMgr.prototype._setFreeDiskSpace = function(val)
{
	this.freeDiskSpace = val;
}


// FileWriters add/remove
// called internally by writers
FileMgr.prototype.addFileWriter = function(filePath,fileWriter)
{
	this.fileWriters[filePath] = fileWriter;
}

FileMgr.prototype.removeFileWriter = function(filePath)
{
	this.fileWriters[filePath] = null;
}

// File readers add/remove
// called internally by readers
FileMgr.prototype.addFileReader = function(filePath,fileReader)
{
	this.fileReaders[filePath] = fileReader;
}

FileMgr.prototype.removeFileReader = function(filePath)
{
	this.fileReaders[filePath] = null;
}

/*******************************************
 *
 *	private reader callback delegation
 *	called from native code
 */
FileMgr.prototype.reader_onloadstart = function(filePath,result)
{
	this.fileReaders[filePath].onloadstart(result);
}

FileMgr.prototype.reader_onprogress = function(filePath,result)
{
	this.fileReaders[filePath].onprogress(result);
}

FileMgr.prototype.reader_onload = function(filePath,result)
{
	this.fileReaders[filePath].result = unescape(result);
	this.fileReaders[filePath].onload(this.fileReaders[filePath].result);
}

FileMgr.prototype.reader_onerror = function(filePath,err)
{
	this.fileReaders[filePath].result = err;
	this.fileReaders[filePath].onerror(err);
}

FileMgr.prototype.reader_onloadend = function(filePath,result)
{
	this.fileReaders[filePath].onloadend(result);
}

/*******************************************
 *
 *	private writer callback delegation
 *	called from native code
*/
FileMgr.prototype.writer_onerror = function(filePath,err)
{
	this.fileWriters[filePath].onerror(err);
}

FileMgr.prototype.writer_oncomplete = function(filePath,result)
{
	this.fileWriters[filePath].oncomplete(result); // result contains bytes written
}


FileMgr.prototype.getFileBasePaths = function()
{
	//PhoneGap.exec("File.getFileBasePaths");
}

FileMgr.prototype.testFileExists = function(fileName, successCallback, errorCallback)
{
	var test = FileUtil.testFileExists(fileName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.testDirectoryExists = function(dirName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	var test = FileUtil.testDirectoryExists(dirName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.createDirectory = function(dirName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	var test = FileUtils.createDirectory(dirName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.deleteDirectory = function(dirName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	var test = FileUtils.deleteDirectory(dirName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.deleteFile = function(fileName, successCallback, errorCallback)
{
	this.successCallback = successCallback;
	this.errorCallback = errorCallback;
	FileUtils.deleteFile(fileName);
	test ? successCallback() : errorCallback();
}

FileMgr.prototype.getFreeDiskSpace = function(successCallback, errorCallback)
{
	if(this.freeDiskSpace > 0)
	{
		return this.freeDiskSpace;
	}
	else
	{
		this.successCallback = successCallback;
		this.errorCallback = errorCallback;
		this.freeDiskSpace = FileUtils.getFreeDiskSpace();
  		(this.freeDiskSpace > 0) ? successCallback() : errorCallback();
	}
}


// File Reader


function FileReader()
{
	this.fileName = "";
	this.result = null;
	this.onloadstart = null;
	this.onprogress = null;
	this.onload = null;
	this.onerror = null;
	this.onloadend = null;
}


FileReader.prototype.abort = function()
{
	// Not Implemented
}

FileReader.prototype.readAsText = function(file)
{
	if(this.fileName && this.fileName.length > 0)
	{
		navigator.fileMgr.removeFileReader(this.fileName,this);
	}
	this.fileName = file;
	navigator.fileMgr.addFileReader(this.fileName,this);

  	return FileUtil.read(this.fileName);
}

// File Writer

function FileWriter()
{
	this.fileName = "";
	this.result = null;
	this.readyState = 0; // EMPTY
	this.result = null;
	this.onerror = null;
	this.oncomplete = null;
}

FileWriter.prototype.writeAsText = function(file,text,bAppend)
{
	if(this.fileName && this.fileName.length > 0)
	{
		navigator.fileMgr.removeFileWriter(this.fileName,this);
	}
	this.fileName = file;
	if(bAppend != true)
	{
		bAppend = false; // for null values
	}
	navigator.fileMgr.addFileWriter(file,this);
	this.readyState = 0; // EMPTY
  	var call = FileUtil.write(file, text, bAppend);
	this.result = null;
}
