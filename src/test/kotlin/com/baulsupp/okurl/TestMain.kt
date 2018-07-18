package com.baulsupp.okurl

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("--dns dnsoverhttps --ip ipv4only https://httpbin.org/json")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
