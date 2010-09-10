
/*
 * This is purely for the Android 1.5/1.6 HTML 5 Storage
 * I was hoping that Android 2.0 would deprecate this, but given the fact that
 * most manufacturers ship with Android 1.5 and do not do OTA Updates, this is required
 */

var DroidDB = function() {
    this.txQueue = [];
};

DroidDB.prototype.addResult = function(rawdata, tx_id) {
    eval("var data = " + rawdata);
    var tx = this.txQueue[tx_id];
    tx.resultSet.push(data);
};

DroidDB.prototype.completeQuery = function(tx_id) {
    var tx = this.txQueue[tx_id];
    var r = new result();
    r.rows.resultSet = tx.resultSet;
    r.rows.length = tx.resultSet.length;
    tx.win(r);
};

DroidDB.prototype.fail = function(reason, tx_id) {
    var tx = this.txQueue[tx_id];
    tx.fail(reason);
};

var DatabaseShell = function() {
};

DatabaseShell.prototype.transaction = function(process) {
    tx = new Tx();
    process(tx);
};

var Tx = function() {
    droiddb.txQueue.push(this);
    this.id = droiddb.txQueue.length - 1;
    this.resultSet = [];
};

Tx.prototype.executeSql = function(query, params, win, fail) {
    PhoneGap.execAsync(null, null, "Storage", "executeSql", [query, params, this.id]);
    tx.win = win;
    tx.fail = fail;
};

var result = function() {
    this.rows = new Rows();
};

var Rows = function() {
    this.resultSet = [];
    this.length = 0;
};

Rows.prototype.item = function(row_id) {
    return this.resultSet[id];
};

var dbSetup = function(name, version, display_name, size) {
    PhoneGap.execAsync(null, null, "Storage", "openDatabase", [name, version, display_name, size]);
    db_object = new DatabaseShell();
    return db_object;
};

PhoneGap.addConstructor(function() {
    if (typeof window.openDatabase == "undefined") {
        navigator.openDatabase = window.openDatabase = dbSetup;
        window.droiddb = new DroidDB();
    }
});
