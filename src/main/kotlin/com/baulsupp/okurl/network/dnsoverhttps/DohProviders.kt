/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baulsupp.okurl.network.dnsoverhttps

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.ArrayList

/**
 * Temporary registry of known DNS over HTTPS providers.
 *
 * https://github.com/curl/curl/wiki/DNS-over-HTTPS
 */
object DohProviders {
  fun buildGoogle(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
      .url(parseUrl("https://dns.google/dns-query"))
      .bootstrapDnsHosts(getByIp("8.8.4.4"), getByIp("8.8.8.8"))
      .build()
  }

  fun buildGooglePost(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
      .url(parseUrl("https://dns.google/dns-query"))
      .bootstrapDnsHosts(getByIp("8.8.4.4"), getByIp("8.8.8.8"))
      .post(true)
      .build()
  }

  fun buildCloudflare(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
      .url(parseUrl("https://1.1.1.1/dns-query?ct=application/dns-udpwireformat"))
      .bootstrapDnsHosts(getByIp("1.1.1.1"), getByIp("1.0.0.1"))
      .includeIPv6(false)
      .build()
  }

  fun buildCloudflarePost(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
      .url(parseUrl("https://dns.cloudflare.com/.well-known/dns-query"))
      .includeIPv6(false)
      .post(true)
      .build()
  }

  fun buildCleanBrowsing(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
      .url(parseUrl("https://doh.cleanbrowsing.org/doh/family-filter"))
      .includeIPv6(false)
      .build()
  }

  fun providers(client: OkHttpClient, getOnly: Boolean): List<DnsOverHttps> {
    val result = ArrayList<DnsOverHttps>()

    result.add(buildGoogle(client))
    if (!getOnly) {
      result.add(buildGooglePost(client))
    }
    result.add(buildCloudflare(client))
    if (!getOnly) {
      result.add(buildCloudflarePost(client))
    }
    result.add(buildCleanBrowsing(client))

    return result
  }

  internal fun parseUrl(s: String): HttpUrl {

    return s.toHttpUrlOrNull() ?: throw NullPointerException("unable to parse url")
  }

  private fun getByIp(host: String): InetAddress {
    try {
      return InetAddress.getByName(host)
    } catch (e: UnknownHostException) {
      // unlikely
      throw RuntimeException(e)
    }
  }
}
