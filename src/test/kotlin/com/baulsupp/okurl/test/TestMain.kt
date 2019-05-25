package com.baulsupp.okurl.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes

@ExperimentalCoroutinesApi
private suspend fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

@ExperimentalCoroutinesApi
suspend fun main() {
  DebugProbes.install()

  runMain("--ct FAIL --ctHost *.twitter.com https://api.twitter.com/robots.txt")
}
