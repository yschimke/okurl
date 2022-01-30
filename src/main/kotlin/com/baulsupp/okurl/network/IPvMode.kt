package com.baulsupp.okurl.network

import com.baulsupp.schoutput.UsageException

enum class IPvMode(val code: String) {
  SYSTEM("system"),
  IPV6_FIRST("ipv6"),
  IPV4_FIRST("ipv4"),
  IPV6_ONLY("ipv6only"),
  IPV4_ONLY("ipv4only");

  companion object {
    @JvmStatic
    fun fromString(ipMode: String): IPvMode =
      values().find { it.code == ipMode } ?: throw UsageException("Unknown value $ipMode")
  }
}
