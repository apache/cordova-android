/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

/*
 * This is purely for the Android 1.5/1.6 HTML 5 Storage
 * I was hoping that Android 2.0 would deprecate this, but given the fact that
 * most manufacturers ship with Android 1.5 and do not do OTA Updates, this is required
 */

/**
 * Storage object that is called by native code when performing queries.
 * PRIVATE METHOD
 */
var DroidDB = function() {
    this.queryQueue = {};
};

/**
 * Callback from native code when query is complete.
 * PRIVATE METHOD
 *
 * @param id                Query id
 */
DroidDB.prototype.completeQuery = function(id, data) {
    var query = this.queryQueue[id];
    if (query) {
        try {
            delete this.queryQueue[id];

            // Get transaction
            var tx = query.tx;

            // If transaction hasn't failed
            // Note: We ignore all query results if previous query
            //       in the same transaction failed.
            if (tx && tx.queryList[id]) {

                // Save query results
                var r = new DroidDB_Result();
                r.rows.resultSet = data;
                r.rows.length = data.length;
                try {
                    if (typeof query.successCallback === 'function') {
                        query.successCallback(query.tx, r);
                    }
                } catch (ex) {
                    console.log("executeSql error calling user success callback: "+ex);
                }

                tx.queryComplete(id);
            }
        } catch (e) {
            console.log("executeSql error: "+e);
        }
    }
};

/**
 * Callback from native code when query fails
 * PRIVATE METHOD
 *
 * @param reason            Error message
 * @param id                Query id
 */
DroidDB.prototype.fail = function(reason, id) {
    var query = this.queryQueue[id];
    if (query) {
        try {
            delete this.queryQueue[id];

            // Get transaction
            var tx = query.tx;

            // If transaction hasn't failed
            // Note: We ignore all query results if previous query
            //       in the same transaction failed.
            if (tx && tx.queryList[id]) {
                tx.queryList = {};

                try {
                    if (typeof query.errorCallback === 'function') {
                        query.errorCallback(query.tx, reason);
                    }
                } catch (ex) {
                    console.log("executeSql error calling user error callback: "+ex);
                }

                tx.queryFailed(id, reason);
            }

        } catch (e) {
            console.log("executeSql error: "+e);
        }
    }
};

/**
 * Transaction object
 * PRIVATE METHOD
 */
var DroidDB_Tx = function() {

    // Set the id of the transaction
    this.id = PhoneGap.createUUID();

    // Callbacks
    this.successCallback = null;
    this.errorCallback = null;

    // Query list
    this.queryList = {};
};


var DatabaseShell = function() {
};

/**
 * Start a transaction.
 * Does not support rollback in event of failure.
 *
 * @param process {Function}            The transaction function
 * @param successCallback {Function}
 * @param errorCallback {Function}
 */
DatabaseShell.prototype.transaction = function(process, errorCallback, successCallback) {
    var tx = new DroidDB_Tx();
    tx.successCallback = successCallback;
    tx.errorCallback = errorCallback;
    try {
        process(tx);
    } catch (e) {
        console.log("Transaction error: "+e);
        if (tx.errorCallback) {
            try {
                tx.errorCallback(e);
            } catch (ex) {
                console.log("Transaction error calling user error callback: "+e);
            }
        }
    }
};


/**
 * Mark query in transaction as complete.
 * If all queries are complete, call the user's transaction success callback.
 *
 * @param id                Query id
 */
DroidDB_Tx.prototype.queryComplete = function(id) {
    delete this.queryList[id];

    // If no more outstanding queries, then fire transaction success
    if (this.successCallback) {
        var count = 0;
        var i;
        for (i in this.queryList) {
            if (this.queryList.hasOwnProperty(i)) {
                count++;   
            }
        }
        if (count === 0) {
            try {
                this.successCallback();
            } catch(e) {
                console.log("Transaction error calling user success callback: " + e);
            }
        }
    }
};

/**
 * Mark query in transaction as failed.
 *
 * @param id                Query id
 * @param reason            Error message
 */
DroidDB_Tx.prototype.queryFailed = function(id, reason) {

    // The sql queries in this transaction have already been run, since
    // we really don't have a real transaction implemented in native code.
    // However, the user callbacks for the remaining sql queries in transaction
    // will not be called.
    this.queryList = {};

    if (this.errorCallback) {
        try {
            this.errorCallback(reason);
        } catch(e) {
            console.log("Transaction error calling user error callback: " + e);
        }
    }
};

/**
 * SQL query object
 * PRIVATE METHOD
 *
 * @param tx                The transaction object that this query belongs to
 */
var DroidDB_Query = function(tx) {

    // Set the id of the query
    this.id = PhoneGap.createUUID();

    // Add this query to the queue
    droiddb.queryQueue[this.id] = this;

    // Init result
    this.resultSet = [];

    // Set transaction that this query belongs to
    this.tx = tx;

    // Add this query to transaction list
    this.tx.queryList[this.id] = this;

    // Callbacks
    this.successCallback = null;
    this.errorCallback = null;

};

/**
 * Execute SQL statement
 *
 * @param sql                   SQL statement to execute
 * @param params                Statement parameters
 * @param successCallback       Success callback
 * @param errorCallback         Error callback
 */
DroidDB_Tx.prototype.executeSql = function(sql, params, successCallback, errorCallback) {

    // Init params array
    if (typeof params === 'undefined') {
        params = [];
    }

    // Create query and add to queue
    var query = new DroidDB_Query(this);
    droiddb.queryQueue[query.id] = query;

    // Save callbacks
    query.successCallback = successCallback;
    query.errorCallback = errorCallback;

    // Call native code
    PhoneGap.exec(null, null, "Storage", "executeSql", [sql, params, query.id]);
};

/**
 * SQL result set that is returned to user.
 * PRIVATE METHOD
 */
DroidDB_Result = function() {
    this.rows = new DroidDB_Rows();
};

/**
 * SQL result set object
 * PRIVATE METHOD
 */
DroidDB_Rows = function() {
    this.resultSet = [];    // results array
    this.length = 0;        // number of rows
};

/**
 * Get item from SQL result set
 *
 * @param row           The row number to return
 * @return              The row object
 */
DroidDB_Rows.prototype.item = function(row) {
    return this.resultSet[row];
};

/**
 * Open database
 *
 * @param name              Database name
 * @param version           Database version
 * @param display_name      Database display name
 * @param size              Database size in bytes
 * @return                  Database object
 */
DroidDB_openDatabase = function(name, version, display_name, size) {
    PhoneGap.exec(null, null, "Storage", "openDatabase", [name, version, display_name, size]);
    var db = new DatabaseShell();
    return db;
};


/**
 * For browsers with no localStorage we emulate it with SQLite. Follows the w3c api. 
 * TODO: Do similar for sessionStorage. 
 */

var CupcakeLocalStorage = function() {
		try {

			this.db = openDatabase('localStorage', '1.0', 'localStorage', 2621440);	
			var storage = {};
			this.db.transaction(
				function (transaction) {
				    var i;
					transaction.executeSql('CREATE TABLE IF NOT EXISTS storage (id NVARCHAR(40) PRIMARY KEY, body NVARCHAR(255))');
					transaction.executeSql('SELECT * FROM storage', [], function(tx, result) {
	                    for(i = 0; i < result.rows.length; i++) {
							storage[result.rows.item(i).id] =  result.rows.item(i).body;
						}
	                    PhoneGap.initializationComplete("cupcakeStorage");
					});
					
				}, 
				function (err) {
					alert(err.message);
				}
			);
			this.setItem = function(key, val) {
				//console.log('set');
				storage[key] = val;
				
				this.db.transaction(
					function (transaction) {
						transaction.executeSql('CREATE TABLE IF NOT EXISTS storage (id NVARCHAR(40) PRIMARY KEY, body NVARCHAR(255))');
						
						transaction.executeSql('REPLACE INTO storage (id, body) values(?,?)', [key,val]);
					}
				);
			};
			this.getItem = function(key) {			
				return storage[key];
			};
			this.removeItem = function(key) {
				delete storage[key];
				this.db.transaction(
					function (transaction) {
						transaction.executeSql('CREATE TABLE IF NOT EXISTS storage (id NVARCHAR(40) PRIMARY KEY, body NVARCHAR(255))');
						
						transaction.executeSql('DELETE FROM storage where id=?', [key]);
					}
				);
			};
			this.clear = function() {
				storage = {};
				this.db.transaction(
					function (transaction) {
						transaction.executeSql('CREATE TABLE IF NOT EXISTS storage (id NVARCHAR(40) PRIMARY KEY, body NVARCHAR(255))');
						
						transaction.executeSql('DELETE FROM storage', []);
					}
				);
			};				
		} catch(e) {
			alert("Database error "+e+".");
		    return;
		}
};
PhoneGap.addConstructor(function() {
	if (typeof window.openDatabase === "undefined") {
        navigator.openDatabase = window.openDatabase = DroidDB_openDatabase;
        window.droiddb = new DroidDB();
    }
    
    if (typeof window.localStorage === "undefined") {
        navigator.localStorage = window.localStorage = new CupcakeLocalStorage();
        PhoneGap.waitForInitialization("cupcakeStorage");
    }
});

