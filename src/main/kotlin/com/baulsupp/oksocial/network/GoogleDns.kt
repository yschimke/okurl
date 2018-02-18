package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.authenticator.AuthUtil

import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

fun fromHosts(clientSupplier: () -> OkHttpClient, mode: IPvMode,
              vararg ips: String): GoogleDns {
  val hosts = ips.map { InetAddress.getByName(it) }

  return GoogleDns(hosts, mode, clientSupplier)
}

class GoogleDns(private val dnsHosts: List<InetAddress>, private val mode: IPvMode,
                private val client: () -> OkHttpClient) : Dns {

  // TODO implement DnsMode internally
  override fun lookup(host: String): List<InetAddress> {
    if (host == "dns.google.com") {
      return dnsHosts
    }

    return try {
      // TODO map punycode here?
      val url = HttpUrl.parse("https://dns.google.com/resolve?name=" + host + "&type=" + type(mode))
      val request = Request.Builder().url(url!!).header("Accept", "application/dns+json").build()
      val result = runBlocking { AuthUtil.makeJsonMapRequest(client(), request) }

      responseToList(result)
    } catch (e: IOException) {
      val unknownHostException = UnknownHostException("failed to lookup $host via dns.google.com")
      unknownHostException.initCause(e)
      throw unknownHostException
    }
  }

  private fun type(mode: IPvMode): String {
    // TODO support IPv6 preferred etc, e.g. two queries
    return when (mode) {
      IPvMode.IPV6_ONLY -> "AAAA"
      else -> "A"
    }
  }

  private fun responseToList(result: Map<String, Any>): List<InetAddress> {
    if (result["Status"] != 0) {
      // TODO response codes
      throw UnknownHostException("Status from dns.google.com: " + result["Status"])
    }

    val answer = result["Answer"] as List<Map<String, Any>>

    return answer
      .filter { a -> a["type"] == 1 || a["type"] == 28 }
      .map { a -> InetAddress.getByName(a["data"] as String) }
  }
}
