package com.baulsupp.oksocial.network

enum class IPvMode {
  SYSTEM,
  IPV6_FIRST,
  IPV4_FIRST,
  IPV6_ONLY,
  IPV4_ONLY;

  companion object {
    @JvmStatic
    fun fromString(ipMode: String): IPvMode {
      return when (ipMode) {
        "ipv6" -> IPvMode.IPV6_FIRST
        "ipv4" -> IPvMode.IPV4_FIRST
        "ipv6only" -> IPvMode.IPV6_ONLY
        "ipv4only" -> IPvMode.IPV4_ONLY
        else -> IPvMode.SYSTEM
      }
    }
  }
}
