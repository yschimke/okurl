package com.baulsupp.okurl.test

private suspend fun runMain(s: String) {
  com.baulsupp.okurl.main(s.split(" ").toTypedArray())
}

suspend fun main(args: Array<String>) {
//  DebugProbes.install()

  runMain("https://graph.facebook.com/robots.txt")
}
