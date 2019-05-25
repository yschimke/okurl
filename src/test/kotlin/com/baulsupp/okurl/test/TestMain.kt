package com.baulsupp.okurl.test

import kotlinx.coroutines.runBlocking

private suspend fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

fun main() {
  runBlocking {
    runMain("--ct FAIL --ctHost *.twitter.com https://api.twitter.com/robots.txt")
  }
}
