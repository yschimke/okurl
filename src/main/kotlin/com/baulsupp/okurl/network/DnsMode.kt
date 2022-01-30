package com.baulsupp.okurl.network

import com.baulsupp.schoutput.UsageException
import java.util.*

enum class DnsMode {
  JAVA,
  GOOGLE,
  DNSOVERHTTPS;

  companion object {
    @JvmStatic
    fun fromString(dnsMode: String): DnsMode =
      values().find { it.name.lowercase(Locale.getDefault()) == dnsMode } ?: throw UsageException(
        "unknown dns mode '$dnsMode'"
      )
  }
}
