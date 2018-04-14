package com.baulsupp.oksocial.network

import com.baulsupp.oksocial.credentials.NoToken
import com.baulsupp.oksocial.kotlin.client
import com.baulsupp.oksocial.kotlin.execute
import com.baulsupp.oksocial.kotlin.request
import com.moparisthebest.dns.Util.readPacket
import com.moparisthebest.dns.dto.Packet
import com.moparisthebest.dns.resolve.AbstractQueueProcessingResolver
import io.netty.buffer.Unpooled
import io.netty.handler.codec.dns.DatagramDnsQuery
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder
import io.netty.handler.codec.dns.DefaultDnsQuery
import io.netty.handler.codec.dns.DefaultDnsQuestion
import io.netty.handler.codec.dns.DefaultDnsRecordEncoder
import io.netty.handler.codec.dns.DnsRecordType
import io.netty.handler.codec.dns.DnsSection
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.DataInputStream
import java.net.InetSocketAddress

val DnsUdpWireFormat = MediaType.parse("application/dns-udpwireformat")

class OkHttpResolver(maxRetries: Int, name: String, val url: String, val client: OkHttpClient) :
  AbstractQueueProcessingResolver(maxRetries, name) {
  override fun resolve(requestPacket: Packet): Packet {
    return runBlocking {
      sendQuery(requestPacket)
    }
  }

  suspend fun sendQuery(requestPacket: Packet): Packet {
    val response = client.execute(request(url, NoToken) {
      header("Content-Type", "application/dns-udpwireformat")
      header("Accept", "application/dns-udpwireformat")
      post(RequestBody.create(DnsUdpWireFormat, requestPacket.buf.array()))
    })

    println(response.code())

    val responseBody = response.body()!!
    val dis = DataInputStream(responseBody.byteStream())
    return readPacket(responseBody.contentLength().toInt(), dis)
  }
}

fun main(args: Array<String>) {
  val r = OkHttpResolver(3, "google", "https://dns.google.com/experimental?ct", client)
  val encoder = DatagramDnsQueryEncoder()

  val question = DefaultDnsQuestion("google.com", DnsRecordType.A)
  val query = DatagramDnsQuery(InetSocketAddress.createUnresolved("localhost", 8080), InetSocketAddress.createUnresolved("localhost", 8080), 1)
  query.addRecord(DnsSection.QUESTION, question)

  val out = Unpooled.buffer()
  encoder.encode(question, out)
  val bytes = ByteArray(out.readableBytes())
  out.readBytes(bytes)

  val response = r.resolve(Packet(bytes))

  println(response)
}
