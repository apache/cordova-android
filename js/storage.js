/*
 * This is purely for the Android 1.5/1.6 HTML 5 Storage
 * I was hoping that Android 2.0 would deprecate this, but given the fact that 
 * most manufacturers ship with Android 1.5 and do not do OTA Updates, this is required
 */

var droiddb = new function()
{
  this.txQueue = [];
}

droiddb.prototype.addResult(rawdata, tx_id)
{
  eval("var data = " + rawdata);
  var tx = this.txQueue(tx_id);
  tx.resultSet.push(data);
}

droiddb.prototype.completeQuery(tx_id)
{
  var tx = this.txQueue(tx_id);
  var r = new result();
  r.rows.resultSet = tx.resultSet;
  r.rows.length = resultSet.length;
  tx.win(r);
}

var DatabaseShell = function()
{
  
}

DatabaseShell.transaction(process)
{
  tx = new Tx();
  process(tx);
}

var Tx = function()
{
  droiddb.txQueue.push(this);
  this.id = droiddb.txQueue.length - 1;
  this.resultSet = [];
}

Tx.prototype.executeSql = function(query, params, win, fail)
{
  droidStorage.executeSql(query, params, tx_id);
  tx.win = win;
  tx.fail = fail;
}

var result = function()
{
  this.rows = new Rows();
}

var Rows = function()
{
  this.resultSet = [];
  this.length = 0;
}

Rows.prototype.item = function(row_id)
{
  return this.resultSet[id];
}

PhoneGap.addConstructor(function() {
  if (typeof navigator.openDatabase == "undefined") {
    var openDatabase = function(name, version, display_name, size)
    {
      droidStorage.openDatabase(name, version, display_name, size)
      db_object = new DatabaseShell();
    }
  }
});
