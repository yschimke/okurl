package com.baulsupp.okurl.commands

import com.baulsupp.okurl.Main
import com.baulsupp.okurl.network.DnsMode
import com.baulsupp.okurl.network.IPvMode
import com.baulsupp.okurl.okhttp.ConnectionSpecOption
import okhttp3.Protocol
import okhttp3.TlsVersion
import java.util.*

suspend fun Main.listOptions(option: String): Collection<String> {
  return when (option) {
    "service" -> authenticatingInterceptor.names()
    "tokenset" -> credentialsStore.names()
    "ipmode" -> IPvMode.values().map { it.code }
    "dnsmode" -> DnsMode.values().map { it.name.lowercase(Locale.getDefault()) }
    "protocol" -> Protocol.values().map { it.toString() }
    "method" -> listOf("GET", "HEAD", "POST", "DELETE", "PUT", "PATCH")
    "connectionSpec" -> ConnectionSpecOption.values().map { it.name }
    "cipherSuite" -> com.baulsupp.okurl.okhttp.cipherSuites().map { it.javaName }
    "tlsVersions" -> TlsVersion.values().map { it.javaName }
    "complete" -> listOf(
      "service",
      "alias",
      "tokenset",
      "ipmode",
      "dnsmode",
      "protocol",
      "method",
      "connectionSpec",
      "cipherSuite",
      "tlsVersions",
      "complete"
    )
    else -> listOf()
  }
}
