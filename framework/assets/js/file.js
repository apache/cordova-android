/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

/**
 * This class provides generic read and write access to the mobile device file system.
 * They are not used to read files from a server.
 */

/**
 * This class provides some useful information about a file.
 * This is the fields returned when navigator.fileMgr.getFileProperties() 
 * is called.
 */
function FileProperties(filePath) {
    this.filePath = filePath;
    this.size = 0;
    this.lastModifiedDate = null;
};

/**
 * Create an event object since we can't set target on DOM event.
 *
 * @param type
 * @param target
 *
 */
File._createEvent = function(type, target) {
    // Can't create event object, since we can't set target (its readonly)
    //var evt = document.createEvent('Events');
    //evt.initEvent("onload", false, false);
    var evt = {"type": type};
    evt.target = target;
    return evt;
};

function FileError() {
   this.code = null;
};

// File error codes
// Found in DOMException
FileError.NOT_FOUND_ERR = 1;
FileError.SECURITY_ERR = 2;
FileError.ABORT_ERR = 3;

// Added by this specification
FileError.NOT_READABLE_ERR = 4;
FileError.ENCODING_ERR = 5;
FileError.NO_MODIFICATION_ALLOWED_ERR = 6;
FileError.INVALID_STATE_ERR = 7;
FileError.SYNTAX_ERR = 8;

//-----------------------------------------------------------------------------
// File manager
//-----------------------------------------------------------------------------

function FileMgr() {
};

FileMgr.prototype.getFileProperties = function(filePath) {
    return PhoneGap.exec(null, null, "File", "getFile", [filePath]);
};

FileMgr.prototype.getFileBasePaths = function() {
};

FileMgr.prototype.getRootPaths = function() {
    return PhoneGap.exec(null, null, "File", "getRootPaths", []);
};

FileMgr.prototype.testSaveLocationExists = function(successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "testSaveLocationExists", []);
};

FileMgr.prototype.testFileExists = function(fileName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "testFileExists", [fileName]);
};

FileMgr.prototype.testDirectoryExists = function(dirName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "testDirectoryExists", [dirName]);
};

FileMgr.prototype.createDirectory = function(dirName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "createDirectory", [dirName]);
};

FileMgr.prototype.deleteDirectory = function(dirName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "deleteDirectory", [dirName]);
};

FileMgr.prototype.deleteFile = function(fileName, successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "deleteFile", [fileName]);
};

FileMgr.prototype.getFreeDiskSpace = function(successCallback, errorCallback) {
    return PhoneGap.exec(successCallback, errorCallback, "File", "getFreeDiskSpace", []);
};

FileMgr.prototype.writeAsText = function(fileName, data, append, successCallback, errorCallback) {
    PhoneGap.exec(successCallback, errorCallback, "File", "writeAsText", [fileName, data, append]);
};

FileMgr.prototype.write = function(fileName, data, position, successCallback, errorCallback) {
    PhoneGap.exec(successCallback, errorCallback, "File", "write", [fileName, data, position]);
};

FileMgr.prototype.truncate = function(fileName, size, successCallback, errorCallback) {
    PhoneGap.exec(successCallback, errorCallback, "File", "truncate", [fileName, size]);
};

FileMgr.prototype.readAsText = function(fileName, encoding, successCallback, errorCallback) {
    PhoneGap.exec(successCallback, errorCallback, "File", "readAsText", [fileName, encoding]);
};

FileMgr.prototype.readAsDataURL = function(fileName, successCallback, errorCallback) {
    PhoneGap.exec(successCallback, errorCallback, "File", "readAsDataURL", [fileName]);
};

PhoneGap.addConstructor(function() {
    if (typeof navigator.fileMgr == "undefined") navigator.fileMgr = new FileMgr();
});

//-----------------------------------------------------------------------------
// File Reader
//-----------------------------------------------------------------------------
// TODO: All other FileMgr function operate on the SD card as root.  However,
//       for FileReader & FileWriter the root is not SD card.  Should this be changed?

/**
 * This class reads the mobile device file system.
 *
 * For Android:
 *      The root directory is the root of the file system.
 *      To read from the SD card, the file name is "sdcard/my_file.txt"
 */
function FileReader() {
    this.fileName = "";

    this.readyState = 0;

    // File data
    this.result = null;

    // Error
    this.error = null;

    // Event handlers
    this.onloadstart = null;    // When the read starts.
    this.onprogress = null;     // While reading (and decoding) file or fileBlob data, and reporting partial file data (progess.loaded/progress.total)
    this.onload = null;         // When the read has successfully completed.
    this.onerror = null;        // When the read has failed (see errors).
    this.onloadend = null;      // When the request has completed (either in success or failure).
    this.onabort = null;        // When the read has been aborted. For instance, by invoking the abort() method.
};

// States
FileReader.EMPTY = 0;
FileReader.LOADING = 1;
FileReader.DONE = 2;

/**
 * Abort reading file.
 */
FileReader.prototype.abort = function() {
    this.readyState = FileReader.DONE;
    this.result = null;

    // set error
    var error = new FileError();
    error.code = error.ABORT_ERR;
    this.error = error;
   
    // If error callback
    if (typeof this.onerror == "function") {
        var evt = File._createEvent("error", this);
        this.onerror(evt);
    }
    // If abort callback
    if (typeof this.onabort == "function") {
        var evt = File._createEvent("abort", this);
        this.onabort(evt);
    }
    // If load end callback
    if (typeof this.onloadend == "function") {
        var evt = File._createEvent("loadend", this);
        this.onloadend(evt);
    }
};

/**
 * Read text file.
 *
 * @param file          The name of the file
 * @param encoding      [Optional] (see http://www.iana.org/assignments/character-sets)
 */
FileReader.prototype.readAsText = function(file, encoding) {
    this.fileName = file;

    // LOADING state
    this.readyState = FileReader.LOADING;

    // If loadstart callback
    if (typeof this.onloadstart == "function") {
        var evt = File._createEvent("loadstart", this);
        this.onloadstart(evt);
    }

    // Default encoding is UTF-8
    var enc = encoding ? encoding : "UTF-8";

    var me = this;

    // Read file
    navigator.fileMgr.readAsText(file, enc,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // If onload callback
            if (typeof me.onload == "function") {
                var evt = File._createEvent("load", me);
                me.onload(evt);
            }

            // DONE state
            me.readyState = FileReader.DONE;

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // DONE state
            me.readyState = FileReader.DONE;

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        }
        );
};


/**
 * Read file and return data as a base64 encoded data url.
 * A data url is of the form:
 *      data:[<mediatype>][;base64],<data>
 *
 * @param file          The name of the file
 */
FileReader.prototype.readAsDataURL = function(file) {
    this.fileName = file;

    // LOADING state
    this.readyState = FileReader.LOADING;

    // If loadstart callback
    if (typeof this.onloadstart == "function") {
        var evt = File._createEvent("loadstart", this);
        this.onloadstart(evt);
    }

    var me = this;

    // Read file
    navigator.fileMgr.readAsDataURL(file,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // If onload callback
            if (typeof me.onload == "function") {
                var evt = File._createEvent("load", me);
                me.onload(evt);
            }

            // DONE state
            me.readyState = FileReader.DONE;

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileReader.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // DONE state
            me.readyState = FileReader.DONE;

            // If onloadend callback
            if (typeof me.onloadend == "function") {
                var evt = File._createEvent("loadend", me);
                me.onloadend(evt);
            }
        }
        );
};

/**
 * Read file and return data as a binary data.
 *
 * @param file          The name of the file
 */
FileReader.prototype.readAsBinaryString = function(file) {
    // TODO - Can't return binary data to browser.
    this.fileName = file;
};

/**
 * Read file and return data as a binary data.
 *
 * @param file          The name of the file
 */
FileReader.prototype.readAsArrayBuffer = function(file) {
    // TODO - Can't return binary data to browser.
    this.fileName = file;
};

//-----------------------------------------------------------------------------
// File Writer
//-----------------------------------------------------------------------------

/**
 * This class writes to the mobile device file system.
 *
 * For Android:
 *      The root directory is the root of the file system.
 *      To write to the SD card, the file name is "sdcard/my_file.txt"
 *      
 * @param filePath the file to write to
 * @param append if true write to the end of the file, otherwise overwrite the file
 */
function FileWriter(filePath, append) {
    this.fileName = "";
    this.length = 0;
	if (filePath) {
		var f = navigator.fileMgr.getFileProperties(filePath);
	    this.fileName = f.name;
	    this.length = f.size;
	}
    // default is to write at the beginning of the file
    this.position = (append !== true) ? 0 : this.length;

    this.readyState = 0; // EMPTY

    this.result = null;

    // Error
    this.error = null;

    // Event handlers
    this.onwritestart = null;	// When writing starts
    this.onprogress = null;		// While writing the file, and reporting partial file data
    this.onwrite = null;		// When the write has successfully completed.
    this.onwriteend = null;		// When the request has completed (either in success or failure).
    this.onabort = null;		// When the write has been aborted. For instance, by invoking the abort() method.
    this.onerror = null;		// When the write has failed (see errors).
};

// States
FileWriter.INIT = 0;
FileWriter.WRITING = 1;
FileWriter.DONE = 2;

/**
 * Abort writing file.
 */
FileWriter.prototype.abort = function() {
    // set error
    var error = new FileError();
    error.code = error.ABORT_ERR;
    this.error = error;
    
    // If error callback
    if (typeof this.onerror == "function") {
        var evt = File._createEvent("error", this);
        this.onerror(evt);
    }
    // If abort callback
    if (typeof this.onabort == "function") {
        var evt = File._createEvent("abort", this);
        this.onabort(evt);
    }
    
    this.readyState = FileWriter.DONE;

    // If load end callback
    if (typeof this.onloadend == "function") {
        var evt = File._createEvent("writeend", this);
        this.onloadend(evt);
    }
};

/**
 * @Deprecated: use write instead
 * 
 * @param file to write the data to
 * @param text to be written
 * @param bAppend if true write to end of file, otherwise overwrite the file
 */
FileWriter.prototype.writeAsText = function(file, text, bAppend) {
	// Throw an exception if we are already writing a file
	if (this.readyState == FileWriter.WRITING) {
		throw FileError.INVALID_STATE_ERR;
	}

	if (bAppend != true) {
        bAppend = false; // for null values
    }

    this.fileName = file;

    // WRITING state
    this.readyState = FileWriter.WRITING;

    var me = this;

    // If onwritestart callback
    if (typeof me.onwritestart == "function") {
        var evt = File._createEvent("writestart", me);
        me.onwritestart(evt);
    }

    // Write file
    navigator.fileMgr.writeAsText(file, text, bAppend,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // If onwrite callback
            if (typeof me.onwrite == "function") {
                var evt = File._createEvent("write", me);
                me.onwrite(evt);
            }

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onwriteend callback
            if (typeof me.onwriteend == "function") {
                var evt = File._createEvent("writeend", me);
                me.onwriteend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onwriteend callback
            if (typeof me.onwriteend == "function") {
                var evt = File._createEvent("writeend", me);
                me.onwriteend(evt);
            }
        }
        );

};

/**
 * Writes data to the file
 *  
 * @param text to be written
 */
FileWriter.prototype.write = function(text) {
	// Throw an exception if we are already writing a file
	if (this.readyState == FileWriter.WRITING) {
		throw FileError.INVALID_STATE_ERR;
	}

    // WRITING state
    this.readyState = FileWriter.WRITING;

    var me = this;

    // If onwritestart callback
    if (typeof me.onwritestart == "function") {
        var evt = File._createEvent("writestart", me);
        me.onwritestart(evt);
    }

    // Write file
    navigator.fileMgr.write(this.fileName, text, this.position,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // So if the user wants to keep appending to the file
            me.length = Math.max(me.length, me.position + r);
            // position always increases by bytes written because file would be extended
            me.position += r;

            // If onwrite callback
            if (typeof me.onwrite == "function") {
                var evt = File._createEvent("write", me);
                me.onwrite(evt);
            }

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onwriteend callback
            if (typeof me.onwriteend == "function") {
                var evt = File._createEvent("writeend", me);
                me.onwriteend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onwriteend callback
            if (typeof me.onwriteend == "function") {
                var evt = File._createEvent("writeend", me);
                me.onwriteend(evt);
            }
        }
        );

};

/** 
 * Moves the file pointer to the location specified.
 * 
 * If the offset is a negative number the position of the file 
 * pointer is rewound.  If the offset is greater than the file 
 * size the position is set to the end of the file.  
 * 
 * @param offset is the location to move the file pointer to.
 */
FileWriter.prototype.seek = function(offset) {
    // Throw an exception if we are already writing a file
    if (this.readyState === FileWriter.WRITING) {
        throw FileError.INVALID_STATE_ERR;
    }

    if (!offset) {
        return;
    }
    
    // See back from end of file.
    if (offset < 0) {
		this.position = Math.max(offset + this.length, 0);
	}
    // Offset is bigger then file size so set position 
    // to the end of the file.
	else if (offset > this.length) {
		this.position = this.length;
	}
    // Offset is between 0 and file size so set the position
    // to start writing.
	else {
		this.position = offset;
	}	
};

/** 
 * Truncates the file to the size specified.
 * 
 * @param size to chop the file at.
 */
FileWriter.prototype.truncate = function(size) {
	// Throw an exception if we are already writing a file
	if (this.readyState == FileWriter.WRITING) {
		throw FileError.INVALID_STATE_ERR;
	}

    // WRITING state
    this.readyState = FileWriter.WRITING;

    var me = this;

    // If onwritestart callback
    if (typeof me.onwritestart == "function") {
        var evt = File._createEvent("writestart", me);
        me.onwritestart(evt);
    }

    // Write file
    navigator.fileMgr.truncate(this.fileName, size,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Update the length of the file
            me.length = r;
            me.position = Math.min(me.position, r);;

            // If onwrite callback
            if (typeof me.onwrite == "function") {
                var evt = File._createEvent("write", me);
                me.onwrite(evt);
            }

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onwriteend callback
            if (typeof me.onwriteend == "function") {
                var evt = File._createEvent("writeend", me);
                me.onwriteend(evt);
            }
        },

        // Error callback
        function(e) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save error
            me.error = e;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onwriteend callback
            if (typeof me.onwriteend == "function") {
                var evt = File._createEvent("writeend", me);
                me.onwriteend(evt);
            }
        }
        );
};

