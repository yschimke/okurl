package com.baulsupp.oksocial.network.dnsoverhttps

import com.baulsupp.oksocial.network.dnsoverhttps.DnsOverHttps.Companion.DNS_MESSAGE
import com.baulsupp.oksocial.network.dnsoverhttps.DnsOverHttps.Companion.UDPWIREFORMAT
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.util.ArrayList

/**
 * Temporary registry of known DNS over HTTPS providers.
 *
 * https://github.com/curl/curl/wiki/DNS-over-HTTPS
 */
object DohProviders {
  fun buildGoogle(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    val url = parseUrl("https://dns.google.com/experimental?ct=$UDPWIREFORMAT")

    val bootstrapDns = BootstrapDns("dns.google.com", getByIp("216.58.204.78"),
      getByIp("2a00:1450:4009:814:0:0:0:200e"))

    return DnsOverHttps(bootstrapClient, url, bootstrapDns, true, "GET",
      UDPWIREFORMAT)
  }

  fun buildGooglePost(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    val url = parseUrl("https://dns.google.com/experimental")

    val bootstrapDns = BootstrapDns("dns.google.com", getByIp("216.58.204.78"),
      getByIp("2a00:1450:4009:814:0:0:0:200e"))

    return DnsOverHttps(bootstrapClient, url, bootstrapDns, true, "POST",
      UDPWIREFORMAT)
  }

  fun buildCloudflare(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    val url = parseUrl("https://cloudflare-dns.com/dns-query?ct=application/dns-udpwireformat")

    val bootstrapDns = BootstrapDns("cloudflare-dns.com", getByIp("104.16.111.25"),
      getByIp("104.16.112.25"),
      getByIp("2400:cb00:2048:1:0:0:6810:7019"),
      getByIp("2400:cb00:2048:1:0:0:6810:6f19"))

    return DnsOverHttps(bootstrapClient, url, bootstrapDns, false, "GET", DNS_MESSAGE)
  }

  fun buildCloudflarePost(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    val url = parseUrl("https://dns.cloudflare.com/.well-known/dns-query")

    return DnsOverHttps(bootstrapClient, url, null, false, "POST",
      UDPWIREFORMAT)
  }

  fun buildCleanBrowsing(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    return DnsOverHttps(bootstrapClient,
      parseUrl("https://doh.cleanbrowsing.org/doh/family-filter"), null, false, "GET",
      DNS_MESSAGE)
  }

  fun buildChantra(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    return DnsOverHttps(bootstrapClient, parseUrl("https://dns.dnsoverhttps.net/dns-query"), null, false, "GET", DNS_MESSAGE)
  }

  fun buildCryptoSx(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    return DnsOverHttps(bootstrapClient, parseUrl("https://doh.crypto.sx/dns-query"), null,
      false, "GET", DNS_MESSAGE)
  }

  fun buildSecureDns(bootstrapClient: () -> OkHttpClient): DnsOverHttps {
    return DnsOverHttps(bootstrapClient, parseUrl("https://doh.securedns.eu/dns-query"), null,
      false, "GET", DNS_MESSAGE)
  }

  fun providers(
    client: () -> OkHttpClient,
    http2Only: Boolean,
    workingOnly: Boolean,
    getOnly: Boolean
  ): List<DnsOverHttps> {

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
    if (!http2Only) {
      result.add(buildSecureDns(client))
    }
    if (!workingOnly) {
      result.add(buildCryptoSx(client)) // 521 - server down
      result.add(buildChantra(client)) // 400
    }

    return result
  }

  private fun parseUrl(s: String): HttpUrl = HttpUrl.parse(s) ?: throw NullPointerException("unable to parse url")

  private fun getByIp(host: String): InetAddress {
    return InetAddress.getByName(host)
  }
}
