package com.baulsupp.okurl.preferences

import com.baulsupp.okurl.network.DnsMode
import com.baulsupp.okurl.network.IPvMode
import com.baulsupp.okurl.tracing.TracingMode
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import java.io.File
import java.net.InetSocketAddress
import java.util.logging.Level
import java.util.logging.Logger

data class Proxy(val host: String, val port: Int, val user: String?, val password: String?, val type: java.net.Proxy.Type?) {
  fun build(): java.net.Proxy {
    val address = InetSocketAddress(host, port)
    return java.net.Proxy(java.net.Proxy.Type.SOCKS, address)
  }
}

data class Preferences(
  val osProxy: Boolean? = null,
  val proxy: Proxy? = null,
  val dnsMode: DnsMode? = null,
  val ipMode: IPvMode? = null,
  val tracing: TracingMode? = null,
  val cache: String? = null) {
  companion object {
    private val logger = Logger.getLogger(Preferences::class.java.name)

    val local: Preferences by lazy {
      val prefFile = File(System.getenv("HOME"), ".okurl/prefs.json")

      if (prefFile.exists()) {
        try {
          val moshi = Moshi.Builder().build()
          moshi.adapter(Preferences::class.java).fromJson(prefFile.source().buffer()) ?: Preferences()
        } catch (e: Exception) {
          logger.log(Level.FINE, "failed loading preferences", e)
          Preferences()
        }
      } else {
        Preferences()
      }
    }
  }
}
