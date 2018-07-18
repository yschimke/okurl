package com.baulsupp.okurl.network

import com.baulsupp.oksocial.output.UsageException

enum class DnsMode {
  JAVA,
  NETTY,
  GOOGLE,
  DNSOVERHTTPS;

  companion object {
    @JvmStatic
    fun fromString(dnsMode: String): DnsMode =
      DnsMode.values().find { it.name.toLowerCase() == dnsMode } ?: throw UsageException(
        "unknown dns mode '$dnsMode'")
  }
}
