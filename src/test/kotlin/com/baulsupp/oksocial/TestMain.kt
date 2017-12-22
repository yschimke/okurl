package com.baulsupp.oksocial

object TestMain {
  @Throws(Exception::class)
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("https://api. --urlCompletion")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
