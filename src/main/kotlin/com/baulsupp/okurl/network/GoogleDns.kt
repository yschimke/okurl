package com.baulsupp.okurl.network

import com.baulsupp.okurl.kotlin.query
import com.baulsupp.okurl.kotlin.request
import kotlinx.coroutines.runBlocking
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class GoogleDns(
  private val dnsHosts: List<InetAddress>,
  private val mode: IPvMode,
  private val client: () -> OkHttpClient
) : Dns {
  data class Answer(val name: String, val type: Int, val TTL: Int, val data: String)
  data class Response(val Status: Int, val Answer: List<Answer>)

  override fun lookup(hostname: String): List<InetAddress> {
    if (hostname == "dns.google.com") {
      return dnsHosts
    }

    return try {
      val result = runBlocking {
        client().query<Response>(
          request {
            url(base.newBuilder().addQueryParameter("name", hostname).addQueryParameter("type", type(mode)).build())
            header("Accept", "application/dns+json")
          }
        )
      }

      if (result.Status != 0) {
        throw UnknownHostException("Status from dns.google.com: " + result.Status)
      }

      result.Answer.filter { it.type == 1 || it.type == 28 }.map { InetAddress.getByName(it.data) }
    } catch (e: IOException) {
      val unknownHostException = UnknownHostException("failed to lookup $hostname via dns.google.com")
      unknownHostException.initCause(e)
      throw unknownHostException
    }
  }

  // TODO support IPv6 preferred etc, e.g. two queries
  private fun type(mode: IPvMode) =
    when (mode) {
      IPvMode.IPV6_ONLY -> "AAAA"
      else -> "A"
    }

  companion object {
    val base = HttpUrl.parse("https://dns.google.com/resolve")!!

    fun build(clientSupplier: () -> OkHttpClient, mode: IPvMode): GoogleDns {
      val hosts =
        listOf("216.58.216.142", "216.239.34.10", "2607:f8b0:400a:809::200e").map { InetAddress.getByName(it) }
      return GoogleDns(hosts, mode, clientSupplier)
    }
  }
}
