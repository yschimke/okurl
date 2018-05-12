package com.baulsupp.oksocial.network.dnsoverhttps

import okio.Buffer
import okio.ByteString
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.util.ArrayList

/**
 * Trivial Dns Encoder/Decoder, basically ripped from Netty full implementation.
 */
object DnsRecordCodec {
  private val SERVFAIL: Byte = 2
  private val NXDOMAIN: Byte = 3
  private val TYPE_A = 0x0001
  private val TYPE_AAAA = 0x001c
  private val TYPE_PTR = 0x000c
  private val ASCII = Charset.forName("ASCII")

  fun encodeQuery(host: String, includeIPv6: Boolean): ByteString {
    val buf = Buffer()

    buf.writeShort(0) // query id
    buf.writeShort(256) // flags with recursion
    buf.writeShort(if (includeIPv6) 2 else 1) // question count
    buf.writeShort(0) // answerCount
    buf.writeShort(0) // authorityResourceCount
    buf.writeShort(0) // additional

    val nameBuf = Buffer()
    val labels = host.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (label in labels) {
      nameBuf.writeByte(label.length)
      nameBuf.writeString(label, ASCII)
    }
    nameBuf.writeByte(0) // end

    nameBuf.copyTo(buf, 0, nameBuf.size())
    buf.writeShort(1) // A
    buf.writeShort(1) // CLASS_IN

    if (includeIPv6) {
      nameBuf.copyTo(buf, 0, nameBuf.size())
      buf.writeShort(0x001c) // AAAA
      buf.writeShort(1) // CLASS_IN
    }

    return buf.readByteString()
  }

  @Throws(UnknownHostException::class)
  fun decodeAnswers(hostname: String, byteString: ByteString): List<InetAddress> {
    //System.out.println("Response: " + byteString.hex());

    val result = ArrayList<InetAddress>()

    val buf = Buffer()
    buf.write(byteString)
    buf.readShort() // query id

    val flags = buf.readShort().toInt() and 0xffff
    if (flags shr 15 == 0) {
      throw IllegalArgumentException("not a response")
    }

    val responseCode = (flags and 0xf).toByte()

    //System.out.println("Code: " + responseCode);
    if (responseCode == NXDOMAIN) {
      throw UnknownHostException("$hostname: NXDOMAIN")
    } else if (responseCode == SERVFAIL) {
      throw UnknownHostException("$hostname: SERVFAIL")
    }

    val questionCount = buf.readShort().toInt() and 0xffff
    val answerCount = buf.readShort().toInt() and 0xffff
    buf.readShort() // authority record count
    buf.readShort() // additional record count

    for (i in 0 until questionCount) {
      consumeName(buf) // name
      buf.readShort() // type
      buf.readShort() // class
    }

    for (i in 0 until answerCount) {
      consumeName(buf) // name

      val type = buf.readShort().toInt() and 0xffff
      buf.readShort() // class
      buf.readInt() // ttl
      val length = buf.readShort().toInt() and 0xffff

      if (type == TYPE_A || type == TYPE_AAAA) {
        val bytes = ByteArray(length)
        buf.read(bytes)
        result.add(InetAddress.getByAddress(bytes))
      } else {
        buf.skip(length.toLong())
      }
    }

    return result
  }

  private fun consumeName(`in`: Buffer) {
    // 0 - 63 bytes
    var length = `in`.readByte().toInt()

    if (length < 0) {
      // compressed name pointer, first two bits are 1
      // drop second byte of compression offset
      `in`.skip(1)
    } else {
      while (length > 0) {
        // skip each part of the domain name
        length = `in`.readByte().toInt()
      }
    }
  }
}
