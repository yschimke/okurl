package com.baulsupp.oksocial

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("--show-credentials facebook")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
