package com.baulsupp.okurl.test

private fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runMain("https://www.googleapis.com/gmail/v1/users/me/history --apidoc")

  Thread.sleep(5000)
}
