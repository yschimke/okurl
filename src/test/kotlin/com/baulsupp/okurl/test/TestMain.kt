package com.baulsupp.okurl.test

private fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runMain("--showCredentials")

  Thread.sleep(5000)
}
