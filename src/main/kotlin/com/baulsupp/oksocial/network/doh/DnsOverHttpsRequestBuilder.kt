package com.baulsupp.oksocial.network.doh

import com.baulsupp.oksocial.kotlin.request
import com.google.common.net.InetAddresses
import okhttp3.Request
import java.net.InetAddress

interface DnsOverHttpsRequestBuilder {
  fun build(query: String): Request

  val dnsEntries: Map<String, List<InetAddress>>
}

private fun knownIps(host: String, vararg ips: String): Map<String, List<InetAddress>> =
  mapOf(host to ips.map { InetAddresses.forString(it) })

object GoogleDnsOverHttps : DnsOverHttpsRequestBuilder {
  override fun build(query: String): Request = request("https://dns.google.com/experimental?ct&dns=$query")

  override val dnsEntries: Map<String, List<InetAddress>> = knownIps("dns.google.com", "216.58.204.78", "2a00:1450:4009:814:0:0:0:200e")
}

object CloudflareDnsOverHttps : DnsOverHttpsRequestBuilder {
  override fun build(query: String): Request = request("https://cloudflare-dns.com/dns-query?ct=application/dns-udpwireformat&dns=$query")

  override val dnsEntries: Map<String, List<InetAddress>> = knownIps("cloudflare-dns.com", "104.16.111.25", "104.16.112.25", "2400:cb00:2048:1:0:0:6810:7019", "2400:cb00:2048:1:0:0:6810:6f19")
}

object TestDnsOverHttps : DnsOverHttpsRequestBuilder {
  override fun build(query: String): Request = request("https://dns.google.com/experimental?ct&dns=q80BAAABAAAAAAAAA3d3dwdleGFtcGxlA2NvbQAAAQAB")

  override val dnsEntries: Map<String, List<InetAddress>> = mapOf()
}
