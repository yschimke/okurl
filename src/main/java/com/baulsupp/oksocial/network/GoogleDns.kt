package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.authenticator.AuthUtil
import com.google.common.net.InetAddresses
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.function.Supplier
import kotlin.streams.toList

class GoogleDns(private val dnsHosts: List<InetAddress>, private val mode: IPvMode,
    private val client: Supplier<OkHttpClient>) : Dns {

  // TODO implement DnsMode internally
  @Throws(UnknownHostException::class)
  override fun lookup(host: String): List<InetAddress> {
    if (host == "dns.google.com") {
      return dnsHosts
    }

    try {
      // TODO map punycode here?
      val url = HttpUrl.parse("https://dns.google.com/resolve?name=" + host + "&type=" + type(mode))
      val request = Request.Builder().url(url!!).header("Accept", "application/dns+json").build()
      val result = AuthUtil.makeJsonMapRequest(client.get(), request)

      return responseToList(result)
    } catch (e: IOException) {
      val unknownHostException = UnknownHostException("failed to lookup $host via dns.google.com")
      unknownHostException.initCause(e)
      throw unknownHostException
    }

  }

  private fun type(mode: IPvMode): String {
    // TODO support IPv6 preferred etc, e.g. two queries
    when (mode) {
      IPvMode.IPV6_ONLY -> return "AAAA"
      else -> return "A"
    }
  }

  @Throws(UnknownHostException::class)
  private fun responseToList(result: Map<String, Any>): List<InetAddress> {
    if (result["Status"] != 0) {
      // TODO response codes
      throw UnknownHostException("Status from dns.google.com: " + result["Status"])
    }

    val answer = result["Answer"] as List<Map<String, Any>>

    return answer.stream()
        .filter { a -> a["type"] == 1 || a["type"] == 28 }
        .map { a -> InetAddresses.forString(a["data"] as String) }
        .toList()
  }

  companion object {

    fun fromResourceList(): GoogleDns {
      throw UnsupportedOperationException()
    }

    fun fromHosts(clientSupplier: Supplier<OkHttpClient>, mode: IPvMode,
        vararg ips: String): GoogleDns {
      val hosts = ips.map { InetAddresses.forString(it) }

      return GoogleDns(hosts, mode, clientSupplier)
    }
  }
}
