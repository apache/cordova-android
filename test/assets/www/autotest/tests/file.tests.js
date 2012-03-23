/**
 * Retrieves root file system entries once, so they don't have to be 
 * repeated for every test (file system shouldn't change during test run). 
 */ 
var getFileSystemRoot = (function() {

    // private
    var temp_root, persistent_root;

    var onError = function(error) {
        console.log('unable to retrieve file system: ' + error.code);
    };
    
    // one-time retrieval of the root file system entry
    var init = function() {
        window.requestFileSystem(LocalFileSystem.PERSISTENT, 0,
                function(fileSystem) {
                    persistent_root = fileSystem.root;
                }, onError);
        window.requestFileSystem(LocalFileSystem.TEMPORARY, 0,
                function(fileSystem) {
                    temp_root = fileSystem.root;
                }, onError);
    };
    document.addEventListener("deviceready", init, true); 

    // public function returns private root entry
    return function() {
        // When testing, it is a good idea to run the test suite once for each
        // file system type.  Just change the return value from this function.  
        //return temp_root;
        return persistent_root;
    };
}()); // execute immediately

Tests.prototype.FileTests = function() {
    module('FileError interface');
    test("FileError constants should be defined", function() {
        expect(12);
        equal(FileError.NOT_FOUND_ERR, 1, "FileError.NOT_FOUND_ERR should be defined");
        equal(FileError.SECURITY_ERR, 2, "FileError.SECURITY_ERR should be defined");
        equal(FileError.ABORT_ERR, 3, "FileError.ABORT should be defined");
        equal(FileError.NOT_READABLE_ERR, 4, "FileError.NOT_READABLE_ERR should be defined");
        equal(FileError.ENCODING_ERR, 5, "FileError.ENCODING_ERR should be defined");
        equal(FileError.NO_MODIFICATION_ALLOWED_ERR, 6, "FileError.NO_MODIFICATION_ALLOWED_ERR should be defined");
        equal(FileError.INVALID_STATE_ERR, 7, "FileError.INVALID_STATE_ERR should be defined");
        equal(FileError.SYNTAX_ERR, 8, "FileError.SYNTAX_ERR should be defined");
        equal(FileError.INVALID_MODIFICATION_ERR, 9, "FileError.INVALID_MODIFICATION_ERR should be defined");
        equal(FileError.QUOTA_EXCEEDED_ERR, 10, "FileError.QUOTA_EXCEEDED_ERR should be defined");
        equal(FileError.TYPE_MISMATCH_ERR, 11, "FileError.TYPE_MISMATCH_ERR should be defined");
        equal(FileError.PATH_EXISTS_ERR, 12, "FileError.PATH_EXISTS_ERR should be defined");
    });

    module('LocalFileSystem interface');
    test("window.requestFileSystem function should be defined", function() {
        expect(1);
        ok(typeof window.requestFileSystem === 'function', "window.requestFileSystem should be a function.");
    });
    test("window.resolveLocalFileSystemURI function should be defined", function() {
        expect(1);
        ok(typeof window.resolveLocalFileSystemURI === 'function', "window.resolveLocalFileSystemURI should be a function.");
    });
    test("File system types should be defined", function() {
        expect(2);
        equal(LocalFileSystem.TEMPORARY, 0, "LocalFileSystem.TEMPORARY should be defined");
        equal(LocalFileSystem.PERSISTENT, 1, "LocalFileSystem.PERSISTENT should be defined");
    });
    test("retrieve PERSISTENT file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);

        var testPersistent = function(fileSystem) {
            ok(typeof fileSystem !== 'undefined' && fileSystem !== null, "window.requestFileSystem should return an object.");
            ok(typeof fileSystem.name !== 'undefined' && fileSystem.name !== null, "filesystem should include a 'name' property.");
            equal(fileSystem.name, "persistent", "file system 'name' attribute should be set properly");
            ok(typeof fileSystem.root !== 'undefined' && fileSystem.root !== null, "filesystem should include a 'root' property.");
            QUnit.start();
        };
        
        // retrieve PERSISTENT file system
        window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, testPersistent, 
                function(error) {
                    console.log('error retrieving file system: ' + error.code);
                });            
    });
    test("retrieve TEMPORARY file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);

        var testTemporary = function(fileSystem) {
            ok(typeof fileSystem !== 'undefined' && fileSystem !== null, "window.requestFileSystem should return an object.");
            ok(typeof fileSystem.name !== 'undefined' && fileSystem.name !== null, "filesystem should include a 'name' property.");
            equal(fileSystem.name, "temporary", "file system 'name' attribute should be set properly");
            ok(typeof fileSystem.root !== 'undefined' && fileSystem.root !== null, "filesystem should include a 'root' property.");
            QUnit.start();
        };
        
        // Request the file system
        window.requestFileSystem(LocalFileSystem.TEMPORARY, 0, testTemporary, 
                function(error) {
                    console.log('error retrieving file system: ' + error.code);
                });            
    });
    test("request a file system that is too large", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);

        var failFS = function(error) {
            ok(error !== null, "error should not be null.");
            equal(error.code, FileError.QUOTA_EXCEEDED_ERR, "Shoud receive error code FileError.QUOTA_EXCEEDED_ERR");
            QUnit.start();
        };
        
        // Request the file system
        window.requestFileSystem(LocalFileSystem.TEMPORARY, 1000000000000000, null, failFS);
    });
    test("request a file system that does not exist", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);

        var failFS = function(error) {
            ok(typeof error !== 'undefined' && error !== null, "error should not be null.");
            equal(error.code, FileError.SYNTAX_ERR, "Shoud receive error code FileError.SYNTAX_ERR");
            QUnit.start();
        };
        
        // Request the file system
        window.requestFileSystem(-1, 0, null, failFS);
    });
    test("resolve invalid file name", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);

        var failURI = function(error) {
            ok(typeof error !== 'undefined' && error !== null, "error should not be null.");
            equal(error.code, FileError.NOT_FOUND_ERR, "Shoud receive error code FileError.NOT_FOUND_ERR");
            QUnit.start();
        };
        
        // lookup file system entry
        window.resolveLocalFileSystemURI("file:///this.is.not.a.valid.file.txt", null, failURI);
    });
    test("resolve invalid URI", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);

        var failURI = function(error) {
            ok(typeof error !== 'undefined' && error !== null, "error should not be null.");
            equal(error.code, FileError.ENCODING_ERR, "Shoud receive an error code FileError.ENCODING_ERR");
            QUnit.start();
        };
        
        // lookup file system entry
        window.resolveLocalFileSystemURI("/this.is.not.a.valid.url", null, failURI);
    });

    module('Metadata interface');
    test("Metadata constructor should exist", function() {
        expect(2);
        var metadata = new Metadata();
        ok(metadata !== null, "Metadata object should not be null.");
        ok(typeof metadata.modificationTime !== 'undefined', "Metadata object should have a 'modificationTime' property.");
    });
    module('Flags interface');
    test("Flags constructor should exist", function() {
        expect(5);
        var flags = new Flags(false, true);
        ok(flags !== null, "Flags object should not be null.");
        ok(typeof flags.create !== 'undefined' && flags.create !== null, "Flags object should have a 'create' property.");
        equal(flags.create, false, "Flags.create should be set properly");
        ok(typeof flags.exclusive !== 'undefined' && flags.exclusive !== null, "Flags object should have an 'exclusive' property.");
        equal(flags.exclusive, true, "flags.exclusive should be set properly")
    });
    module('FileSystem interface');
    test("FileSystem root should be a DirectoryEntry", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(15);

        var root = getFileSystemRoot(),
            testFSRoot = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "entry should be non-null");
                equal(entry.isFile, false, "entry.isFile should be false");
                equal(entry.isDirectory, true, "entry.isDirectory should be true");
                ok(typeof entry.name !== 'undefined' && entry.name !== null, "entry should include a 'name' property.");
                ok(typeof entry.fullPath !== 'undefined' && entry.fullPath !== null, "entry should include a 'fullPath' property.");
                ok(typeof entry.getMetadata === 'function', "entry object should have a 'getMetadata' function.");
                ok(typeof entry.moveTo === 'function', "entry object should have a 'moveTo' function.");
                ok(typeof entry.copyTo === 'function', "entry object should have a 'copyTo' function.");
                ok(typeof entry.toURI === 'function', "entry object should have a 'toURI' function.");
                ok(typeof entry.remove === 'function', "entry object should have a 'remove' function.");
                ok(typeof entry.getParent === 'function', "entry object should have a 'getParent' function.");
                ok(typeof entry.createReader === 'function', "entry object should have a 'createReader' function.");
                ok(typeof entry.getFile === 'function', "entry object should have a 'getFile' function.");
                ok(typeof entry.getDirectory === 'function', "entry object should have a 'getDirectory' function.");
                ok(typeof entry.removeRecursively === 'function', "entry object should have a 'removeRecursively' function.");
                QUnit.start();
		 };
		 
		window.resolveLocalFileSystemURI(root.toURI(), testFSRoot, null);
        
    });
    module('DirectoryEntry interface', {
        // setup function will run before each test
        setup: function() {
            this.root = getFileSystemRoot();
            this.fail = function(error) {
                console.log('file error: ' + error.code);
            };
		   this.unexpectedSuccess = function() {
				console.log('!!! success function called when not expected !!!');
		   };
        }
    });
    test("DirectoryEntry.getFile: get Entry for file that does not exist", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "de.no.file",
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            testFile = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "retrieving a file that does not exist is an error");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
                
                // cleanup
                QUnit.start();
            };
                
        // create:false, exclusive:false, file does not exist
        this.root.getFile(fileName, {create:false}, null, testFile); 
    });
    test("DirectoryEntry.getFile: create new file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var fileName = "de.create.file",
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            testFile = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null");
                equal(entry.isFile, true, "entry 'isFile' attribute should be true");
                equal(entry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equal(entry.name, fileName, "entry 'name' attribute should be set");
                equal(entry.fullPath, filePath, "entry 'fullPath' attribute should be set");
                
                // cleanup
                entry.remove(null, that.fail);
                QUnit.start();
            };
                
        // create:true, exclusive:false, file does not exist
        this.root.getFile(fileName, {create: true}, testFile, this.fail); 
    });
    test("DirectoryEntry.getFile: create new file (exclusive)", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var fileName = "de.create.exclusive.file",
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            testFile = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null");
                equal(entry.isFile, true, "entry 'isFile' attribute should be true");
                equal(entry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equal(entry.name, fileName, "entry 'name' attribute should be set");
                equal(entry.fullPath, filePath, "entry 'fullPath' attribute should be set");
                
                // cleanup
                entry.remove(null, that.fail);
                QUnit.start();
            };
                
        // create:true, exclusive:true, file does not exist
        this.root.getFile(fileName, {create: true, exclusive:true}, testFile, this.fail); 
    });
    test("DirectoryEntry.getFile: create file that already exists", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var fileName = "de.create.existing.file",
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            getFile = function(file) {
                // create:true, exclusive:false, file exists
                that.root.getFile(fileName, {create:true}, testFile, that.fail);             
            },
            testFile = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null");
                equal(entry.isFile, true, "entry 'isFile' attribute should be true");
                equal(entry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equal(entry.name, fileName, "entry 'name' attribute should be set");
                equal(entry.fullPath, filePath, "entry 'fullPath' attribute should be set");
                
                // cleanup
                entry.remove(null, that.fail);
                QUnit.start();
            };
                
        // create file to kick off test
        this.root.getFile(fileName, {create:true}, getFile, this.fail); 
    });
    test("DirectoryEntry.getFile: create file that already exists (exclusive)", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "de.create.exclusive.existing.file",
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            existingFile,
            getFile = function(file) {
                existingFile = file;
                // create:true, exclusive:true, file exists
                that.root.getFile(fileName, {create:true, exclusive:true}, null, testFile);             
            },
            testFile = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "creating exclusive file that already exists is an error");
                equal(error.code, FileError.PATH_EXISTS_ERR, "error code should be FileError.PATH_EXISTS_ERR");
               
                // cleanup
                existingFile.remove(null, that.fail);
                QUnit.start();
            };
                
        // create file to kick off test
        this.root.getFile(fileName, {create:true}, getFile, this.fail); 
    });
    test("DirectoryEntry.getFile: get Entry for existing file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var fileName = "de.get.file",
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            getFile = function(file) {
                // create:false, exclusive:false, file exists
                that.root.getFile(fileName, {create:false}, testFile, that.fail);             
            },
            testFile = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null");
                equal(entry.isFile, true, "entry 'isFile' attribute should be true");
                equal(entry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equal(entry.name, fileName, "entry 'name' attribute should be set");
                equal(entry.fullPath, filePath, "entry 'fullPath' attribute should be set");
				
				// cleanup
                entry.remove(null, that.fail);
                QUnit.start();
            };
                
        // create file to kick off test
        this.root.getFile(fileName, {create:true}, getFile, this.fail); 
    });
    test("DirectoryEntry.getFile: get FileEntry for invalid path", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "de:invalid:path",
            that = this,
            testFile = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "retrieving a file using an invalid path is an error");
                equal(error.code, FileError.ENCODING_ERR, "error code should be FileError.ENCODING_ERR");
                
                // cleanup
                QUnit.start();
            };
                
        // create:false, exclusive:false, invalid path
        this.root.getFile(fileName, {create:false}, null, testFile); 
    });
    test("DirectoryEntry.getDirectory: get Entry for directory that does not exist", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "de.no.dir",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            testDir = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "retrieving a directory that does not exist is an error");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
                
                // cleanup
                QUnit.start();
            };
                
        // create:false, exclusive:false, directory does not exist
        this.root.getDirectory(dirName, {create:false}, null, testDir); 
    });
    test("DirectoryEntry.getDirectory: create new dir with space then resolveFileSystemURI", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var dirName = "de create dir",
        dirPath = this.root.fullPath + '/' + dirName,
        that = this,
        getDir = function(dirEntry) {
            
            var dirURI = dirEntry.toURI();
            // now encode URI and try to resolve
            window.resolveLocalFileSystemURI(dirURI, testDirFromURI, that.fail);
            
        },
        testDirFromURI = function(directory) {
            ok(typeof directory !== 'undefined' && directory !== null, "directory entry should not be null");
            equal(directory.isFile, false, "directory 'isFile' attribute should be false");
            equal(directory.isDirectory, true, "directory 'isDirectory' attribute should be true");
            equal(directory.name, dirName, "directory 'name' attribute should be set");
            equal(directory.fullPath, dirPath, "directory 'fullPath' attribute should be set");
            
            // cleanup
            directory.remove(null, that.fail);
            QUnit.start();
        };
        
        // create:true, exclusive:false, directory does not exist
        this.root.getDirectory(dirName, {create: true}, getDir, this.fail); 
    });
    test("DirectoryEntry.getDirectory: create new dir with space resolveFileSystemURI with encoded URI", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var dirName = "de create dir",
        dirPath = this.root.fullPath + '/' + dirName,
        that = this,
        getDir = function(dirEntry) {
            
            var dirURI = dirEntry.toURI();
            // now encode URI and try to resolve
            window.resolveLocalFileSystemURI(encodeURI(dirURI), testDirFromURI, that.fail);
            
        },
        testDirFromURI = function(directory) {
            ok(typeof directory !== 'undefined' && directory !== null, "directory entry should not be null");
            equal(directory.isFile, false, "directory 'isFile' attribute should be false");
            equal(directory.isDirectory, true, "directory 'isDirectory' attribute should be true");
            equal(directory.name, dirName, "directory 'name' attribute should be set");
            equal(directory.fullPath, dirPath, "directory 'fullPath' attribute should be set");
        
            // cleanup
            directory.remove(null, that.fail);
            QUnit.start();
        };
        
        // create:true, exclusive:false, directory does not exist
        this.root.getDirectory(dirName, {create: true}, getDir, this.fail); 
    });

    test("DirectoryEntry.getDirectory: create new directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var dirName = "de.create.dir",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            testDir = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "directory entry should not be null");
                equal(directory.isFile, false, "directory 'isFile' attribute should be false");
                equal(directory.isDirectory, true, "directory 'isDirectory' attribute should be true");
                equal(directory.name, dirName, "directory 'name' attribute should be set");
                equal(directory.fullPath, dirPath, "directory 'fullPath' attribute should be set");
                
                // cleanup
                directory.remove(null, that.fail);
                QUnit.start();
            };
                
        // create:true, exclusive:false, directory does not exist
        this.root.getDirectory(dirName, {create: true}, testDir, this.fail); 
    });
    
    test("DirectoryEntry.getDirectory: create new directory (exclusive)", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var dirName = "de.create.exclusive.dir",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            testDir = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "directory entry should not be null");
                equal(directory.isFile, false, "directory 'isFile' attribute should be false");
                equal(directory.isDirectory, true, "directory 'isDirectory' attribute should be true");
                equal(directory.name, dirName, "directory 'name' attribute should be set");
                equal(directory.fullPath, dirPath, "directory 'fullPath' attribute should be set");
               
                // cleanup
                directory.remove(null, that.fail);
                QUnit.start();
            };
                
        // create:true, exclusive:true, directory does not exist
        this.root.getDirectory(dirName, {create: true, exclusive:true}, testDir, this.fail); 
    });
    test("DirectoryEntry.getDirectory: create directory that already exists", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var dirName = "de.create.existing.dir",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            getDir = function(directory) {
                // create:true, exclusive:false, directory exists
                that.root.getDirectory(dirName, {create:true}, testDir, that.fail);             
            },
            testDir = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "directory entry should not be null");
                equal(directory.isFile, false, "directory 'isFile' attribute should be false");
                equal(directory.isDirectory, true, "directory 'isDirectory' attribute should be true");
                equal(directory.name, dirName, "directory 'name' attribute should be set");
                equal(directory.fullPath, dirPath, "directory 'fullPath' attribute should be set");
                
                // cleanup
                directory.remove(null, that.fail);
                QUnit.start();
            };
                
        // create directory to kick off test
        this.root.getDirectory(dirName, {create:true}, getDir, this.fail); 
    });
    test("DirectoryEntry.getDirectory: create directory that already exists (exclusive)", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "de.create.exclusive.existing.dir",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            existingDir,
            getDir = function(directory) {
                existingDir = directory;
                // create:true, exclusive:true, directory exists
                that.root.getDirectory(dirName, {create:true, exclusive:true}, null, testDir);             
            },
            testDir = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "creating exclusive directory that already exists is an error");
                equal(error.code, FileError.PATH_EXISTS_ERR, "error code should be FileError.PATH_EXISTS_ERR");
                
                // cleanup
                existingDir.remove(null, that.fail);
                QUnit.start();
            };
                
        // create directory to kick off test
        this.root.getDirectory(dirName, {create:true}, getDir, this.fail); 
    });
    test("DirectoryEntry.getDirectory: get Entry for existing directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var dirName = "de.get.dir",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            getDir = function(directory) {
                // create:false, exclusive:false, directory exists
                that.root.getDirectory(dirName, {create:false}, testDir, that.fail);             
            },
            testDir = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "directory entry should not be null");
                equal(directory.isFile, false, "directory 'isFile' attribute should be false");
                equal(directory.isDirectory, true, "directory 'isDirectory' attribute should be true");
                equal(directory.name, dirName, "directory 'name' attribute should be set");
                equal(directory.fullPath, dirPath, "directory 'fullPath' attribute should be set");
                
                // cleanup
                directory.remove(null, that.fail);
                QUnit.start();
            };
                
        // create directory to kick off test
        this.root.getDirectory(dirName, {create:true}, getDir, this.fail); 
    });
    test("DirectoryEntry.getDirectory: get DirectoryEntry for invalid path", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "de:invalid:path",
            that = this,
            testDir = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "retrieving a directory using an invalid path is an error");
                equal(error.code, FileError.ENCODING_ERR, "error code should be FileError.ENCODING_ERR");
                
                // cleanup
                QUnit.start();
            };
                
        // create:false, exclusive:false, invalid path
        this.root.getDirectory(dirName, {create:false}, null, testDir); 
    });
    test("DirectoryEntry.getDirectory: get DirectoryEntry for existing file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "de.existing.file",
            existingFile,
            filePath = this.root.fullPath + '/' + fileName,
            that = this,
            getDir = function(file) {
                existingFile = file;
                // create:false, exclusive:false, existing file
                that.root.getDirectory(fileName, {create:false}, null, testDir);             
            },
            testDir = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "retrieving directory for existing file is an error");
                equal(error.code, FileError.TYPE_MISMATCH_ERR, "error code should be FileError.TYPE_MISMATCH_ERR");
                
                // cleanup
                existingFile.remove(null, that.fail);
                QUnit.start();
            };
                
        // create file to kick off test
        this.root.getFile(fileName, {create:true}, getDir, this.fail); 
    });
    test("DirectoryEntry.getFile: get FileEntry for existing directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "de.existing.dir",
            existingDir,
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            getFile = function(directory) {
                existingDir = directory;
                // create:false, exclusive:false, existing directory
                that.root.getFile(dirName, {create:false}, null, testFile);             
            },
            testFile = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "retrieving file for existing directory is an error");
                equal(error.code, FileError.TYPE_MISMATCH_ERR, "error code should be FileError.TYPE_MISMATCH_ERR");
               
                // cleanup
                existingDir.remove(null, that.fail);
                QUnit.start();
            };
                
        // create directory to kick off test
        this.root.getDirectory(dirName, {create:true}, getFile, this.fail); 
    });
    test("DirectoryEntry.removeRecursively on directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "de.removeRecursively",
            subDirName = "dir",
            dirPath = this.root.fullPath + '/' + dirName,
            //subDirPath = this.root.fullPath + '/' + subDirName,
			subDirPath = dirPath + '/' + subDirName,
            that = this,
            entryCallback = function(entry) {
                // delete directory
				var deleteDirectory = function(directory) {
                    entry.removeRecursively(testRemove, that.fail);  
                }; 
                // create a sub-directory within directory
                entry.getDirectory(subDirName, {create: true}, deleteDirectory, that.fail);
				},
				testRemove = function() {
					// test that removed directory no longer exists
					that.root.getDirectory(dirName, {create:false}, null, testDirExists);
				},
				testDirExists = function(error){
					ok(typeof error !== 'undefined' && error !== null, "removed directory should not exist");
					equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
					QUnit.start();
				};

        // create a new directory entry to kick off test
        this.root.getDirectory(dirName, {create:true}, entryCallback, this.fail);
    });
    test("DirectoryEntry.createReader: create reader on existing directory", function() {
        expect(2);
        
        // create reader for root directory 
        var reader = this.root.createReader();
        ok(typeof reader !== 'undefined' && reader !== null, "reader object should not be null");
        ok(typeof reader.readEntries === 'function', "reader object should have a 'readEntries' method");
    });
    module('DirectoryReader interface', {
        // setup function will run before each test
        setup: function() {
            this.root = getFileSystemRoot();
            this.fail = function(error) {
                console.log('file error: ' + error.code);
            };   
        }
    });
    test("DirectoryReader.readEntries: read contents of existing directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var reader,
		 testEntries = function(entries) {
                ok(typeof entries !== 'undefined' && entries !== null, "directory entries should not be null");
                ok(entries.constructor === Array, "readEntries should return an array of entries");
                QUnit.start();
            };
                
        // create reader for root directory 
        reader = this.root.createReader();
        ok(typeof reader !== 'undefined' && reader !== null, "reader object should not be null");
        ok(typeof reader.readEntries === 'function', "reader object should have a 'readEntries' method");
        
        // read entries
        reader.readEntries(testEntries, this.fail);
    });
    test("DirectoryReader.readEntries: read contents of directory that has been removed", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var dirName = "de.createReader.notfound",
            dirPath = this.root.fullPath + '/' + dirName,
            that = this,
            entryCallback = function(directory) {
                // read entries
                var readEntries = function() {
                    var reader = directory.createReader();
                    reader.readEntries(null, testReader);
                };
                // delete directory
                directory.removeRecursively(readEntries, that.fail);  
            },
            testReader = function(error) {
				var testDirectoryExists = function(error) {
					ok(typeof error !== 'undefined' && error !== null, "reading entries on a directory that does not exist is an error")
					equal(error.code, FileError.NOT_FOUND_ERR, "removed directory should not exist");
					QUnit.start();
				};
                ok(typeof error !== 'undefined' && error !== null, "reading entries on a directory that does not exist is an error")
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
				that.root.getDirectory(dirName, {create:false}, null, testDirectoryExists);
            };

        // create a new directory entry to kick off test
        this.root.getDirectory(dirName, {create:true}, entryCallback, this.fail);
    });
    test("DirectoryEntry.removeRecursively on root file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var testRemove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "removing root file system should generate an error");
                equal(error.code, FileError.NO_MODIFICATION_ALLOWED_ERR, "error code should be FileError.NO_MODIFICATION_ALLOWED_ERR");
                QUnit.start();
            };

        // remove root file system
        this.root.removeRecursively(null, testRemove);
    });
    module('File interface');
    test("File constructor should be defined", function() {
        expect(1);
        ok(typeof File === 'function', "File constructor should be a function.");
    });
    test("File attributes should be defined", function() {
        expect(5);
        var file = new File();
        ok(typeof file.name !== 'undefined', "File object should have a 'name' attribute");
        ok(typeof file.fullPath !== 'undefined', "File object should have a 'fullPath' attribute");
        ok(typeof file.type !== 'undefined', "File object should have a 'type' attribute");
        ok(typeof file.lastModifiedDate !== 'undefined', "File object should have a 'lastModifiedDate' attribute");
        ok(typeof file.size !== 'undefined', "File object should have a 'size' attribute");
    });
    module('FileEntry interface', {
        // setup function will run before each test
        setup: function() {
            this.root = getFileSystemRoot();
            this.fail = function(error) {
                console.log('file error: ' + error.code);
            };   
        }
    });
    test("FileEntry methods should be defined", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(3);
        
        var fileName = "fe.methods",
            that = this,
            testFileEntry = function(fileEntry) {
                ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "FileEntry should not be null");
                ok(typeof fileEntry.createWriter === 'function', "FileEntry should have a 'createWriter' method");
                ok(typeof fileEntry.file === 'function', "FileEntry should have a 'file' method");
                
                // cleanup 
                fileEntry.remove(null, that.fail);
                QUnit.start();
            };
                
        // create a new file entry to kick off test
        this.root.getFile(fileName, {create:true}, testFileEntry, this.fail);
    });
    test("FileEntry.createWriter should return a FileWriter object", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "fe.createWriter",
            that = this,
            testFile,
            entryCallback = function(fileEntry) {
                testFile = fileEntry;
                fileEntry.createWriter(testWriter, that.fail);
            },
            testWriter = function(writer) {
                ok(typeof writer !== 'undefined' && writer !== null, "FileWriter object should not be null");
                ok(writer.constructor === FileWriter, "writer should be a FileWriter object");
                
                // cleanup 
                testFile.remove(null, that.fail);
                QUnit.start();                
            };
                
        // create a new file entry to kick off test
        this.root.getFile(fileName, {create:true}, entryCallback, this.fail);
    });
    test("FileEntry.file should return a File object", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "fe.file",
            that = this,
            newFile,
            entryCallback = function(fileEntry) {
                newFile = fileEntry;
                fileEntry.file(testFile, that.fail);
            },
            testFile = function(file) {
                ok(typeof file !== 'undefined' && file !== null, "File object should not be null");
                ok(file.constructor === File, "File object should be a File");
                
                // cleanup 
                newFile.remove(null, that.fail);
                QUnit.start();                
            };
                
        // create a new file entry to kick off test
        this.root.getFile(fileName, {create:true}, entryCallback, this.fail);
    });
    test("FileEntry.file: on File that has been removed", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "fe.no.file",
            that = this,
            entryCallback = function(fileEntry) {
                // create File object
                var getFile = function() {
                    fileEntry.file(null, testFile);
                };
                // delete file
                fileEntry.remove(getFile, that.fail);
            },
            testFile = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "invoking FileEntry.file on a file that does not exist is an error");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
                QUnit.start();                
            };
                
        // create a new file entry to kick off test
        this.root.getFile(fileName, {create:true}, entryCallback, this.fail);
    });
    module('Entry interface', {
        // setup function will run before each test
        setup: function() {
            var that = this;
            this.root = getFileSystemRoot();
            this.fail = function(error) {
                console.log('file error: ' + error.code);
            };
			this.unexpectedSuccess = function() {
				console.log('!!! success function called when not expected !!!');
			};
            // deletes specified file or directory
            this.deleteEntry = function(name, success, error) {
                // deletes entry, if it exists
                window.resolveLocalFileSystemURI(that.root.toURI() + '/' + name, 
                        function(entry) {
                            console.log('Deleting: ' + entry.fullPath);
                            if (entry.isDirectory === true) {
                                entry.removeRecursively(success, error); 
                            }
                            else {
                                entry.remove(success, error);
                            }
                        }, 
                        // doesn't exist
                        success);
            };
            // deletes and re-creates the specified file
            this.createFile = function(fileName, success, error) {
                that.deleteEntry(fileName, function() {
                    console.log('Creating file: ' + that.root.fullPath + '/' + fileName);
                    that.root.getFile(fileName, {create: true}, success, error);                
                }, error);
            };
            // deletes and re-creates the specified directory
            this.createDirectory = function(dirName, success, error) {
                that.deleteEntry(dirName, function() {
                   console.log('Creating directory: ' + that.root.fullPath + '/' + dirName);
                   that.root.getDirectory(dirName, {create: true}, success, error); 
                }, error);
            };
        }
    });
    test("Entry object", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(13);

        var fileName = "entry",
            that = this,
            fullPath = this.root.fullPath + '/' + fileName,
            testEntry = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "entry should not be null.");
                equal(entry.isFile, true, "entry.isFile should be true");
                equal(entry.isDirectory, false, "entry.isDirectory should be false");
                equal(entry.name, fileName, "entry object 'name' property should be set");
                equal(entry.fullPath, fullPath, "entry object 'fullPath' property should be set");
                ok(typeof entry.getMetadata === 'function', "entry object should have a 'getMetadata' function.");
                ok(typeof entry.moveTo === 'function', "entry object should have a 'moveTo' function.");
                ok(typeof entry.copyTo === 'function', "entry object should have a 'copyTo' function.");
                ok(typeof entry.toURI === 'function', "entry object should have a 'toURI' function.");
                ok(typeof entry.remove === 'function', "entry object should have a 'remove' function.");
                ok(typeof entry.getParent === 'function', "entry object should have a 'getParent' function.");
                ok(typeof entry.createWriter === 'function', "entry object should have a 'createWriter' function.");
                ok(typeof entry.file === 'function', "entry object should have a 'file' function.");
                
                // cleanup
                that.deleteEntry(fileName);
                QUnit.start();
            };

        // create a new file entry
        this.createFile(fileName, testEntry, this.fail);
    });
    test("Entry.getMetadata on file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "entry.metadata.file",
            that = this,
            entryCallback = function(entry) {
                entry.getMetadata(testMetadata, this.fail);
            },
            testMetadata = function(metadata) {
                ok(typeof metadata !== 'undefined' && metadata !== null, "metadata should not be null.");
                ok(metadata.modificationTime instanceof Date, "metadata.modificationTime should be Date object");

                // cleanup
                that.deleteEntry(fileName);
                QUnit.start();
            };
        
        // create a new file entry
        this.createFile(fileName, entryCallback, this.fail);
    });
    test("Entry.getMetadata on directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "entry.metadata.dir",
            that = this,
            entryCallback = function(entry) {
                entry.getMetadata(testMetadata, this.fail);
            },
            testMetadata = function(metadata) {
                ok(typeof metadata !== 'undefined' && metadata !== null, "metadata should not be null.");
                ok(metadata.modificationTime instanceof Date, "metadata.modificationTime should be Date object");

                // cleanup
                that.deleteEntry(dirName);
                QUnit.start();
            };
        
        // create a new directory entry
        this.createDirectory(dirName, entryCallback, this.fail);
    });
    test("Entry.getParent on file in root file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "entry.parent.file",
            that = this,
            rootPath = this.root.fullPath,
            entryCallback = function(entry) {
                entry.getParent(testParent, this.fail);
            },
            testParent = function(parent) {
                ok(typeof parent !== 'undefined' && parent !== null, "parent directory should not be null.");
                equal(parent.fullPath, rootPath, "parent fullPath should be root file system");

                // cleanup
                that.deleteEntry(fileName);
                QUnit.start();
            };
    
        // create a new file entry
        this.createFile(fileName, entryCallback, this.fail);
    });
    test("Entry.getParent on directory in root file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "entry.parent.dir",
            that = this,
            rootPath = this.root.fullPath,
            entryCallback = function(entry) {
                entry.getParent(testParent, this.fail);
            },
            testParent = function(parent) {
                ok(typeof parent !== 'undefined' && parent !== null, "parent directory should not be null.");
                equal(parent.fullPath, rootPath, "parent fullPath should be root file system");

                // cleanup
                that.deleteEntry(dirName);
                QUnit.start();
            };

        // create a new directory entry
        this.createDirectory(dirName, entryCallback, this.fail);
    });
    test("Entry.getParent on root file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var rootPath = this.root.fullPath,
		 testParent = function(parent) {
                ok(typeof parent !== 'undefined' && parent !== null, "parent directory should not be null.");
                equal(parent.fullPath, rootPath, "parent fullPath should be root file system");
                QUnit.start();
            };

        // create a new directory entry
        this.root.getParent(testParent, this.fail);
    });
    test("Entry.toURI on file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var fileName = "entry.uri.file",
            that = this,
            rootPath = this.root.fullPath,
            testURI = function(entry) {
                var uri = entry.toURI();
                ok(typeof uri !== 'undefined' && uri !== null, "URI should not be null.");
                ok(uri.indexOf(rootPath) !== -1, "URI should contain root file system path");

                // cleanup
                that.deleteEntry(fileName);
                QUnit.start();
            };
    
        // create a new file entry
        this.createFile(fileName, testURI, this.fail);
    });
    test("Entry.toURI on directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var dirName = "entry.uri.dir",
            that = this,
            rootPath = this.root.fullPath,
            testURI = function(entry) {
                var uri = entry.toURI();
                ok(typeof uri !== 'undefined' && uri !== null, "URI should not be null.");
                ok(uri.indexOf(rootPath) !== -1, "URI should contain root file system path");

                // cleanup
                that.deleteEntry(dirName);
                QUnit.start();
            };

        // create a new directory entry
        this.createDirectory(dirName, testURI, this.fail);
    });
    test("Entry.remove on file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(3);
        
        var fileName = "entry.rm.file",
            that = this,
            fullPath = this.root.fullPath + '/' + fileName,
		 entryCallback = function(entry) {
				var checkRemove = function() {
					that.root.getFile(fileName, null, that.unexpectedSuccess, testRemove);  
				};
                ok(typeof entry !== 'undefined' && entry !== null, "entry should not be null.");
                entry.remove(checkRemove, that.fail);
            },
			testRemove = function(error) {
				ok(typeof error !== 'undefined' && error !== null, "file should not exist");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR"); 
                // cleanup
                that.deleteEntry(fileName);
                QUnit.start();
            };
    
        // create a new file entry
        this.createFile(fileName, entryCallback, this.fail);
    });
    test("Entry.remove on empty directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(3);
        
        var dirName = "entry.rm.dir",
            that = this,
            fullPath = this.root.fullPath + '/' + dirName,
            entryCallback = function(entry) {
				var checkRemove = function() {
					that.root.getDirectory(dirName, null, that.unexpectedSuccess, testRemove);  
				};
                ok(typeof entry !== 'undefined' && entry !== null, "entry should not be null.");
                entry.remove(checkRemove, that.fail);
            },
            testRemove = function(error) {
				ok(typeof error !== 'undefined' && error !== null, "directory should not exist");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR"); 
                // cleanup
                that.deleteEntry(dirName);                
                QUnit.start();
            };

        // create a new directory entry
        this.createDirectory(dirName, entryCallback, this.fail);
    });
    test("Entry.remove on non-empty directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var dirName = "entry.rm.dir.not.empty",
            that = this,
            fullPath = this.root.fullPath + '/' + dirName,
            fileName = "remove.txt",
            entryCallback = function(entry) {
				var checkFile = function(error) {
					ok(typeof error !== 'undefined' && error !== null, "removing non-empty directory should generate an error");
					equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
					// verify that dir still exists
					that.root.getDirectory(dirName, null, testRemove, that.fail);  
				};
                // delete directory
                var deleteDirectory = function(fileEntry) {
                    entry.remove(that.unexpectedSuccess, checkFile);  
                }; 
                // create a file within directory, then try to delete directory
                entry.getFile(fileName, {create: true}, deleteDirectory, that.fail);
            },
			testRemove = function(entry) {
				ok(typeof entry !== 'undefined' && entry !== null, "entry should not be null.");
				equal(entry.fullPath, fullPath, "dir entry should still exisit");
                // cleanup
                that.deleteEntry(dirName);
                QUnit.start();
            };

        // create a new directory entry
        this.createDirectory(dirName, entryCallback, this.fail);
    });
    test("Entry.remove on root file system", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var testRemove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "removing root file system should generate an error");
                equal(error.code, FileError.NO_MODIFICATION_ALLOWED_ERR, "error code should be FileError.NO_MODIFICATION_ALLOWED_ERR");
                QUnit.start();
            };

        // remove entry that doesn't exist
        this.root.remove(null, testRemove);
    });
    test("Entry.copyTo: file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(10);
        
        var file1 = "entry.copy.file1",
            file2 = "entry.copy.file2",
            that = this,
            fullPath = this.root.fullPath + '/' + file2,
            entryCallback = function(entry) {
                // copy file1 to file2
                entry.copyTo(that.root, file2, testCopy, that.fail);
            },
			testCopy = function(entry) {
				
				ok(typeof entry !== 'undefined' && entry !== null, "copied file entry should not be null");
				equals(entry.isFile, true, "entry 'isFile' attribute should be set to true");
				equals(entry.isDirectory, false, "entry 'isDirectory' attribute should be set to false");
				equals(entry.fullPath, fullPath, "entry 'fullPath' should be set correctly");
				equals(entry.name, file2, "entry 'name' attribute should be set correctly");
				that.root.getFile(file2, {create:false}, testFileExists, null);							  
                
            },
			testFileExists = function(entry2) {
				// a bit redundant since copy returned this entry already
				ok(typeof entry2 !== 'undefined' && entry2 !== null, "copied file entry should not be null");
				equals(entry2.isFile, true, "entry 'isFile' attribute should be set to true");
				equals(entry2.isDirectory, false, "entry 'isDirectory' attribute should be set to false");
				equals(entry2.fullPath, fullPath, "entry 'fullPath' should be set correctly");
				equals(entry2.name, file2, "entry 'name' attribute should be set correctly");
		 
				// cleanup
				that.deleteEntry(file1);
				that.deleteEntry(file2);
				QUnit.start();
			};

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.copyTo: file onto itself", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var file1 = "entry.copy.fos.file1",
            that = this,
            entryCallback = function(entry) {
                // copy file1 onto itself
                entry.copyTo(that.root, null, null, testCopy);
            },
		 testCopy = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to copy an entry into its parent if a different name is not specified");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");

                // cleanup
                that.deleteEntry(file1);
                QUnit.start();
            };

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.copyTo: directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(15);
        
        var file1 = "file1",
            srcDir = "entry.copy.srcDir",
            dstDir = "entry.copy.dstDir",
            dstPath = this.root.fullPath + '/' + dstDir,
            filePath = dstPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var copyDir = function(fileEntry) {
                    // copy srcDir to dstDir
                    directory.copyTo(that.root, dstDir, testCopy, that.fail);                    
                };
              // create a file within new directory
              directory.getFile(file1, {create: true}, copyDir, that.fail);
            },
            testCopy = function(directory) {
                
                ok(typeof directory !== 'undefined' && directory !== null, "copied directory entry should not be null");
                equals(directory.isFile, false, "entry 'isFile' attribute should be false");
                equals(directory.isDirectory, true, "entry 'isDirectory' attribute should be true");
                equals(directory.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(directory.name, dstDir, "entry 'name' attribute should be set correctly");
         
                that.root.getDirectory(dstDir, {create:false}, testDirExists, that.fail);
           },
            testDirExists = function(dirEntry) {
                 ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "copied directory entry should not be null");
                 equals(dirEntry.isFile, false, "entry 'isFile' attribute should be false");
                 equals(dirEntry.isDirectory, true, "entry 'isDirectory' attribute should be true");
                 equals(dirEntry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                 equals(dirEntry.name, dstDir, "entry 'name' attribute should be set correctly");
                 
                 dirEntry.getFile(file1, {create:false}, testFileExists, that.fail);
         
         };
            testFileExists = function(fileEntry) {
                ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "copied directory entry should not be null");
                equals(fileEntry.isFile, true, "entry 'isFile' attribute should be true");
                equals(fileEntry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
                equals(fileEntry.name, file1, "entry 'name' attribute should be set correctly");
                

                // cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // create a new directory entry to kick off test
        this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.copyTo: directory to backup at same root directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(15);
        
        var file1 = "file1",
            srcDir = "entry.copy.srcDir",
            dstDir = "entry.copy.srcDir-backup",
            dstPath = this.root.fullPath + '/' + dstDir,
            filePath = dstPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var copyDir = function(fileEntry) {
                    // copy srcDir to dstDir
                    directory.copyTo(that.root, dstDir, testCopy, that.fail);                    
                };
              // create a file within new directory
              directory.getFile(file1, {create: true}, copyDir, that.fail);
            },
            testCopy = function(directory) {
                
                ok(typeof directory !== 'undefined' && directory !== null, "copied directory entry should not be null");
                equals(directory.isFile, false, "entry 'isFile' attribute should be false");
                equals(directory.isDirectory, true, "entry 'isDirectory' attribute should be true");
                equals(directory.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(directory.name, dstDir, "entry 'name' attribute should be set correctly");
         
                that.root.getDirectory(dstDir, {create:false}, testDirExists, that.fail);
           },
            testDirExists = function(dirEntry) {
                 ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "copied directory entry should not be null");
                 equals(dirEntry.isFile, false, "entry 'isFile' attribute should be false");
                 equals(dirEntry.isDirectory, true, "entry 'isDirectory' attribute should be true");
                 equals(dirEntry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                 equals(dirEntry.name, dstDir, "entry 'name' attribute should be set correctly");
                 
                 dirEntry.getFile(file1, {create:false}, testFileExists, that.fail);
         
         };
            testFileExists = function(fileEntry) {
                ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "copied directory entry should not be null");
                equals(fileEntry.isFile, true, "entry 'isFile' attribute should be true");
                equals(fileEntry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
                equals(fileEntry.name, file1, "entry 'name' attribute should be set correctly");
                

                // cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // create a new directory entry to kick off test
        this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.copyTo: directory onto itself", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(6);
        
        var file1 = "file1",
            srcDir = "entry.copy.dos.srcDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            filePath = srcPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var copyDir = function(fileEntry) {
                    // copy srcDir onto itself
                    directory.copyTo(that.root, null, null, testCopy);                    
                };
              // create a file within new directory
              directory.getFile(file1, {create: true}, copyDir, that.fail);
            },
            testCopy = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to copy an entry into its parent if a different name is not specified");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
                
				that.root.getDirectory(srcDir, {create:false}, testDirectoryExists, null);
			},
			 testDirectoryExists = function(dirEntry) {
				// returning confirms existence so just check fullPath entry
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory should exist.");
				equals(dirEntry.fullPath, srcPath, "entry 'fullPath' should be set correctly");
			 
				dirEntry.getFile(file1, {create:false}, testFileExists, null);
			 },
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
                
                // cleanup
                that.deleteEntry(srcDir);
                QUnit.start();
            };

        // create a new directory entry to kick off test
        this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.copyTo: directory into itself", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var srcDir = "entry.copy.dis.srcDir",
            dstDir = "entry.copy.dis.dstDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            that = this,
            entryCallback = function(directory) {
                // copy source directory into itself
                directory.copyTo(directory, dstDir, null, testCopy);                    
            },
            testCopy = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to copy a directory into itself");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
                
				that.root.getDirectory(srcDir, {create:false}, testDirectoryExists, null);
			},
			testDirectoryExists = function(dirEntry) {
				// returning confirms existence so just check fullPath entry
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory should exist.");
				equals(dirEntry.fullPath, srcPath, "entry 'fullPath' should be set correctly");
		 
                // cleanup
                that.deleteEntry(srcDir);
                QUnit.start();
            };

        // create a new directory entry to kick off test
        this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.copyTo: directory that does not exist", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var file1 = "entry.copy.dnf.file1", 
            dstDir = "entry.copy.dnf.dstDir",
            filePath = this.root.fullPath + '/' + file1,
            dstPath = this.root.fullPath + '/' + dstDir,
            that = this,
            entryCallback = function(entry) {
                // copy file to target directory that does not exist
                directory = new DirectoryEntry();
				directory.fullPath = dstPath;
                entry.copyTo(directory, null, null, testCopy);                 
            },
            testCopy = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to copy to a directory that does not exist");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
				that.root.getFile(file1, {create: false}, testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original file should exist");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
		 
                // cleanup
                that.deleteEntry(file1);
                QUnit.start();
            };

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.copyTo: invalid target name", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var file1 = "entry.copy.itn.file1",
            file2 = "bad:file:name",
            that = this,
            filePath = this.root.fullPath + '/' + file1,
            entryCallback = function(entry) {
                // copy file1 to file2
                entry.copyTo(that.root, file2, null, testCopy);
            },
            testCopy = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "invalid file name should result in error");
                equal(error.code, FileError.ENCODING_ERR, "error code should be FileError.ENCODING_ERR");

                // cleanup
                that.deleteEntry(file1);
                QUnit.start();
            };

        // create a new file entry
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.moveTo: file to same parent", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "entry.move.fsp.file1",
            file2 = "entry.move.fsp.file2",
            that = this,
            srcPath = this.root.fullPath + '/' + file1,
            dstPath = this.root.fullPath + '/' + file2,
            entryCallback = function(entry) {
                // move file1 to file2
                entry.moveTo(that.root, file2, testMove, that.fail);
            },
            testMove = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null");
                equals(entry.isFile, true, "entry 'isFile' attribute should be set to true");
                equals(entry.isDirectory, false, "entry 'isDirectory' attribute should be set to false");
                equals(entry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(entry.name, file2, "entry 'name' attribute should be set correctly");
		 
				that.root.getFile(file2, {create:false}, testMovedExists, null);
			},
			testMovedExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "moved file should exist");
				equals(fileEntry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
               
				that.root.getFile(file1, {create:false}, null, testOrig);
			},
			testOrig = function(error) {
                //ok(navigator.fileMgr.testFileExists(srcPath) === false, "original file should not exist.");
				ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
		 
		 
                // cleanup
                that.deleteEntry(file1);
                that.deleteEntry(file2);
                QUnit.start();
            };

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.moveTo: file to new parent", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "entry.move.fnp.file1",
            dir = "entry.move.fnp.dir",
            that = this,
            srcPath = this.root.fullPath + '/' + file1,
            dstPath = this.root.fullPath + '/' + dir + '/' + file1,
            entryCallback = function(entry) {
                // move file1 to new directory
                var moveFile = function(directory) {
					
					var testMove = function(entry) {
						ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null");
						equals(entry.isFile, true, "entry 'isFile' attribute should be set to true");
						equals(entry.isDirectory, false, "entry 'isDirectory' attribute should be set to false");
						equals(entry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
						equals(entry.name, file1, "entry 'name' attribute should be set correctly");
						// test the moved file exists
						directory.getFile(file1, {create:false}, testMovedExists, null);
					};
					// move the file
					entry.moveTo(directory, null, testMove, that.fail);
				};
		 
                // create a parent directory to move file to
                that.root.getDirectory(dir, {create: true}, moveFile, that.fail);
            },
			testMovedExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "moved file should exist");
				equals(fileEntry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
		 
				that.root.getFile(file1, {create:false}, null, testOrig);
			},
			testOrig = function(error) {
				ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
				
				// cleanup
                that.deleteEntry(file1);
                that.deleteEntry(dir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dir, function() {
            // create a new file entry to kick off test
            that.createFile(file1, entryCallback, that.fail);            
        }, this.fail);
    });
    test("Entry.moveTo: directory to same parent", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "file1",
            srcDir = "entry.move.dsp.srcDir",
            dstDir = "entry.move.dsp.dstDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            dstPath = this.root.fullPath + '/' + dstDir,
            filePath = dstPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var moveDir = function(fileEntry) {
                    // move srcDir to dstDir
                    directory.moveTo(that.root, dstDir, testMove, that.fail);                    
                };
              // create a file within directory
              directory.getFile(file1, {create: true}, moveDir, that.fail);
            },
            testMove = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "new directory entry should not be null");
                equals(directory.isFile, false, "entry 'isFile' attribute should be false");
                equals(directory.isDirectory, true, "entry 'isDirectory' attribute should be true");
                equals(directory.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(directory.name, dstDir, "entry 'name' attribute should be set correctly");
                // test that moved file exists in destination dir
                directory.getFile(file1, {create:false}, testMovedExists, null);
            },
            testMovedExists = function(fileEntry) {
                ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "moved file should exist within moved directory");
                equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
                // test that the moved file no longer exists in original dir
                that.root.getFile(file1, {create:false}, null, testOrig);
            },
            testOrig = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
         
                // cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dstDir, function() {
            // create a new directory entry to kick off test
            that.createDirectory(srcDir, entryCallback, that.fail);
        }, this.fail);
    });
    test("Entry.moveTo: directory to same parent with same name", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "file1",
            srcDir = "entry.move.dsp.srcDir",
            dstDir = "entry.move.dsp.srcDir-backup",
            srcPath = this.root.fullPath + '/' + srcDir,
            dstPath = this.root.fullPath + '/' + dstDir,
            filePath = dstPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var moveDir = function(fileEntry) {
                    // move srcDir to dstDir
                    directory.moveTo(that.root, dstDir, testMove, that.fail);                    
                };
              // create a file within directory
              directory.getFile(file1, {create: true}, moveDir, that.fail);
            },
            testMove = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "new directory entry should not be null");
                equals(directory.isFile, false, "entry 'isFile' attribute should be false");
                equals(directory.isDirectory, true, "entry 'isDirectory' attribute should be true");
                equals(directory.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(directory.name, dstDir, "entry 'name' attribute should be set correctly");
                // test that moved file exists in destination dir
                directory.getFile(file1, {create:false}, testMovedExists, null);
            },
            testMovedExists = function(fileEntry) {
                ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "moved file should exist within moved directory");
                equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
                // test that the moved file no longer exists in original dir
                that.root.getFile(file1, {create:false}, null, testOrig);
            },
            testOrig = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
         
                // cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dstDir, function() {
            // create a new directory entry to kick off test
            that.createDirectory(srcDir, entryCallback, that.fail);
        }, this.fail);
    });
    test("Entry.moveTo: directory to new parent", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "file1",
            srcDir = "entry.move.dnp.srcDir",
            dstDir = "entry.move.dnp.dstDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            dstPath = this.root.fullPath + '/' + dstDir,
            filePath = dstPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var moveDir = function(fileEntry) {
                    // move srcDir to dstDir
                    directory.moveTo(that.root, dstDir, testMove, that.fail);                    
                };
              // create a file within directory
              directory.getFile(file1, {create: true}, moveDir, that.fail);
            },
            testMove = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "new directory entry should not be null");
                equals(directory.isFile, false, "entry 'isFile' attribute should be false");
                equals(directory.isDirectory, true, "entry 'isDirectory' attribute should be true");
                equals(directory.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(directory.name, dstDir, "entry 'name' attribute should be set correctly");
				// test that moved file exists in destination dir
				directory.getFile(file1, {create:false}, testMovedExists, null);
			},
			testMovedExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "moved file should exist within moved directory");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
				// test that the moved file no longer exists in original dir
				that.root.getFile(file1, {create:false}, null, testOrig);
			},
			testOrig = function(error) {
				ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
				
				// cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dstDir, function() {
            // create a new directory entry to kick off test
            that.createDirectory(srcDir, entryCallback, that.fail);
        }, this.fail);
    });
    test("Entry.moveTo: directory onto itself", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(6);
        
        var file1 = "file1",
            srcDir = "entry.move.dos.srcDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            filePath = srcPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var moveDir = function(fileEntry) {
                    // move srcDir onto itself
                    directory.moveTo(that.root, null, null, testMove);                    
                };
              // create a file within new directory
              directory.getFile(file1, {create: true}, moveDir, that.fail);
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to move an entry into its parent if a different name is not specified");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
                
				// test that original dir still exists
				that.root.getDirectory(srcDir, {create:false}, testDirectoryExists, null);
			},
			testDirectoryExists = function(dirEntry) {
				// returning confirms existence so just check fullPath entry
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory should exist.");
				equals(dirEntry.fullPath, srcPath, "entry 'fullPath' should be set correctly");
		 
				dirEntry.getFile(file1, {create:false}, testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
						
				// cleanup
                that.deleteEntry(srcDir);
                QUnit.start();
            };

        // create a new directory entry to kick off test
        this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.moveTo: directory into itself", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var srcDir = "entry.move.dis.srcDir",
            dstDir = "entry.move.dis.dstDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            that = this,
            entryCallback = function(directory) {
                // move source directory into itself
                directory.moveTo(directory, dstDir, null, testMove);                    
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to move a directory into itself");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
				// make sure original directory still exists
				that.root.getDirectory(srcDir, {create:false}, testDirectoryExists, null);
			},
			testDirectoryExists = function(entry) {
				ok(typeof entry !== 'undefined' && entry !== null, "original directory should exist.");
				equals(entry.fullPath, srcPath, "entry 'fullPath' should be set correctly");
		 
				// cleanup
                that.deleteEntry(srcDir);
                QUnit.start();
            };

        // create a new directory entry to kick off test
        this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.moveTo: file onto itself", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);
        
        var file1 = "entry.move.fos.file1",
            filePath = this.root.fullPath + '/' + file1,
            that = this,
            entryCallback = function(entry) {
                // move file1 onto itself
                entry.moveTo(that.root, null, null, testMove);
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to move an entry into its parent if a different name is not specified");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
                
				//test that original file still exists
				that.root.getFile(file1, {create:false}, testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
		 
                // cleanup
                that.deleteEntry(file1);
                QUnit.start();
            };

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.moveTo: file onto existing directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(6);
        
        var file1 = "entry.move.fod.file1",
            dstDir = "entry.move.fod.dstDir",
            subDir = "subDir",
            dirPath = this.root.fullPath + '/' + dstDir + '/' + subDir,
            filePath = this.root.fullPath + '/' + file1,
            that = this,
            entryCallback = function(entry) {
                var createSubDirectory = function(directory) {
                    var moveFile = function(subDirectory) {
						var testMove = function(error) {
							ok(typeof error !== 'undefined' && error !== null, "it is an error to move a file onto an existing directory");
							equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
							// test that original dir still exists
							directory.getDirectory(subDir, {create:false}, testDirectoryExists, null);
						};
                        // move file1 onto sub-directory
                        entry.moveTo(directory, subDir, null, testMove);                    
                    };
                    // create sub-directory 
                    directory.getDirectory(subDir, {create: true}, moveFile, that.fail);                    
                };
                // create top level directory 
                that.root.getDirectory(dstDir, {create: true}, createSubDirectory, that.fail);
            },
			testDirectoryExists = function(dirEntry) {
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory contents should exist.");
				equals(dirEntry.fullPath, dirPath, "entry 'fullPath' should be set correctly");
				// test that original file still exists
				that.root.getFile(file1, {create:false},testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");

                // cleanup
                that.deleteEntry(file1);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dstDir, function() {
            // create a new file entry to kick off test
            that.createFile(file1, entryCallback, that.fail);
        }, this.fail);
    });
    test("Entry.moveTo: directory onto existing file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(6);
        
        var file1 = "entry.move.dof.file1",
            srcDir = "entry.move.dof.srcDir",
            dirPath = this.root.fullPath + '/' + srcDir,
            filePath = this.root.fullPath + '/' + file1,
            that = this,
            entryCallback = function(entry) {
                    var moveDir = function(fileEntry) {
                        // move directory onto file
                        entry.moveTo(that.root, file1, null, testMove);                    
                    };
                // create file
                that.root.getFile(file1, {create: true}, moveDir, that.fail);
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to move a directory onto an existing file");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
                // test that original directory exists
				that.root.getDirectory(srcDir, {create:false}, testDirectoryExists, null);
			},
			testDirectoryExists = function(dirEntry) {
				// returning confirms existence so just check fullPath entry
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory should exist.");
				equals(dirEntry.fullPath, dirPath, "entry 'fullPath' should be set correctly");
				// test that original file exists
				that.root.getFile(file1, {create:false}, testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
		 
		 
                // cleanup
                that.deleteEntry(file1);
                that.deleteEntry(srcDir);
                QUnit.start();
            };

          // create a new directory entry to kick off test
          this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.copyTo: directory onto existing file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(6);
        
        var file1 = "entry.copy.dof.file1",
            srcDir = "entry.copy.dof.srcDir",
            dirPath = this.root.fullPath + '/' + srcDir,
            filePath = this.root.fullPath + '/' + file1,
            that = this,
            entryCallback = function(entry) {
                    var copyDir = function(fileEntry) {
                        // move directory onto file
                        entry.copyTo(that.root, file1, null, testMove);                    
                    };
                // create file
                that.root.getFile(file1, {create: true}, copyDir, that.fail);
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to copy a directory onto an existing file");
                equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
                //test that original dir still exists
				that.root.getDirectory(srcDir, {create:false}, testDirectoryExists, null);
			},
			testDirectoryExists = function(dirEntry) {
				// returning confirms existence so just check fullPath entry
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory should exist.");
				equals(dirEntry.fullPath, dirPath, "entry 'fullPath' should be set correctly");
				// test that original file still exists
				that.root.getFile(file1, {create:false}, testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
		 
                // cleanup
                that.deleteEntry(file1);
                that.deleteEntry(srcDir);
                QUnit.start();
            };

          // create a new directory entry to kick off test
          this.createDirectory(srcDir, entryCallback, this.fail);
    });
    test("Entry.moveTo: directory onto directory that is not empty", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(6);
        
        var srcDir = "entry.move.dod.srcDir",
            dstDir = "entry.move.dod.dstDir",
            subDir = "subDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            dstPath = this.root.fullPath + '/' + dstDir + '/' + subDir,
            that = this,
            entryCallback = function(entry) {
                var createSubDirectory = function(directory) {
                    var moveDir = function(subDirectory) {
                        // move srcDir onto dstDir (not empty)
                        entry.moveTo(that.root, dstDir, null, testMove);                    
                    };
					var testMove = function(error) {
						ok(typeof error !== 'undefined' && error !== null, "it is an error to move a directory onto a directory that is not empty");
						equal(error.code, FileError.INVALID_MODIFICATION_ERR, "error code should be FileError.INVALID_MODIFICATION_ERR");
		 
						// test that destination directory still exists
						directory.getDirectory(subDir, {create:false}, testDirectoryExists, null);
					};
                    // create sub-directory 
                    directory.getDirectory(subDir, {create: true}, moveDir, that.fail);                    
                };
                // create top level directory 
                that.root.getDirectory(dstDir, {create: true}, createSubDirectory, that.fail);
            },
			testDirectoryExists = function(dirEntry) {
				// returning confirms existence so just check fullPath entry
				ok(typeof dirEntry !== 'undefined' && dirEntry !== null, "original directory should exist.");
				equals(dirEntry.fullPath, dstPath, "entry 'fullPath' should be set correctly");
				// test that source directory exists
				that.root.getDirectory(srcDir,{create:false}, testSrcDirectoryExists, null);
			},
			testSrcDirectoryExists = function(srcEntry){
				ok(typeof srcEntry !== 'undefined' && srcEntry !== null, "original directory should exist.");
				equals(srcEntry.fullPath, srcPath, "entry 'fullPath' should be set correctly");
                // cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dstDir, function() {
            // create a new file entry to kick off test
            that.createDirectory(srcDir, entryCallback, that.fail);
        }, this.fail);
    });
    test("Entry.moveTo: file replace existing file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "entry.move.frf.file1",
            file2 = "entry.move.frf.file2",
            file1Path = this.root.fullPath + '/' + file1,
            file2Path = this.root.fullPath + '/' + file2,
            that = this,
            entryCallback = function(entry) {
                    var moveFile = function(fileEntry) {
                        // replace file2 with file1
                        entry.moveTo(that.root, file2, testMove, that.fail);                    
                    };
                // create file
                that.root.getFile(file2, {create: true}, moveFile, that.fail);
            },
		 testMove = function(entry) {
                ok(typeof entry !== 'undefined' && entry !== null, "file entry should not be null")
                equals(entry.isFile, true, "entry 'isFile' attribute should be true");
                equals(entry.isDirectory, false, "entry 'isDirectory' attribute should be false");
                equals(entry.fullPath, file2Path, "entry 'fullPath' should be set correctly");
                equals(entry.name, file2, "entry 'name' attribute should be set correctly");
                
				// test that old file does not exists
				that.root.getFile(file1, {create:false}, null, testFileMoved);
			},
		 testFileMoved = function(error){
				ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
				// test that new file exists
				that.root.getFile(file2, {create:false}, testFileExists, null);
			},
		 testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, file2Path, "entry 'fullPath' should be set correctly");
		 
                // cleanup
                that.deleteEntry(file1);
                that.deleteEntry(file2);
                QUnit.start();
            };

          // create a new directory entry to kick off test
          this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.moveTo: directory replace empty directory", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(9);
        
        var file1 = "file1",
            srcDir = "entry.move.drd.srcDir",
            dstDir = "entry.move.drd.dstDir",
            srcPath = this.root.fullPath + '/' + srcDir,
            dstPath = this.root.fullPath + '/' + dstDir,
            filePath = dstPath + '/' + file1,
            that = this,
            entryCallback = function(directory) {
                var mkdir = function(fileEntry) {
                    // create destination directory
                    that.root.getDirectory(dstDir, {create: true}, moveDir, that.fail);
                };
                var moveDir = function(fileEntry) {
                    // move srcDir to dstDir
                    directory.moveTo(that.root, dstDir, testMove, that.fail);                    
                };
              // create a file within source directory
              directory.getFile(file1, {create: true}, mkdir, that.fail);
            },
            testMove = function(directory) {
                ok(typeof directory !== 'undefined' && directory !== null, "new directory entry should not be null");
                equals(directory.isFile, false, "entry 'isFile' attribute should be false");
                equals(directory.isDirectory, true, "entry 'isDirectory' attribute should be true");
                equals(directory.fullPath, dstPath, "entry 'fullPath' should be set correctly");
                equals(directory.name, dstDir, "entry 'name' attribute should be set correctly");
                // test that old directory contents have been moved
				directory.getFile(file1, {create:false}, testFileExists, null);
			},
			testFileExists = function(fileEntry) {
				ok(typeof fileEntry !== 'undefined' && fileEntry !== null, "original directory contents should exist.");
				equals(fileEntry.fullPath, filePath, "entry 'fullPath' should be set correctly");
		 
                // test that old directory no longer exists
				that.root.getDirectory(srcDir, {create:false}, null, testRemoved);
			},
			testRemoved = function(error){
				ok(typeof error !== 'undefined' && error !== null, "it is an error if original file exists after a move");
				equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
                
                // cleanup
                that.deleteEntry(srcDir);
                that.deleteEntry(dstDir);
                QUnit.start();
            };

        // ensure destination directory is cleaned up before test
        this.deleteEntry(dstDir, function() {
            // create a new directory entry to kick off test
            that.createDirectory(srcDir, entryCallback, that.fail);
        }, this.fail);
    });
    test("Entry.moveTo: directory that does not exist", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var file1 = "entry.move.dnf.file1", 
            dstDir = "entry.move.dnf.dstDir",
            filePath = this.root.fullPath + '/' + file1,
            dstPath = this.root.fullPath + '/' + dstDir,
            that = this,
            entryCallback = function(entry) {
                // move file to directory that does not exist
                directory = new DirectoryEntry();
				directory.fullPath = dstPath;
                entry.moveTo(directory, null, null, testMove);                 
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "it is an error to move to a directory that does not exist");
                equal(error.code, FileError.NOT_FOUND_ERR, "error code should be FileError.NOT_FOUND_ERR");
				
				// cleanup
                that.deleteEntry(file1);
                QUnit.start();
            };

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    test("Entry.moveTo: invalid target name", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var file1 = "entry.move.itn.file1",
            file2 = "bad:file:name",
            that = this,
            filePath = this.root.fullPath + '/' + file1,
            entryCallback = function(entry) {
                // move file1 to file2
                entry.moveTo(that.root, file2, null, testMove);
            },
            testMove = function(error) {
                ok(typeof error !== 'undefined' && error !== null, "invalid file name should result in error");
                equal(error.code, FileError.ENCODING_ERR, "error code should be FileError.ENCODING_ERR");

                // cleanup
                that.deleteEntry(file1);
                QUnit.start();
            };

        // create a new file entry to kick off test
        this.createFile(file1, entryCallback, this.fail);
    });
    module('FileReader model');
    test("FileReader object should have correct methods", function() {
        expect(6);
        var reader = new FileReader();
        ok(reader !== null, "new FileReader() should not be null.");
        ok(typeof reader.readAsBinaryString === 'function', "FileReader object should have a readAsBinaryString function.");
        ok(typeof reader.readAsDataURL === 'function', "FileReader object should have a readAsDataURL function.");
        ok(typeof reader.readAsText === 'function', "FileReader object should have a readAsText function.");
        ok(typeof reader.readAsArrayBuffer === 'function', "FileReader object should have a readAsArrayBuffer function.");
        ok(typeof reader.abort === 'function', "FileReader object should have an abort function.");
    });
    module('FileReader read', {
        setup: function() {
            this.root = getFileSystemRoot(); 
            this.fail = function(error) {
                console.log('file error: ' + error.code);
            };            
        }
    });
    test("should read file properly, File object", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(1);
            
            // path of file
        var fileName = "reader.txt",
            filePath = this.root.fullPath + '/' + fileName;
            // file content
            rule = "There is an exception to every rule.  Except this one.",
            // creates a FileWriter object
            create_writer = function(fileEntry) {
                fileEntry.createWriter(write_file, this.fail);
            },
            // writes file and reads it back in
            write_file = function(writer) {
                writer.onwriteend = read_file; 
                writer.write(rule);
            },
            // reads file and compares content to what was written
            read_file = function(evt) {
                var reader = new FileReader();
                reader.onloadend = function(evt) {
                    console.log("read success");
                    console.log(evt.target.result);
                    ok(evt.target.result === rule, "reader.result should be equal to the text written.");
                    QUnit.start();
                };
                var myFile = new File();
                myFile.fullPath = filePath; 
                reader.readAsText(myFile);
            };
        
        // create a file, write to it, and read it in again
        this.root.getFile(fileName, {create: true}, create_writer, this.fail);
    });
    test("should read empty file properly", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(1);
            
            // path of file
        var fileName = "empty.txt",
            filePath = this.root.fullPath + '/' + fileName;
            // file content
            rule = "",
            // reads file and compares content to what was written
            read_file = function(evt) {
                var reader = new FileReader();
                reader.onloadend = function(evt) {
                    console.log("read success");
                    console.log(evt.target.result);
                    ok(evt.target.result === rule, "reader.result should be equal to the empty string.");
                    QUnit.start();
                };
                var myFile = new File();
                myFile.fullPath = filePath; 
                reader.readAsText(myFile);
            };
        
        // create a file, write to it, and read it in again
        this.root.getFile(fileName, {create: true}, read_file, this.fail);
    });
    test("should error out on non-existent file", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(1);
            
        var reader = new FileReader();
		reader.onerror = function(evt) {
			console.log("Properly got a file error as no file exists.");
			ok(evt.target.error.code === 1, "Should throw a NOT_FOUND_ERR.");
			QUnit.start();
		}
        var myFile = new File();
        myFile.fullPath = this.root.fullPath + '/' + "doesnotexist.err"; 
        reader.readAsText(myFile);
    });
    test("should read file properly, Data URL", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(1);
            
            // path of file
        var fileName = "reader.txt",
            filePath = this.root.fullPath + '/' + fileName;
            // file content
            rule = "There is an exception to every rule.  Except this one.",
            // creates a FileWriter object
            create_writer = function(fileEntry) {
                fileEntry.createWriter(write_file, this.fail);
            },
            // writes file and reads it back in
            write_file = function(writer) {
                writer.onwriteend = read_file; 
                writer.write(rule);
            },
            // reads file and compares content to what was written
            read_file = function(evt) {
                var reader = new FileReader();
                reader.onloadend = function(evt) {
                    console.log("read success");
                    console.log(evt.target.result);
                    ok(evt.target.result.substr(0,23) === "data:text/plain;base64,", "reader.result should be base64 encoded.");
                    QUnit.start();
                };
                var myFile = new File();
                myFile.fullPath = filePath; 
                reader.readAsDataURL(myFile);
            };
        
        // create a file, write to it, and read it in again
        this.root.getFile(fileName, {create: true}, create_writer, this.fail);
    });
    module('FileWriter model', {
        // setup function will run before each test
        setup: function() {
            var that = this;
            this.root = getFileSystemRoot(); 
            this.fail = function(error) {
                console.log('file error: ' + error.code);
            };
            // deletes file, if it exists, then invokes callback
            this.deleteFile = function(fileName, callback) {
                that.root.getFile(fileName, null, 
                        // remove file system entry
                        function(entry) {
                            entry.remove(callback, that.fail); 
                        },
                        // doesn't exist
                        callback);
            };
            // deletes and re-creates the specified file, then invokes callback
            this.createFile = function(fileName, callback) {
                // creates file
                var create_file = function() {
                    that.root.getFile(fileName, {create: true}, callback, that.fail);                
                };
                
                // deletes file, then re-creates it
                that.deleteFile(fileName, create_file);
            };
        }
    });    
    test("FileWriter object should have correct methods", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        // retrieve a FileWriter object
        var fileName = "writer.methods",
            that = this,
            test_writer = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    ok(typeof writer !== 'undefined' && writer !== null, "FileEntry.createWriter should return a FileWriter object.");
                    ok(typeof writer.write === 'function', "FileWriter object should have a write function.");
                    ok(typeof writer.seek === 'function', "FileWriter object should have a seek function.");
                    ok(typeof writer.truncate === 'function', "FileWriter object should have a truncate function.");
                    ok(typeof writer.abort === 'function', "FileWriter object should have an abort function.");

                    // cleanup
                    that.deleteFile(fileName);
                    QUnit.start();
                }, this.fail);
            };

        // test FileWriter
        this.root.getFile(fileName, {create: true}, test_writer, this.fail);                        
    });
    test("should be able to write and append to file, createWriter", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);

        var that = this,
            fileName = "writer.append",
            filePath = this.root.fullPath + '/' + fileName,
            // file content
            rule = "There is an exception to every rule.",
            // for testing file length
            length = rule.length,
            // writes initial file content
            write_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
                        ok(writer.length === length, "should have written " + length + " bytes");
                        ok(writer.position === length, "position should be at " + length);
                        append_file(writer);
                    };
                    writer.write(rule); 
                }, that.fail);
            }, 
            // appends to file
            append_file = function(writer) {
                var exception = "  Except this one.";            
                writer.onwriteend = function(evt) {
                    ok(writer.length === length, "file length should be " + length);
                    ok(writer.position === length, "position should be at " + length);

                    // cleanup
                    that.deleteFile(fileName);
                    QUnit.start();
                };
                length += exception.length;
                writer.seek(writer.length);
                writer.write(exception); 
            };
        
        // create file, then write and append to it
        this.createFile(fileName, write_file);
    });
    test("should be able to write and append to file, File object", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);

        var that = this,
            fileName = "writer.append",
            filePath = this.root.fullPath + '/' + fileName,
            // file content
            rule = "There is an exception to every rule.",
            // for testing file length
            length = rule.length,
            // writes initial file content
            write_file = function(file) {
                var writer = new FileWriter(file);
                    writer.onwriteend = function(evt) {
                        ok(writer.length === length, "should have written " + length + " bytes");
                        ok(writer.position === length, "position should be at " + length);
                        append_file(writer);
                    };
                    writer.write(rule); 
            }, 
            // appends to file
            append_file = function(writer) {
                var exception = "  Except this one.";            
                writer.onwriteend = function(evt) {
                    ok(writer.length === length, "file length should be " + length);
                    ok(writer.position === length, "position should be at " + length);

                    // cleanup
                    that.deleteFile(fileName);
                    QUnit.start();
                };
                length += exception.length;
                writer.seek(writer.length);
                writer.write(exception); 
            };
        
        // create file, then write and append to it
		var file = new File();
		file.fullPath = filePath;
        write_file(file);
    });
    test("should be able to seek to the middle of the file and write more data than file.length", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);

        var that = this,
            fileName = "writer.seek.write",
            filePath = this.root.fullPath + '/' + fileName,
            // file content
            rule = "This is our sentence.",
            // for testing file length
            length = rule.length,
            // writes initial file content
            write_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
                        ok(writer.length === length, "should have written " + length + " bytes");
                        ok(writer.position === length, "position should be at " + length);
                        append_file(writer);
                    };
                    writer.write(rule); 
                }, that.fail);
            }, 
            // appends to file
            append_file = function(writer) {
                var exception = "newer sentence.";            
                writer.onwriteend = function(evt) {
                    ok(writer.length === length, "file length should be " + length);
                    ok(writer.position === length, "position should be at " + length);

                    // cleanup
                    that.deleteFile(fileName);
                    QUnit.start();
                };
                length = 12 + exception.length;
                writer.seek(12);
                writer.write(exception); 
            };
        
        // create file, then write and append to it
        this.createFile(fileName, write_file);
    });
    test("should be able to seek to the middle of the file and write less data than file.length", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(4);

        var that = this,
            fileName = "writer.seek.write2",
            filePath = this.root.fullPath + '/' + fileName,
            // file content
            rule = "This is our sentence.",
            // for testing file length
            length = rule.length,
            // writes initial file content
            write_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
                        ok(writer.length === length, "should have written " + length + " bytes");
                        ok(writer.position === length, "position should be at " + length);
                        append_file(writer);
                    };
                    writer.write(rule); 
                }, that.fail);
            }, 
            // appends to file
            append_file = function(writer) {
                var exception = "new.";            
                writer.onwriteend = function(evt) {
                    ok(writer.length === length, "file length should be " + length);
                    ok(writer.position === length, "position should be at " + length);

                    // cleanup
                    that.deleteFile(fileName);
                    QUnit.start();
                };
                length = 8 + exception.length;
                writer.seek(8);
                writer.write(exception); 
            };
        
        // create file, then write and append to it
        this.createFile(fileName, write_file);
    });
    test("should be able to write XML data", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);

        var that = this,
            fileName = "writer.xml",
            filePath = this.root.fullPath + '/' + fileName,
            // file content
            rule = '<?xml version="1.0" encoding="UTF-8"?>\n<test prop="ack">\nData\n</test>\n',
            // for testing file length
            length = rule.length,
            // writes file content
            write_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
						ok(writer.length === length, "should have written " + length + " bytes");
                        ok(writer.position === length, "position should be at " + length);

                        // cleanup
                        that.deleteFile(fileName);
                        QUnit.start();
                    };
                    writer.write(rule); 
                }, that.fail);
            };
            
        // creates file, then write XML data
        this.createFile(fileName, write_file);
    });
    test("should be able to write JSON data", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);

        var that = this,
            fileName = "writer.json",
            filePath = this.root.fullPath + '/' + fileName,
            // file content
            rule = '{ "name": "Guy Incognito", "email": "here@there.com" }',
            // for testing file length
            length = rule.length,
            // writes file content
            write_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
                        ok(writer.length === length, "should have written " + length + " bytes");
                        ok(writer.position === length, "position should be at " + length);

                        // cleanup
                        that.deleteFile(fileName);
                        QUnit.start();
                    };
                    writer.write(rule); 
                }, that.fail);
            };
        
        // creates file, then write JSON content
        this.createFile(fileName, write_file);
    });
	test("should write and read special characters", function() {
		 QUnit.stop(Tests.TEST_TIMEOUT);
		 expect(1);
		 
		 var that = this,
			// path of file
			fileName = "reader.txt",
			filePath = this.root.fullPath + '/' + fileName,
			// file content
			rule = "H\u00EBll\u00F5 Euro \u20AC\u00A1",
			// creates a FileWriter object
			create_writer = function(fileEntry) {
				fileEntry.createWriter(write_file, this.fail);
			},
			// writes file and reads it back in
			write_file = function(writer) {
				writer.onwriteend = read_file; 
				writer.write(rule);
			},
			// reads file and compares content to what was written
			read_file = function(evt) {
				var reader = new FileReader();
					reader.onloadend = function(evt) {
					console.log("read success");
					console.log(evt.target.result);
					ok(evt.target.result === rule, "reader.result should be equal to the text written.");
					// cleanup
					that.deleteFile(fileName);
					QUnit.start();
				};
				var myFile = new File();
				myFile.fullPath = filePath; 
				reader.readAsText(myFile);
			};
		 
		 // create a file, write to it, and read it in again
		 this.createFile(fileName, create_writer, this.fail);
		 });
    test("should be able to seek", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(5);
        
        var that = this,
            fileName = "writer.seek",
            // file content
            rule = "There is an exception to every rule.  Except this one.",
            // for testing file length
            length = rule.length,
            // writes file content and tests writer.seek
            seek_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
                        ok(writer.position == length, "position should be at " + length); 
                        writer.seek(-5);
                        ok(writer.position == (length-5), "position should be at " + (length-5)); 
                        writer.seek(100);
                        ok(writer.position == length, "position should be at " + length); 
                        writer.seek(10);
                        ok(writer.position == 10, "position should be at 10"); 

                        // cleanup
                        that.deleteFile(fileName);
                        QUnit.start();
                    };
                    writer.seek(-100);
                    ok(writer.position == 0, "position should be at 0");        
                    writer.write(rule);
                }, that.fail);
            };
            
        // creates file, then write JSON content
        this.createFile(fileName, seek_file);
    });
    test("should be able to truncate", function() {
        QUnit.stop(Tests.TEST_TIMEOUT);
        expect(2);
        
        var that = this,
            fileName = "writer.truncate",
            rule = "There is an exception to every rule.  Except this one.",
            // writes file content 
            write_file = function(fileEntry) {
                fileEntry.createWriter(function(writer) {
                    writer.onwriteend = function(evt) {
                        truncate_file(writer);
                    }; 
                    writer.write(rule);
                }, that.fail);
            },
            // and tests writer.truncate
            truncate_file = function(writer) {
                writer.onwriteend = function(evt) {
                    ok(writer.length == 36, "file length should be 36");
                    ok(writer.position == 36, "position should be at 36");  
                    
                    // cleanup
                    that.deleteFile(fileName);
                    QUnit.start();
                }; 
                writer.truncate(36);                
            };

        // creates file, writes to it, then truncates it
        this.createFile(fileName, write_file);
    });
};
