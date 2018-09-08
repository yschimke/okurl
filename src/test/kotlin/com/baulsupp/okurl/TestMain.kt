package com.baulsupp.okurl

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("https://httpbin. --urlCompletion")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
