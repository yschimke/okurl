package com.baulsupp.oksocial.network

import okhttp3.Dns
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.logging.Logger
import java.util.stream.Collectors.joining

class DnsSelector(private val mode: IPvMode, private val delegate: Dns) : Dns {

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        var addresses = delegate.lookup(hostname)

        addresses = when (mode) {
            IPvMode.IPV6_FIRST -> addresses.sortedBy { Inet4Address::class.java.isInstance(it) }
            IPvMode.IPV4_FIRST -> addresses.sortedBy { Inet6Address::class.java.isInstance(it) }
            IPvMode.IPV6_ONLY -> addresses.filter({ Inet6Address::class.java.isInstance(it) })
            IPvMode.IPV4_ONLY -> addresses.filter({ Inet4Address::class.java.isInstance(it) })
            IPvMode.SYSTEM -> addresses
        }

        logger.fine("Dns ($hostname): " + addresses.map { it.toString() }.joinToString(", "))

        return addresses
    }

    companion object {
        private val logger = Logger.getLogger(DnsSelector::class.java.name)
    }
}
