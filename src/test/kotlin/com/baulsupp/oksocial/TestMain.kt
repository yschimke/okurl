package com.baulsupp.oksocial

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("--dns dnsoverhttps --ip ipv4only https://graph.facebook.com/robots.txt")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
