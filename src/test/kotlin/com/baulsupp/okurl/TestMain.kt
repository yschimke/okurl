package com.baulsupp.okurl

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("https://httpbin.org/basic-auth/a/b")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
