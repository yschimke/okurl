package com.baulsupp.okurl.test

private fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runMain("--ct FAIL --ctHost *.twitter.com https://api.twitter.com/robots.txt")
}
