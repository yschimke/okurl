package com.baulsupp.oksocial.commands

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.network.DnsMode
import com.baulsupp.oksocial.network.IPvMode
import com.baulsupp.oksocial.okhttp.ConnectionSpecOption
import okhttp3.Protocol
import okhttp3.TlsVersion

suspend fun Main.listOptions(option: String): Collection<String> {
  return when (option) {
    "service" -> authenticatingInterceptor.names()
    "alias" -> commandRegistry.names()
    "tokenset" -> credentialsStore.names()
    "ipmode" -> IPvMode.values().map { it.code }
    "dnsmode" -> DnsMode.values().map { it.name.toLowerCase() }
    "protocol" -> Protocol.values().map { it.toString() }
    "method" -> listOf("GET", "HEAD", "POST", "DELETE", "PUT", "PATCH")
    "connectionSpec" -> ConnectionSpecOption.values().map { it.name }
    "cipherSuite" -> com.baulsupp.oksocial.okhttp.cipherSuites().map { it.javaName() }
    "tlsVersions" -> TlsVersion.values().map { it.javaName() }
    else -> listOf()
  }
}
