Object.prototype.toString = function xToString() { return JSON.stringify(this); };

var okshell = Java.type("com.baulsupp.oksocial.jjs.OkShell").instance();
okshell.listenForMainExit();

var query = function(url) {
  // TODO the response format to decide how to parse
  return JSON.parse(okshell.query(url));
}

var location = function() {
  var a = Java.type("com.baulsupp.oksocial.location.Location");
  return a.read().get();
}