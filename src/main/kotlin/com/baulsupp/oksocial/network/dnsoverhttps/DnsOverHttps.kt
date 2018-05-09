package com.baulsupp.oksocial.network.dnsoverhttps

import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.platform.Platform
import okio.ByteString
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * DNS over HTTPS implementation.
 *
 * Implementation of https://tools.ietf.org/html/draft-ietf-doh-dns-over-https-07
 */
class DnsOverHttps(client: () -> OkHttpClient, val url: HttpUrl,
                   bootstrapDns: Dns?, private val includeIPv6: Boolean, method: String, private val contentType: MediaType) : Dns {
  private val client by lazy {
    if (bootstrapDns != null) client.invoke().newBuilder().dns(bootstrapDns).build() else client.invoke()
  }

  val isPost: Boolean = method == "POST"

  init {
    if (method != "GET" && method != "POST") {
      throw UnsupportedOperationException("Only GET and POST Supported")
    }
  }

  @Throws(UnknownHostException::class)
  override fun lookup(hostname: String): List<InetAddress> {
    try {
      //System.out.println("Host: " + hostname);

      val query = DnsRecordCodec.encodeQuery(hostname, includeIPv6)

      val request = buildRequest(query)
      val response = client.newCall(request).execute()

      // TODO reenable (currently noisy with test servers)
      if (response.cacheResponse() == null && response.protocol() != Protocol.HTTP_2) {
        Platform.get().log(Platform.WARN, "Incorrect protocol: " + response.protocol(), null)
      }

      // TODO remove (temporary info only currently)
      if (client.cache() != null && !isPost && response.cacheResponse() == null) {
        Platform.get().log(Platform.INFO, "DNS missed cache: $hostname", null)
      }

      try {
        if (!response.isSuccessful) {
          throw IOException("response: " + response.code() + " " + response.message())
        }

        val responseBytes = response.body()!!.source().readByteString()

        //System.out.println("Response: " + responseBytes.hex());

        return DnsRecordCodec.decodeAnswers(hostname, responseBytes)
      } finally {
        response.close()
      }
    } catch (uhe: UnknownHostException) {
      throw uhe
    } catch (e: Exception) {
      val unknownHostException = UnknownHostException(hostname)
      unknownHostException.initCause(e)
      throw unknownHostException
    }

  }

  private fun buildRequest(query: ByteString): Request {
    val builder: Request.Builder

    if (isPost) {
      builder = Request.Builder().url(url)

      builder.post(RequestBody.create(contentType, query))
    } else {
      val encoded = query.base64Url().replace("=", "")

      //System.out.println("Query: " + encoded);

      val requestUrl = url.newBuilder().addQueryParameter("dns", encoded).build()

      builder = Request.Builder().url(requestUrl)
    }

    //System.out.println("URL: " + requestUrl);

    builder.header("Accept", contentType.toString())

    return builder.build()
  }

  companion object {
    val DNS_MESSAGE = MediaType.parse("application/dns-message")!!
    val UDPWIREFORMAT = MediaType.parse("application/dns-udpwireformat")!!
  }
}
