package com.baulsupp.oksocial.network.dnsoverhttps

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Internal Bootstrap DNS implementation for handling initial connection to DNS over HTTPS server.
 *
 * Returns hardcoded results for the known host.
 */
class BootstrapDns(private val dnsHost: String, vararg dnsServers: InetAddress) : Dns {
  private val dnsServers: List<InetAddress> = dnsServers.toList()

  @Throws(UnknownHostException::class)
  override fun lookup(hostname: String): List<InetAddress> {
    if (hostname == dnsHost) {
      return dnsServers
    }

    throw UnknownHostException(hostname)
  }
}
