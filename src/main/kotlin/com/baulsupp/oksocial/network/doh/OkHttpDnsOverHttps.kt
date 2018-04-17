package com.baulsupp.oksocial.network.doh

import com.baulsupp.oksocial.kotlin.execute
import com.baulsupp.oksocial.network.IPvMode
import com.baulsupp.oksocial.network.doh.DatagramDnsResponseDecoder.readDnsResponse
import io.netty.handler.codec.dns.DatagramDnsQuery
import io.netty.handler.codec.dns.DefaultDnsQuestion
import io.netty.handler.codec.dns.DnsRawRecord
import io.netty.handler.codec.dns.DnsRecord
import io.netty.handler.codec.dns.DnsRecordType
import io.netty.handler.codec.dns.DnsResponse
import io.netty.handler.codec.dns.DnsSection
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.InetSocketAddress

val localhost8080 = InetSocketAddress.createUnresolved("localhost", 8080)

private fun DnsRawRecord.toInetAddress(): InetAddress {
  val bytes = ByteArray(content().readableBytes())
  content().readBytes(bytes)
  return InetAddress.getByAddress(bytes)
}

class OkHttpDnsOverHttps(val requestBuilder: DnsOverHttpsRequestBuilder, val ipMode: IPvMode, val client: () -> OkHttpClient) : Dns {
  override fun lookup(host: String): MutableList<InetAddress> = runBlocking {
    sendQuery(host).toMutableList()
  }

  fun dnsRecords(mode: IPvMode) =
    when (mode) {
      IPvMode.IPV6_FIRST -> listOf(DnsRecordType.AAAA, DnsRecordType.A)
      IPvMode.IPV6_ONLY -> listOf(DnsRecordType.AAAA)
      IPvMode.IPV4_ONLY -> listOf(DnsRecordType.A)
      else -> listOf(DnsRecordType.A, DnsRecordType.AAAA)
    }

  suspend fun sendQuery(host: String): List<InetAddress> {
    val fixedResult = requestBuilder.dnsEntries.get(host)

    if (fixedResult != null) {
      return fixedResult
    }

    val query = dnsQuery(host)
    val queryString = DatagramDnsQueryEncoder.encode(query)
    val request = requestBuilder.build(queryString)

    val responseBytes = client().execute(request).body()!!.source().readByteString()

    val dnsResponse = readDnsResponse(responseBytes)

    return readAddressList(dnsResponse)
  }

  fun readAddressList(dnsResponse: DnsResponse) =
    (0 until dnsResponse.count(DnsSection.ANSWER)).map { dnsResponse.recordAt<DnsRecord>(DnsSection.ANSWER, it) }.filter { it.type() == DnsRecordType.A || it.type() == DnsRecordType.AAAA }.filterIsInstance<DnsRawRecord>().map { it.toInetAddress() }

  fun dnsQuery(host: String): DatagramDnsQuery {
    val query = DatagramDnsQuery(localhost8080, localhost8080, 0)
    query.isRecursionDesired = true
    dnsRecords(ipMode).forEachIndexed { i, record ->
      query.addRecord(DnsSection.QUESTION, i, DefaultDnsQuestion(host, record))
    }
    return query
  }
}
