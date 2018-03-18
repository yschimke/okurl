package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.output.util.UsageException

enum class DnsMode {
  JAVA,
  NETTY,
  GOOGLE;

  companion object {
    @JvmStatic
    fun fromString(dnsMode: String): DnsMode =
      DnsMode.values().find { it.name.toLowerCase() == dnsMode } ?: throw UsageException(
        "unknown dns mode '$dnsMode'")
  }
}
