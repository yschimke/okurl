Object.prototype.toString = function xToString() { return JSON.stringify(this); };

var okshell = Java.type("com.baulsupp.oksocial.jjs.OkShell").instance();
okshell.listenForMainExit();

var query = function(url) {
  // TODO the response format to decide how to parse
  return JSON.parse(okshell.query(url));
}

var execute = function(request) {
  // TODO the response format to decide how to parse
  return JSON.parse(okshell.execute(request));
}

var show = function(url) {
  return okshell.show(url);
}

var location = function() {
  return okshell.location();
}

var readParam = Java.type("com.baulsupp.oksocial.jjs.OkShell").readParam;

var credentials = function(s) {
  return okshell.credentials(s);
}

var UsageException = Java.type("com.baulsupp.oksocial.util.UsageException");

var usage = function(error) {
  throw new UsageException(error);
}

var FormBuilder = Java.type("okhttp3.FormBody.Builder");

var requestBuilder = okshell.requestBuilder;
var client = okshell.client;

// http://stackoverflow.com/questions/2686855/is-there-a-javascript-function-that-can-pad-a-string-to-get-to-a-determined-leng
function pad(length, str, padLeft) {
  var padding = Array(length).join(' ');

  if (typeof str === 'undefined')
    return pad;
  if (padLeft) {
    return (padding + str).slice(-padding.length);
  } else {
    return (str + padding).substring(0, padding.length);
  }
}

function padRight(length, str) {
  return pad(length, str, false);
}

function padLeft(length, str) {
  return pad(length, str, true);
}

var terminalWidth = okshell.outputHandler.terminalWidth();

function warmup(paths) {
  paths.forEach(function(path) {
    okshell.warmup(path);
  });
}
