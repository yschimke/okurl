/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.baulsupp.oksocial.network.doh

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.CorruptedFrameException
import io.netty.handler.codec.dns.DatagramDnsResponse
import io.netty.handler.codec.dns.DefaultDnsRecordDecoder
import io.netty.handler.codec.dns.DnsOpCode
import io.netty.handler.codec.dns.DnsRecord
import io.netty.handler.codec.dns.DnsResponse
import io.netty.handler.codec.dns.DnsResponseCode
import io.netty.handler.codec.dns.DnsSection
import okio.ByteString

object DatagramDnsResponseDecoder {
  fun readDnsResponse(responseBytes: ByteString) =
    DatagramDnsResponseDecoder.decode(Unpooled.wrappedBuffer(responseBytes.toByteArray()))

  fun decode(buf: ByteBuf): DnsResponse {
    val response = newResponse(buf)
    val questionCount = buf.readUnsignedShort()
    val answerCount = buf.readUnsignedShort()
    val authorityRecordCount = buf.readUnsignedShort()
    val additionalRecordCount = buf.readUnsignedShort()

    decodeQuestions(response, buf, questionCount)
    decodeRecords(response, DnsSection.ANSWER, buf, answerCount)
    decodeRecords(response, DnsSection.AUTHORITY, buf, authorityRecordCount)
    decodeRecords(response, DnsSection.ADDITIONAL, buf, additionalRecordCount)

    return response
  }

  fun newResponse(buf: ByteBuf): DnsResponse {
    val id = buf.readUnsignedShort()

    val flags = buf.readUnsignedShort()
    if (flags shr 15 == 0) {
      throw CorruptedFrameException("not a response")
    }

    val response = DatagramDnsResponse(
      localhost8080,
      localhost8080,
      id,
      DnsOpCode.valueOf((flags shr 11 and 0xf).toByte().toInt()), DnsResponseCode.valueOf((flags and 0xf).toByte().toInt()))

    response.isRecursionDesired = flags shr 8 and 1 == 1
    response.isAuthoritativeAnswer = flags shr 10 and 1 == 1
    response.isTruncated = flags shr 9 and 1 == 1
    response.isRecursionAvailable = flags shr 7 and 1 == 1
    response.setZ(flags shr 4 and 0x7)
    return response
  }

  fun decodeQuestions(response: DnsResponse, buf: ByteBuf, questionCount: Int) {
    for (i in questionCount downTo 1) {
      response.addRecord(DnsSection.QUESTION, DefaultDnsRecordDecoder.DEFAULT.decodeQuestion(buf))
    }
  }

  fun decodeRecords(
    response: DnsResponse,
    section: DnsSection,
    buf: ByteBuf,
    count: Int
  ) {
    for (i in count downTo 1) {
      val r = DefaultDnsRecordDecoder.DEFAULT.decodeRecord<DnsRecord>(buf)
        ?: // Truncated response
        break

      response.addRecord(section, r)
    }
  }
}
