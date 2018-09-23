package com.baulsupp.okurl

import org.conscrypt.OpenSSLProvider
import javax.net.ssl.SSLContext

object TestMain {
  @JvmStatic
  fun main(args: Array<String>) {
    SSLContext.getInstance("TLSv1.3", OpenSSLProvider())

    runMain("https://graph.facebook.com/robots.txt --debug --ssldebug")
  }

  private fun runMain(s: String) {
    Main.main(*s.split(" ").toTypedArray())
  }
}
