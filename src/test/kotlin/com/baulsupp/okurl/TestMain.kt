package com.baulsupp.okurl

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("https://nghttp2.org/httpbin/get --zipkinTrace")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
