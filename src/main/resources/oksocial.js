Object.prototype.toString = function xToString() { return JSON.stringify(this); };

var okshell = Java.type("com.baulsupp.oksocial.jjs.OkShell").instance();
okshell.listenForMainExit();

var query = function(url) {
  return JSON.parse(okshell.query(url));
}