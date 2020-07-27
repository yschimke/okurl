package com.baulsupp.okurl.network

import com.baulsupp.oksocial.output.UsageException

enum class DnsMode {
  JAVA,
  GOOGLE,
  DNSOVERHTTPS;

  companion object {
    @JvmStatic
    fun fromString(dnsMode: String): DnsMode =
      values().find { it.name.toLowerCase() == dnsMode } ?: throw UsageException(
        "unknown dns mode '$dnsMode'"
      )
  }
}
