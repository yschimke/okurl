package com.baulsupp.okurl.test

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

private fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runMain("--ssldebug --insecure=httpbin.org -r https://httpbin.org/brotli")
}
