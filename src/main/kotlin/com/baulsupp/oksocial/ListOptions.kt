package com.baulsupp.oksocial

import com.baulsupp.oksocial.network.DnsMode
import com.baulsupp.oksocial.network.IPvMode
import okhttp3.Protocol

suspend fun Main.listOptions(option: String): Collection<String> {
  return when (option) {
    "service" -> authenticatingInterceptor.names()
    "alias" -> commandRegistry.names()
    "tokenset" -> credentialsStore.names()
    "ipmode" -> IPvMode.values().map { it.code }
    "dnsmode" -> DnsMode.values().map { it.name.toLowerCase() }
    "protocol" -> Protocol.values().map { it.toString() }
    "method" -> listOf("GET", "HEAD", "POST", "DELETE", "PUT", "PATCH")
    else -> listOf()
  }
}
