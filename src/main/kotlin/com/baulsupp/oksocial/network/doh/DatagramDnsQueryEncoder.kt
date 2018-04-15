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
import io.netty.handler.codec.dns.DatagramDnsQuery
import io.netty.handler.codec.dns.DnsQuery
import io.netty.handler.codec.dns.DnsQuestion
import io.netty.handler.codec.dns.DnsRecord
import io.netty.handler.codec.dns.DnsRecordEncoder
import io.netty.handler.codec.dns.DnsSection
import okio.ByteString

object DatagramDnsQueryEncoder {
  fun encode(envelope: DatagramDnsQuery): String {
    val query = envelope.content()
    val buf = Unpooled.buffer(1024)

    encodeHeader(query, buf)
    encodeQuestions(query, buf)
    encodeRecords(query, DnsSection.ADDITIONAL, buf)

    val bytes = ByteString.of(buf.array(), 0, buf.readableBytes())

    return bytes.base64Url().trim('=')
  }

  fun encodeHeader(query: DnsQuery, buf: ByteBuf) {
    buf.writeShort(query.id())
    var flags = 0
    flags = flags or ((query.opCode().byteValue().toInt() and 0xFF) shl 14)
    if (query.isRecursionDesired) {
      flags = flags or (1 shl 8)
    }
    buf.writeShort(flags)
    buf.writeShort(query.count(DnsSection.QUESTION))
    buf.writeShort(0) // answerCount
    buf.writeShort(0) // authorityResourceCount
    buf.writeShort(query.count(DnsSection.ADDITIONAL))
  }

  fun encodeQuestions(query: DnsQuery, buf: ByteBuf) {
    val count = query.count(DnsSection.QUESTION)
    for (i in 0 until count) {
      DnsRecordEncoder.DEFAULT.encodeQuestion(query.recordAt<DnsRecord>(DnsSection.QUESTION, i) as DnsQuestion, buf)
    }
  }

  fun encodeRecords(query: DnsQuery, section: DnsSection, buf: ByteBuf) {
    val count = query.count(section)
    for (i in 0 until count) {
      DnsRecordEncoder.DEFAULT.encodeRecord(query.recordAt(section, i), buf)
    }
  }
}
