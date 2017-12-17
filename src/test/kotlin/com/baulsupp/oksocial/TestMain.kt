package com.baulsupp.oksocial

object TestMain {
  @Throws(Exception::class)
  @JvmStatic
  fun main(args: Array<String>) {
    runMain("https://linen-centaur-133323.firebaseio.com/admins/jill/ --urlCompletion")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
