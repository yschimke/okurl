package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.output.util.UsageException

enum class DnsMode {
    JAVA,
    NETTY,
    DNSGOOGLE;

    companion object {
        fun fromString(dnsMode: String): DnsMode {
            when (dnsMode) {
                "java" -> return DnsMode.JAVA
                "netty" -> return DnsMode.NETTY
                "dnsgoogle" -> return DnsMode.DNSGOOGLE
                else -> throw UsageException("unknown dns mode '$dnsMode'")
            }
        }
    }
}
