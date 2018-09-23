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

import java.net.InetAddress
import java.net.UnknownHostException
import java.util.ArrayList
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps

/**
 * Temporary registry of known DNS over HTTPS providers.
 *
 * https://github.com/curl/curl/wiki/DNS-over-HTTPS
 */
object DohProviders {
  fun buildGoogle(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
            .url(parseUrl("https://dns.google.com/experimental"))
            .bootstrapDnsHosts(getByIp("216.58.204.78"), getByIp("2a00:1450:4009:814:0:0:0:200e"))
            .build()
  }

  fun buildGooglePost(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
            .url(parseUrl("https://dns.google.com/experimental"))
            .bootstrapDnsHosts(getByIp("216.58.204.78"), getByIp("2a00:1450:4009:814:0:0:0:200e"))
            .post(true)
            .build()
  }

  fun buildCloudflare(bootstrapClient: OkHttpClient): DnsOverHttps {
    return DnsOverHttps.Builder().client(bootstrapClient)
            .url(parseUrl("https://cloudflare-dns.com/dns-query?ct=application/dns-udpwireformat"))
            .bootstrapDnsHosts(getByIp("104.16.111.25"), getByIp("104.16.112.25"),
                    getByIp("2400:cb00:2048:1:0:0:6810:7019"), getByIp("2400:cb00:2048:1:0:0:6810:6f19"))
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

    return HttpUrl.parse(s) ?: throw NullPointerException("unable to parse url")
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
