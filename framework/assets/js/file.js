/**
 * This class provides generic read and write access to the mobile device file system.
 * They are not used to read files from a server.
 */

/**
 * List of files
 */
function FileList() {
    this.files = {};
};

/**
 * Describes a single file in a FileList
 */
function File() {
    this.name = null;
    this.type = null;
    this.urn = null;
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
   // File error codes
   // Found in DOMException
   this.NOT_FOUND_ERR = 8;
   this.SECURITY_ERR = 18;
   this.ABORT_ERR = 20;

   // Added by this specification
   this.NOT_READABLE_ERR = 24;
   this.ENCODING_ERR = 26;

   this.code = null;
};

//-----------------------------------------------------------------------------
// File manager
//-----------------------------------------------------------------------------

function FileMgr() {
};

FileMgr.prototype.getFileBasePaths = function() {
};

FileMgr.prototype.testSaveLocationExists = function(successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "testSaveLocationExists", []);
};

FileMgr.prototype.testFileExists = function(fileName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "testFileExists", [fileName]);
};

FileMgr.prototype.testDirectoryExists = function(dirName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "testDirectoryExists", [dirName]);
};

FileMgr.prototype.createDirectory = function(dirName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "createDirectory", [dirName]);
};

FileMgr.prototype.deleteDirectory = function(dirName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "deleteDirectory", [dirName]);
};

FileMgr.prototype.deleteFile = function(fileName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "deleteFile", [fileName]);
};

FileMgr.prototype.getFreeDiskSpace = function(successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "getFreeDiskSpace", []);
};

FileMgr.prototype.writeAsText = function(fileName, data, append, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "writeAsText", [fileName, data, append]);
};

FileMgr.prototype.readAsText = function(fileName, encoding, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "readAsText", [fileName, encoding]);
};

FileMgr.prototype.readAsDataURL = function(fileName, successCallback, errorCallback) {
    PhoneGap.execAsync(successCallback, errorCallback, "File", "readAsDataURL", [fileName]);
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

    // If abort callback
    if (typeof this.onabort == "function") {
        var evt = File._createEvent("abort", this);
        this.onabort(evt);
    }

    // TODO: Anything else to do?  Maybe sent to native?
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

            // DONE state
            me.readyState = FileReader.DONE;

            // If onload callback
            if (typeof me.onload == "function") {
                var evt = File._createEvent("load", me);
                me.onload(evt);
            }

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

            // DONE state
            me.readyState = FileReader.DONE;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

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

            // DONE state
            me.readyState = FileReader.DONE;

            // If onload callback
            if (typeof me.onload == "function") {
                var evt = File._createEvent("load", me);
                me.onload(evt);
            }

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

            // DONE state
            me.readyState = FileReader.DONE;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }

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

//-----------------------------------------------------------------------------
// File Writer
//-----------------------------------------------------------------------------

/**
 * This class writes to the mobile device file system.
 *
 * For Android:
 *      The root directory is the root of the file system.
 *      To write to the SD card, the file name is "sdcard/my_file.txt"
 */
function FileWriter() {
    this.fileName = "";
    this.result = null;
    this.readyState = 0; // EMPTY
    this.result = null;
    this.onerror = null;
    this.oncomplete = null;
};

// States
FileWriter.EMPTY = 0;
FileWriter.LOADING = 1;
FileWriter.DONE = 2;

FileWriter.prototype.writeAsText = function(file, text, bAppend) {
    if (bAppend != true) {
        bAppend = false; // for null values
    }

    this.fileName = file;

    // LOADING state
    this.readyState = FileWriter.LOADING;

    var me = this;

    // Read file
    navigator.fileMgr.writeAsText(file, text, bAppend,

        // Success callback
        function(r) {

            // If DONE (cancelled), then don't do anything
            if (me.readyState == FileWriter.DONE) {
                return;
            }

            // Save result
            me.result = r;

            // DONE state
            me.readyState = FileWriter.DONE;

            // If oncomplete callback
            if (typeof me.oncomplete == "function") {
                var evt = File._createEvent("complete", me);
                me.oncomplete(evt);
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

            // DONE state
            me.readyState = FileWriter.DONE;

            // If onerror callback
            if (typeof me.onerror == "function") {
                var evt = File._createEvent("error", me);
                me.onerror(evt);
            }
        }
        );

};

