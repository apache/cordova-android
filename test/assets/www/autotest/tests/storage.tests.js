Tests.prototype.StorageTests = function() 
{
  module("Session Storage");
  test("should exist", function() {
	expect(7);
	ok(window.sessionStorage != null, "sessionStorage is defined");
    ok(typeof window.sessionStorage.length != 'undefined', "sessionStorage.length is defined");
    ok(typeof(window.sessionStorage.key) == "function", "sessionStorage.key is defined");
    ok(typeof(window.sessionStorage.getItem) == "function", "sessionStorage.getItem is defined");
    ok(typeof(window.sessionStorage.setItem) == "function", "sessionStorage.setItem is defined");
    ok(typeof(window.sessionStorage.removeItem) == "function", "sessionStorage.removeItem is defined");
    ok(typeof(window.sessionStorage.clear) == "function", "sessionStorage.clear is defined");
  });
  test("check length", function() {
    expect(3);
    ok(window.sessionStorage.length == 0, "length should be 0");
    window.sessionStorage.setItem("key","value");
    ok(window.sessionStorage.length == 1, "length should be 1");
    window.sessionStorage.removeItem("key");   
    ok(window.sessionStorage.length == 0, "length should be 0");
  });
  test("check key", function() {
	expect(3);
	ok(window.sessionStorage.key(0) == null, "key should be null");
	window.sessionStorage.setItem("test","value");
	ok(window.sessionStorage.key(0) == "test", "key should be 'test'");
	window.sessionStorage.removeItem("test");   
	ok(window.sessionStorage.key(0) == null, "key should be null");
  });
  test("check getItem", function() {
	expect(3);
    ok(window.sessionStorage.getItem("item") == null, "item should be null");
    window.sessionStorage.setItem("item","value");
    ok(window.sessionStorage.getItem("item") == "value", "The value of the item should be 'value'");
    window.sessionStorage.removeItem("item");   
    ok(window.sessionStorage.getItem("item") == null, "item should be null");
  });
  test("check setItem", function() {
    expect(4);
    ok(window.sessionStorage.getItem("item") == null, "item should be null");
    window.sessionStorage.setItem("item","value");
    ok(window.sessionStorage.getItem("item") == "value", "The value of the item should be 'value'");
    window.sessionStorage.setItem("item","newval");
    ok(window.sessionStorage.getItem("item") == "newval", "The value of the item should be 'newval'");
    window.sessionStorage.removeItem("item");   
    ok(window.sessionStorage.getItem("item") == null, "item should be null");
  });
  test("check removeItem", function() {
    expect(3);
    ok(window.sessionStorage.getItem("item") == null, "item should be null");
    window.sessionStorage.setItem("item","value");
    ok(window.sessionStorage.getItem("item") == "value", "The value of the item should be 'value'");
    window.sessionStorage.removeItem("item");   
    ok(window.sessionStorage.getItem("item") == null, "item should be null");
  });
  test("check clear", function() {
    expect(11);
    ok(window.sessionStorage.getItem("item1") == null, "item1 should be null");
    ok(window.sessionStorage.getItem("item2") == null, "item2 should be null");
    ok(window.sessionStorage.getItem("item3") == null, "item3 should be null");
    window.sessionStorage.setItem("item1","value");
    window.sessionStorage.setItem("item2","value");
    window.sessionStorage.setItem("item3","value");
    ok(window.sessionStorage.getItem("item1") == "value", "item1 should be null");
    ok(window.sessionStorage.getItem("item2") == "value", "item2 should be null");
    ok(window.sessionStorage.getItem("item3") == "value", "item3 should be null");	    
    ok(window.sessionStorage.length == 3, "length should be 3");
    window.sessionStorage.clear();
    ok(window.sessionStorage.length == 0, "length should be 0");
    ok(window.sessionStorage.getItem("item1") == null, "item1 should be null");
    ok(window.sessionStorage.getItem("item2") == null, "item2 should be null");
    ok(window.sessionStorage.getItem("item3") == null, "item3 should be null");	    
  });
  test("check dot notation", function() {
    expect(3);
    ok(window.sessionStorage.item == null, "item should be null");
    window.sessionStorage.item = "value";
    ok(window.sessionStorage.item == "value", "The value of the item should be 'value'");
    window.sessionStorage.removeItem("item");   
    ok(window.sessionStorage.item == null, "item should be null");
  });
  module("Local Storage");
  test("should exist", function() {
	expect(7);
	ok(window.localStorage != null, "localStorage is defined");
    ok(typeof window.localStorage.length != 'undefined', "localStorage.length is defined");
    ok(typeof(window.localStorage.key) == "function", "localStorage.key is defined");
    ok(typeof(window.localStorage.getItem) == "function", "localStorage.getItem is defined");
    ok(typeof(window.localStorage.setItem) == "function", "localStorage.setItem is defined");
    ok(typeof(window.localStorage.removeItem) == "function", "localStorage.removeItem is defined");
    ok(typeof(window.localStorage.clear) == "function", "localStorage.clear is defined");
  });  
  test("check length", function() {
    expect(3);
    ok(window.localStorage.length == 0, "length should be 0");
    window.localStorage.setItem("key","value");
    ok(window.localStorage.length == 1, "length should be 1");
    window.localStorage.removeItem("key");   
    ok(window.localStorage.length == 0, "length should be 0");
  });
  test("check key", function() {
    expect(3);
    ok(window.localStorage.key(0) == null, "key should be null");
    window.localStorage.setItem("test","value");
    ok(window.localStorage.key(0) == "test", "key should be 'test'");
    window.localStorage.removeItem("test");   
    ok(window.localStorage.key(0) == null, "key should be null");
  });
  test("check getItem", function() {
    expect(3);
    ok(window.localStorage.getItem("item") == null, "item should be null");
    window.localStorage.setItem("item","value");
    ok(window.localStorage.getItem("item") == "value", "The value of the item should be 'value'");
    window.localStorage.removeItem("item");   
    ok(window.localStorage.getItem("item") == null, "item should be null");
  });
  test("check setItem", function() {
    expect(4);
    ok(window.localStorage.getItem("item") == null, "item should be null");
    window.localStorage.setItem("item","value");
    ok(window.localStorage.getItem("item") == "value", "The value of the item should be 'value'");
    window.localStorage.setItem("item","newval");
    ok(window.localStorage.getItem("item") == "newval", "The value of the item should be 'newval'");
    window.localStorage.removeItem("item");   
    ok(window.localStorage.getItem("item") == null, "item should be null");
  });
  test("check removeItem", function() {
    expect(3);
    ok(window.localStorage.getItem("item") == null, "item should be null");
    window.localStorage.setItem("item","value");
    ok(window.localStorage.getItem("item") == "value", "The value of the item should be 'value'");
    window.localStorage.removeItem("item");   
    ok(window.localStorage.getItem("item") == null, "item should be null");
  });
  test("check clear", function() {
    expect(11);
    ok(window.localStorage.getItem("item1") == null, "item1 should be null");
    ok(window.localStorage.getItem("item2") == null, "item2 should be null");
    ok(window.localStorage.getItem("item3") == null, "item3 should be null");
    window.localStorage.setItem("item1","value");
    window.localStorage.setItem("item2","value");
    window.localStorage.setItem("item3","value");
    ok(window.localStorage.getItem("item1") == "value", "item1 should be null");
    ok(window.localStorage.getItem("item2") == "value", "item2 should be null");
    ok(window.localStorage.getItem("item3") == "value", "item3 should be null");	    
    ok(window.localStorage.length == 3, "length should be 3");
    window.localStorage.clear();
    ok(window.localStorage.length == 0, "length should be 0");
    ok(window.localStorage.getItem("item1") == null, "item1 should be null");
    ok(window.localStorage.getItem("item2") == null, "item2 should be null");
    ok(window.localStorage.getItem("item3") == null, "item3 should be null");	    
  });
  test("check dot notation", function() {
    expect(3);
    ok(window.localStorage.item == null, "item should be null");
    window.localStorage.item = "value";
    ok(window.localStorage.item == "value", "The value of the item should be 'value'");
    window.localStorage.removeItem("item");   
    ok(window.localStorage.item == null, "item should be null");
  });
  module("HTML 5 Storage");
  test("should exist", function() {
    expect(1);
    ok(typeof(window.openDatabase) == "function", "Database is defined");
  });
  test("Should open a database", function() {
    var db = openDatabase("Database", "1.0", "HTML5 Database API example", 200000);
    ok(db != null, "Database should be opened");
  });
}
