package com.baulsupp.okurl.test

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

private fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runMain("--wireshark https://api.twitter.com/robots.txt")

  Thread.sleep(5000)
}
