package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.output.util.UsageException

enum class DnsMode {
    JAVA,
    NETTY,
    DNSGOOGLE;

    companion object {
        @JvmStatic fun fromString(dnsMode: String): DnsMode {
            return when (dnsMode) {
                "java" -> DnsMode.JAVA
                "netty" -> DnsMode.NETTY
                "dnsgoogle" -> DnsMode.DNSGOOGLE
                else -> throw UsageException("unknown dns mode '$dnsMode'")
            }
        }
    }
}
