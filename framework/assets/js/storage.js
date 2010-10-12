
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
    this.txQueue = {};
};

/**
 * Callback from native code when result from a query is available.
 * PRIVATE METHOD
 *
 * @param rawdata           JSON string of the row data
 * @param tx_id             Transaction id
 */
DroidDB.prototype.addResult = function(rawdata, tx_id) {
    try {
        eval("var data = " + rawdata + ";");
        var tx = this.txQueue[tx_id];
        tx.resultSet.push(data);
    } catch (e) {
        console.log("DroidDB.addResult(): Error="+e);
    }
};

/**
 * Callback from native code when query is complete.
 * PRIVATE METHOD
 *
 * @param tx_id
 */
DroidDB.prototype.completeQuery = function(tx_id) {
    var tx = null;
    try {
        tx = this.txQueue[tx_id];
        var r = new DroidDB_Result();
        r.rows.resultSet = tx.resultSet;
        r.rows.length = tx.resultSet.length;
        delete this.txQueue[tx_id];
    } catch (e) {
        console.log("DroidDB.completeQuery(): Error="+e);
    }
    try {
        tx.successCallback(tx, r);
    } catch (e) {
        console.log("DroidDB.completeQuery(): Error calling user success callback="+e);
    }
};

/**
 * Callback from native code when query fails
 * PRIVATE METHOD
 *
 * @param reason
 * @param tx_id
 */
DroidDB.prototype.fail = function(reason, tx_id) {
    var tx = null;
    try {
        tx = this.txQueue[tx_id];
        delete this.txQueue[tx_id];
    } catch (e) {
        console.log("DroidDB.fail(): Error="+e);
    }
    try {
        tx.errorCallback(reason);
    } catch (e) {
        console.log("DroidDB.fail(): Error calling user error callback="+e);
    }
};

var DatabaseShell = function() {
};

/**
 * Start a transaction.
 *
 * @param process {Function}        The transaction function
 */
DatabaseShell.prototype.transaction = function(process) {
    var tx = new DroidDB_Tx();
    process(tx);
};

/**
 * Transaction object
 * PRIVATE METHOD
 */
var DroidDB_Tx = function() {

    // Set the id of the transaction
    this.id = PhoneGap.createUUID();

    // Add this transaction to the queue
    droiddb.txQueue[this.id] = this;

    // Init result
    this.resultSet = [];
};

/**
 * Execute SQL statement
 *
 * @param query
 * @param params
 * @param successCallback
 * @param errorCallback
 */
DroidDB_Tx.prototype.executeSql = function(query, params, successCallback, errorCallback) {

    // Init params array
    if (typeof params == 'undefined') {
        params = [];
    }

    // Save callbacks
    var tx = droiddb.txQueue[this.id];
    tx.successCallback = successCallback;
    tx.errorCallback = errorCallback;

    // Call native code
    PhoneGap.execAsync(null, null, "Storage", "executeSql", [query, params, this.id]);
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
    PhoneGap.execAsync(null, null, "Storage", "openDatabase", [name, version, display_name, size]);
    var db = new DatabaseShell();
    return db;
};

PhoneGap.addConstructor(function() {
    if (typeof window.openDatabase == "undefined") {
        navigator.openDatabase = window.openDatabase = DroidDB_openDatabase;
        window.droiddb = new DroidDB();
    }
});
